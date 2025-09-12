package com.compiler.lexer.tokenizer;

import java.util.List;

/**
 * Demonstration class showing how to use the tokenizer with various input examples.
 * This class provides examples of how to set up lexical rules and tokenize different
 * types of input strings.
 */
public class TokenizerDemo {

    /**
     * Main method to run the tokenizer demonstration.
     */
    public static void main(String[] args) {
        // Create tokenizer with standard language rules
        List<LexicalRule> rules = new LexicalRulesBuilder()
                .addStandardLanguageRules()
                .build();
        
        Tokenizer tokenizer = new Tokenizer(rules);
        
        // Test cases
        String[] testCases = {
            "if (x <= 10) { return true; }",
            "int count = 42;",
            "float pi = 3.14159;",
            "// This is a comment\nstring name = \"Hello World\";",
            "while (i < n && found) { i++; }",
            "boolean flag = true;",
            "/* Multi-line\n   comment */\nvoid main() {}"
        };
        
        System.out.println("=== TOKENIZER DEMONSTRATION ===\n");
        
        for (int i = 0; i < testCases.length; i++) {
            String input = testCases[i];
            System.out.println("Test Case " + (i + 1) + ":");
            System.out.println("Input: " + input.replace("\n", "\\n"));
            System.out.println("Tokens:");
            
            try {
                List<Token> tokens = tokenizer.tokenize(input);
                for (Token token : tokens) {
                    if (token.getType() != TokenType.EOF) {
                        System.out.printf("  %-15s : '%s'", 
                                        token.getType(), 
                                        token.getLexeme().replace("\n", "\\n"));
                        if (token.getPosition() >= 0) {
                            System.out.printf(" (pos: %d, line: %d, col: %d)", 
                                            token.getPosition(), token.getLine(), token.getColumn());
                        }
                        System.out.println();
                    }
                }
                System.out.println();
            } catch (TokenizerException e) {
                System.out.println("  ERROR: " + e.getMessage());
                System.out.println();
            }
        }
        
        // Demonstrate maximal munch
        demonstrateMaximalMunch(tokenizer);
        
        // Demonstrate priority handling
        demonstratePriorityHandling(tokenizer);
        
        // Show rule information
        showRuleInformation(rules);
    }
    
    /**
     * Demonstrates the maximal munch (longest match) strategy.
     */
    private static void demonstrateMaximalMunch(Tokenizer tokenizer) {
        System.out.println("=== MAXIMAL MUNCH DEMONSTRATION ===\n");
        
        String[] maximalMunchCases = {
            "<",      // Should be LESS_THAN
            "<=",     // Should be LESS_EQUAL (longer match)
            "=",      // Should be ASSIGN
            "==",     // Should be EQUALS (longer match)
            "& &&",   // Should be BITWISE_AND followed by LOGICAL_AND
        };
        
        for (String input : maximalMunchCases) {
            System.out.println("Input: '" + input + "'");
            try {
                List<Token> tokens = tokenizer.tokenize(input);
                for (Token token : tokens) {
                    if (token.getType() != TokenType.EOF) {
                        System.out.println("  " + token.getType() + " : '" + token.getLexeme() + "'");
                    }
                }
                System.out.println();
            } catch (TokenizerException e) {
                System.out.println("  ERROR: " + e.getMessage());
                System.out.println();
            }
        }
    }
    
    /**
     * Demonstrates priority-based conflict resolution.
     */
    private static void demonstratePriorityHandling(Tokenizer tokenizer) {
        System.out.println("=== PRIORITY HANDLING DEMONSTRATION ===\n");
        
        String[] priorityCases = {
            "if",       // Should be KEYWORD_IF, not IDENTIFIER
            "else",     // Should be KEYWORD_ELSE, not IDENTIFIER  
            "myif",     // Should be IDENTIFIER (doesn't match keyword exactly)
            "true",     // Should be KEYWORD_TRUE, not IDENTIFIER
            "false",    // Should be KEYWORD_FALSE, not IDENTIFIER
        };
        
        System.out.println("Keywords vs Identifiers (keywords have higher priority):");
        for (String input : priorityCases) {
            System.out.println("Input: '" + input + "'");
            try {
                List<Token> tokens = tokenizer.tokenize(input);
                for (Token token : tokens) {
                    if (token.getType() != TokenType.EOF) {
                        System.out.println("  " + token.getType() + " : '" + token.getLexeme() + "'");
                    }
                }
                System.out.println();
            } catch (TokenizerException e) {
                System.out.println("  ERROR: " + e.getMessage());
                System.out.println();
            }
        }
    }
    
    /**
     * Shows information about the lexical rules being used.
     */
    private static void showRuleInformation(List<LexicalRule> rules) {
        System.out.println("=== LEXICAL RULES INFORMATION ===\n");
        System.out.println("Total rules: " + rules.size());
        System.out.println("Rules (in priority order):");
        
        for (int i = 0; i < Math.min(10, rules.size()); i++) {
            LexicalRule rule = rules.get(i);
            System.out.printf("  %2d. %-20s : %-15s %s%n", 
                            rule.getPriority(), 
                            rule.getTokenType(), 
                            "'" + rule.getPattern() + "'",
                            rule.isIgnore() ? "(ignored)" : "");
        }
        
        if (rules.size() > 10) {
            System.out.println("  ... and " + (rules.size() - 10) + " more rules");
        }
        System.out.println();
    }
    
    /**
     * Demonstrates error handling with invalid input.
     */
    public static void demonstrateErrorHandling() {
        List<LexicalRule> rules = new LexicalRulesBuilder()
                .addStandardLanguageRules()
                .build();
        
        Tokenizer tokenizer = new Tokenizer(rules);
        
        System.out.println("=== ERROR HANDLING DEMONSTRATION ===\n");
        
        String[] errorCases = {
            "@invalid",    // @ is not defined in our rules
            "#hashtag",    // # is not defined in our rules
            "\"unclosed string",  // Unclosed string literal
        };
        
        for (String input : errorCases) {
            System.out.println("Input: '" + input + "'");
            try {
                List<Token> tokens = tokenizer.tokenize(input);
                for (Token token : tokens) {
                    if (token.getType() != TokenType.EOF) {
                        System.out.println("  " + token.getType() + " : '" + token.getLexeme() + "'");
                    }
                }
            } catch (TokenizerException e) {
                System.out.println("  ERROR: " + e.getMessage());
            }
            System.out.println();
        }
    }
}