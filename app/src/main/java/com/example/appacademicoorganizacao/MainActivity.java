package com.example.appacademicoorganizacao;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.nav_view);

        // Define quais telas são principais
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_tasks,
                R.id.navigation_calendar,
                R.id.navigation_foco
        ).build();

        NavController navController = Navigation.findNavController(
                this,
                R.id.nav_host_fragment_activity_main
        );
        NavigationUI.setupWithNavController(navView, navController);
    }
}