package com.roomchatapps.Pmishra.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.TypedValue;

import androidx.core.content.ContextCompat;

import com.roomchatapps.Pmishra.R;

import java.text.NumberFormat;
import java.util.Locale;

public class CoinUtils {

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
