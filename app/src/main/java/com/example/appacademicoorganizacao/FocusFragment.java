package com.example.appacademicoorganizacao;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import java.util.Locale;

public class FocusFragment extends Fragment {

    private TextView textTimer, textMode;
    private ImageButton btnStartPause, btnReset;
    private CountDownTimer timer;
    private boolean isRunning = false;
    private long timeLeft = 1500000; // 25 min
    private boolean isPomodoro = true;
    private long endTime;
    private CircularProgressView progressCircle;
    private long totalTime = 1500000;
    private ValueAnimator progressAnimator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_focus, container, false);
        progressCircle = view.findViewById(R.id.progressCircle);

        textTimer = view.findViewById(R.id.textTimer);
        textMode = view.findViewById(R.id.textMode);
        btnStartPause = view.findViewById(R.id.btnStartPause);
        btnReset = view.findViewById(R.id.btnReset);

        updateTimerText();

        btnStartPause.setOnClickListener(v -> {
            if (isRunning) {
                pauseTimer();
            } else {
                if (timer != null) {
                    timer.cancel();
                }
                startTimer();
            }
        });

        btnReset.setOnClickListener(v -> resetTimer());

        return view;
    }

    private void startTimer() {
        animateProgress(timeLeft);
        endTime = System.currentTimeMillis() + timeLeft;
        btnStartPause.setImageResource(R.drawable.icon_pause44);
        timer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = millisUntilFinished;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                MediaPlayer mediaPlayer = MediaPlayer.create(getContext(), R.raw.notification);
                mediaPlayer.start();
                
                switchMode();
            }
        }.start();

        isRunning = true;
    }

    private void pauseTimer() {
        if (timer != null) timer.cancel();

        if (progressAnimator != null) {
            progressAnimator.cancel();
        }

        btnStartPause.setImageResource(R.drawable.icon_play44);
        isRunning = false;
    }

    private void resetTimer() {
        if (progressAnimator != null) {
            progressAnimator.cancel();
        }
        isPomodoro = true;
        timeLeft = 1500000;
        isRunning = false;

        updateUI();
        updateTimerText();
    }

    private void switchMode() {
        if (isPomodoro) {
            timeLeft = 1500000;
            totalTime = 1500000;
        } else {
            timeLeft = 300000;
            totalTime = 300000;
        }
        updateUI();
        startTimer();
    }

    private void updateTimerText() {
        int minutes = (int) (timeLeft / 1000) / 60;
        int seconds = (int) (timeLeft / 1000) % 60;

        String time = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        textTimer.setText(time);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isRunning) {
            timeLeft = endTime - System.currentTimeMillis();

            if (timeLeft <= 0) {
                switchMode();
            } else {
                startTimer();
            }
        }
    }

    private void updateUI() {
        if (isPomodoro) {
            textMode.setText("Pomodoro");
            View view = getView();
            if (view != null) {
                view.setBackgroundColor(Color.parseColor("#0F172A"));
            }
        } else {
            textMode.setText("Pausa");
            View view = getView();
            if (view != null) {
                view.setBackgroundColor(Color.parseColor("#1E293B"));
            }
        }
    }
    private void animateProgress(long duration) {

        if (progressAnimator != null) {
            progressAnimator.cancel();
        }

        float start = (float) timeLeft / totalTime;

        progressAnimator = ValueAnimator.ofFloat(start, 0f);
        progressAnimator.setDuration(duration);
        progressAnimator.setInterpolator(new android.view.animation.LinearInterpolator());

        progressAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            progressCircle.setProgress(value);
        });

        progressAnimator.start();
    }
}