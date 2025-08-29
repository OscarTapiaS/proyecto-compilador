package com.compiler.lexer.regex;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.lang.StringBuilder;


/**
 * Utility class for regular expression parsing using the Shunting Yard
 * algorithm.
 * <p>
 * Provides methods to preprocess regular expressions by inserting explicit
 * concatenation operators, and to convert infix regular expressions to postfix
 * notation for easier parsing and NFA construction.
 */
public class ShuntingYard {

    /**
     * Default constructor for ShuntingYard.
     */
    public ShuntingYard() {
        // Constructor doesn't need specific implementation
    }

    /**
     * Inserts the explicit concatenation operator ('·') into the regular
     * expression according to standard rules. This makes implicit
     * concatenations explicit, simplifying later parsing.
     *
     * @param regex Input regular expression (may have implicit concatenation).
     * @return Regular expression with explicit concatenation operators.
     */
    public static String insertConcatenationOperator(String regex) {
        // TODO: Implement insertConcatenationOperator
        /*
            Pseudocode:
            For each character in regex:
                - Append current character to output
                - If not at end of string:
                        - Check if current and next character form an implicit concatenation
                        - If so, append '·' to output
            Return output as string
         */


        if (regex == null || regex.isEmpty()) {
        return regex;
    }
    StringBuilder output = new StringBuilder();
    for (int i = 0; i < regex.length(); i++) {
        char current = regex.charAt(i);
        output.append(current);

        if (i < regex.length() - 1) {
            char next = regex.charAt(i + 1);
            boolean needsConcat = (isOperand(current) || current == ')' || current == '*' ||
                                   current == '+' || current == '?') &&
                                  (isOperand(next) || next == '(');
            if (needsConcat) {
                output.append('·');
            }
        }
    }
    return output.toString();
    }

    /**
     * Determines if the given character is an operand (not an operator or
     * parenthesis).
     *
     * @param c Character to evaluate.
     * @return true if it is an operand, false otherwise.
     */
    private static boolean isOperand(char c) {
        // TODO: Implement isOperand
        /*
        Pseudocode:
        Return true if c is not one of: '|', '*', '?', '+', '(', ')', '·'
         */


        return c != '|' && c != '*' && c != '?' && c != '+' && c != '(' && c != ')' && c != '·';
    }

    /**
     * Converts an infix regular expression to postfix notation using the
     * Shunting Yard algorithm. This is useful for constructing NFAs from
     * regular expressions.
     *
     * @param infixRegex Regular expression in infix notation.
     * @return Regular expression in postfix notation.
     */
    public static String toPostfix(String infixRegex) {

        // TODO: Implement toPostfix
        /*
        Pseudocode:
        1. Define operator precedence map
        2. Preprocess regex to insert explicit concatenation operators
        3. For each character in regex:
            - If operand: append to output
            - If '(': push to stack
            - If ')': pop operators to output until '(' is found
            - If operator: pop operators with higher/equal precedence, then push current operator
        4. After loop, pop remaining operators to output
        5. Return output as string
        */

        Map<Character, Integer> precedence = new HashMap<>();
        precedence.put('|', 1);  
        precedence.put('·', 2);  
        precedence.put('*', 3);  
        precedence.put('+', 3);  
        precedence.put('?', 3);  


        // Step 1: preprocess the regex to add explicit concatenations
        String preprocessed = insertConcatenationOperator(infixRegex);
        StringBuilder output = new StringBuilder();
        Stack<Character> operatorStack = new Stack<>();

        for (char c : preprocessed.toCharArray()) {
            if (isOperand(c)) {
                output.append(c);
            } else if (c == '(') {
                operatorStack.push(c);
            } else if (c == ')') {
                while (!operatorStack.isEmpty() && operatorStack.peek() != '(') {
                    output.append(operatorStack.pop());
                }
                if (!operatorStack.isEmpty()) operatorStack.pop();
            } else if (precedence.containsKey(c)) {
                while (!operatorStack.isEmpty() &&
                        operatorStack.peek() != '(' &&
                        precedence.containsKey(operatorStack.peek()) &&
                        precedence.get(operatorStack.peek()) >= precedence.get(c)) {
                    output.append(operatorStack.pop());
                }
                operatorStack.push(c);
            }
        }

        while (!operatorStack.isEmpty()) {
            output.append(operatorStack.pop());
        }

        return output.toString();
    }
}