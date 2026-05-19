package com.example.appacademicoorganizacao;

import android.os.CountDownTimer;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FocusViewModel extends ViewModel {
    private CountDownTimer timer;
    private final MutableLiveData<Long> timeLeft = new MutableLiveData<>(1500000L);
    private final MutableLiveData<Boolean> isRunning = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isPomodoro = new MutableLiveData<>(true);
    private long totalTime = 1500000L;

    // os getters
    public LiveData<Long> getTimeLeft() {
        return timeLeft;
    }

    public LiveData<Boolean> getIsRunning() {
        return isRunning;
    }

    public LiveData<Boolean> getIsPomodoro() {
        return isPomodoro;
    }

    public long getTotalTime() {
        return totalTime;
    }

    // start

    public void startTimer() {

        if (timer != null) {
            timer.cancel();
        }

        Long currentTime = timeLeft.getValue();

        if (currentTime == null) return;

        timer = new CountDownTimer(currentTime, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

                timeLeft.setValue(millisUntilFinished);
            }

            @Override
            public void onFinish() {

                switchMode();
            }

        }.start();

        isRunning.setValue(true);
    }

    // o pause

    public void pauseTimer() {

        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        isRunning.setValue(false);
    }

    // o reset

    public void resetTimer() {

        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        timeLeft.setValue(1500000L);

        totalTime = 1500000L;

        isPomodoro.setValue(true);

        isRunning.setValue(false);
    }

    // a troca nos modos

    private void switchMode() {

        Boolean pomodoro = isPomodoro.getValue();

        if (pomodoro != null && pomodoro) {

            // pausa
            timeLeft.setValue(300000L);
            totalTime = 300000L;

            isPomodoro.setValue(false);

        } else {

            // pomodoro
            timeLeft.setValue(1500000L);
            totalTime = 1500000L;

            isPomodoro.setValue(true);
        }

        startTimer();
    }
}