package com.waleed.expensetracker;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Number and date helpers. */
final class Fmt {
    static final long DAY = 24L * 60 * 60 * 1000;

    private Fmt() {}

    static String money(Context c, double v) {
        return Prefs.symbol(c) + String.format(Locale.US, "%,.2f", v);
    }

    static String moneyShort(Context c, double v) {
        return Prefs.symbol(c) + String.format(Locale.US, "%,.0f", v);
    }

    static String signed(Context c, Txn t) {
        return (t.income ? "+ " : "- ") + money(c, t.amount);
    }

    static String date(long ms) {
        return new SimpleDateFormat("MMM d, yyyy", Locale.US).format(new Date(ms));
    }

    static String time(long ms) {
        return new SimpleDateFormat("hh:mm a", Locale.US).format(new Date(ms));
    }

    static long startOfDay(long ms) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ms);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    static long addDays(long ms, int n) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ms);
        c.add(Calendar.DAY_OF_YEAR, n);
        return c.getTimeInMillis();
    }

    /** First moment of the month containing ms, shifted by addMonths. */
    static long startOfMonth(long ms, int addMonths) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(startOfDay(ms));
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.add(Calendar.MONTH, addMonths);
        return c.getTimeInMillis();
    }

    /** First moment of the year containing ms, shifted by addYears. */
    static long startOfYear(long ms, int addYears) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(startOfDay(ms));
        c.set(Calendar.DAY_OF_YEAR, 1);
        c.add(Calendar.YEAR, addYears);
        return c.getTimeInMillis();
    }

    /** "Today, Apr 26, 2025" / "Yesterday, Apr 25, 2025" / "Apr 24, 2025". */
    static String dayHeader(long ms) {
        long today = startOfDay(System.currentTimeMillis());
        long d = startOfDay(ms);
        if (d == today) return "Today, " + date(ms);
        if (d == startOfDay(addDays(today, -1))) return "Yesterday, " + date(ms);
        return date(ms);
    }

    /** "Today" / "Yesterday" / "Apr 24". */
    static String relDay(long ms) {
        long today = startOfDay(System.currentTimeMillis());
        long d = startOfDay(ms);
        if (d == today) return "Today";
        if (d == startOfDay(addDays(today, -1))) return "Yesterday";
        return new SimpleDateFormat("MMM d", Locale.US).format(new Date(ms));
    }
}
