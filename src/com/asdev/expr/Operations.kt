package com.asdev.expr

/**
 * Created by Asdev on 03/23/17. All rights reserved.
 * Unauthorized copying via any medium is stricitly
 * prohibited.
 *
 * Authored by Shahbaz Momi as part of ExpressionSolver
 * under the package com.asdev.expr
 */

/**
 * Represents an operation.
 */
enum class Operation {

    PLUS,
    MINUS,
    MULTIPLY,
    DIVIDE,
    POWER,
    MODULO,
    GT,
    LT,
    EQUALITY,
    AND,
    OR,
    ROOT,
    FACTORIAL;

    /**
     * Returns whether or not this operation requires a value to the left of it.
     */
    fun requireLeftValue(): Boolean {
        when(this) {
            FACTORIAL -> return true
            else -> return true
        }
    }

    /**
     * Returns whether or not this operation requires a value to the right of it.
     */
    fun requireRightValue(): Boolean {
        when(this) {
            FACTORIAL -> return false
            else -> return true
        }
    }

    /**
     * Performs this operation and returns the result.
     */
    fun execute(a: Float, b: Float): Float {
        if(this == PLUS) {
            return a + b
        } else if(this == MINUS) {
            return a - b
        } else if(this == MULTIPLY) {
            return a * b
        } else if(this == DIVIDE) {
            return a / b
        } else if(this == POWER) {
            return Math.pow(a.toDouble(), b.toDouble()).toFloat()
        } else if(this == MODULO) {
            return a % b
        } else if(this == GT) {
            return if(a > b) 1f else 0f
        } else if(this == LT) {
            return if(a < b) 1f else 0f
        } else if(this == EQUALITY) {
            return if(a == b) 1f else 0f
        } else if(this == AND) {
            return if((a > 0f) && (b > 0f)) 1f else 0f
        } else if(this == OR) {
            return if((a > 0f) || (b > 0f)) 1f else 0f
        } else if(this == ROOT) {
            if(a == 2f) {
                return Math.sqrt(b.toDouble()).toFloat()
            } else if(a == 3f) {
                return Math.cbrt(b.toDouble()).toFloat()
            } else {
                val factor = 1.0 / a // 4^0.5 = 2 root 4, 8^0.3333... = 3 root 8
                return Math.pow(b.toDouble(), factor).toFloat()
            }
        } else if(this == FACTORIAL) {
            if(a > 34f) {
                throw IllegalArgumentException("Factorial number too high: $a")
            }

            return factorial(a.toInt())
        }

        throw IllegalArgumentException("Unknown operation: $name")
    }

    private fun factorial(i: Int): Float {
        var result = 1f
        for(j in 1..i) {
            result *= j.toFloat()
        }

        return result
    }
}

fun getOperationForChar(c: Char): Operation? {
    if(c == '+') {
        return Operation.PLUS
    } else if(c == '-') {
        return Operation.MINUS
    } else if(c == '/') {
        return Operation.DIVIDE
    } else if(c == '*') {
        return Operation.MULTIPLY
    } else if(c == '^') {
        return Operation.POWER
    } else if(c == '%') {
        return Operation.MODULO
    } else if(c == '>') {
        return Operation.GT
    } else if(c == '<') {
        return Operation.LT
    } else if(c == '=') {
        return Operation.EQUALITY
    } else if(c == '&') {
        return Operation.AND
    } else if(c == '|') {
        return Operation.OR
    } else if(c == '√') {
        return Operation.ROOT
    } else if(c == '!') {
        return Operation.FACTORIAL
    }

    return null
}

fun isOperator(c: Char) = getOperationForChar(c) != null