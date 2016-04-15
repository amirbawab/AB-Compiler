# AB-Compiler

## Lexical analysis

### Deterministic Finite Automaton
The user input is scanned and classified as tokens based on a modified version of my repository: <a href="https://github.com/amirbawab/Finite-Automata-machine-simulator">Finite-Automata-machine</a> <br/>
DFA input file: <a href="https://github.com/amirbawab/AB-Compiler/blob/master/src/main/resources/scanner/machine.dfa">machine.dfa</a>

<img src="https://github.com/amirbawab/AB-Compiler/blob/master/src/main/resources/images/dfa/DFA.jpg"/>

### Final States Tokens

|         Token         |       Value       | Message |
|:---------------------:|:-----------------:|:-----------------:|
|        T_ASSIGN       |         =         | - |
|        T_IS_EQUAL     |         ==        | - |
|       T_LESS_THAN     |         <         | - |
|     T_LESS_OR_EQUAL   |         <=        | - |
|      T_IS_NOT_EQUAL   |         <>        | - |
|      T_GREATER_THAN   |         >         | - |
|    T_GREATER_OR_EQUAL |         >=        | - |
|       T_SEMICOLON     |         ;         | - |
|         T_COMMA       |         ,         | - |
|          T_DOT        |         .         | - |
|          T_PLUS       |         +         | - |
|         T_MINUS       |         -         | - |
|        T_MULTIPLY     |         *         | - |
|         T_DIVIDE      |         /         | - |
|     T_BLOCK_COMMENT   |        /**/       | - |
|       T_OPEN_PAREN    |         (         | - |
|      T_CLOSE_PAREN    |         )         | - |
|       T_OPEN_CURLY    |         {         | - |
|      T_CLOSE_CURLY    |         }         | - |
|      T_OPEN_SQUARE    |         [         | - |
|      T_CLOSE_SQUARE   |         ]         | - |
|       T_IDENTIFIER    |        *id*       | - |
|        T_INTEGER      |        0-9        | - |
|         T_FLOAT       |   *0.25*,  *1.5*  | - |
|     T_INLINE_COMMENT  |         //        | - |
|           T_IF        |         if        | - |
|          T_THEN       |        then       | - |
|          T_ELSE       |        else       | - |
|          T_FOR        |        for        | - |
|         T_CLASS       |       class       | - |
|        T_INT_TYPE     |        int        | - |
|       T_FLOAT_TYPE    |       float       | - |
|          T_GET        |        get        | - |
|          T_PUT        |        put        | - |
|         T_RETURN      |       return      | - |
|          T_AND        |        and        | - |
|          T_NOT        |        not        | - |
|           T_OR        |         or        | - |
|        T_PROGRAM      |      program      | - |
|    T_ERR_LEADING_ZERO |       *0123*      | Number '%s' at line %d col %d cannot start with a zero |
|    T_ERR_INVALID_CHAR |        *@*        | Invalid character '%s' at line %d col %d |
|   T_ERR_TRAILING_ZERO |        1.00       | Float number '%s' at line %d col %d cannot end with a zero |
|    T_ERR_FLOAT_FORMAT |       *1.a*       | Wrong float number '%s' at line %d col %d |
|   T_ERR_BLOCK_COMMENT |       /* EOF      | Block comment '%s' at line %d col %d is not closed |


## Syntax analysis: (Top-down parsing)

Link to the most recent Grammar file:
<a href="https://github.com/amirbawab/AB-Compiler/blob/master/src/main/resources/parser/grammar.bnf">grammar.bnf</a>

The file syntax is compatible with the <a href="http://atocc.de/cgi-bin/atocc/site.cgi?lang=en&site=main">AtoCC kfG Edit</a> software.<br/>
*Note: To try the grammar on the AtoCC software remove all the semantic action tokens of the form `#action#`. You can use replace all using
regex `#[^ ]+#`*

### Error recovery

**Pop** grammar stack if the next input token is in the follow set of the top of the grammar stack or if the input reaches EOF represented by `$`. 
Otherwise **scan** next input token.

### Error messages:

List of the most recent file:
<a href="https://github.com/amirbawab/AB-Compiler/blob/master/src/main/java/parser/helper/ABParserMessageHelper.java">ABParserMessageHelper.java</a>

## Semantic analysis

In order to perform semantic analysis on the user input, the grammar is augmented with semantic actions tokens of the form` #action#`. Each time the parser encounters a semantic action token, it will execute the corresponding function assigned to it in the code.

### Semantic actions
Link to all semantic action tokens:
<a href="https://github.com/amirbawab/AB-Compiler/blob/master/src/main/java/semantic/ABSemantic.java#L44">ABSemantic.java</a>
<br/>

### Error messages
Link to most recent file: 
<a href="https://github.com/amirbawab/AB-Compiler/blob/master/src/main/java/semantic/helper/ABSemanticMessageHelper.java">ABSemanticMessageHelper.java</a>
