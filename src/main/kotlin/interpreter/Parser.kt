package interpreter

import interpreter.visitors.print
import interpreter.visitors.run
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

    private fun consume(expected: TokenInfo, ignoreNewLines: Boolean = true): Token {
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
        consume(TokenInfo.NEWLINE, false)
        if (peekTokenIgnoreNewLines() != TokenInfo.NEWLINE)
            throw ParseException("Expecting end of program, found ${peekTokenIgnoreNewLines()}")
        return statements
    }

    private fun series(): List<Statement> {
        val statements = mutableListOf(stmt())
        while (tokensIt.hasNext() && peekTokenIgnoreNewLines() == TokenInfo.SEMICOLON) {
            consume(TokenInfo.SEMICOLON)
            statements.add(stmt())
        }
        return statements
    }

    private fun stmt(): Statement {
        return when (peekTokenIgnoreNewLines()) {
            TokenInfo.VARNAME -> assignStmt()
            TokenInfo.WRITE -> outputStmt()
            TokenInfo.READ -> inputStmt()
            TokenInfo.SKIP -> {
                consume(TokenInfo.SKIP)
                SkipStatement()
            }
            TokenInfo.IF -> condStmt()
            TokenInfo.WHILE -> loopStmt()
            else -> throw ParseException("Could not parse stmt, found ${peekTokenIgnoreNewLines()}")
        }
    }

    private fun loopStmt(): Statement {
        consume(TokenInfo.WHILE)
        val condition = bExpr()
        consume(TokenInfo.DO)
        val statements = series()
        consume(TokenInfo.OD)
        return LoopStatement(condition, statements)
    }

    private fun condStmt(): Statement {
        consume(TokenInfo.IF)
        val condition = bExpr()
        consume(TokenInfo.THEN)
        val statements = series()

        val elseStmt = if (peekTokenIgnoreNewLines() == TokenInfo.ELSE) {
            consume(TokenInfo.ELSE)
            series()
        } else {
            emptyList()
        }

        consume(TokenInfo.FI)
        return ConditionalStatement(condition, statements, elseStmt)
    }

    private fun assignStmt(): Statement {
        val variable = consume(TokenInfo.VARNAME).value
        consume(TokenInfo.ASSIGNMENT)
        val algebraicExpression = aExpr()
        return AssignStatement(variable, algebraicExpression)
    }


    private fun aExpr(): AlgebraicExpression {
        val leftOperand = aTerm()

        return if (peekTokenIgnoreNewLines() == TokenInfo.AWEAKOP) {
            when (consume(TokenInfo.AWEAKOP).value) {
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
            when (consume(TokenInfo.ASTRONGOP).value) {
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
            TokenInfo.NUMBER -> NumberLiteral(consume(TokenInfo.NUMBER).value.toLong())
            TokenInfo.VARNAME -> NumericVariable(consume(TokenInfo.VARNAME).value)
            TokenInfo.OPENINGBRACKET -> {
                consume(TokenInfo.OPENINGBRACKET)
                val algebraicExpression = aExpr()
                consume(TokenInfo.CLOSINGBRACKET)
                algebraicExpression
            }
            else -> throw ParseException("Could not parse aElem, found ${peekTokenIgnoreNewLines()}")
        }
    }

    private fun bExpr(): BooleanExpression {
        val leftOperand = bTerm()

        return if (peekTokenIgnoreNewLines() == TokenInfo.OR) {
            consume(TokenInfo.OR)
            OrExpression(leftOperand, bExpr())
        } else {
            leftOperand
        }
    }

    private fun bTerm(): BooleanExpression {
        val leftOperand = bElem()

        return if (peekTokenIgnoreNewLines() == TokenInfo.AND) {
            consume(TokenInfo.AND)
            AndExpression(leftOperand, bTerm())
        } else {
            leftOperand
        }
    }

    private fun bElem(): BooleanExpression {
        return when (peekTokenIgnoreNewLines()) {
            TokenInfo.BCONSTANT -> BooleanConstant(consume(TokenInfo.BCONSTANT).value.toBoolean())
            TokenInfo.NOT -> {
                consume(TokenInfo.NOT)
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
        return when (consume(TokenInfo.RELATION).value) {
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
        consume(TokenInfo.OPENINGBRACKET)
        val binaryExpr = bExpr()
        consume(TokenInfo.CLOSINGBRACKET)
        return binaryExpr
    }

    private fun outputStmt(): Statement {
        consume(TokenInfo.WRITE)
        return WriteStatement(varList())
    }

    private fun inputStmt(): Statement {
        consume(TokenInfo.READ)
        return ReadStatment(varList())
    }

    private fun varList(): List<String> {
        val list = mutableListOf(consume(TokenInfo.VARNAME).value)

        if (peekTokenIgnoreNewLines() == TokenInfo.COMMA) {
            consume(TokenInfo.COMMA)
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
    val file = File(if (args.isNotEmpty()) args[0] else "prog1.txt").readText()
    val tokens = Tokenizer().tokenize(file)
    val root = Parser(tokens).buildAst()
    root.print()
    root.run()
}
