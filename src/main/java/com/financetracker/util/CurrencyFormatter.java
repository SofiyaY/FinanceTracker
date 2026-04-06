package com.financetracker.util;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyFormatter {
    private static final Locale NORWEGIAN = new Locale("nb", "NO");
    private static final NumberFormat FORMAT = NumberFormat.getNumberInstance(NORWEGIAN);

    static {
        FORMAT.setMinimumFractionDigits(2);
        FORMAT.setMaximumFractionDigits(2);
    }

    public static String format(double amount) {
        return FORMAT.format(amount) + " kr";
    }

    public static String formatSigned(double amount) {
        String formatted = FORMAT.format(Math.abs(amount)) + " kr";
        return amount < 0 ? "-" + formatted : formatted;
    }
}
