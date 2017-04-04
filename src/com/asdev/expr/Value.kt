package com.asdev.expr

/**
 * Created by Asdev on 03/23/17. All rights reserved.
 * Unauthorized copying via any medium is stricitly
 * prohibited.
 *
 * Authored by Shahbaz Momi as part of ExpressionSolver
 * under the package com.asdev.expr
 */


fun main(args: Array<String>) {
    println("Define variables with ! followed by variable letter and equals value. E.g. !x=2")
    println("Define functions with ! followed by the function letter and variable in parenthesis. E.g. !f(x)=x^2")
    println("Supported operations: % ! ^ √ * / + - > < = & |")
    println("Logic operations will consider any number > 0 to be true, and < 0 false")
    println("Logic operations will return 1 for true and 0 for false")
    println("Denote negative numbers with _ rather than -")

    val env = ExpressionEnvironment()

    while(true) {
        println("Enter expression:")
        val exprRaw = readLine() ?: continue

        env.parseAndPrint(exprRaw)
    }
}

/**
 * A class which can represent either a literal float value in the form of a string or an expression to be resolved.
 */
data class Value(val textualValue: String) {

    var resolvedValue = Float.NaN

    /**
     * Resolves this expression and returns it. Also caches it in the resolvedValue field.
     */
    fun resolve(): Float {
        // try and parse the text as a literal float for the most basic parsing
        try {
            resolvedValue = textualValue.toFloat()
        } catch (e: NumberFormatException) {}

        val tokens = ArrayList<Any>()

        // not an actual value, break into operations and values and parse
        var index = 0

        var numStartIndex = -1

        while(index < textualValue.length) {
            val c = textualValue[index]

            if(c.isWhitespace()) {
                index ++
                continue
            }

            // parse any literal float values.
            if(c.isDigit() || c == '_') {
                if(numStartIndex == -1) {
                    numStartIndex = index
                } else if(c == '_') {
                    throw IllegalArgumentException("Negative operand within a number!")
                }
            } else if(!c.isDigit() && c != '.'){
                // push the current number to the stack
                if(numStartIndex != -1) {
                    // from numStartIndex .. index
                    val num = textualValue.substring(numStartIndex, index).replace("_", "-").toFloat()
                    tokens.add(num)
                }

                numStartIndex = -1
            }

            // check if its a bracket
            if(c == '(') {
                // begin substring search of bracket
                var bracketDepth = 0

                // search for the end bracket
                for(seek in index + 1 until textualValue.length) {
                    // if there is a bracket within a bracket, add to the depth
                    if(textualValue[seek] == '(')
                        bracketDepth ++

                    // check for the end bracket
                    if(textualValue[seek] == ')') {
                        // make sure there are no other sub brackets in this bracket
                        if(bracketDepth == 0) {
                            // from index + 1 to seek
                            val bracketContent = textualValue.substring(index + 1, seek)
                            // this is its own value
                            val subVal = Value(bracketContent)
                            // push to stack
                            tokens.add(subVal.resolve())
                            index = seek
                            bracketDepth = -1
                            break
                        } else {
                            bracketDepth --
                        }
                    }
                }

                if(bracketDepth != -1) {
                    throw IllegalArgumentException("Bracket was never finished!")
                }
            } else if(isOperator(c)) {
                // push the operation to the stack
                tokens.add(getOperationForChar(c)!!)
            } else if(!c.isDigit() && c != '_' && c != '.') {
                println("WARN: unrecognized character: $c. Ignoring")
            }


            index ++
        }

        if(numStartIndex != -1) {
            // push last digit
            val num = textualValue.substring(numStartIndex, textualValue.length).replace("_", "-").toFloat()
            tokens.add(num)
        }

        flatten(tokens, Operation.FACTORIAL)
        flatten(tokens, Operation.MODULO)

        // flatten based off of BEDMAS with respect to specials ops
        flatten(tokens, Operation.POWER)
        flatten(tokens, Operation.ROOT)
        flatten(tokens, Operation.DIVIDE, Operation.MULTIPLY)
        flatten(tokens, Operation.PLUS, Operation.MINUS)

        // special logic operators
        flatten(tokens, Operation.LT, Operation.GT, Operation.EQUALITY)
        flatten(tokens, Operation.AND, Operation.OR)

        // tokens should be size of 1
        if(tokens.size != 1) {
            throw IllegalArgumentException("Unable to fully resolve expression: ${tokens.joinToString( separator = " ")}")
        }
        resolvedValue = tokens[0] as Float

        return resolvedValue
    }

}

/**
 * Flattens the tokens specified within the list and simplifies them.
 */
fun flatten(tokens: ArrayList<Any>, vararg ops: Operation) {
    val tokenSwap = ArrayList<Any>()

    tokenSwap.addAll(tokens)

    while(!ops.none { tokens.contains(it) }) {
        println(tokens.joinToString(separator = " "))

        for (i in tokens.indices) {
            var topBreak = false
            for(o in ops) {
                if (tokens[i] == o) {
                    // left and right tokens should be floats
                    if(i == 0 && o.requireLeftValue()) {
                        throw IllegalArgumentException("Expression cannot begin with an operator: $o")
                    } else if(i == tokens.size - 1 && o.requireRightValue()) {
                        throw IllegalArgumentException("Expression cannot end with an operator: $o")
                    }

                    if(o.requireRightValue() && o.requireLeftValue()) {
                        val a = tokens[i - 1] as? Float ?: throw IllegalArgumentException("Chained operators: $o and ${tokens[i - 1]}")
                        val b = tokens[i + 1] as? Float ?: throw IllegalArgumentException("Chained operators: $o and ${tokens[i + 1]}")
                        // perform and add
                        val v = o.execute(a, b)
                        // push to swap
                        val localIndex = findIndex(tokens[i - 1], tokens[i], tokens[i + 1], list = tokenSwap)
                        for (j in 0..2) {
                            tokenSwap.removeAt(localIndex)
                        }

                        tokenSwap.add(localIndex, v)
                        topBreak = true
                        break
                    } else if(o.requireRightValue()) {
                        val b = tokens[i + 1] as? Float ?: throw IllegalArgumentException("Chained operators: $o and ${tokens[i + 1]}")
                        // perform and add
                        val v = o.execute(0f, b)
                        // push to swap
                        val localIndex = findIndex(tokens[i], tokens[i + 1], list = tokenSwap)
                        for (j in 0..1) {
                            tokenSwap.removeAt(localIndex)
                        }

                        tokenSwap.add(localIndex, v)
                        topBreak = true
                        break
                    } else if(o.requireLeftValue()) {
                        val a = tokens[i - 1] as? Float ?: throw IllegalArgumentException("Chained operators: $o and ${tokens[i - 1]}")
                        // perform and add
                        val v = o.execute(a, 0f)
                        // push to swap
                        val localIndex = findIndex(tokens[i - 1], tokens[i], list = tokenSwap)
                        for (j in 0..1) {
                            tokenSwap.removeAt(localIndex)
                        }

                        tokenSwap.add(localIndex, v)
                        topBreak = true
                        break
                    }
                }
            }

            if(topBreak)
                break
        }

        tokens.clear()
        tokens.addAll(tokenSwap)
    }
}

/**
 * Finds the index of the given objects in the given ArrayList.
 */
fun findIndex(vararg objs: Any, list: ArrayList<Any>) = list.indices.firstOrNull { i -> list[i] == objs[0] && objs.indices.none { list[i + it] != objs[it] } } ?: -1