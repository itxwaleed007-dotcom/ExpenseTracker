package com.waleed.expensetracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Category picker: returns the chosen category id in EXTRA_CAT. */
public class CategoriesActivity extends AppCompatActivity {
    static final String EXTRA_INCOME = "income";
    static final String EXTRA_CAT = "cat";

    private final List<Cat> all = new ArrayList<>();
    private final List<Cat> shown = new ArrayList<>();
    private CatAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        all.addAll(Cat.list(getIntent().getBooleanExtra(EXTRA_INCOME, false)));
        shown.addAll(all);

        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.rv_cats);
        rv.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new CatAdapter();
        rv.setAdapter(adapter);

        EditText search = findViewById(R.id.et_search);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {
            }

            @Override
            public void onTextChanged(CharSequence s, int a, int b, int c) {
                String q = s.toString().trim().toLowerCase(Locale.US);
                shown.clear();
                for (Cat cat : all) {
                    if (q.isEmpty() || cat.name.toLowerCase(Locale.US).contains(q)) shown.add(cat);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void choose(Cat c) {
        Intent data = new Intent();
        data.putExtra(EXTRA_CAT, c.id);
        setResult(RESULT_OK, data);
        finish();
    }

    private class CatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
            return new RecyclerView.ViewHolder(v) {
            };
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {
            final Cat c = shown.get(position);
            Ui.catIcon((ImageView) h.itemView.findViewById(R.id.iv_icon), c);
            ((TextView) h.itemView.findViewById(R.id.tv_name)).setText(c.name);
            h.itemView.setOnClickListener(v -> choose(c));
        }

        @Override
        public int getItemCount() {
            return shown.size();
        }
    }
}
