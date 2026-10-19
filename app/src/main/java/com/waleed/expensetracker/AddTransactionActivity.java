package com.waleed.expensetracker;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;

import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

/** Add a new transaction, or edit an existing one when EXTRA_ID is given. */
public class AddTransactionActivity extends AppCompatActivity {
    static final String EXTRA_ID = "id";
    static final String EXTRA_INCOME = "income";

    private boolean income;
    private String catId;
    private long date;
    private long editId = -1;

    private TextView tvTitle, tvExpense, tvIncome, tvCur, tvCat, tvDate, tvDelete;
    private ImageView ivCat;
    private EditText etAmount, etNote;

    private final ActivityResultLauncher<Intent> catPicker = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    catId = result.getData().getStringExtra(CategoriesActivity.EXTRA_CAT);
                    showCategory();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        tvTitle = findViewById(R.id.tv_title);
        tvExpense = findViewById(R.id.tv_expense);
        tvIncome = findViewById(R.id.tv_income);
        tvCur = findViewById(R.id.tv_cur);
        tvCat = findViewById(R.id.tv_cat);
        tvDate = findViewById(R.id.tv_date);
        tvDelete = findViewById(R.id.tv_delete);
        ivCat = findViewById(R.id.iv_cat);
        etAmount = findViewById(R.id.et_amount);
        etNote = findViewById(R.id.et_note);

        tvCur.setText(Prefs.symbol(this).trim());

        editId = getIntent().getLongExtra(EXTRA_ID, -1);
        income = getIntent().getBooleanExtra(EXTRA_INCOME, false);
        date = System.currentTimeMillis();

        if (editId >= 0) {
            Txn t = DB.get(this).find(editId);
            if (t == null) {
                finish();
                return;
            }
            income = t.income;
            catId = t.catId;
            date = t.date;
            etAmount.setText(String.format(Locale.US, "%.2f", t.amount));
            etNote.setText(t.note);
            tvDelete.setVisibility(android.view.View.VISIBLE);
        }

        applyType();
        showCategory();
        showDate();

        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
        tvExpense.setOnClickListener(v -> setIncome(false));
        tvIncome.setOnClickListener(v -> setIncome(true));
        findViewById(R.id.row_cat).setOnClickListener(v -> {
            Intent i = new Intent(this, CategoriesActivity.class);
            i.putExtra(CategoriesActivity.EXTRA_INCOME, income);
            catPicker.launch(i);
        });
        findViewById(R.id.row_date).setOnClickListener(v -> pickDate());
        findViewById(R.id.iv_calc).setOnClickListener(v -> showCalculator());
        findViewById(R.id.btn_save).setOnClickListener(v -> save());
        tvDelete.setOnClickListener(v -> confirmDelete());
    }

    private void setIncome(boolean value) {
        if (income == value) return;
        income = value;
        if (catId != null && Cat.get(catId).income != income) catId = null;
        applyType();
        showCategory();
    }

    private void applyType() {
        String verb = editId >= 0 ? "Edit " : "Add ";
        tvTitle.setText(verb + (income ? "Income" : "Expense"));
        int white = getColor(R.color.white);
        int gray = getColor(R.color.text_sub);
        tvExpense.setBackgroundResource(!income ? R.drawable.bg_pill_red : 0);
        tvExpense.setTextColor(!income ? white : gray);
        tvIncome.setBackgroundResource(income ? R.drawable.bg_pill_selected : 0);
        tvIncome.setTextColor(income ? white : gray);
    }

    private void showCategory() {
        if (catId == null) {
            tvCat.setText("Select Category");
            tvCat.setTextColor(getColor(R.color.text_sub));
            ivCat.setImageResource(R.drawable.ic_search);
            ivCat.setColorFilter(getColor(R.color.text_sub));
            ivCat.setBackground(null);
        } else {
            Cat c = Cat.get(catId);
            tvCat.setText(c.name);
            tvCat.setTextColor(getColor(R.color.text));
            Ui.catIcon(ivCat, c);
        }
    }

    private void showDate() {
        tvDate.setText(Fmt.date(date));
    }

    private void pickDate() {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date);
        new DatePickerDialog(this, (view, y, m, d) -> {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(date); // keep the time of day
            c.set(y, m, d);
            date = c.getTimeInMillis();
            showDate();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showCalculator() {
        final EditText e = new EditText(this);
        e.setHint("e.g. 12.5 + 3 * 4");
        e.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        e.setText(etAmount.getText());
        FrameLayout box = new FrameLayout(this);
        int p = Ui.dp(this, 20);
        box.setPadding(p, p / 2, p, 0);
        box.addView(e);
        new AlertDialog.Builder(this)
                .setTitle("Calculator")
                .setMessage("Use + - * / and brackets")
                .setView(box)
                .setPositiveButton("Use result", (d, w) -> {
                    try {
                        double v = Calc.eval(e.getText().toString());
                        if (Double.isNaN(v) || Double.isInfinite(v) || v <= 0) {
                            throw new IllegalArgumentException();
                        }
                        etAmount.setText(String.format(Locale.US, "%.2f", v));
                    } catch (Exception ex) {
                        Toast.makeText(this, "Invalid calculation", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void save() {
        double amount;
        try {
            amount = Double.parseDouble(etAmount.getText().toString().trim().replace(",", ""));
        } catch (Exception e) {
            amount = 0;
        }
        if (amount <= 0) {
            etAmount.setError("Enter an amount");
            return;
        }
        if (catId == null) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        Txn t = new Txn();
        t.id = editId;
        t.income = income;
        t.amount = Math.round(amount * 100) / 100.0;
        t.catId = catId;
        t.note = etNote.getText().toString().trim();
        t.date = date;

        DB db = DB.get(this);
        if (editId >= 0) db.update(t);
        else db.insert(t);

        if (!income && Prefs.notif(this)) checkLimits(db, t);
        finish();
    }

    /** Shows a warning if this expense pushed the month over the budget or a category limit. */
    private void checkLimits(DB db, Txn t) {
        long from = Fmt.startOfMonth(t.date, 0);
        long to = Fmt.startOfMonth(t.date, 1);
        double budget = Prefs.budget(this);
        if (budget > 0 && db.sum(false, from, to) > budget) {
            Toast.makeText(getApplicationContext(), "Warning: monthly budget exceeded",
                    Toast.LENGTH_LONG).show();
            return;
        }
        double limit = Prefs.limit(this, t.catId);
        if (limit > 0) {
            Map<String, Double> byCat = db.expenseByCat(from, to);
            Double spent = byCat.get(t.catId);
            if (spent != null && spent > limit) {
                Toast.makeText(getApplicationContext(),
                        "Warning: " + Cat.get(t.catId).name + " limit exceeded",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete transaction?")
                .setMessage("This cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> {
                    DB.get(this).delete(editId);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
