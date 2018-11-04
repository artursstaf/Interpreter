package interpreter.visitors

import interpreter.*
import java.util.*

class PrintVisitor : Visitor {
    private val indentStack = Stack<String>().apply { push("") }

    override fun visit(node: SumExpression) = printBinaryNode(node)
    override fun visit(node: MinusExpression) = printBinaryNode(node)
    override fun visit(node: DivisionExpression) = printBinaryNode(node)
    override fun visit(node: MultiplicationExpression) = printBinaryNode(node)

    override fun visit(node: NumberLiteral) {
        printNodeName(node)
        printAttribute("value", node.value.toString())
    }

    override fun visit(node: NumericVariable) {
        printNodeName(node)
        printAttribute("name", node.value)
    }

    override fun visit(node: AndExpression) = printBinaryNode(node)
    override fun visit(node: OrExpression) = printBinaryNode(node)

    override fun visit(node: NotExpression) {
        printNodeName(node)
        printNode(node.expr)
    }

    override fun visit(node: BooleanConstant) {
        printNodeName(node)
        printAttribute("value", node.value.toString())
    }

    override fun visit(node: EqualsRelation) = printBinaryNode(node)
    override fun visit(node: NotEqualsRelation) = printBinaryNode(node)
    override fun visit(node: SmallerThanRelation) = printBinaryNode(node)
    override fun visit(node: GreaterThanRelation) = printBinaryNode(node)
    override fun visit(node: SmallerThanOrEqualRelation) = printBinaryNode(node)
    override fun visit(node: GreaterThanOrEqualRelation) = printBinaryNode(node)

    override fun visit(node: AssignStatement) {
        printNodeName(node)
        printAttribute("variable", node.variable)
        printNode(node.expr)
    }

    override fun visit(node: LoopStatement) {
        printNodeName(node)
        printSection("Condition") { printNode(node.condition) }
        printSection("Body") { printNodeList(node.body) }
    }

    override fun visit(node: ConditionalStatement) {
        printNodeName(node)
        printSection("Condition") { printNode(node.condition) }
        printSection("Then") { printNodeList(node.thenStmt) }
        if (node.elseStmt.isNotEmpty())
            printSection("Else") { printNodeList(node.elseStmt) }
    }

    override fun visit(node: ReadStatment) {
        printNodeName(node)
        printAttribute("variables", node.varList.toString())
    }

    override fun visit(node: WriteStatement) {
        printNodeName(node)
        printAttribute("variables", node.varList.toString())
    }

    override fun visit(node: SkipStatement) {
        printNodeName(node)
    }

    override fun visit(node: Program) {
        printNodeName(node)
        printNodeList(node.statements)
    }

    private fun printNodeName(node: Node) = println("${indentStack.peek()}+- ${node.javaClass.simpleName}")

    private fun printSection(name: String, section: () -> Unit) {
        println("${indentStack.peek()}     $name:")
        indentStack.push(indentStack.peek() + "   ")
        section()
        indentStack.pop()
    }

    private fun printAttribute(key: String, value: String) = println("${indentStack.peek()}     $key: \"$value\"")

    private fun printNode(node: Node) {
        indentStack.push(indentStack.peek() + "     ")
        node.accept(this)
        indentStack.pop()
    }

    private fun printNodeList(nodes: List<Node>) {
        indentStack.push(indentStack.peek() + "     ")
        nodes.forEach { it.accept(this) }
        indentStack.pop()
    }

    private fun printBinaryNode(node: BinaryAlgebraicExpression) {
        printNodeName(node)
        printNode(node.left)
        printNode(node.right)
    }

    private fun printBinaryNode(node: BinaryBooleanExpression) {
        printNodeName(node)
        printNode(node.left)
        printNode(node.right)
    }

    private fun printBinaryNode(node: Relation) {
        printNodeName(node)
        printNode(node.left)
        printNode(node.right)
    }
}

fun Node.print() = this.accept(PrintVisitor())
