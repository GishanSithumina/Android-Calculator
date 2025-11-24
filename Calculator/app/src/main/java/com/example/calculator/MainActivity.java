package com.example.calculator;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements OCRHelper.OCRListener {

    private DrawingView drawingView;
    private TextView equationText, resultText;
    private Button clearButton, solveButton, buttonCalculatorBtn;
    private MathExpressionParser mathParser;
    private OCRHelper ocrHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupListeners();
        setupUserGuidance();

        mathParser = new MathExpressionParser();
        ocrHelper = new OCRHelper();
    }

    private void initializeViews() {
        drawingView = findViewById(R.id.drawingView);
        equationText = findViewById(R.id.equationText);
        resultText = findViewById(R.id.resultText);
        clearButton = findViewById(R.id.clearButton);
        solveButton = findViewById(R.id.solveButton);
        buttonCalculatorBtn = findViewById(R.id.buttonCalculatorBtn);
    }

    private void setupListeners() {
        clearButton.setOnClickListener(v -> {
            drawingView.clearCanvas();
            equationText.setText("Draw your equation below");
            resultText.setText("Result will appear here");
        });

        solveButton.setOnClickListener(v -> {
            // Get the drawing as bitmap and process with OCR
            Bitmap drawingBitmap = drawingView.getBitmap();
            if (drawingBitmap != null) {
                equationText.setText("Processing...");
                resultText.setText("Recognizing equation...");
                ocrHelper.recognizeTextFromBitmap(drawingBitmap, this);
            } else {
                Toast.makeText(this, "Please draw an equation first", Toast.LENGTH_SHORT).show();
            }
        });

        buttonCalculatorBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ButtonCalculatorActivity.class);
            startActivity(intent);
        });
    }

    private void setupUserGuidance() {
        // Show initial instructions
        equationText.setText("Tips: Write numbers and operators clearly\n• Make digits large and clear\n• Use +, -, *, / for operations\n• Leave space between symbols");
    }

    // OCRListener implementation
    @Override
    public void onOCRSuccess(String recognizedText) {
        runOnUiThread(() -> {
            if (recognizedText != null && !recognizedText.trim().isEmpty()) {
                String finalText = finalCleanText(recognizedText);
                equationText.setText("Recognized: " + finalText);

                // Validate if it looks like a math expression
                if (isValidMathExpression(finalText)) {
                    try {
                        double result = mathParser.evaluateExpression(finalText);
                        String resultString = formatResult(result);
                        resultText.setText("✓ Result: " + resultString);
                    } catch (Exception e) {
                        resultText.setText("✗ Can't solve: " + finalText + "\nTry writing more clearly");
                    }
                } else {
                    resultText.setText("✗ Not a valid math expression\nTry: 2+3, 5*4, etc.");
                }
            } else {
                equationText.setText("No text recognized");
                resultText.setText("Please draw numbers and operators clearly");
            }
        });
    }

    @Override
    public void onOCRError(String error) {
        runOnUiThread(() -> {
            equationText.setText("Recognition Error");
            resultText.setText(error);
            Toast.makeText(this, "OCR failed: " + error, Toast.LENGTH_LONG).show();
        });
    }

    private String finalCleanText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // Additional cleaning specific to mathematical expressions
        return text
                .replaceAll("[^0-9+\\-*/().]", "") // Keep only math characters
                .replaceAll("\\+\\+", "+")          // Remove duplicate operators
                .replaceAll("--", "-")
                .replaceAll("\\*\\*", "*")
                .replaceAll("//", "/")
                .replaceAll("\\.\\.", ".")          // Remove duplicate decimals
                .replaceAll("\\(", "(")             // Ensure proper parentheses
                .replaceAll("\\)", ")");
    }

    private String formatResult(double result) {
        if (result == (int) result) {
            return String.valueOf((int) result);
        } else {
            // Format to remove trailing zeros
            String formatted = String.format("%.4f", result);
            return formatted.replaceAll("0*$", "").replaceAll("\\.$", "");
        }
    }

    private boolean isValidMathExpression(String expression) {
        if (expression == null || expression.isEmpty()) {
            return false;
        }

        // Basic check for valid math characters
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ocrHelper != null) {
            ocrHelper.close();
        }
    }
}