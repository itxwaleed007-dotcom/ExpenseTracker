package com.waleed.expensetracker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {
    private View root;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_profile, container, false);
        ImageView avatar = root.findViewById(R.id.iv_avatar);
        avatar.setBackground(Ui.circle(getResources().getColor(R.color.green, null)));
        root.findViewById(R.id.iv_edit).setOnClickListener(v -> editProfile());
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        final Context c = requireContext();
        ((TextView) root.findViewById(R.id.tv_name)).setText(Prefs.name(c));
        ((TextView) root.findViewById(R.id.tv_email)).setText(Prefs.email(c));

        LinearLayout rows = root.findViewById(R.id.ll_rows);
        rows.removeAllViews();
        addRow(rows, R.drawable.ic_person, "Personal Information", null, v -> editProfile(), true);
        addRow(rows, R.drawable.ic_wallet, "Budget Settings", null,
                v -> startActivity(new Intent(c, BudgetActivity.class)), true);
        addRow(rows, R.drawable.ic_bell, "Notifications", Prefs.notif(c) ? "On" : "Off", v -> {
            boolean now = !Prefs.notif(c);
            Prefs.setNotif(c, now);
            Toast.makeText(c, now ? "Budget alerts turned on" : "Budget alerts turned off",
                    Toast.LENGTH_SHORT).show();
            refresh();
        }, true);
        addRow(rows, R.drawable.ic_dollar, "Currency", Prefs.currencyLabel(c), v -> pickCurrency(), true);
        addRow(rows, R.drawable.ic_cloud, "Backup & Sync", "Export", v -> exportCsv(), true);
        addRow(rows, R.drawable.ic_help, "Help & Support", null, v -> showHelp(), true);
        addRow(rows, R.drawable.ic_info, "About App", null, v -> showAbout(), false);
    }

    private void addRow(LinearLayout parent, int icon, String title, String value,
                        View.OnClickListener l, boolean divider) {
        Context c = requireContext();
        View row = LayoutInflater.from(c).inflate(R.layout.item_setting, parent, false);
        ((ImageView) row.findViewById(R.id.iv_icon)).setImageResource(icon);
        ((TextView) row.findViewById(R.id.tv_title)).setText(title);
        if (value != null) {
            TextView tv = row.findViewById(R.id.tv_value);
            tv.setText(value);
            tv.setVisibility(View.VISIBLE);
        }
        row.setOnClickListener(l);
        parent.addView(row);
        if (divider) {
            View d = new View(c);
            d.setBackgroundColor(c.getColor(R.color.line));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 1);
            lp.setMarginStart(Ui.dp(c, 52));
            parent.addView(d, lp);
        }
    }

    private void editProfile() {
        final Context c = requireContext();
        LinearLayout box = new LinearLayout(c);
        box.setOrientation(LinearLayout.VERTICAL);
        int p = Ui.dp(c, 20);
        box.setPadding(p, p / 2, p, 0);

        final EditText name = new EditText(c);
        name.setHint("Full name");
        name.setText(Prefs.name(c));
        name.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        final EditText email = new EditText(c);
        email.setHint("Email");
        email.setText(Prefs.email(c));
        email.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        box.addView(name);
        box.addView(email);

        new AlertDialog.Builder(c)
                .setTitle("Personal Information")
                .setView(box)
                .setPositiveButton("Save", (d, w) -> {
                    String n = name.getText().toString().trim();
                    if (!n.isEmpty()) Prefs.setName(c, n);
                    Prefs.setEmail(c, email.getText().toString().trim());
                    refresh();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void pickCurrency() {
        final Context c = requireContext();
        String[] labels = new String[Prefs.CURRENCIES.length];
        for (int i = 0; i < labels.length; i++) {
            labels[i] = Prefs.CURRENCIES[i][0] + " - " + Prefs.CURRENCIES[i][2];
        }
        new AlertDialog.Builder(c)
                .setTitle("Currency")
                .setSingleChoiceItems(labels, Prefs.currencyIndex(c), (d, which) -> {
                    Prefs.setCurrency(c, Prefs.CURRENCIES[which][0]);
                    d.dismiss();
                    refresh();
                })
                .show();
    }

    private void exportCsv() {
        Context c = requireContext();
        List<Txn> all = DB.get(c).query(0, Long.MAX_VALUE, -1, 0);
        if (all.isEmpty()) {
            Toast.makeText(c, "Nothing to export yet", Toast.LENGTH_SHORT).show();
            return;
        }
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
        StringBuilder sb = new StringBuilder("Date,Type,Category,Amount,Note\n");
        for (Txn t : all) {
            sb.append(f.format(new Date(t.date))).append(',')
                    .append(t.income ? "Income" : "Expense").append(',')
                    .append('"').append(Cat.get(t.catId).name.replace("\"", "\"\"")).append("\",")
                    .append(String.format(Locale.US, "%.2f", t.amount)).append(',')
                    .append('"').append(t.note.replace("\"", "\"\"")).append("\"\n");
        }
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, "Expense Tracker export");
        i.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(i, "Export transactions"));
    }

    private void showHelp() {
        final Context c = requireContext();
        String[] items = {"Add sample data", "Delete all data"};
        new AlertDialog.Builder(c)
                .setTitle("Help & Support")
                .setItems(items, (d, which) -> {
                    if (which == 0) {
                        DB.get(c).addSample();
                        Toast.makeText(c, "Sample data added", Toast.LENGTH_SHORT).show();
                    } else {
                        new AlertDialog.Builder(c)
                                .setTitle("Delete all data?")
                                .setMessage("Every transaction will be removed. This cannot be undone.")
                                .setPositiveButton("Delete", (d2, w2) -> {
                                    DB.get(c).clear();
                                    Toast.makeText(c, "All data deleted", Toast.LENGTH_SHORT).show();
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    }
                })
                .show();
    }

    private void showAbout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Expense Tracker")
                .setMessage("Version 1.0\n\nManage your money, build a better tomorrow.\n\n"
                        + "All your data is stored on this device.")
                .setPositiveButton("OK", null)
                .show();
    }
}
