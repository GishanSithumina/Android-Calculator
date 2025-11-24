package com.example.calculator;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class ButtonCalculatorActivity extends AppCompatActivity {

    private TextView displayText, resultText;
    private StringBuilder currentInput = new StringBuilder();
    private MathExpressionParser mathParser;
    private boolean lastInputWasOperator = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button_calculator);

        initializeViews();
        setupClickListeners();
        mathParser = new MathExpressionParser();
    }

    private void initializeViews() {
        displayText = findViewById(R.id.displayText);
        resultText = findViewById(R.id.calcResultText);

        // Set initial display
        displayText.setText("0");
        resultText.setText("Ready");
    }

    private void setupClickListeners() {
        // Number buttons
        setNumberButtonClick(R.id.btn_0, "0");
        setNumberButtonClick(R.id.btn_1, "1");
        setNumberButtonClick(R.id.btn_2, "2");
        setNumberButtonClick(R.id.btn_3, "3");
        setNumberButtonClick(R.id.btn_4, "4");
        setNumberButtonClick(R.id.btn_5, "5");
        setNumberButtonClick(R.id.btn_6, "6");
        setNumberButtonClick(R.id.btn_7, "7");
        setNumberButtonClick(R.id.btn_8, "8");
        setNumberButtonClick(R.id.btn_9, "9");

        // Operator buttons
        setOperatorButtonClick(R.id.btn_add, "+");
        setOperatorButtonClick(R.id.btn_subtract, "-");
        setOperatorButtonClick(R.id.btn_multiply, "*");
        setOperatorButtonClick(R.id.btn_divide, "/");
        setOperatorButtonClick(R.id.btn_decimal, ".");

        // Function buttons
        findViewById(R.id.btn_equals).setOnClickListener(v -> calculateResult());
        findViewById(R.id.btn_clear).setOnClickListener(v -> clearCalculator());
        findViewById(R.id.btn_backspace).setOnClickListener(v -> backspace());
        findViewById(R.id.btn_parentheses).setOnClickListener(v -> toggleParentheses());
    }

    private void setNumberButtonClick(int buttonId, String value) {
        findViewById(buttonId).setOnClickListener(v -> appendToInput(value, false));
    }

    private void setOperatorButtonClick(int buttonId, String value) {
        findViewById(buttonId).setOnClickListener(v -> appendToInput(value, true));
    }

    private void appendToInput(String value, boolean isOperator) {
        if (currentInput.length() == 0 && isOperator && !value.equals("-")) {
            return; // Don't start with operators except minus
        }

        if (isOperator && lastInputWasOperator) {
            // Replace the last operator with new one
            if (currentInput.length() > 0) {
                currentInput.deleteCharAt(currentInput.length() - 1);
            }
        }

        currentInput.append(value);
        displayText.setText(currentInput.toString());
        lastInputWasOperator = isOperator;

        // Auto-calculate as user types
        if (!isOperator) {
            calculateResult();
        }
    }

    private void calculateResult() {
        if (currentInput.length() == 0) {
            resultText.setText("0");
            return;
        }

        try {
            String expression = currentInput.toString();
            // Validate expression
            if (mathParser.isValidExpression(expression)) {
                double result = mathParser.evaluateExpression(expression);
                String resultString = formatResult(result);
                resultText.setText("= " + resultString);
            } else {
                resultText.setText("Invalid expression");
            }
        } catch (Exception e) {
            resultText.setText("Error");
        }
    }

    private String formatResult(double result) {
        if (result == (int) result) {
            return String.valueOf((int) result);
        } else {
            // Format to remove trailing zeros
            String formatted = String.format("%.6f", result);
            return formatted.replaceAll("0*$", "").replaceAll("\\.$", "");
        }
    }

    private void clearCalculator() {
        currentInput.setLength(0);
        displayText.setText("0");
        resultText.setText("Ready");
        lastInputWasOperator = false;
    }

    private void backspace() {
        if (currentInput.length() > 0) {
            char lastChar = currentInput.charAt(currentInput.length() - 1);
            currentInput.deleteCharAt(currentInput.length() - 1);

            if (currentInput.length() == 0) {
                displayText.setText("0");
                resultText.setText("Ready");
            } else {
                displayText.setText(currentInput.toString());
                // Check if last character was operator
                lastInputWasOperator = isOperator(lastChar);
                calculateResult();
            }
        }
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private void toggleParentheses() {
        String expression = currentInput.toString();
        int openCount = countOccurrences(expression, '(');
        int closeCount = countOccurrences(expression, ')');

        if (openCount <= closeCount) {
            appendToInput("(", true);
        } else {
            appendToInput(")", true);
        }
    }

    private int countOccurrences(String str, char ch) {
        int count = 0;
        for (char c : str.toCharArray()) {
            if (c == ch) count++;
        }
        return count;
    }
}