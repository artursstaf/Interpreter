package interpreter

import interpreter.visitors.print
import interpreter.visitors.interpret
import java.io.File

class Parser(private val tokens: List<Token>) {

    private var tokensIt = tokens.listIterator()

    fun buildAst() = Program(progr())

    private fun iteratorSnapShot() = tokens.listIterator(tokensIt.nextIndex())

    private fun peekTokenIgnoreNewLines(): TokenInfo {
        val savedIt = iteratorSnapShot()
        while (savedIt.hasNext()) {
            val next = savedIt.next()
            if (next.info != TokenInfo.NEWLINE)
                return next.info
        }
        return TokenInfo.NEWLINE
    }

    private fun peekToken() = tokens[tokensIt.nextIndex()].info

    private fun eatToken(expected: TokenInfo, ignoreNewLines: Boolean = true): Token {
        with(tokensIt) {
            if (ignoreNewLines) {
                while (hasNext() && peekToken() == TokenInfo.NEWLINE) next()
            }
            if (hasNext()) {
                val tok = next()
                if (tok.info == expected) return tok
            }
        }
        throw ParseException("Could not find $expected found ${tokensIt.previous()} tokenIndex: ${tokensIt.nextIndex()}")
    }

    private fun progr(): List<Statement> {
        val statements = series()
        eatToken(TokenInfo.NEWLINE, ignoreNewLines = false)
        if (peekTokenIgnoreNewLines() != TokenInfo.NEWLINE)
            throw ParseException("Expecting end of program, found ${peekTokenIgnoreNewLines()}")
        return statements
    }

    private fun series(): List<Statement> {
        val statements = mutableListOf(stmt())
        if(peekTokenIgnoreNewLines() == TokenInfo.SEMICOLON){
            eatToken(TokenInfo.SEMICOLON)
            statements.addAll(series())
        }
        return statements
    }

    private fun stmt(): Statement {
        return when (peekTokenIgnoreNewLines()) {
            TokenInfo.VARNAME -> assignStmt()
            TokenInfo.WRITE -> outputStmt()
            TokenInfo.READ -> inputStmt()
            TokenInfo.SKIP -> {
                eatToken(TokenInfo.SKIP)
                SkipStatement()
            }
            TokenInfo.IF -> condStmt()
            TokenInfo.WHILE -> loopStmt()
            else -> throw ParseException("Could not parse stmt, found ${peekTokenIgnoreNewLines()}")
        }
    }

    private fun loopStmt(): Statement {
        eatToken(TokenInfo.WHILE)
        val condition = bExpr()
        eatToken(TokenInfo.DO)
        val statements = series()
        eatToken(TokenInfo.OD)
        return LoopStatement(condition, statements)
    }

    private fun condStmt(): Statement {
        eatToken(TokenInfo.IF)
        val condition = bExpr()
        eatToken(TokenInfo.THEN)
        val statements = series()

        val elseStmt = if (peekTokenIgnoreNewLines() == TokenInfo.ELSE) {
            eatToken(TokenInfo.ELSE)
            series()
        } else {
            emptyList()
        }

        eatToken(TokenInfo.FI)
        return ConditionalStatement(condition, statements, elseStmt)
    }

    private fun assignStmt(): Statement {
        val variable = eatToken(TokenInfo.VARNAME).value
        eatToken(TokenInfo.ASSIGNMENT)
        val algebraicExpression = aExpr()
        return AssignStatement(variable, algebraicExpression)
    }


    private fun aExpr(): AlgebraicExpression {
        val leftOperand = aTerm()

        return if (peekTokenIgnoreNewLines() == TokenInfo.AWEAKOP) {
            when (eatToken(TokenInfo.AWEAKOP).value) {
                "+" -> SumExpression(leftOperand, aExpr())
                "-" -> MinusExpression(leftOperand, aExpr())
                else -> throw ParseException("Could not find +,- operators")
            }
        } else {
            leftOperand
        }
    }

    private fun aTerm(): AlgebraicExpression {
        val leftOperand = aElem()

        return if (peekTokenIgnoreNewLines() == TokenInfo.ASTRONGOP) {
            when (eatToken(TokenInfo.ASTRONGOP).value) {
                "*" -> MultiplicationExpression(leftOperand, aTerm())
                "/" -> DivisionExpression(leftOperand, aTerm())
                else -> throw ParseException("Could not find *,/ operators")
            }
        } else {
            leftOperand
        }
    }

