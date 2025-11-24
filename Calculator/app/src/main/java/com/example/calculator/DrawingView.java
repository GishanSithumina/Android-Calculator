package com.example.calculator;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DrawingView extends View {
    private Paint drawPaint;
    private Path drawPath;
    private Bitmap canvasBitmap;
    private Canvas drawCanvas;
    private int backgroundColor = Color.WHITE;
    private Paint canvasPaint;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
        drawPath = new Path();

        // Modern paint settings
        drawPaint = new Paint();
        drawPaint.setColor(Color.parseColor("#6366F1")); // Primary color
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(18f); // Slightly thicker for modern look
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);

        setBackgroundColor(backgroundColor);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
        drawCanvas.drawColor(backgroundColor);

        // Draw a subtle grid for better user experience
        drawGrid();
    }

    private void drawGrid() {
        Paint gridPaint = new Paint();
        gridPaint.setColor(Color.parseColor("#F1F5F9"));
        gridPaint.setStrokeWidth(1f);

        int gridSize = 50; // Grid spacing

        // Vertical lines
        for (int x = gridSize; x < getWidth(); x += gridSize) {
            drawCanvas.drawLine(x, 0, x, getHeight(), gridPaint);
        }

        // Horizontal lines
        for (int y = gridSize; y < getHeight(); y += gridSize) {
            drawCanvas.drawLine(0, y, getWidth(), y, gridPaint);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                break;

            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                break;

            case MotionEvent.ACTION_UP:
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
                break;

            default:
                return false;
        }

        invalidate();
        return true;
    }

    public void clearCanvas() {
        drawCanvas.drawColor(backgroundColor, PorterDuff.Mode.CLEAR);
        drawCanvas.drawColor(backgroundColor);
        drawGrid(); // Redraw grid after clearing
        drawPath.reset();
        invalidate();
    }

    public Bitmap getBitmap() {
        return canvasBitmap;
    }

    public void setStrokeColor(int color) {
        drawPaint.setColor(color);
    }

    public void setStrokeWidth(float width) {
        drawPaint.setStrokeWidth(width);
    }
}