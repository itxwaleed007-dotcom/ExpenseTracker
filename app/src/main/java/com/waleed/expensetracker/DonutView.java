package com.waleed.expensetracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

/** Donut chart with two lines of text in the middle. */
public class DonutView extends View {
    private float[] vals = new float[0];
    private int[] cols = new int[0];
    private String top = "";
    private String bottom = "Total";
    private final Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tp = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bp = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF r = new RectF();

    public DonutView(Context c) {
        this(c, null);
    }

    public DonutView(Context c, AttributeSet a) {
        super(c, a);
        float d = getResources().getDisplayMetrics().density;
        ring.setStyle(Paint.Style.STROKE);
        ring.setStrokeWidth(24 * d);
        tp.setColor(0xFF111827);
        tp.setTextSize(18 * d);
        tp.setTypeface(Typeface.DEFAULT_BOLD);
        tp.setTextAlign(Paint.Align.CENTER);
        bp.setColor(0xFF6B7280);
        bp.setTextSize(11 * d);
        bp.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(float[] v, int[] c, String topText, String bottomText) {
        vals = v;
        cols = c;
        top = topText;
        bottom = bottomText;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas cv) {
        float d = getResources().getDisplayMetrics().density;
        float sw = 24 * d;
        float size = Math.min(getWidth(), getHeight());
        float cx = getWidth() / 2f, cy = getHeight() / 2f;
        r.set(cx - size / 2 + sw / 2, cy - size / 2 + sw / 2, cx + size / 2 - sw / 2, cy + size / 2 - sw / 2);

        float sum = 0;
        for (float f : vals) sum += f;
        if (sum <= 0) {
            ring.setColor(0xFFE5E7EB);
            cv.drawArc(r, 0, 360, false, ring);
        } else {
            float start = -90;
            float gap = vals.length > 1 ? 2f : 0f;
            for (int i = 0; i < vals.length; i++) {
                float sweep = vals[i] / sum * 360f;
                ring.setColor(cols[i]);
                cv.drawArc(r, start + gap / 2, Math.max(0.5f, sweep - gap), false, ring);
                start += sweep;
            }
        }
        cv.drawText(top, cx, cy + 2 * d, tp);
        cv.drawText(bottom, cx, cy + 18 * d, bp);
    }
}
