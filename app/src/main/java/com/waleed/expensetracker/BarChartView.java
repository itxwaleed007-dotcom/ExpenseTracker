package com.waleed.expensetracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.Locale;

/** Simple bar chart with a y-axis grid and optional x labels (empty label = skipped). */
public class BarChartView extends View {
    private float[] vals = new float[0];
    private String[] labels = new String[0];
    private final Paint bar = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint grid = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint txt = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rf = new RectF();

    public BarChartView(Context c) {
        this(c, null);
    }

    public BarChartView(Context c, AttributeSet a) {
        super(c, a);
        float d = getResources().getDisplayMetrics().density;
        grid.setColor(0xFFEEF0EE);
        grid.setStrokeWidth(d);
        txt.setColor(0xFF6B7280);
        txt.setTextSize(10 * d);
    }

    public void setData(float[] v, String[] l) {
        vals = v;
        labels = l;
        invalidate();
    }

    private static float niceStep(float x) {
        if (x <= 0) return 25f;
        double exp = Math.floor(Math.log10(x));
        double f = x / Math.pow(10, exp);
        double nf = f <= 1 ? 1 : f <= 2 ? 2 : f <= 2.5 ? 2.5 : f <= 5 ? 5 : 10;
        return (float) (nf * Math.pow(10, exp));
    }

    private static String num(float v) {
        if (v == Math.floor(v)) return String.valueOf((int) v);
        return String.format(Locale.US, "%.1f", v);
    }

    private static String axis(float v) {
        return v >= 1000 ? num(v / 1000f) + "k" : num(v);
    }

    @Override
    protected void onDraw(Canvas cv) {
        float d = getResources().getDisplayMetrics().density;
        float left = 38 * d, bottom = 24 * d, top = 8 * d, right = 4 * d;
        float w = getWidth() - left - right;
        float h = getHeight() - top - bottom;

        float max = 0, sum = 0;
        for (float f : vals) {
            max = Math.max(max, f);
            sum += f;
        }
        float avg = vals.length == 0 ? 0 : sum / vals.length;
        float step = niceStep(max / 4f);
        float axisMax = step * 4;

        txt.setTextAlign(Paint.Align.RIGHT);
        for (int i = 0; i <= 4; i++) {
            float y = top + h - (h * i / 4f);
            cv.drawLine(left, y, left + w, y, grid);
            cv.drawText(axis(step * i), left - 6 * d, y + 3.5f * d, txt);
        }

        int n = vals.length;
        if (n == 0) return;
        float slot = w / n;
        float bw = Math.min(slot * 0.6f, 22 * d);
        float rad = Math.min(bw / 2, 4 * d);
        txt.setTextAlign(Paint.Align.CENTER);
        for (int i = 0; i < n; i++) {
            float x = left + slot * i + slot / 2;
            float bh = h * vals[i] / axisMax;
            if (vals[i] > 0 && bh < 2 * d) bh = 2 * d;
            bar.setColor(vals[i] > 0 && vals[i] > avg ? 0xFF0B7A55 : 0xFF8EDDB5);
            rf.set(x - bw / 2, top + h - bh, x + bw / 2, top + h);
            cv.drawRoundRect(rf, rad, rad, bar);
            if (i < labels.length && labels[i] != null && labels[i].length() > 0) {
                cv.drawText(labels[i], x, getHeight() - 6 * d, txt);
            }
        }
    }
}
