package com.example.appacademicoorganizacao;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

// tela de manutenção
public class ServerDownActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_down);
        SystemBars.apply(this, findViewById(R.id.downRoot), false);
    }
}
