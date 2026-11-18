package com.example.appacademicoorganizacao;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.appcompat.content.res.AppCompatResources;

// Ilustracao animada usada nas telas "sem internet" e "fora do ar".

public class OrbitView extends View {
    private final float dp;
    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint corePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Drawable icon;
    private ValueAnimator animator;
    private float angle = 0f;

    public OrbitView(Context context) { this(context, null); }

    public OrbitView(Context context, AttributeSet attrs) {
        super(context, attrs);
        dp = context.getResources().getDisplayMetrics().density;

        int ringColor = 0x33FFFFFF;
        int coreColor = 0xFF6726CC;
        int dotColor = 0xFFFFFFFF;
        int iconTint = 0xFFFFFFFF;
        int iconRes = 0;

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OrbitView);
            try {
                ringColor = a.getColor(R.styleable.OrbitView_orbitRingColor, ringColor);
                coreColor = a.getColor(R.styleable.OrbitView_orbitCoreColor, coreColor);
                dotColor = a.getColor(R.styleable.OrbitView_orbitDotColor, dotColor);
                iconTint = a.getColor(R.styleable.OrbitView_orbitIconTint, iconTint);
                iconRes = a.getResourceId(R.styleable.OrbitView_orbitIcon, 0);
            } finally {
                a.recycle();
            }
        }

        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(1.5f * dp);
        ringPaint.setColor(ringColor);
        corePaint.setColor(coreColor);
        dotPaint.setColor(dotColor);

        if (iconRes != 0) {
            Drawable d = AppCompatResources.getDrawable(context, iconRes);
            if (d != null) {
                icon = d.mutate();
                icon.setTint(iconTint);
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        animator = ValueAnimator.ofFloat(0f, 360f);
        animator.setDuration(12000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(a -> {
            angle = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
        super.onDetachedFromWindow();
    }

    // vai pausar a animacao quando o app vai para segundo plano.
    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (animator == null) return;
        if (visibility == VISIBLE) animator.resume(); else animator.pause();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float max = Math.min(cx, cy) - 2f * dp;

        float outer = max * 0.94f;
        float middle = max * 0.70f;
        float coreR = max * 0.45f;

        canvas.drawCircle(cx, cy, outer, ringPaint);
        canvas.drawCircle(cx, cy, middle, ringPaint);
        canvas.drawCircle(cx, cy, coreR, corePaint);

        if (icon != null) {
            int half = Math.round(coreR * 0.55f);
            icon.setBounds((int) cx - half, (int) cy - half, (int) cx + half, (int) cy + half);
            icon.draw(canvas);
        }

        // o satelite no anel de fora: gira no sentido horario, comeca a direita.
        double a1 = Math.toRadians(angle - 12);
        canvas.drawCircle(cx + (float) Math.cos(a1) * outer,
                cy + (float) Math.sin(a1) * outer, 5f * dp, dotPaint);

        // o segundo satelite no anel do meio: gira no sentido anti-horario, comeca em cima a esquerda.
        double a2 = Math.toRadians(-angle * 1.5 - 111);
        canvas.drawCircle(cx + (float) Math.cos(a2) * middle,
                cy + (float) Math.sin(a2) * middle, 5f * dp, dotPaint);
    }
}
