package interpreter

import interpreter.visitors.Visitable
import interpreter.visitors.Visitor

interface Node : Visitable
interface Statement : Node
interface Expression : Node

data class Program(val statements: List<Statement>) : Node {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

interface AlgebraicExpression : Expression
interface BinaryAlgebraicExpression : AlgebraicExpression {
    val left: AlgebraicExpression
    val right: AlgebraicExpression
}

interface BooleanExpression : Expression
interface BinaryBooleanExpression : BooleanExpression {
    val left: BooleanExpression
    val right: BooleanExpression
}

data class SumExpression(override val left: AlgebraicExpression, override val right: AlgebraicExpression) :
    BinaryAlgebraicExpression {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

data class MinusExpression(override val left: AlgebraicExpression, override val right: AlgebraicExpression) :
    BinaryAlgebraicExpression {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

data class DivisionExpression(override val left: AlgebraicExpression, override val right: AlgebraicExpression) :
    BinaryAlgebraicExpression {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

data class MultiplicationExpression(override val left: AlgebraicExpression, override val right: AlgebraicExpression) :
    BinaryAlgebraicExpression {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

data class NumberLiteral(val value: Long) : AlgebraicExpression {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

data class NumericVariable(val value: String) : AlgebraicExpression {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}


data class AndExpression(override val left: BooleanExpression, override val right: BooleanExpression) :
    BinaryBooleanExpression {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

data class OrExpression(override val left: BooleanExpression, override val right: BooleanExpression) :
    BinaryBooleanExpression {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

data class NotExpression(val expr: BooleanExpression) : BooleanExpression {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

data class BooleanConstant(val value: Boolean) : BooleanExpression {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

interface Relation : BooleanExpression {
    val left: AlgebraicExpression
    val right: AlgebraicExpression
}

data class EqualsRelation(override val left: AlgebraicExpression, override val right: AlgebraicExpression) : Relation {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

data class NotEqualsRelation(override val left: AlgebraicExpression, override val right: AlgebraicExpression) :
    Relation {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

data class SmallerThanRelation(override val left: AlgebraicExpression, override val right: AlgebraicExpression) :
    Relation {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

data class GreaterThanRelation(override val left: AlgebraicExpression, override val right: AlgebraicExpression) :
    Relation {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

data class SmallerThanOrEqualRelation(override val left: AlgebraicExpression, override val right: AlgebraicExpression) :
    Relation {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

data class GreaterThanOrEqualRelation(override val left: AlgebraicExpression, override val right: AlgebraicExpression) :
    Relation {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

data class AssignStatement(val variable: String, val expr: AlgebraicExpression) : Statement {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

data class LoopStatement(val condition: BooleanExpression, val body: List<Statement>) :
    Statement {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

data class ConditionalStatement(
    val condition: BooleanExpression,
    val thenStmt: List<Statement>,
    val elseStmt: List<Statement>
) : Statement {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

data class ReadStatment(val varList: List<String>) : Statement {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

data class WriteStatement(val varList: List<String>) : Statement {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

class SkipStatement : Statement {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}
