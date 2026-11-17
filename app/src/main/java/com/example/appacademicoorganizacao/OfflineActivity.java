package com.example.appacademicoorganizacao;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// Tela "Sem conexao" com o minijogo. Fecha sozinha quando a internet volta.
public class OfflineActivity extends AppCompatActivity {

    private JumpGameView game;
    private ConnectivityManager cm;
    private boolean callbackRegistered = false;

    private final ConnectivityManager.NetworkCallback callback =
            new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    checkBackOnline();
                }

                // a rede pode conectar antes de ser validada.
                @Override
                public void onCapabilitiesChanged(Network network, NetworkCapabilities caps) {
                    checkBackOnline();
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);
        SystemBars.apply(this, findViewById(R.id.offlineRoot),
                getResources().getBoolean(R.bool.auth_light_bars));

        game = findViewById(R.id.gameView);

        findViewById(R.id.btnRetry).setOnClickListener(v -> {
            if (NetworkUtils.isOnline(this)) {
                finish();
            } else {
                Toast.makeText(this, R.string.offline_still, Toast.LENGTH_SHORT).show();
            }
        });

        cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    }

    private void checkBackOnline() {
        runOnUiThread(() -> {
            if (!isFinishing() && NetworkUtils.isOnline(this)) finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (cm != null && !callbackRegistered) {
            try {
                cm.registerDefaultNetworkCallback(callback);
                callbackRegistered = true;
            } catch (RuntimeException ignored) {
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        game.resumeGame();
    }

    @Override
    protected void onPause() {
        game.pauseGame();
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (cm != null && callbackRegistered) {
            try {
                cm.unregisterNetworkCallback(callback);
            } catch (RuntimeException ignored) { }
            callbackRegistered = false;
        }
        super.onStop();
    }
}
