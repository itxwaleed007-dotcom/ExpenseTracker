package com.waleed.expensetracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/** List of transactions with date headers (items are either String headers or Txn rows). */
final class TxnAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    interface Listener {
        void onClick(Txn t);

        void onLongClick(Txn t);
    }

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private final List<Object> items = new ArrayList<>();
    private final Listener listener;

    TxnAdapter(Listener listener) {
        this.listener = listener;
    }

    void set(List<Object> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof String ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        View v = inf.inflate(viewType == TYPE_HEADER ? R.layout.item_date_header : R.layout.item_transaction,
                parent, false);
        return new RecyclerView.ViewHolder(v) {
        };
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {
        Object o = items.get(position);
        if (o instanceof String) {
            ((TextView) h.itemView).setText((String) o);
        } else {
            final Txn t = (Txn) o;
            bind(h.itemView, t, true);
            h.itemView.setOnClickListener(v -> listener.onClick(t));
            h.itemView.setOnLongClickListener(v -> {
                listener.onLongClick(t);
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Fills an item_transaction row. showTime=true shows the clock time, false shows Today/Yesterday. */
    static void bind(View row, Txn t, boolean showTime) {
        Context c = row.getContext();
        Cat cat = Cat.get(t.catId);
        Ui.catIcon((ImageView) row.findViewById(R.id.iv_icon), cat);
        TextView title = row.findViewById(R.id.tv_title);
        TextView sub = row.findViewById(R.id.tv_sub);
        if (t.note != null && !t.note.isEmpty()) {
            title.setText(t.note);
            sub.setText(cat.name);
        } else {
            title.setText(cat.name);
            sub.setText(t.income ? "Income" : "Expense");
        }
        TextView amount = row.findViewById(R.id.tv_amount);
        amount.setText(Fmt.signed(c, t));
        amount.setTextColor(c.getColor(t.income ? R.color.income : R.color.red));
        ((TextView) row.findViewById(R.id.tv_time)).setText(showTime ? Fmt.time(t.date) : Fmt.relDay(t.date));
    }
}
