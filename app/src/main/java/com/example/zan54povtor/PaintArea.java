package com.example.zan54povtor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class PaintArea extends View {
    private Bitmap backgroundBitmap;
    private ArrayList<Path> paths = new ArrayList<>();
    private Path currentPath = new Path();
    private Paint drawPaint = new Paint();
    private ArrayList<TextElement> textElements = new ArrayList<>();
    private Paint textPaint = new Paint();

    public PaintArea(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setupPaints();
        setDrawingCacheEnabled(true);
    }

    private void setupPaints() {
        drawPaint.setColor(Color.BLACK);
        drawPaint.setStrokeWidth(12f);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setAntiAlias(true);

        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(48f);
        textPaint.setAntiAlias(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentPath = new Path();
                currentPath.moveTo(x, y);
                paths.add(currentPath);
                break;
            case MotionEvent.ACTION_MOVE:
                currentPath.lineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                currentPath = new Path();
                break;
        }
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (backgroundBitmap != null) {
            canvas.drawBitmap(backgroundBitmap, 0, 0, null);
        }
        for (Path path : paths) {
            canvas.drawPath(path, drawPaint);
        }
        for (TextElement te : textElements) {
            canvas.drawText(te.text, te.x, te.y, textPaint);
        }
    }

    public void addTextElement(float x, float y, String text) {
        textElements.add(new TextElement(x, y, text));
        invalidate();
    }

    public void clearCanvas() {
        paths.clear();
        textElements.clear();
        invalidate();
    }

    public void setBackgroundImage(Bitmap bitmap) {
        backgroundBitmap = Bitmap.createScaledBitmap(bitmap, getWidth(), getHeight(), true);
        invalidate();
    }

    public Bitmap getDrawingCache() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        if (backgroundBitmap != null) {
            canvas.drawBitmap(backgroundBitmap, 0, 0, null);
        }
        for (Path path : paths) {
            canvas.drawPath(path, drawPaint);
        }
        for (TextElement te : textElements) {
            canvas.drawText(te.text, te.x, te.y, textPaint);
        }
        return bitmap;
    }
}