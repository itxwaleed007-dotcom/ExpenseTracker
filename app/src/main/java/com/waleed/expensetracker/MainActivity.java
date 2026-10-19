package com.waleed.expensetracker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

/** Hosts the 4 tab fragments, the bottom bar and the side drawer. */
public class MainActivity extends AppCompatActivity {
    private static final int[] NAV_IDS = {R.id.nav_home, R.id.nav_txn, R.id.nav_reports, R.id.nav_profile};
    private static final int[] ICON_IDS = {R.id.ic_home, R.id.ic_txn, R.id.ic_reports, R.id.ic_profile};
    private static final int[] TEXT_IDS = {R.id.tv_home, R.id.tv_txn, R.id.tv_reports, R.id.tv_profile};
    // drawer rows: home, transactions, reports, budget, settings
    private static final int[] DRAWER_IDS = {R.id.dr_home, R.id.dr_txn, R.id.dr_reports, R.id.dr_budget, R.id.dr_settings};

    private DrawerLayout drawer;
    private int current = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawer = findViewById(R.id.drawer);

        for (int i = 0; i < NAV_IDS.length; i++) {
            final int idx = i;
            findViewById(NAV_IDS[i]).setOnClickListener(v -> select(idx));
        }
        findViewById(R.id.fab).setOnClickListener(v ->
                startActivity(new Intent(this, AddTransactionActivity.class)));

        findViewById(R.id.dr_home).setOnClickListener(v -> drawerGo(0));
        findViewById(R.id.dr_txn).setOnClickListener(v -> drawerGo(1));
        findViewById(R.id.dr_reports).setOnClickListener(v -> drawerGo(2));
        findViewById(R.id.dr_settings).setOnClickListener(v -> drawerGo(3));
        findViewById(R.id.dr_budget).setOnClickListener(v -> {
            drawer.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, BudgetActivity.class));
        });
        findViewById(R.id.dr_logout).setOnClickListener(v -> confirmLogout());

        if (savedInstanceState == null) {
            select(0);
        } else {
            current = savedInstanceState.getInt("current", 0);
            updateNav();
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else if (current != 0) {
                    select(0);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putInt("current", current);
    }

    private void drawerGo(int tab) {
        drawer.closeDrawer(GravityCompat.START);
        select(tab);
    }

    /** Switch to a tab: 0 Home, 1 Transactions, 2 Reports, 3 Profile. */
    void select(int idx) {
        if (idx == current) return;
        Fragment f;
        switch (idx) {
            case 1: f = new TransactionsFragment(); break;
            case 2: f = new ReportsFragment(); break;
            case 3: f = new ProfileFragment(); break;
            default: f = new HomeFragment(); break;
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.container, f).commit();
        current = idx;
        updateNav();
    }

    private void updateNav() {
        int green = getColor(R.color.green);
        int gray = getColor(R.color.text_sub);
        for (int i = 0; i < NAV_IDS.length; i++) {
            boolean on = i == current;
            ((ImageView) findViewById(ICON_IDS[i])).setColorFilter(on ? green : gray);
            TextView tv = findViewById(TEXT_IDS[i]);
            tv.setTextColor(on ? green : gray);
        }
        int activeDrawer = current == 3 ? 4 : current;
        for (int i = 0; i < DRAWER_IDS.length; i++) {
            View row = findViewById(DRAWER_IDS[i]);
            row.setBackgroundResource(i == activeDrawer ? R.drawable.bg_drawer_selected : 0);
        }
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("You will go back to the welcome screen. Your transactions stay on this device.")
                .setPositiveButton("Logout", (d, w) -> {
                    Prefs.setStarted(this, false);
                    Intent i = new Intent(this, SplashActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
