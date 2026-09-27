package com.roomchatapps.Pmishra.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CoinUtils {

    /**
     * Formats coin values into compact human-readable strings.
     * Examples:
     * - 500       -> "500"
     * - 1000      -> "1k"
     * - 3600      -> "3.6k"
     * - 3650      -> "3.65k"
     * - 100000    -> "1L"   (1 Lakh)
     * - 150000    -> "1.5L" (1.5 Lakh)
     * - 360000    -> "3.6L" (3.6 Lakh)
     * - 10000000  -> "1Cr"  (1 Crore)
     */
    public static String formatCoins(long coins) {
        if (coins <= 0) {
            return "0";
        } else if (coins < 1000) {
            return String.valueOf(coins);
        } else if (coins < 100000) {
            double val = coins / 1000.0;
            return formatDecimal(val) + "k";
        } else if (coins < 10000000) {
            double val = coins / 100000.0;
            return formatDecimal(val) + "L";
        } else {
            double val = coins / 10000000.0;
            return formatDecimal(val) + "Cr";
        }
    }

    private static String formatDecimal(double val) {
        DecimalFormat df = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));
        return df.format(val);
    }
}
