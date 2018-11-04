package interpreter.visitors

import interpreter.*

interface Visitable {
    fun accept(visitor: Visitor)
}

interface Visitor {
    fun visit(node: SumExpression)
    fun visit(node: MinusExpression)
    fun visit(node: DivisionExpression)
    fun visit(node: MultiplicationExpression)
    fun visit(node: NumberLiteral)
    fun visit(node: NumericVariable)
    fun visit(node: AndExpression)
    fun visit(node: OrExpression)
    fun visit(node: NotExpression)
    fun visit(node: BooleanConstant)
    fun visit(node: EqualsRelation)
    fun visit(node: NotEqualsRelation)
    fun visit(node: SmallerThanRelation)
    fun visit(node: GreaterThanRelation)
    fun visit(node: SmallerThanOrEqualRelation)
    fun visit(node: GreaterThanOrEqualRelation)
    fun visit(node: AssignStatement)
    fun visit(node: LoopStatement)
    fun visit(node: ConditionalStatement)
    fun visit(node: ReadStatment)
    fun visit(node: WriteStatement)
    fun visit(node: SkipStatement)
    fun visit(node: Program)
}
