package com.asdev.expr

/**
 * Created by Asdev on 2017/04/04. All rights reserved.
 * Unauthorized copying via any medium is stricitly
 * prohibited.
 *
 * Authored by Shahbaz Momi as part of ExpressionSolver
 * under the package com.asdev.expr
 */

val VALID_VAR_NAMES = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

class ExpressionEnvironment {

    val vars = HashMap<Char, Float>()
    val funcs = HashMap<Pair<Char, Char>, String>()

    fun putVariable(name: Char, value: Float) = vars.put(name, value)
    fun putFunction(function: Pair<Char, Char>, value: String) = funcs.put(function, value)

    fun reset() {
        vars.clear()
        funcs.clear()
    }

    fun parseAndPrint(exprRaw: String) {
        var varname: Char? = null

        var expr: String = exprRaw

        expr = expr.trim().replace(" ", "")

        if(expr[0] == '!') {

            // determine if function or variable
            if(expr[2] == '(' && expr[4] == ')' && expr[5] == '=') {
                // function
                val funcName = expr[1]
                val funcVar = expr[3]

                if (!VALID_VAR_NAMES.contains(funcName)) {
                    throw IllegalArgumentException("Invalid function name $varname. Must be a single alphabetical character!")
                }

                if (!VALID_VAR_NAMES.contains(funcVar)) {
                    throw IllegalArgumentException("Invalid function variable $varname. Must be a single alphabetical character!")
                }


                putFunction(funcName to funcVar, expr.substring(6))
                println("$funcName($funcVar)=${funcs[funcName to funcVar]}")
                return
            } else {
                // variable definition
                varname = expr[1]
                if (!VALID_VAR_NAMES.contains(varname)) {
                    throw IllegalArgumentException("Invalid variable name $varname. Must be a single alphabetical character!")
                }

                if (expr[2] != '=') {
                    throw IllegalArgumentException("Variable names can only be 1 character and must be followed by an equals!")
                }

                expr = expr.substring(3)
            }
        } else if(expr[0] == '?') {
            // retrieve a var/func
            // determine if function or variable
            if(expr.length >= 5 && expr[2] == '(' && expr[4] == ')') {
                // function
                val funcName = expr[1]
                val funcVar = expr[3]

                if (!VALID_VAR_NAMES.contains(funcName)) {
                    throw IllegalArgumentException("Invalid function name $varname. Must be a single alphabetical character!")
                }

                if (!VALID_VAR_NAMES.contains(funcVar)) {
                    throw IllegalArgumentException("Invalid function variable $varname. Must be a single alphabetical character!")
                }

                println("$funcName($funcVar)=${funcs[funcName to funcVar]}")
                return
            } else {
                // variable definition
                varname = expr[1]
                if (!VALID_VAR_NAMES.contains(varname)) {
                    throw IllegalArgumentException("Invalid variable name $varname. Must be a single alphabetical character!")
                }

                println("$varname=${vars[varname]}")
                return
            }
        }

        // resolve any functions within the expression
        for((key, value) in funcs) {
            while(expr.contains("${key.first}(")) {
                // contains an expression notation
                val funStart = expr.indexOf("${key.first}(") + 2

                var bracketDepth = 0
                for(seek in funStart until expr.length) {
                    if(expr[seek] == '(') {
                        bracketDepth ++
                    } else if(expr[seek] == ')') {
                        if(bracketDepth == 0) {
                            val funContents = expr.substring(funStart, seek)
                            val funcReplaced = "(" + value.replace(key.second.toString(), "($funContents)") + ")"
                            expr = expr.replaceRange(funStart - 2, seek + 1, funcReplaced)
                            bracketDepth = -1
                            break
                        } else {
                            bracketDepth --
                        }
                    }
                }

                if(bracketDepth != -1) {
                    throw IllegalArgumentException("Function bracket was never finished!")
                }
            }
        }

        // sub in any values
        for(c in VALID_VAR_NAMES) {
            if(vars.containsKey(c)) {
                // variable with name c
                // replace the letter with the value, but if a digit is next to it add a multiplication
                while(expr.contains(c)) {
                    var mulLeft = false
                    var mulRight = false

                    var index = -1

                    for (i in expr.indices) {
                        if (expr[i] == c) {
                            if (i > 0) {
                                // get char left
                                val left = expr[i - 1]
                                if (left.isLetterOrDigit()) {
                                    // add multiplication
                                    mulLeft = true
                                }
                            }

                            if (i < expr.length - 1) {
                                val right = expr[i + 1]
                                if (right.isLetterOrDigit()) {
                                    mulRight = true
                                }
                            }
                            index = i
                            break
                        }
                    }

                    val replaceWith = (if (mulLeft) "*" else "") + (vars[c].toString().replace("-", "_")) + (if (mulRight) "*" else "")
                    expr = expr.replaceRange(index, index + 1, replaceWith)
                }
            } else if(expr.contains(c)) {
                throw IllegalArgumentException("Undefined variable: $c")
            }
        }

        // resolve implicit multiplication of brackets
        var bracketsResolved: Boolean
        while(true) {
            bracketsResolved = true

            for (i in expr.indices) {
                val c = expr[i]

                if (c == '(' && i > 0) {
                    if ((expr[i - 1].isLetterOrDigit() || expr[i - 1] == ')') && !isOperator(expr[i - 1])) {
                        // add in a multiplication
                        expr = expr.replaceRange(i, i + 1, "*(")
                        bracketsResolved = false
                        break
                    }
                } else if (c == ')' && i < expr.length - 1) {
                    if ((expr[i + 1].isLetterOrDigit() || expr[i + 1] == '(') && !isOperator(expr[i + 1])) {
                        expr = expr.replaceRange(i, i + 1, ")*")
                        bracketsResolved = false
                        break
                    }
                }
            }

            if(bracketsResolved) {
                break
            }
        }

        val v = Value(expr)
        v.resolve()

        if(varname != null) {
            print("$varname=")
            putVariable(varname, v.resolvedValue)
        }


        println(v.resolvedValue)
    }

}