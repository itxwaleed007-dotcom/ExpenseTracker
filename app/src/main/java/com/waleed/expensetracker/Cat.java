package com.waleed.expensetracker;

import java.util.ArrayList;
import java.util.List;

/** A transaction category (static list, no database needed). */
final class Cat {
    final String id;
    final String name;
    final int color;
    final int icon;
    final boolean income;

    private static final List<Cat> ALL = new ArrayList<>();

    static {
        // Expense categories (same 9 as the design)
        add("food", "Food & Drink", 0xFFF97316, R.drawable.ic_food, false);
        add("transport", "Transport", 0xFF3B82F6, R.drawable.ic_car, false);
        add("shopping", "Shopping", 0xFF8B5CF6, R.drawable.ic_bag, false);
        add("bills", "Bills", 0xFFEF4444, R.drawable.ic_receipt, false);
        add("health", "Health", 0xFF10B981, R.drawable.ic_health, false);
        add("entertainment", "Entertainment", 0xFFF43F5E, R.drawable.ic_game, false);
        add("travel", "Travel", 0xFF2563EB, R.drawable.ic_plane, false);
        add("education", "Education", 0xFFF59E0B, R.drawable.ic_school, false);
        add("other", "Other", 0xFF6B7280, R.drawable.ic_more, false);
        // Income categories
        add("salary", "Salary", 0xFF10B981, R.drawable.ic_work, true);
        add("freelance", "Freelance", 0xFF14B8A6, R.drawable.ic_laptop, true);
        add("business", "Business", 0xFF6366F1, R.drawable.ic_store, true);
        add("gift", "Gift", 0xFFEC4899, R.drawable.ic_gift, true);
        add("investment", "Investment", 0xFF0EA5E9, R.drawable.ic_trend, true);
        add("other_inc", "Other", 0xFF6B7280, R.drawable.ic_more, true);
    }

    private Cat(String id, String name, int color, int icon, boolean income) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.icon = icon;
        this.income = income;
    }

    private static void add(String id, String name, int color, int icon, boolean income) {
        ALL.add(new Cat(id, name, color, icon, income));
    }

    static Cat get(String id) {
        for (Cat c : ALL) {
            if (c.id.equals(id)) return c;
        }
        return ALL.get(8); // "other"
    }

    static List<Cat> list(boolean income) {
        List<Cat> out = new ArrayList<>();
        for (Cat c : ALL) {
            if (c.income == income) out.add(c);
        }
        return out;
    }
}
