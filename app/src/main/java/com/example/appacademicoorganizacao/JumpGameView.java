package com.example.appacademicoorganizacao;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.Choreographer;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class JumpGameView extends View implements Choreographer.FrameCallback {

    private static final String PREFS = "game";
    private static final String KEY_BEST = "best";

    private static final float GRAVITY = 2800f;      // dp/s^2
    private static final float JUMP_VELOCITY = 720f; // dp/s
    private static final float START_SPEED = 220f;   // dp/s
    private static final float MAX_SPEED = 340f;
    private static final float SPEED_STEP = 8f;      // aumenta a cada ponto
    private static final float BALL_RADIUS = 12f;
    private static final float SPIKE_W = 18f;
    private static final float SPIKE_H = 30f;
    private static final float GROUND_FROM_BOTTOM = 28f;
    private enum State { READY, RUNNING, OVER }
    private static class Obstacle {
        float x;
        int count;     // espinhos lado a lado
        boolean scored;
    }

    private final float dp;
    private final Random random = new Random();
    private final List<Obstacle> obstacles = new ArrayList<>();
    private final Path spikePath = new Path();

    private final Paint ballPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint eyePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint spikePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint groundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private State state = State.READY;
    private boolean running = false;
    private long lastFrameNanos = 0;

    private float ballY;
    private float ballVy;
    private float speed;
    private float nextSpawnIn;
    private int score = 0;
    private int best = 0;
    private final int mutedColor;
    private final int inkColor;
    private String scoreText;
    private String bestText;

    public JumpGameView(Context context) { this(context, null); }

    public JumpGameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        dp = context.getResources().getDisplayMetrics().density;

        ballPaint.setColor(ContextCompat.getColor(context, R.color.auth_brand));
        eyePaint.setColor(ContextCompat.getColor(context, R.color.auth_white));
        spikePaint.setColor(ContextCompat.getColor(context, R.color.auth_ink));
        groundPaint.setColor(ContextCompat.getColor(context, R.color.auth_game_ground));
        groundPaint.setStrokeWidth(2f * dp);
        textPaint.setColor(ContextCompat.getColor(context, R.color.auth_ink));
        textPaint.setTextSize(13f * dp);
        mutedColor = ContextCompat.getColor(context, R.color.auth_muted);
        inkColor = ContextCompat.getColor(context, R.color.auth_ink);

        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        best = prefs.getInt(KEY_BEST, 0);
        resetGame();
    }

    public void resumeGame() {
        if (running) return;
        running = true;
        lastFrameNanos = 0;
        Choreographer.getInstance().postFrameCallback(this);
    }

    public void pauseGame() {
        running = false;
        Choreographer.getInstance().removeFrameCallback(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        resumeGame();
    }

    @Override
    protected void onDetachedFromWindow() {
        pauseGame();
        super.onDetachedFromWindow();
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        if (!running) return;
        if (lastFrameNanos != 0) {
            float dt = (frameTimeNanos - lastFrameNanos) / 1_000_000_000f;
            update(Math.min(dt, 0.05f)); // evita saltos grandes se o app engasgar
        }
        lastFrameNanos = frameTimeNanos;
        invalidate();
        Choreographer.getInstance().postFrameCallback(this);
    }

    private void resetGame() {
        obstacles.clear();
        ballY = 0;
        ballVy = 0;
        speed = START_SPEED * dp;
        score = 0;
        nextSpawnIn = 260f * dp;
        refreshTexts();
    }

    private void refreshTexts() {
        scoreText = getContext().getString(R.string.game_score, score);
        bestText = getContext().getString(R.string.game_best, best);
    }

    private void jump() {
        if (ballY <= 0.5f) {
            ballVy = JUMP_VELOCITY * dp;
        }
    }

    private void update(float dt) {
        if (state != State.RUNNING) return;

        // Fisica da bolinha
        ballVy -= GRAVITY * dp * dt;
        ballY += ballVy * dt;
        if (ballY <= 0f) {
            ballY = 0f;
            ballVy = 0f;
        }

        //  obstaculos
        float dx = speed * dt;
        for (Obstacle o : obstacles) o.x -= dx;

        // cria novos obstaculos
        nextSpawnIn -= dx;
        if (nextSpawnIn <= 0f) {
            Obstacle o = new Obstacle();
            o.count = 1 + random.nextInt(3);
            o.x = getWidth();
            obstacles.add(o);
            // Distancia minima garante que sempre da tempo de pousar e pular de novo.
            float minGap = 230f * dp + (speed - START_SPEED * dp) * 0.5f;
            nextSpawnIn = minGap + random.nextFloat() * 160f * dp + o.count * SPIKE_W * dp;
        }

        float ballX = getWidth() * 0.18f;
        float r = BALL_RADIUS * dp;
        float groundY = getHeight() - GROUND_FROM_BOTTOM * dp;
        float ballCenterY = groundY - r - ballY;

        Iterator<Obstacle> it = obstacles.iterator();
        while (it.hasNext()) {
            Obstacle o = it.next();
            float w = o.count * SPIKE_W * dp;

            // Colisao
            float sx0 = o.x + 4f * dp;
            float sx1 = o.x + w - 4f * dp;
            float bx0 = ballX - r + 3f * dp;
            float bx1 = ballX + r - 3f * dp;
            boolean overlapX = bx1 > sx0 && bx0 < sx1;
            boolean overlapY = ballCenterY + r - 3f * dp > groundY - SPIKE_H * dp * 0.75f;
            if (overlapX && overlapY) {
                gameOver();
                return;
            }

            // o espinho inteiro ficou para tras da bolinha
            if (!o.scored && o.x + w < ballX - r) {
                o.scored = true;
                score++;
                refreshTexts();
                speed = Math.min(MAX_SPEED * dp, speed + SPEED_STEP * dp);
            }

            if (o.x + w < -10f * dp) it.remove();
        }
    }

    private void gameOver() {
        state = State.OVER;
        if (score > best) {
            best = score;
            getContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .edit().putInt(KEY_BEST, best).apply();
        }
        refreshTexts();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        float groundY = h - GROUND_FROM_BOTTOM * dp;
        float r = BALL_RADIUS * dp;
        float ballX = w * 0.18f;

        // Chao
        canvas.drawLine(0, groundY, w, groundY, groundPaint);

        // Espinhos
        for (Obstacle o : obstacles) {
            for (int i = 0; i < o.count; i++) {
                float left = o.x + i * SPIKE_W * dp;
                spikePath.reset();
                spikePath.moveTo(left, groundY);
                spikePath.lineTo(left + SPIKE_W * dp / 2f, groundY - SPIKE_H * dp);
                spikePath.lineTo(left + SPIKE_W * dp, groundY);
                spikePath.close();
                canvas.drawPath(spikePath, spikePaint);
            }
        }

        // Bolinha
        float cy = groundY - r - ballY;
        canvas.drawCircle(ballX, cy, r, ballPaint);
        canvas.drawCircle(ballX + r * 0.35f, cy - r * 0.2f, r * 0.18f, eyePaint);

        // Placar (canto superior direito)
        textPaint.setTextAlign(Paint.Align.RIGHT);
        textPaint.setTextSize(12f * dp);
        textPaint.setColor(inkColor);
        canvas.drawText(scoreText, w - 4f * dp, 18f * dp, textPaint);
        textPaint.setColor(mutedColor);
        canvas.drawText(bestText, w - 4f * dp, 35f * dp, textPaint);
        textPaint.setColor(inkColor);

        // Mensagens
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(13f * dp);
        if (state == State.READY) {
            canvas.drawText(getContext().getString(R.string.game_tap_start),
                    w / 2f, h * 0.42f, textPaint);
        } else if (state == State.OVER) {
            canvas.drawText(getContext().getString(R.string.game_over),
                    w / 2f, h * 0.42f, textPaint);
        }
    }

    // ---------- toque ----------

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            switch (state) {
                case READY:
                    state = State.RUNNING;
                    jump();
                    break;
                case RUNNING:
                    jump();
                    break;
                case OVER:
                    resetGame();
                    state = State.RUNNING;
                    break;
            }
            performClick();

            getParent().requestDisallowInterceptTouchEvent(true);
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
