# expression-solver
A Kotlin/Java based mathematical expression solver. Supports custom operators, variables, and functions.

## Usage
#### Basic Usage
For basic parsing, simply construct a `Value()` object with the expression to solve as the parameter. Then, call the `resolve()` function in order to begin parsing.

#### Example
    val expression = Value("2(1/2)+3")
    val floatValue = expression.resolve() // or call resolve() and use expression.resolvedValue
    
#### Usage with variables and functions
To perform more advanced parsing, you must use the `ExpressionEnvironment` class as your parser. First, create a new expression environment as so

    val environment = ExpressionEnvironment()
    
Each environment instance has its own set of variables and functions. These can be set by using the `putVar()` and `putFunction()` methods respectively. The parsing itself is done using the `ExpressionEnvironment.parseAndPrint(expression)`. In expressions, variables are defined using `!x=<expr>` and functions are defined using `!f(x)=<expr>`. They can be retrieved using `?x` and `?f(x)` respectively. Expressions can utilize these variables and functions by simplying calling on them. I.e. `x*2` and `f(2)*2`

#### Example
    val env = ExpressionEnvironment()
    env.parseAndPrint("!g=2") // define variable x
    env.parseAndPrint("!f(x)=x^2") // define function f(x)
    env.parseAndPrint("f(g)") // parse f(g) = (2)^2
   
