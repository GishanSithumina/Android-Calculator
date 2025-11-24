package com.example.calculator;

import java.util.Stack;

public class MathExpressionParser {

    public double evaluateExpression(String expression) {
        try {
            // Remove spaces and handle common variations
            expression = expression.replaceAll("\\s+", "")
                    .replaceAll("x", "*")
                    .replaceAll("X", "*")
                    .replaceAll("÷", "/");

            // Validate expression
            if (!isValidMathExpression(expression)) {
                throw new IllegalArgumentException("Invalid mathematical expression");
            }

            return eval(expression);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid mathematical expression: " + expression);
        }
    }

    // Add this missing method
    public boolean isValidExpression(String expression) {
        return isValidMathExpression(expression);
    }

    private boolean isValidMathExpression(String expression) {
        // Basic validation for mathematical expressions
        if (expression == null || expression.isEmpty()) {
            return false;
        }

        // Check for valid characters
        if (!expression.matches("[0-9+\\-*/().]+")) {
            return false;
        }

        // Check for balanced parentheses
        int balance = 0;
        for (char c : expression.toCharArray()) {
            if (c == '(') balance++;
            if (c == ')') balance--;
            if (balance < 0) return false;
        }

        return balance == 0;
    }

    private double eval(String expression) {
        Stack<Double> numbers = new Stack<>();
        Stack<Character> operators = new Stack<>();

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            // If character is a digit or decimal point, parse the number
            if (Character.isDigit(c) || c == '.') {
                StringBuilder sb = new StringBuilder();
                while (i < expression.length() &&
                        (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    sb.append(expression.charAt(i++));
                }
                i--;
                try {
                    numbers.push(Double.parseDouble(sb.toString()));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid number: " + sb.toString());
                }
            }
            // If opening parenthesis, push to operators stack
            else if (c == '(') {
                operators.push(c);
            }
            // If closing parenthesis, solve entire brace
            else if (c == ')') {
                while (!operators.isEmpty() && operators.peek() != '(') {
                    numbers.push(applyOperation(operators.pop(), numbers.pop(), numbers.pop()));
                }
                if (!operators.isEmpty() && operators.peek() == '(') {
                    operators.pop();
                } else {
                    throw new IllegalArgumentException("Mismatched parentheses");
                }
            }
            // If operator, process according to precedence
            else if (isOperator(c)) {
                while (!operators.isEmpty() && hasPrecedence(c, operators.peek())) {
                    numbers.push(applyOperation(operators.pop(), numbers.pop(), numbers.pop()));
                }
                operators.push(c);
            } else {
                throw new IllegalArgumentException("Invalid character: " + c);
            }
        }

        // Process remaining operators
        while (!operators.isEmpty()) {
            if (operators.peek() == '(') {
                throw new IllegalArgumentException("Mismatched parentheses");
            }
            numbers.push(applyOperation(operators.pop(), numbers.pop(), numbers.pop()));
        }

        if (numbers.size() != 1) {
            throw new IllegalArgumentException("Invalid expression");
        }

        return numbers.pop();
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private boolean hasPrecedence(char op1, char op2) {
        if (op2 == '(' || op2 == ')') {
            return false;
        }
        if ((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-')) {
            return false;
        }
        return true;
    }

    private double applyOperation(char op, double b, double a) {
        switch (op) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0) {
                    throw new UnsupportedOperationException("Cannot divide by zero");
                }
                return a / b;
            default:
                throw new UnsupportedOperationException("Unknown operator: " + op);
        }
    }
}