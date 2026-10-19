package com.waleed.expensetracker;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;

import java.util.Locale;

/** UI helpers shared by all screens. */
final class Ui {
    private Ui() {}

    interface NumberCallback {
        void onNumber(double value);
    }

    static int dp(Context c, float v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    static GradientDrawable round(Context c, int color, float radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(c, radiusDp));
        return g;
    }

    static GradientDrawable circle(int color) {
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(color);
        return g;
    }

    static int alpha(int color, int a) {
        return (color & 0x00FFFFFF) | (a << 24);
    }

    /** Category icon on a soft tinted rounded square. */
    static void catIcon(ImageView iv, Cat c) {
        iv.setImageResource(c.icon);
        iv.setColorFilter(c.color);
        iv.setBackground(round(iv.getContext(), alpha(c.color, 0x26), 14));
    }

    /** Simple dialog asking for a number (0 or empty means "clear"). */
    static void askNumber(Context c, String title, double initial, NumberCallback cb) {
        final EditText e = new EditText(c);
        e.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        e.setHint("0.00");
        if (initial > 0) e.setText(String.format(Locale.US, "%.2f", initial));
        e.setSelectAllOnFocus(true);
        FrameLayout box = new FrameLayout(c);
        int p = dp(c, 20);
        box.setPadding(p, p / 2, p, 0);
        box.addView(e);
        new AlertDialog.Builder(c)
                .setTitle(title)
                .setView(box)
                .setPositiveButton("Save", (d, w) -> {
                    double v = 0;
                    try {
                        v = Double.parseDouble(e.getText().toString().trim());
                    } catch (Exception ignored) {
                    }
                    cb.onNumber(Math.max(0, v));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
