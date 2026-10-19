package com.waleed.expensetracker;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Prefs.started(this)) {
            openMain();
            return;
        }
        setContentView(R.layout.activity_splash);
        findViewById(R.id.btn_start).setOnClickListener(v -> {
            Prefs.setStarted(this, true);
            openMain();
        });
    }

    private void openMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
