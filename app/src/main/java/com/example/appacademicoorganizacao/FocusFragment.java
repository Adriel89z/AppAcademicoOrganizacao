package com.example.appacademicoorganizacao;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import java.util.Locale;

public class FocusFragment extends Fragment {

    private TextView textTimer, textMode;
    private Button btnStartPause, btnReset;

    private CountDownTimer timer;
    private boolean isRunning = false;

    private long timeLeft = 1500000; // 25 min
    private boolean isPomodoro = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_focus, container, false);

        textTimer = view.findViewById(R.id.textTimer);
        textMode = view.findViewById(R.id.textMode);
        btnStartPause = view.findViewById(R.id.btnStartPause);
        btnReset = view.findViewById(R.id.btnReset);

        updateTimerText();

        btnStartPause.setOnClickListener(v -> {
            if (isRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        btnReset.setOnClickListener(v -> resetTimer());

        return view;
    }

    private void startTimer() {
        timer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = millisUntilFinished;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                switchMode();
            }
        }.start();

        isRunning = true;
        btnStartPause.setText("Pausar");
    }

    private void pauseTimer() {
        timer.cancel();
        isRunning = false;
        btnStartPause.setText("Iniciar");
    }

    private void resetTimer() {
        if (timer != null) timer.cancel();

        isPomodoro = true;
        timeLeft = 1500000;
        textMode.setText("Pomodoro");

        isRunning = false;
        btnStartPause.setText("Iniciar");

        updateTimerText();
    }

    private void switchMode() {
        if (isPomodoro) {
            timeLeft = 300000; // 5 min pausa
            textMode.setText("Pausa");
        } else {
            timeLeft = 1500000; // 25 min
            textMode.setText("Pomodoro");
        }

        isPomodoro = !isPomodoro;
        startTimer();
    }

    private void updateTimerText() {
        int minutes = (int) (timeLeft / 1000) / 60;
        int seconds = (int) (timeLeft / 1000) % 60;

        String time = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        textTimer.setText(time);
    }
}