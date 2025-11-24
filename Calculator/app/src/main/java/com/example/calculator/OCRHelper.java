package com.example.calculator;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class OCRHelper {
    private static final String TAG = "OCRHelper";
    private TextRecognizer textRecognizer;

    public interface OCRListener {
        void onOCRSuccess(String recognizedText);
        void onOCRError(String error);
    }

    public OCRHelper() {
        initializeOCR();
    }

    private void initializeOCR() {
        // Initialize text recognizer with default options
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    }

    public void recognizeTextFromBitmap(Bitmap originalBitmap, OCRListener listener) {
        if (textRecognizer == null) {
            listener.onOCRError("OCR not initialized");
            return;
        }

        if (originalBitmap == null) {
            listener.onOCRError("Bitmap is null");
            return;
        }

        try {
            // Preprocess the image for better recognition
            Bitmap processedBitmap = preprocessImage(originalBitmap);

            InputImage image = InputImage.fromBitmap(processedBitmap, 0);

            textRecognizer.process(image)
                    .addOnSuccessListener(new OnSuccessListener<Text>() {
                        @Override
                        public void onSuccess(Text visionText) {
                            String resultText = processOCRResult(visionText);
                            Log.d(TAG, "Raw OCR Result: " + resultText);
                            listener.onOCRSuccess(resultText);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "OCR failed: " + e.getMessage());
                            listener.onOCRError("Recognition failed: " + e.getMessage());
                        }
                    });

        } catch (Exception e) {
            Log.e(TAG, "Image processing error: " + e.getMessage());
            listener.onOCRError("Image processing error");
        }
    }

    private Bitmap preprocessImage(Bitmap original) {
        // Create a new bitmap with the same dimensions
        Bitmap processed = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(processed);
        Paint paint = new Paint();

        // Convert to grayscale and increase contrast
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0f); // Convert to grayscale

        // Increase contrast
        float contrast = 1.5f;
        float scale = contrast;
        float translate = (-0.5f * contrast + 0.5f) * 255f;
        matrix.set(new float[] {
                scale, 0, 0, 0, translate,
                0, scale, 0, 0, translate,
                0, 0, scale, 0, translate,
                0, 0, 0, 1, 0
        });

        paint.setColorFilter(new ColorMatrixColorFilter(matrix));
        canvas.drawBitmap(original, 0, 0, paint);

        return processed;
    }

    private String processOCRResult(Text visionText) {
        StringBuilder result = new StringBuilder();
        double maxConfidence = 0;
        String bestLine = "";

        for (Text.TextBlock block : visionText.getTextBlocks()) {
            for (Text.Line line : block.getLines()) {
                String lineText = line.getText().trim();
                Log.d(TAG, "OCR Line: " + lineText);

                // Get the confidence of the line
                Rect boundingBox = line.getBoundingBox();
                if (boundingBox != null) {
                    // Simple confidence calculation based on text characteristics
                    double confidence = calculateConfidence(lineText);
                    if (confidence > maxConfidence) {
                        maxConfidence = confidence;
                        bestLine = lineText;
                    }
                }

                if (!lineText.isEmpty()) {
                    if (result.length() > 0) {
                        result.append(" ");
                    }
                    result.append(lineText);
                }
            }
        }

        // If we have multiple lines, prefer the one with highest confidence
        if (!bestLine.isEmpty() && maxConfidence > 0.5) {
            String cleanedBest = cleanMathematicalText(bestLine);
            Log.d(TAG, "Best line selected: " + cleanedBest + " (confidence: " + maxConfidence + ")");
            return cleanedBest;
        }

        String finalResult = cleanMathematicalText(result.toString());
        Log.d(TAG, "Final cleaned result: " + finalResult);
        return finalResult;
    }

    private double calculateConfidence(String text) {
        // Calculate confidence based on mathematical expression characteristics
        double confidence = 0.0;

        // Higher confidence for texts that look like math expressions
        if (text.matches(".*[0-9].*")) confidence += 0.3;
        if (text.matches(".*[+\\-*/].*")) confidence += 0.3;
        if (text.matches(".*[()].*")) confidence += 0.2;
        if (text.length() >= 3 && text.length() <= 15) confidence += 0.2; // Reasonable length

        return confidence;
    }

    private String cleanMathematicalText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // Multiple cleaning passes for better accuracy
        String cleaned = text;

        // First pass: Common OCR errors
        cleaned = cleaned
                .replaceAll("\\s+", "")  // Remove all spaces
                .replaceAll("[lL|!I]", "1")  // Common OCR error: l/L/|/!/I as 1
                .replaceAll("[oO]", "0")   // Common OCR error: o/O as 0
                .replaceAll("[sS]", "5")   // Common OCR error: s/S as 5
                .replaceAll("[zZ]", "2")   // Common OCR error: z/Z as 2
                .replaceAll("[aA]", "4")   // Common OCR error: a/A as 4
                .replaceAll(":", "/")      // Common OCR error: : as /
                .replaceAll("[{}]", "()")  // Replace curly braces with parentheses
                .replaceAll("\\[", "(")    // Replace square brackets with parentheses
                .replaceAll("\\]", ")")
                .replaceAll("'", "")       // Remove apostrophes
                .replaceAll("\"", "")      // Remove quotes
                .replaceAll("`", "")       // Remove backticks
                .replaceAll("i", "1")      // Lower i as 1
                .replaceAll("B", "8")      // B as 8
                .replaceAll("b", "6")      // b as 6
                .replaceAll("g", "9")      // g as 9
                .replaceAll("q", "9")      // q as 9
                .replaceAll("t", "7")      // t as 7
                .replaceAll("T", "7")      // T as 7
                .replaceAll("Y", "7")      // Y as 7
                .replaceAll("Z", "2")      // Z as 2
                .replaceAll("S", "5");     // S as 5

        // Second pass: Fix common operator confusions
        cleaned = cleaned
                .replaceAll("\\+\\+", "+")    // Fix double plus
                .replaceAll("--", "-")        // Fix double minus
                .replaceAll("\\*\\*", "*")    // Fix double multiply
                .replaceAll("//", "/")        // Fix double divide
                .replaceAll("=", "")          // Remove equals signs
                .replaceAll(",", ".")         // Commas as decimals
                .replaceAll(";", ".");        // Semicolons as decimals

        // Third pass: Validate and fix structure
        cleaned = fixCommonPatterns(cleaned);

        return cleaned;
    }

    private String fixCommonPatterns(String text) {
        if (text == null || text.length() < 2) {
            return text;
        }

        StringBuilder result = new StringBuilder();
        char[] chars = text.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char current = chars[i];

            // Fix common patterns like "1-" becoming "1-"
            if (i > 0 && isOperator(current) && isOperator(chars[i-1])) {
                // Skip consecutive operators (keep the last one)
                continue;
            }

            // Fix patterns like "23" becoming "23" (no change needed)
            result.append(current);
        }

        return result.toString();
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    public void close() {
        if (textRecognizer != null) {
            textRecognizer.close();
        }
    }
}