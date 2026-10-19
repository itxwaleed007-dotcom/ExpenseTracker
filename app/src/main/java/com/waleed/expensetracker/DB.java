package com.waleed.expensetracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** SQLite storage for all transactions. */
final class DB extends SQLiteOpenHelper {
    private static final String COLS = "id,income,amount,cat,note,date";
    private static DB inst;

    static synchronized DB get(Context c) {
        if (inst == null) inst = new DB(c.getApplicationContext());
        return inst;
    }

    private DB(Context c) {
        super(c, "expense.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE txn(id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " income INTEGER NOT NULL, amount REAL NOT NULL, cat TEXT NOT NULL,"
                + " note TEXT, date INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX idx_txn_date ON txn(date)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        // first version, nothing to migrate
    }

    private static Txn read(Cursor c) {
        Txn t = new Txn();
        t.id = c.getLong(0);
        t.income = c.getInt(1) == 1;
        t.amount = c.getDouble(2);
        t.catId = c.getString(3);
        t.note = c.getString(4) == null ? "" : c.getString(4);
        t.date = c.getLong(5);
        return t;
    }

    private static ContentValues values(Txn t) {
        ContentValues v = new ContentValues();
        v.put("income", t.income ? 1 : 0);
        v.put("amount", t.amount);
        v.put("cat", t.catId);
        v.put("note", t.note);
        v.put("date", t.date);
        return v;
    }

    long insert(Txn t) {
        return getWritableDatabase().insert("txn", null, values(t));
    }

    void update(Txn t) {
        getWritableDatabase().update("txn", values(t), "id=?", new String[]{String.valueOf(t.id)});
    }

    void delete(long id) {
        getWritableDatabase().delete("txn", "id=?", new String[]{String.valueOf(id)});
    }

    void clear() {
        getWritableDatabase().delete("txn", null, null);
    }

    Txn find(long id) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT " + COLS + " FROM txn WHERE id=?", new String[]{String.valueOf(id)})) {
            return c.moveToFirst() ? read(c) : null;
        }
    }

    /**
     * Transactions with from <= date < to, newest first.
     *
     * @param type  -1 = all, 0 = expenses only, 1 = income only
     * @param limit 0 = no limit
     */
    List<Txn> query(long from, long to, int type, int limit) {
        StringBuilder sql = new StringBuilder("SELECT " + COLS + " FROM txn WHERE date>=? AND date<?");
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(from));
        args.add(String.valueOf(to));
        if (type >= 0) {
            sql.append(" AND income=?");
            args.add(String.valueOf(type));
        }
        sql.append(" ORDER BY date DESC");
        if (limit > 0) sql.append(" LIMIT ").append(limit);
        List<Txn> out = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(sql.toString(), args.toArray(new String[0]))) {
            while (c.moveToNext()) out.add(read(c));
        }
        return out;
    }

    double sum(boolean income, long from, long to) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT COALESCE(SUM(amount),0) FROM txn WHERE income=? AND date>=? AND date<?",
                new String[]{income ? "1" : "0", String.valueOf(from), String.valueOf(to)})) {
            return c.moveToFirst() ? c.getDouble(0) : 0;
        }
    }

    /** Expense total per category, biggest first. */
    Map<String, Double> expenseByCat(long from, long to) {
        Map<String, Double> out = new LinkedHashMap<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT cat, SUM(amount) s FROM txn WHERE income=0 AND date>=? AND date<?"
                        + " GROUP BY cat ORDER BY s DESC",
                new String[]{String.valueOf(from), String.valueOf(to)})) {
            while (c.moveToNext()) out.put(c.getString(0), c.getDouble(1));
        }
        return out;
    }

    /** Fills the app with demo data so every screen has something to show. */
    void addSample() {
        Random r = new Random();
        long now = System.currentTimeMillis();
        String[][] defs = {
                {"food", "Groceries", "Restaurant", "Coffee", "Lunch with friends"},
                {"transport", "Taxi", "Fuel", "Bus pass"},
                {"shopping", "Clothes", "Gadgets", "Shoes"},
                {"bills", "Electricity", "Internet", "Water bill"},
                {"health", "Pharmacy", "Doctor visit"},
                {"entertainment", "Movie night", "Games"},
                {"travel", "Train ticket", "Hotel"},
                {"education", "Online course", "Books"}
        };
        double[][] range = {{5, 60}, {3, 40}, {20, 150}, {30, 120}, {10, 80}, {8, 50}, {40, 200}, {15, 100}};
        for (int i = 0; i < 30; i++) {
            int k = r.nextInt(defs.length);
            String[] d = defs[k];
            Txn t = new Txn();
            t.income = false;
            t.catId = d[0];
            t.note = d[1 + r.nextInt(d.length - 1)];
            double a = range[k][0] + r.nextDouble() * (range[k][1] - range[k][0]);
            t.amount = Math.round(a * 100) / 100.0;
            t.date = now - (long) r.nextInt(35) * Fmt.DAY - r.nextInt(12) * 3600000L;
            insert(t);
        }
        Txn salary = new Txn();
        salary.income = true;
        salary.catId = "salary";
        salary.note = "Monthly salary";
        salary.amount = 3200;
        salary.date = now - 3 * Fmt.DAY;
        insert(salary);
        Txn free = new Txn();
        free.income = true;
        free.catId = "freelance";
        free.note = "Freelance project";
        free.amount = 450;
        free.date = now - 10 * Fmt.DAY;
        insert(free);
    }
}
