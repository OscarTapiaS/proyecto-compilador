package com.compiler.lexer.regex;

import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.nfa.State;
import com.compiler.lexer.nfa.Transition;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Stack;

/**
 * RegexParser
 * -----------
 * This class provides functionality to convert infix regular expressions into nondeterministic finite automata (NFA)
 * using Thompson's construction algorithm. It supports standard regex operators: concatenation (·), union (|),
 * Kleene star (*), optional (?), and plus (+). The conversion process uses the Shunting Yard algorithm to transform
 * infix regex into postfix notation, then builds the corresponding NFA.
 *
 * Features:
 * - Parses infix regular expressions and converts them to NFA.
 * - Supports regex operators: concatenation, union, Kleene star, optional, plus.
 * - Implements Thompson's construction rules for NFA generation.
 *
 * Example usage:
 * <pre>
 *     RegexParser parser = new RegexParser();
 *     NFA nfa = parser.parse("a(b|c)*");
 * </pre>
 */
/**
 * Parses regular expressions and constructs NFAs using Thompson's construction.
 */

public class RegexParser {
     /**
     * Default constructor for RegexParser.
     */
    public RegexParser() {
        
    }

    /**
     * Converts an infix regular expression to an NFA.
     *
     * @param infixRegex The regular expression in infix notation.
     * @return The constructed NFA.
     */

    public NFA parse(String infixRegex) {
        // First,i insert the explicit concatenation operator to avoid ambiguities.
        String explicitRegex = ShuntingYard.insertConcatenationOperator(infixRegex);
        // Then, I convert the expression to postfix notation using Shunting Yard.
        String postfixRegex = ShuntingYard.toPostfix(explicitRegex);

        // Finally, I build the NFA from the postfix expression.
        return buildNfaFromPostfix(postfixRegex);
    }

    /**
     * Builds an NFA from a postfix regular expression.
     *
     * @param postfixRegex The regular expression in postfix notation.
     * @return The constructed NFA.
     */

    private NFA buildNfaFromPostfix(String postfixRegex) {
        // Use a stack to build NFA fragments and combine them according to the operator.
        Stack<NFA> stack = new Stack<>();

        for (int i = 0; i < postfixRegex.length(); i++) {
            char c = postfixRegex.charAt(i);
            if (isOperand(c)) {
                // If it's an operand (literal character), create a basic NFA for it.
                stack.push(createNfaForCharacter(c));
            } else {
                // If it's an operator, apply the corresponding construction.
                switch (c) {
                    case '·' -> handleConcatenation(stack);
                    case '|' -> handleUnion(stack);
                    case '*' -> handleKleeneStar(stack);
                    case '+' -> handlePlus(stack);
                    case '?' -> handleOptional(stack);
                    default -> throw new IllegalArgumentException("Unknown operator: " + c);
                }
            }
        }

        if (stack.size() != 1) {
            // If at the end there is not exactly one NFA, the expression was invalid.
            throw new IllegalStateException("Invalid postfix expression");
        }
        return stack.pop();
    }

    /**
     * Handles the '?' operator (zero or one occurrence).
     * Pops an NFA from the stack and creates a new NFA that accepts zero or one occurrence.
     * @param stack The NFA stack.
     */
    private void handleOptional(Stack<NFA> stack) {
        // The '?' operator means the subexpression may appear once or not at all.
        NFA nfa = stack.pop();
        State start = new State();
        State end = new State();
        end.isFinal = true;

        start.transitions.add(new Transition(null, nfa.startState));
        start.transitions.add(new Transition(null, end));

        nfa.endState.isFinal = false;
        nfa.endState.transitions.add(new Transition(null, end));

        stack.push(new NFA(start, end));
    }

    /**
     * Handles the '+' operator (one or more occurrences).
     * Pops an NFA from the stack and creates a new NFA that accepts one or more occurrences.
     * @param stack The NFA stack.
     */

    private void handlePlus(Stack<NFA> stack) {
        // The '+' operator is like '*' but requires at least one occurrence.
        NFA nfa = stack.pop();
        State start = new State();
        State end = new State();
        end.isFinal = true;

        start.transitions.add(new Transition(null, nfa.startState));

        nfa.endState.isFinal = false;
        nfa.endState.transitions.add(new Transition(null, nfa.startState));
        nfa.endState.transitions.add(new Transition(null, end));

        stack.push(new NFA(start, end));
    }

    /**
     * Handles the Kleene star operator (*).
     * Pops an NFA from the stack and creates a new NFA that accepts zero or more repetitions.
     * @param stack The NFA stack.
     */
    private void handleKleeneStar(Stack<NFA> stack) {
        // The '*' operator allows repeating the subexpression any number of times, including zero.
        NFA nfa = stack.pop();
        State start = new State();
        State end = new State();
        end.isFinal = true;

        start.transitions.add(new Transition(null, nfa.startState));
        start.transitions.add(new Transition(null, end));

        nfa.endState.isFinal = false;
        nfa.endState.transitions.add(new Transition(null, nfa.startState));
        nfa.endState.transitions.add(new Transition(null, end));

        stack.push(new NFA(start, end));
    }

    /**
     * Handles the concatenation operator (·).
     * Pops two NFAs from the stack and connects them in sequence.
     * @param stack The NFA stack.
     */
    private void handleConcatenation(Stack<NFA> stack) {
        
        NFA right = stack.pop();
        NFA left = stack.pop();

        left.endState.isFinal = false;
        left.endState.transitions.add(new Transition(null, right.startState));

        stack.push(new NFA(left.startState, right.endState));
    }

    /**
     * Handles the union operator (|).
     * Pops two NFAs from the stack and creates a new NFA that accepts either.
     * @param stack The NFA stack.
     */
    private void handleUnion(Stack<NFA> stack) {
        
        NFA b = stack.pop();
        NFA a = stack.pop();

        State start = new State();
        State end = new State();
        end.isFinal = true;

        start.transitions.add(new Transition(null, a.startState));
        start.transitions.add(new Transition(null, b.startState));

        a.endState.isFinal = false;
        b.endState.isFinal = false;

        a.endState.transitions.add(new Transition(null, end));
        b.endState.transitions.add(new Transition(null, end));

        stack.push(new NFA(start, end));
    }

    private NFA createNfaForCharacter(char c) {
        // Base case: an NFA that recognizes a single character.
        State start = new State();
        State end = new State();
        end.isFinal = true;

        start.transitions.add(new Transition(c, end));
        return new NFA(start, end);
    }

    private boolean isOperand(char c) {
        return c != '|' && c != '*' && c != '?' && c != '+' && c != '(' && c != ')' && c != '·';
    }
}