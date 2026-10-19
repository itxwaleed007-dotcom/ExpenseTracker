package com.waleed.expensetracker;

/** One income or expense record. */
final class Txn {
    long id = -1;
    boolean income;
    double amount;
    String catId;
    String note = "";
    long date; // epoch millis
}
