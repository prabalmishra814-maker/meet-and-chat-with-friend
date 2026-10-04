package com.roomchatapps.Pmishra.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.TypedValue;

import androidx.core.content.ContextCompat;

import com.roomchatapps.Pmishra.R;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class CoinUtils {

    // ROOM COIN FORMAT
    /**
     * Formats room spending into compact Indian notation (K, L, Cr).
     * Rules:
     * 0         -> 0
     * 1000      -> 1K
     * 1500      -> 1.5K
     * 2500      -> 2.5K
     * 10000     -> 10K
     * 99999     -> 99.99K
     * 100000    -> 1L
     * 150000    -> 1.5L
     * 500000    -> 5L
     * 999999    -> 9.99L
     * 1000000   -> 10L
     * 10000000  -> 1Cr
     * 100000000 -> 10Cr
     */
    public static String formatCompactCoins(long amount) {
        if (amount <= 0) {
            return "0";
        }
        if (amount < 1000) {
            return String.valueOf(amount);
        } else if (amount < 100000) { // 1K to <100K (1L)
            double val = amount / 1000.0;
            return formatDecimal(val) + "K";
        } else if (amount < 10000000) { // 1L to <100L (1Cr)
            double val = amount / 100000.0;
            return formatDecimal(val) + "L";
        } else { // 1Cr+
            double val = amount / 10000000.0;
            return formatDecimal(val) + "Cr";
        }
    }

    // ROOM COIN FORMAT
    private static String formatDecimal(double val) {
        DecimalFormat df = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));
        df.setRoundingMode(RoundingMode.DOWN);
        return df.format(val);
    }

    /**
     * Formats coin/energy values into full formatted numbers.
     * Examples:
     * - 500       -> "500"
     * - 1000      -> "1,000"
     * - 100000    -> "100,000"
     * - 150000    -> "150,000"
     * - 10000000  -> "10,000,000"
     */
    public static String formatCoins(long coins) {
        if (coins <= 0) {
            return "0";
        }
        return NumberFormat.getInstance(Locale.US).format(coins);
    }

    /**
     * Returns a CharSequence with the @drawable/coin image icon placed BEFORE the coin number text.
     */
    public static CharSequence getCoinSpannable(Context context, String coinText) {
        if (context == null || coinText == null) return coinText != null ? coinText : "0";
        SpannableStringBuilder builder = new SpannableStringBuilder("  " + coinText);
        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.coin);
        if (drawable != null) {
            int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, context.getResources().getDisplayMetrics());
            drawable.setBounds(0, 0, size, size);
            ImageSpan imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);
            builder.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return builder;
    }

    public static CharSequence getCoinSpannable(Context context, long coins) {
        return getCoinSpannable(context, formatCoins(coins));
    }
}
