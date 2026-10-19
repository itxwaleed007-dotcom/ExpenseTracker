package com.waleed.expensetracker;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportsFragment extends Fragment {
    private View root;
    private int period = 1; // 0 week, 1 month, 2 year

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_reports, container, false);
        root.findViewById(R.id.seg_week).setOnClickListener(v -> setPeriod(0));
        root.findViewById(R.id.seg_month).setOnClickListener(v -> setPeriod(1));
        root.findViewById(R.id.seg_year).setOnClickListener(v -> setPeriod(2));
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void setPeriod(int p) {
        period = p;
        refresh();
    }

    private void styleSeg(int id, boolean on) {
        TextView tv = root.findViewById(id);
        tv.setBackgroundResource(on ? R.drawable.bg_pill_selected : 0);
        tv.setTextColor(requireContext().getColor(on ? R.color.white : R.color.text_sub));
    }

    private void refresh() {
        Context c = requireContext();
        DB db = DB.get(c);
        styleSeg(R.id.seg_week, period == 0);
        styleSeg(R.id.seg_month, period == 1);
        styleSeg(R.id.seg_year, period == 2);

        long now = System.currentTimeMillis();
        long from, to, pFrom, pTo;
        String vs;
        if (period == 0) {
            to = Fmt.addDays(Fmt.startOfDay(now), 1);
            from = Fmt.addDays(to, -7);
            pTo = from;
            pFrom = Fmt.addDays(from, -7);
            vs = "vs previous 7 days";
        } else if (period == 1) {
            from = Fmt.startOfMonth(now, 0);
            to = Fmt.startOfMonth(now, 1);
            pFrom = Fmt.startOfMonth(now, -1);
            pTo = from;
            vs = "vs last month";
        } else {
            from = Fmt.startOfYear(now, 0);
            to = Fmt.startOfYear(now, 1);
            pFrom = Fmt.startOfYear(now, -1);
            pTo = from;
            vs = "vs last year";
        }

        double total = db.sum(false, from, to);
        double prev = db.sum(false, pFrom, pTo);
        ((TextView) root.findViewById(R.id.tv_total)).setText(Fmt.money(c, total));

        TextView change = root.findViewById(R.id.tv_change);
        TextView vsTv = root.findViewById(R.id.tv_vs);
        if (prev > 0) {
            double pct = (total - prev) / prev * 100.0;
            change.setVisibility(View.VISIBLE);
            change.setText((pct <= 0 ? "↓ " : "↑ ") + Math.round(Math.abs(pct)) + "%");
            change.setTextColor(c.getColor(pct <= 0 ? R.color.income : R.color.red));
            vsTv.setText(vs);
        } else {
            change.setVisibility(View.GONE);
            vsTv.setText("No earlier data to compare");
        }

        buildBars(db, from, to);
        buildDonut(c, db, from, to, total);
    }

    private void buildBars(DB db, long from, long to) {
        List<Txn> list = db.query(from, to, 0, 0);
        float[] v;
        String[] l;
        Calendar it = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();

        if (period == 0) {
            int n = 7;
            v = new float[n];
            l = new String[n];
            SimpleDateFormat f = new SimpleDateFormat("EEE", Locale.US);
            for (int i = 0; i < n; i++) {
                it.setTimeInMillis(from);
                it.add(Calendar.DAY_OF_YEAR, i);
                l[i] = f.format(it.getTime());
            }
            for (Txn t : list) {
                int idx = (int) Math.round((Fmt.startOfDay(t.date) - from) / (double) Fmt.DAY);
                if (idx >= 0 && idx < n) v[idx] += (float) t.amount;
            }
        } else if (period == 1) {
            it.setTimeInMillis(from);
            int n = it.getActualMaximum(Calendar.DAY_OF_MONTH);
            v = new float[n];
            l = new String[n];
            SimpleDateFormat f = new SimpleDateFormat("MMM d", Locale.US);
            for (int i = 0; i < n; i++) {
                if (i % 7 == 0) {
                    it.setTimeInMillis(from);
                    it.add(Calendar.DAY_OF_MONTH, i);
                    l[i] = f.format(it.getTime());
                } else {
                    l[i] = "";
                }
            }
            for (Txn t : list) {
                cal.setTimeInMillis(t.date);
                int idx = cal.get(Calendar.DAY_OF_MONTH) - 1;
                if (idx >= 0 && idx < n) v[idx] += (float) t.amount;
            }
        } else {
            int n = 12;
            v = new float[n];
            l = new DateFormatSymbols(Locale.US).getShortMonths();
            for (Txn t : list) {
                cal.setTimeInMillis(t.date);
                int idx = cal.get(Calendar.MONTH);
                if (idx >= 0 && idx < n) v[idx] += (float) t.amount;
            }
        }
        ((BarChartView) root.findViewById(R.id.bar_chart)).setData(v, l);
    }

    private void buildDonut(Context c, DB db, long from, long to, double total) {
        Map<String, Double> byCat = db.expenseByCat(from, to);
        LinearLayout legend = root.findViewById(R.id.ll_legend);
        legend.removeAllViews();
        DonutView donut = root.findViewById(R.id.donut);
        View noData = root.findViewById(R.id.tv_nodata);

        if (byCat.isEmpty() || total <= 0) {
            donut.setData(new float[0], new int[0], Fmt.moneyShort(c, 0), "Total");
            noData.setVisibility(View.VISIBLE);
            return;
        }
        noData.setVisibility(View.GONE);

        List<Float> vals = new ArrayList<>();
        List<Integer> cols = new ArrayList<>();
        List<String> names = new ArrayList<>();
        double others = 0;
        int i = 0;
        for (Map.Entry<String, Double> e : byCat.entrySet()) {
            if (i < 4) {
                Cat cat = Cat.get(e.getKey());
                vals.add(e.getValue().floatValue());
                cols.add(cat.color);
                names.add(cat.name);
            } else {
                others += e.getValue();
            }
            i++;
        }
        if (others > 0) {
            vals.add((float) others);
            cols.add(0xFF9CA3AF);
            names.add("Others");
        }

        float[] fv = new float[vals.size()];
        int[] fc = new int[vals.size()];
        LayoutInflater inf = LayoutInflater.from(c);
        for (int k = 0; k < fv.length; k++) {
            fv[k] = vals.get(k);
            fc[k] = cols.get(k);
            View row = inf.inflate(R.layout.item_legend, legend, false);
            row.findViewById(R.id.dot).setBackground(Ui.circle(fc[k]));
            ((TextView) row.findViewById(R.id.tv_name)).setText(names.get(k));
            ((TextView) row.findViewById(R.id.tv_pct)).setText(Math.round(fv[k] / total * 100) + "%");
            legend.addView(row);
        }
        donut.setData(fv, fc, Fmt.moneyShort(c, total), "Total");
    }
}
