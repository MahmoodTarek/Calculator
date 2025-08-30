package com.mtm.calculator

class ShuntingYardAlgorithm {

    private fun priority(op: Char): Int {
        return when (op) {
            '+', '-' -> 1
            'x', '/', '%' -> 2
            else -> -1
        }
    }

    fun toPostfix(expr: String): String {
        val postfix = StringBuilder()
        val ops = ArrayDeque<Char>()
        val tokens = splitTokens(expr)

        for (token in tokens) {
            when {
                token.toDoubleOrNull() != null -> {
                    postfix.append(token).append(" ")
                }
                token in OPERATORS -> {
                    val curOp = token[0]
                    while (ops.isNotEmpty() && priority(ops.first()) >= priority(curOp)) {
                        postfix.append(ops.removeFirst()).append(" ")
                    }
                    ops.addFirst(curOp)
                }
            }
        }

        while (ops.isNotEmpty()) {
            postfix.append(ops.removeFirst()).append(" ")
        }

        return postfix.toString().trim()
    }

    fun evaluatePostfix(expr: String): String {
        val numbers = ArrayDeque<Double>()
        val tokens = expr.trim().split("\\s+".toRegex())

        for (token in tokens) {
            when {
                token.toDoubleOrNull() != null -> numbers.addFirst(token.toDouble())
                token in OPERATORS -> {
                    val firstNumber = numbers.removeFirst()
                    val secondNumber = numbers.removeFirst()
                    val res = calculate(token, firstNumber, secondNumber)

                    if (res.isNaN() || res.isInfinite()) {
                        throw ArithmeticException("Invalid result")
                    }
                    numbers.addFirst(res)
                }
            }
        }

        return format(numbers.first())
    }


    private fun splitTokens(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        val cur = StringBuilder()

        for (c in expr) {
            when (c) {
                in '0'..'9', '.' -> cur.append(c)
                in listOf('+', '-', 'x', '/', '%') -> {
                    if (cur.isNotEmpty()) {
                        tokens.add(cur.toString())
                        cur.clear()
                    }
                    tokens.add(c.toString())
                }
            }
        }
        if (cur.isNotEmpty()) tokens.add(cur.toString())
        return tokens
    }

    private fun calculate(operation: String, firstNumber: Double, secondNumber: Double): Double {
        return when (operation) {
            "+" -> firstNumber + secondNumber
            "-" -> firstNumber - secondNumber
            "x" -> firstNumber * secondNumber
            "/" -> {
                if (secondNumber == 0.0) throw ArithmeticException("Division by zero")
                firstNumber / secondNumber
            }
            "%" -> {
                if (secondNumber == 0.0) throw ArithmeticException("Division by zero")
                firstNumber % secondNumber
            }
            else -> throw IllegalArgumentException("Unknown operator: $operation")
        }
    }

    private fun format(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            String.format("%.10f", value)
                .trimEnd('0')
                .trimEnd('.')
        }
    }

    companion object {
        private val OPERATORS = listOf("+", "-", "x", "/", "%")
    }
}