    private fun aElem(): AlgebraicExpression {
        return when (peekTokenIgnoreNewLines()) {
            TokenInfo.NUMBER -> NumberLiteral(eatToken(TokenInfo.NUMBER).value.toLong())
            TokenInfo.VARNAME -> NumericVariable(eatToken(TokenInfo.VARNAME).value)
            TokenInfo.OPENINGBRACKET -> {
                eatToken(TokenInfo.OPENINGBRACKET)
                val algebraicExpression = aExpr()
                eatToken(TokenInfo.CLOSINGBRACKET)
                algebraicExpression
            }
            else -> throw ParseException("Could not parse aElem, found ${peekTokenIgnoreNewLines()}")
        }
    }

    private fun bExpr(): BooleanExpression {
        val leftOperand = bTerm()

        return if (peekTokenIgnoreNewLines() == TokenInfo.OR) {
            eatToken(TokenInfo.OR)
            OrExpression(leftOperand, bExpr())
        } else {
            leftOperand
        }
    }

    private fun bTerm(): BooleanExpression {
        val leftOperand = bElem()

        return if (peekTokenIgnoreNewLines() == TokenInfo.AND) {
            eatToken(TokenInfo.AND)
            AndExpression(leftOperand, bTerm())
        } else {
            leftOperand
        }
    }

    private fun bElem(): BooleanExpression {
        return when (peekTokenIgnoreNewLines()) {
            TokenInfo.BCONSTANT -> BooleanConstant(eatToken(TokenInfo.BCONSTANT).value.toBoolean())
            TokenInfo.NOT -> {
                eatToken(TokenInfo.NOT)
                NotExpression(bElem())
            }
            TokenInfo.NUMBER -> aExprRelation()
            TokenInfo.VARNAME -> aExprRelation()
            TokenInfo.OPENINGBRACKET -> resolveBrackets(::aExprRelation, ::bElemBrackets)
            else -> throw ParseException("Could not parse bElem, found ${peekTokenIgnoreNewLines()}")
        }
    }

    private fun aExprRelation(): BooleanExpression {
        val leftOperand = aExpr()
        return when (eatToken(TokenInfo.RELATION).value) {
            "<" -> SmallerThanRelation(leftOperand, aExpr())
            ">" -> GreaterThanRelation(leftOperand, aExpr())
            "=<" -> SmallerThanOrEqualRelation(leftOperand, aExpr())
            ">=" -> GreaterThanOrEqualRelation(leftOperand, aExpr())
            "<>" -> NotEqualsRelation(leftOperand, aExpr())
            "=" -> EqualsRelation(leftOperand, aExpr())
            else -> throw ParseException("Could not find < > =< >= <> = operators")
        }
    }

    private fun bElemBrackets(): BooleanExpression {
        eatToken(TokenInfo.OPENINGBRACKET)
        val binaryExpr = bExpr()
        eatToken(TokenInfo.CLOSINGBRACKET)
        return binaryExpr
    }

    private fun outputStmt(): Statement {
        eatToken(TokenInfo.WRITE)
        return WriteStatement(varList())
    }

    private fun inputStmt(): Statement {
        eatToken(TokenInfo.READ)
        return ReadStatment(varList())
    }

    private fun varList(): List<String> {
        val list = mutableListOf(eatToken(TokenInfo.VARNAME).value)

        if (peekTokenIgnoreNewLines() == TokenInfo.COMMA) {
            eatToken(TokenInfo.COMMA)
            list.addAll(varList())
        }

        return list
    }

    private fun resolveBrackets(first: () -> BooleanExpression, second: () -> BooleanExpression): BooleanExpression {
        val savedIt = iteratorSnapShot()
        return try {
            first()
        } catch (e: ParseException) {
            tokensIt = savedIt
            second()
        }
    }
}

fun main(args: Array<String>) {
    if(args.isEmpty()) throw Exception ("Expecting file argument")
    val verbose = args.size >= 2 && args[1].trim() == "-v"
    val file = File(args[0].trim()).readText()

    val tokens = Tokenizer().tokenize(file)
    val root = Parser(tokens).buildAst()
    if (verbose) root.print()
    root.interpret(verbose)
}
