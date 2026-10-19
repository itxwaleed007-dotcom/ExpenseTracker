package com.waleed.expensetracker;

import android.content.Context;
import android.content.SharedPreferences;

/** Small wrapper around SharedPreferences (profile, currency, budget, limits). */
final class Prefs {
    // {code, symbol, display name}
    static final String[][] CURRENCIES = {
            {"USD", "$", "US Dollar"},
            {"PKR", "Rs ", "Pakistani Rupee"},
            {"EUR", "€", "Euro"},
            {"GBP", "£", "British Pound"},
            {"INR", "₹", "Indian Rupee"},
            {"AED", "AED ", "UAE Dirham"},
            {"SAR", "SAR ", "Saudi Riyal"}
    };

    private Prefs() {}

    private static SharedPreferences sp(Context c) {
        return c.getSharedPreferences("expense_prefs", Context.MODE_PRIVATE);
    }

    static boolean started(Context c) { return sp(c).getBoolean("started", false); }
    static void setStarted(Context c, boolean v) { sp(c).edit().putBoolean("started", v).apply(); }

    static String name(Context c) { return sp(c).getString("name", "Waleed Arshad"); }
    static void setName(Context c, String v) { sp(c).edit().putString("name", v).apply(); }

    static String firstName(Context c) {
        String n = name(c).trim();
        if (n.isEmpty()) return "there";
        int i = n.indexOf(' ');
        return i > 0 ? n.substring(0, i) : n;
    }

    static String email(Context c) { return sp(c).getString("email", "waleed@example.com"); }
    static void setEmail(Context c, String v) { sp(c).edit().putString("email", v).apply(); }

    static boolean notif(Context c) { return sp(c).getBoolean("notif", true); }
    static void setNotif(Context c, boolean v) { sp(c).edit().putBoolean("notif", v).apply(); }

    static int currencyIndex(Context c) {
        String code = sp(c).getString("currency", "USD");
        for (int i = 0; i < CURRENCIES.length; i++) {
            if (CURRENCIES[i][0].equals(code)) return i;
        }
        return 0;
    }
    static void setCurrency(Context c, String code) { sp(c).edit().putString("currency", code).apply(); }
    static String symbol(Context c) { return CURRENCIES[currencyIndex(c)][1]; }
    static String currencyLabel(Context c) {
        String[] cur = CURRENCIES[currencyIndex(c)];
        return cur[0] + " - " + cur[2];
    }

    private static double getD(Context c, String key) {
        return Double.longBitsToDouble(sp(c).getLong(key, Double.doubleToRawLongBits(0d)));
    }
    private static void putD(Context c, String key, double v) {
        sp(c).edit().putLong(key, Double.doubleToRawLongBits(v)).apply();
    }

    static double budget(Context c) { return getD(c, "budget"); }
    static void setBudget(Context c, double v) { putD(c, "budget", v); }

    static double limit(Context c, String catId) { return getD(c, "limit_" + catId); }
    static void setLimit(Context c, String catId, double v) { putD(c, "limit_" + catId, v); }
}
