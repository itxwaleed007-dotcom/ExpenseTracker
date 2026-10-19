package com.waleed.expensetracker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

public class HomeFragment extends Fragment {
    private View root;
    private boolean hidden = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_home, container, false);

        setupAction(R.id.act_expense, R.drawable.ic_arrow_up, 0xFFEF4444, v -> openAdd(false));
        setupAction(R.id.act_income, R.drawable.ic_arrow_down, 0xFF16A34A, v -> openAdd(true));
        setupAction(R.id.act_reports, R.drawable.ic_chart, 0xFF3B82F6,
                v -> ((MainActivity) requireActivity()).select(2));
        setupAction(R.id.act_budget, R.drawable.ic_wallet, 0xFF8B5CF6,
                v -> startActivity(new Intent(requireContext(), BudgetActivity.class)));

        root.findViewById(R.id.tv_see_all).setOnClickListener(v -> ((MainActivity) requireActivity()).select(1));
        root.findViewById(R.id.iv_eye).setOnClickListener(v -> {
            hidden = !hidden;
            refresh();
        });
        root.findViewById(R.id.iv_bell).setOnClickListener(v ->
                Toast.makeText(requireContext(), "No new notifications", Toast.LENGTH_SHORT).show());
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void setupAction(int id, int icon, int color, View.OnClickListener l) {
        ViewGroup act = root.findViewById(id);
        ImageView iv = (ImageView) act.getChildAt(0);
        iv.setImageResource(icon);
        iv.setBackground(Ui.circle(color));
        act.setOnClickListener(l);
    }

    private void openAdd(boolean income) {
        Intent i = new Intent(requireContext(), AddTransactionActivity.class);
        i.putExtra(AddTransactionActivity.EXTRA_INCOME, income);
        startActivity(i);
    }

    private void refresh() {
        Context c = requireContext();
        DB db = DB.get(c);
        long now = System.currentTimeMillis();
        long ms = Fmt.startOfMonth(now, 0);
        long me = Fmt.startOfMonth(now, 1);

        double monthIncome = db.sum(true, ms, me);
        double monthExpense = db.sum(false, ms, me);
        double balance = db.sum(true, 0, Long.MAX_VALUE) - db.sum(false, 0, Long.MAX_VALUE);

        ((TextView) root.findViewById(R.id.tv_hello)).setText("Hello, " + Prefs.firstName(c) + " 👋");
        double budget = Prefs.budget(c);
        ((TextView) root.findViewById(R.id.tv_sub)).setText(
                budget > 0 && monthExpense > budget
                        ? "Careful! You're over your monthly budget."
                        : "Keep going! You're on track.");

        String masked = Prefs.symbol(c) + " ••••";
        ((TextView) root.findViewById(R.id.tv_balance)).setText(hidden ? masked : Fmt.money(c, balance));
        ((TextView) root.findViewById(R.id.tv_income)).setText(hidden ? masked : Fmt.money(c, monthIncome));
        ((TextView) root.findViewById(R.id.tv_expense)).setText(hidden ? masked : Fmt.money(c, monthExpense));

        LinearLayout list = root.findViewById(R.id.ll_recent);
        list.removeAllViews();
        List<Txn> recent = db.query(0, Long.MAX_VALUE, -1, 5);
        LayoutInflater inf = LayoutInflater.from(c);
        for (final Txn t : recent) {
            View row = inf.inflate(R.layout.item_transaction, list, false);
            TxnAdapter.bind(row, t, false);
            row.setOnClickListener(v -> {
                Intent i = new Intent(requireContext(), AddTransactionActivity.class);
                i.putExtra(AddTransactionActivity.EXTRA_ID, t.id);
                startActivity(i);
            });
            list.addView(row);
        }
        root.findViewById(R.id.tv_empty).setVisibility(recent.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
