package com.waleed.expensetracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Map;

/** Monthly budget + per-category limits. */
public class BudgetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
        findViewById(R.id.iv_edit_budget).setOnClickListener(v ->
                Ui.askNumber(this, "Monthly budget", Prefs.budget(this), value -> {
                    Prefs.setBudget(this, value);
                    refresh();
                }));
        findViewById(R.id.tv_edit).setOnClickListener(v -> chooseCategory());
        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void setBar(View track, View fill, double pct, int color) {
        track.setBackground(Ui.round(this, getColor(R.color.line), 4));
        fill.setBackground(Ui.round(this, color, 4));
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) fill.getLayoutParams();
        lp.weight = (float) Math.max(0, Math.min(100, pct));
        fill.setLayoutParams(lp);
    }

    private void refresh() {
        long now = System.currentTimeMillis();
        long from = Fmt.startOfMonth(now, 0);
        long to = Fmt.startOfMonth(now, 1);
        DB db = DB.get(this);
        double spent = db.sum(false, from, to);
        double budget = Prefs.budget(this);

        TextView tvBudget = findViewById(R.id.tv_budget);
        TextView tvPct = findViewById(R.id.tv_pct);
        TextView tvSpent = findViewById(R.id.tv_spent);
        TextView tvLeft = findViewById(R.id.tv_left);
        int green = getColor(R.color.green);
        int red = getColor(R.color.red);

        if (budget > 0) {
            double pct = spent / budget * 100;
            boolean over = spent > budget;
            tvBudget.setText(Fmt.money(this, budget));
            tvPct.setText(Math.round(pct) + "%");
            tvPct.setTextColor(over ? red : green);
            setBar(findViewById(R.id.bar_track), findViewById(R.id.bar_fill), pct, over ? red : green);
            tvSpent.setText(Fmt.money(this, spent) + " spent");
            tvSpent.setTextColor(over ? red : green);
            tvLeft.setText(over ? Fmt.money(this, spent - budget) + " over"
                    : Fmt.money(this, budget - spent) + " left");
            tvLeft.setTextColor(over ? red : green);
        } else {
            tvBudget.setText("Not set");
            tvPct.setText("0%");
            tvPct.setTextColor(green);
            setBar(findViewById(R.id.bar_track), findViewById(R.id.bar_fill), 0, green);
            tvSpent.setText(Fmt.money(this, spent) + " spent");
            tvSpent.setTextColor(getColor(R.color.text_sub));
            tvLeft.setText("Tap the pencil to set a budget");
            tvLeft.setTextColor(getColor(R.color.text_sub));
        }

        buildLimits(db.expenseByCat(from, to));
    }

    private void buildLimits(Map<String, Double> byCat) {
        LinearLayout list = findViewById(R.id.ll_limits);
        list.removeAllViews();
        LayoutInflater inf = LayoutInflater.from(this);
        for (final Cat cat : Cat.list(false)) {
            View row = inf.inflate(R.layout.item_limit, list, false);
            Ui.catIcon((ImageView) row.findViewById(R.id.iv_icon), cat);
            ((TextView) row.findViewById(R.id.tv_name)).setText(cat.name);

            Double s = byCat.get(cat.id);
            double spent = s == null ? 0 : s;
            double limit = Prefs.limit(this, cat.id);
            TextView tvLimit = row.findViewById(R.id.tv_limit);
            TextView tvDetail = row.findViewById(R.id.tv_detail);
            View track = row.findViewById(R.id.bar_track);
            View fill = row.findViewById(R.id.bar_fill);

            if (limit > 0) {
                boolean over = spent > limit;
                tvLimit.setText(Fmt.money(this, limit));
                tvDetail.setText(Fmt.money(this, spent) + " / " + Fmt.money(this, limit));
                setBar(track, fill, spent / limit * 100, over ? getColor(R.color.red) : cat.color);
            } else {
                tvLimit.setText("No limit");
                tvLimit.setTextColor(getColor(R.color.text_sub));
                tvDetail.setText(Fmt.money(this, spent) + " spent");
                setBar(track, fill, 0, cat.color);
            }
            row.setOnClickListener(v -> editLimit(cat));
            list.addView(row);
        }
    }

    private void editLimit(final Cat cat) {
        Ui.askNumber(this, cat.name + " limit", Prefs.limit(this, cat.id), value -> {
            Prefs.setLimit(this, cat.id, value);
            refresh();
        });
    }

    private void chooseCategory() {
        final List<Cat> cats = Cat.list(false);
        String[] names = new String[cats.size()];
        for (int i = 0; i < names.length; i++) names[i] = cats.get(i).name;
        new AlertDialog.Builder(this)
                .setTitle("Edit category limit")
                .setItems(names, (d, which) -> editLimit(cats.get(which)))
                .show();
    }
}
