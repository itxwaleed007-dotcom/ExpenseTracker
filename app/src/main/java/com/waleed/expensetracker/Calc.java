package com.waleed.expensetracker;

/** Tiny arithmetic evaluator: + - * / ( ) and decimals. */
final class Calc {
    private final String s;
    private int p = 0;

    private Calc(String s) { this.s = s; }

    static double eval(String input) {
        String clean = input.replace(" ", "").replace("×", "*").replace("÷", "/").replace("−", "-");
        Calc c = new Calc(clean);
        double v = c.expr();
        if (c.p != c.s.length()) throw new IllegalArgumentException("Bad expression");
        return v;
    }

    private double expr() {
        double v = term();
        while (p < s.length()) {
            char ch = s.charAt(p);
            if (ch == '+') { p++; v += term(); }
            else if (ch == '-') { p++; v -= term(); }
            else break;
        }
        return v;
    }

    private double term() {
        double v = factor();
        while (p < s.length()) {
            char ch = s.charAt(p);
            if (ch == '*') { p++; v *= factor(); }
            else if (ch == '/') {
                p++;
                double d = factor();
                if (d == 0) throw new ArithmeticException("Divide by zero");
                v /= d;
            } else break;
        }
        return v;
    }

    private double factor() {
        if (p < s.length() && s.charAt(p) == '-') { p++; return -factor(); }
        if (p < s.length() && s.charAt(p) == '(') {
            p++;
            double v = expr();
            if (p >= s.length() || s.charAt(p) != ')') throw new IllegalArgumentException("Missing )");
            p++;
            return v;
        }
        int st = p;
        while (p < s.length() && (Character.isDigit(s.charAt(p)) || s.charAt(p) == '.')) p++;
        if (st == p) throw new IllegalArgumentException("Number expected");
        return Double.parseDouble(s.substring(st, p));
    }
}
