package com.example.appacademicoorganizacao;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CircularProgressView extends View {

    private Paint backgroundPaint;
    private Paint progressPaint;

    private float progress =1f; //indica q ta cheio

    public CircularProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(0x33FFFFFF);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(20);
        backgroundPaint.setAntiAlias(true);

        progressPaint = new Paint();
        progressPaint.setColor(0xFFFFFFFF);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(20);
        progressPaint.setAntiAlias(true);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setProgress(float value) {
        progress = value;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float size = Math.min(getWidth(), getHeight());
        float radius = size / 2 - 20;

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;

        //o fundo
        canvas.drawCircle(cx, cy, radius, backgroundPaint);

        //o progresso
        canvas.drawArc(
                cx - radius,
                cy - radius,
                cx + radius,
                cy + radius,
                -90,
                360 * progress,
                false,
                progressPaint
        );
    }
}
