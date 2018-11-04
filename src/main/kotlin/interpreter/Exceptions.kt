package interpreter

import java.lang.Exception

class VariableNotDefinedException(name: String) : Exception("Variable $name is used before definition")
class InvalidIntegerValue(value: String) : Exception("Input value $value is not valid integer")
class ParseException(message: String) : Exception(message)
