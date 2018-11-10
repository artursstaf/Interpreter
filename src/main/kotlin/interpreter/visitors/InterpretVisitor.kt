package interpreter.visitors

import interpreter.*
import java.util.*

class InterpretVisitor(private val verbose: Boolean) : Visitor {

    private val variables = mutableMapOf<String, Long>()
    private val arithmeticStack = Stack<Long>()
    private val booleanStack = Stack<Boolean>()

    override fun visit(node: SumExpression) = visitArithmetic(node) { l, r -> l + r }
    override fun visit(node: MinusExpression) = visitArithmetic(node) { l, r -> l - r }
    override fun visit(node: DivisionExpression) = visitArithmetic(node) { l, r -> l / r }
    override fun visit(node: MultiplicationExpression) = visitArithmetic(node) { l, r -> l * r }

    override fun visit(node: NumberLiteral) {
        arithmeticStack.push(node.value)
    }

    override fun visit(node: NumericVariable) {
        arithmeticStack.push(getVar(node.value))
    }

    override fun visit(node: AndExpression) = visitBoolean(node, false) { l, r -> l and r }
    override fun visit(node: OrExpression) = visitBoolean(node, true) { l, r -> l or r }
    override fun visit(node: NotExpression) {
        node.expr.accept(this)
        booleanStack.push(!booleanStack.pop())
    }

    override fun visit(node: BooleanConstant) {
        booleanStack.push(node.value)
    }

    override fun visit(node: EqualsRelation) = visitRelation(node) { l, r -> l == r }
    override fun visit(node: NotEqualsRelation) = visitRelation(node) { l, r -> l != r }
    override fun visit(node: SmallerThanRelation) = visitRelation(node) { l, r -> l < r }
    override fun visit(node: GreaterThanRelation) = visitRelation(node) { l, r -> l > r }
    override fun visit(node: SmallerThanOrEqualRelation) = visitRelation(node) { l, r -> l <= r }
    override fun visit(node: GreaterThanOrEqualRelation) = visitRelation(node) { l, r -> l >= r }

    override fun visit(node: AssignStatement) {
        node.expr.accept(this)
        variables[node.variable] = arithmeticStack.pop()
        if(verbose) println("State: $variables")
    }

    override fun visit(node: LoopStatement) {
        node.condition.accept(this)
        while (booleanStack.peek()) {
            if (booleanStack.pop()) node.body.forEach { it.accept(this) }
            node.condition.accept(this)
        }
    }

    override fun visit(node: ConditionalStatement) {
        node.condition.accept(this)
        if (booleanStack.pop()) node.thenStmt.forEach { it.accept(this) }
        else node.elseStmt.forEach { it.accept(this) }
    }

    override fun visit(node: ReadStatment) = node.varList.forEach {
        print("Input: ")
        val strValue = readLine()!!.trim()
        val asLong = strValue.toLongOrNull() ?: throw InvalidIntegerValue(strValue)
        variables[it] = asLong
    }.also { if(verbose) println("State: $variables") }

    override fun visit(node: WriteStatement) = node.varList.forEach { println("Output: ${getVar(it)}") }
    override fun visit(node: SkipStatement) = Unit
    override fun visit(node: Program){
        if(verbose) println("Running program: ")
        node.statements.forEach { it.accept(this) }
    }

    private fun getVar(name: String): Long {
        if (!variables.containsKey(name)) throw VariableNotDefinedException(name)
        return variables[name]!!
    }

    private inline fun visitRelation(node: Relation, comparator: (Long, Long) -> Boolean) {
        node.left.accept(this)
        node.right.accept(this)
        val right = arithmeticStack.pop()
        val left = arithmeticStack.pop()
        booleanStack.push(comparator(left, right))
    }

    private inline fun visitArithmetic(node: BinaryAlgebraicExpression, function: (Long, Long) -> Long) {
        node.left.accept(this)
        node.right.accept(this)
        val right = arithmeticStack.pop()
        val left = arithmeticStack.pop()
        arithmeticStack.push(function(left, right))
    }

    private inline fun visitBoolean(
        node: BinaryBooleanExpression,
        earlyReturnOn: Boolean,
        function: (Boolean, Boolean) -> Boolean
    ) {
        node.left.accept(this)
        if (booleanStack.peek() == earlyReturnOn) {
            booleanStack.pop()
            booleanStack.push(earlyReturnOn)
            return
        }
        node.right.accept(this)
        val right = booleanStack.pop()
        val left = booleanStack.pop()
        booleanStack.push(function(left, right))
    }
}

fun Program.interpret(verbose: Boolean = false) = this.accept(InterpretVisitor(verbose))
