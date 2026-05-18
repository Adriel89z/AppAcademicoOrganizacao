package com.example.appacademicoorganizacao;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.Locale;

public class FocusFragment extends Fragment {

    private TextView textTimer, textMode;
    private ImageButton btnStartPause;
    private ImageButton btnReset;
    private ImageView topSand;
    private ImageView bottomSand;

    private CountDownTimer timer;

    private boolean isRunning = false;
    private boolean isPomodoro = true;

    // 25 minutos
    private long timeLeft = 1500000;
    private long totalTime = 1500000;

    private long endTime;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_focus, container, false);

        // texto
        textTimer = view.findViewById(R.id.textTimer);
        textMode = view.findViewById(R.id.textMode);

        // os botões
        btnStartPause = view.findViewById(R.id.btnStartPause);
        btnReset = view.findViewById(R.id.btnReset);

        // a areia
        topSand = view.findViewById(R.id.topSand);
        bottomSand = view.findViewById(R.id.bottomSand);


        // glow da ampulheta + animação
        ImageView glow = view.findViewById(R.id.hourglass_glow);

        ObjectAnimator glowAnimator =
                ObjectAnimator.ofFloat(glow, "alpha", 0.55f, 1f);

        glowAnimator.setDuration(1800);
        glowAnimator.setRepeatMode(ValueAnimator.REVERSE);
        glowAnimator.setRepeatCount(ValueAnimator.INFINITE);
        glowAnimator.start();

        updateTimerText();
        updateSandAnimation();


        //botão de play e pause

        btnStartPause.setOnClickListener(v -> {

            animateButton(btnStartPause);

            if (isRunning) {
                pauseTimer();
            } else {
                startTimer();
            }

        });

        //botão reset

        btnReset.setOnClickListener(v -> {

            animateButton(btnReset);

            resetTimer();
        });

        return view;
    }

    //animação dos botoes
    private void animateButton(View button) {

        button.animate()
                .scaleX(0.90f)
                .scaleY(0.90f)
                .setDuration(80)
                .withEndAction(() ->

                        button.animate().scaleX(1f).scaleY(1f).setDuration(80).start()

                ).start();
    }

     //inicia o timer
    private void startTimer() {

        endTime = System.currentTimeMillis() + timeLeft;

        btnStartPause.setImageResource(R.drawable.icon_pause44);

        timer = new CountDownTimer(timeLeft, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

                timeLeft = millisUntilFinished;

                updateTimerText();

                updateSandAnimation();

            }

            @Override
            public void onFinish() {

                MediaPlayer mediaPlayer =
                        MediaPlayer.create(getContext(), R.raw.notification);

                mediaPlayer.start();

                switchMode();

            }

        }.start();

        isRunning = true;
    }

    //pausa o timer

    private void pauseTimer() {

        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        btnStartPause.setImageResource(R.drawable.icon_play44);

        isRunning = false;
    }

    //recomeça o timer

    private void resetTimer() {

        if (timer != null) {
            timer.cancel();
        }

        isPomodoro = true;

        timeLeft = 1500000;
        totalTime = 1500000;

        isRunning = false;
        btnStartPause.setImageResource(R.drawable.icon_play44);

        updateUI();
        updateTimerText();
        updateSandAnimation();
    }

    //modo de pausa

    private void switchMode() {

        if (isPomodoro) {

            // pausa = 5 min
            timeLeft = 300000;
            totalTime = 300000;

            isPomodoro = false;

        } else {

            // pomodoro = 25 min
            timeLeft = 1500000;
            totalTime = 1500000;

            isPomodoro = true;
        }

        updateUI();
        updateTimerText();
        updateSandAnimation();
        startTimer();
    }

    //numeros do timer

    private void updateTimerText() {

        int minutes = (int) (timeLeft / 1000) / 60;

        int seconds = (int) (timeLeft / 1000) % 60;

        String time = String.format(
                Locale.getDefault(),
                "%02d:%02d",
                minutes,
                seconds
        );

        textTimer.setText(time);
    }

    //tentativa de animar a areia

    private void updateSandAnimation() {

        float progress = (float) timeLeft / totalTime;

        // começo rápido e final lento
        float visualProgress = (float) Math.pow(progress, 0.5f);

        // areia de cima
        topSand.post(() -> {

            int height = topSand.getHeight();

            int visibleHeight = (int) (height * visualProgress);

            topSand.setClipBounds(
                    new android.graphics.Rect(
                            0,
                            height - visibleHeight,
                            topSand.getWidth(),
                            height
                    )
            );
        });

        // areia de baixo
        bottomSand.post(() -> {

            int height = bottomSand.getHeight();

            int visibleHeight = (int) (height * (1f - visualProgress));

            bottomSand.setClipBounds(
                    new android.graphics.Rect(
                            0,
                            height - visibleHeight,
                            bottomSand.getWidth(),
                            height
                    )
            );
        });
    }

    //muda o icone

    private void updateUI() {

        if (isPomodoro) {

            textMode.setText("Pomodoro");

        } else {

            textMode.setText("Pausa");
        }
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

}