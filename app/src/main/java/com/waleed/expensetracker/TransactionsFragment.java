package com.waleed.expensetracker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TransactionsFragment extends Fragment {
    private static final String[] RANGES = {"All time", "This month", "Last 7 days"};

    private View root;
    private TxnAdapter adapter;
    private int type = -1;   // -1 all, 0 expense, 1 income
    private int range = 0;   // index in RANGES

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_transactions, container, false);

        adapter = new TxnAdapter(new TxnAdapter.Listener() {
            @Override
            public void onClick(Txn t) {
                Intent i = new Intent(requireContext(), AddTransactionActivity.class);
                i.putExtra(AddTransactionActivity.EXTRA_ID, t.id);
                startActivity(i);
            }

            @Override
            public void onLongClick(Txn t) {
                confirmDelete(t);
            }
        });
        RecyclerView rv = root.findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        root.findViewById(R.id.chip_all).setOnClickListener(v -> setType(-1));
        root.findViewById(R.id.chip_expense).setOnClickListener(v -> setType(0));
        root.findViewById(R.id.chip_income).setOnClickListener(v -> setType(1));
        root.findViewById(R.id.iv_filter).setOnClickListener(v -> pickRange());
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        load();
    }

    private void setType(int t) {
        type = t;
        load();
    }

    private void pickRange() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Show transactions from")
                .setSingleChoiceItems(RANGES, range, (d, which) -> {
                    range = which;
                    d.dismiss();
                    load();
                })
                .show();
    }

    private void styleChip(int id, boolean on) {
        TextView tv = root.findViewById(id);
        tv.setBackgroundResource(on ? R.drawable.bg_pill_selected : R.drawable.bg_pill);
        tv.setTextColor(requireContext().getColor(on ? R.color.white : R.color.text_sub));
    }

    private void load() {
        Context c = requireContext();
        styleChip(R.id.chip_all, type == -1);
        styleChip(R.id.chip_expense, type == 0);
        styleChip(R.id.chip_income, type == 1);

        long now = System.currentTimeMillis();
        long from = 0, to = Long.MAX_VALUE;
        if (range == 1) {
            from = Fmt.startOfMonth(now, 0);
            to = Fmt.startOfMonth(now, 1);
        } else if (range == 2) {
            from = Fmt.addDays(Fmt.startOfDay(now), -6);
        }

        List<Txn> list = DB.get(c).query(from, to, type, 0);
        List<Object> out = new ArrayList<>();
        String last = null;
        for (Txn t : list) {
            String h = Fmt.dayHeader(t.date);
            if (!h.equals(last)) {
                out.add(h);
                last = h;
            }
            out.add(t);
        }
        adapter.set(out);
        root.findViewById(R.id.tv_empty).setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void confirmDelete(final Txn t) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete transaction?")
                .setMessage("This cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> {
                    DB.get(requireContext()).delete(t.id);
                    load();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
