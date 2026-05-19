package com.example.appacademicoorganizacao;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.Locale;

public class FocusFragment extends Fragment {

    private TextView textTimer, textMode;
    private ImageButton btnStartPause;
    private ImageButton btnReset;
    private ImageView topSand;
    private ImageView bottomSand;

    private FocusViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_focus,
                container,
                false
        );

        viewModel = new ViewModelProvider(requireActivity())
                .get(FocusViewModel.class);

        // texto

        textTimer = view.findViewById(R.id.textTimer);
        textMode = view.findViewById(R.id.textMode);

        // botões

        btnStartPause = view.findViewById(R.id.btnStartPause);
        btnReset = view.findViewById(R.id.btnReset);

        // areia

        topSand = view.findViewById(R.id.topSand);
        bottomSand = view.findViewById(R.id.bottomSand);

        // observa o tempo

        viewModel.getTimeLeft().observe(
                getViewLifecycleOwner(),
                time -> {

                    updateTimerText(time);

                    updateSandAnimation(time);
                }
        );

        // os modos

        viewModel.getIsRunning().observe(getViewLifecycleOwner(), running -> {

                    if (running != null && running) {

                        btnStartPause.setImageResource(
                                R.drawable.icon_pause44
                        );

                    } else {

                        btnStartPause.setImageResource(
                                R.drawable.icon_play44
                        );
                    }
                }
        );

        // glow

        ImageView glow = view.findViewById(R.id.hourglass_glow);

        ObjectAnimator glowAnimator =
                ObjectAnimator.ofFloat(
                        glow,
                        "alpha",
                        0.55f,
                        1f
                );

        glowAnimator.setDuration(1800);
        glowAnimator.setRepeatMode(ValueAnimator.REVERSE);
        glowAnimator.setRepeatCount(ValueAnimator.INFINITE);
        glowAnimator.start();

        // o play eo pause

        btnStartPause.setOnClickListener(v -> {

            animateButton(btnStartPause);

            Boolean running =
                    viewModel.getIsRunning().getValue();

            if (running != null && running) {

                viewModel.pauseTimer();

            } else {

                viewModel.startTimer();

            }
        });

        // reset
        btnReset.setOnClickListener(v -> {

            animateButton(btnReset);

            viewModel.resetTimer();

        });

        return view;
    }

    // animação botão
    private void animateButton(View button) {

        button.animate()
                .scaleX(0.90f)
                .scaleY(0.90f)
                .setDuration(80)
                .withEndAction(() ->

                        button.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(80)
                                .start()

                ).start();
    }

    // timer texto
    private void updateTimerText(long timeLeft) {

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

    // animação areia
    private void updateSandAnimation(long timeLeft) {

        float progress =
                (float) timeLeft / viewModel.getTotalTime();

        float visualProgress =
                (float) Math.pow(progress, 0.5f);

        // areia cima
        topSand.post(() -> {

            int height = topSand.getHeight();

            int visibleHeight =
                    (int) (height * visualProgress);

            topSand.setClipBounds(
                    new android.graphics.Rect(
                            0,
                            height - visibleHeight,
                            topSand.getWidth(),
                            height
                    )
            );
        });

        // areia baixo
        bottomSand.post(() -> {

            int height = bottomSand.getHeight();

            int visibleHeight =
                    (int) (height * (1f - visualProgress));

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
}