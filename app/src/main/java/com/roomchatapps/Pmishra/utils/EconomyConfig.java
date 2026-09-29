package com.roomchatapps.Pmishra.utils;

public class EconomyConfig {

    /**
     * MANDATORY RULE: Minimum Gift Value = 10,00,000 (10 Lakh Coins).
     * No gift below 10 Lakh is allowed anywhere in the system.
     */
    public static long MIN_GIFT_VALUE = 1000000L; // 10 Lakh

    /**
     * Configurable Gift Tiers (Examples: 10L, 20L, 50L, 1Cr, 2Cr, 5Cr, 10Cr)
     */
    public static long GIFT_TIER_1 = 1000000L;   // 10 Lakh
    public static long GIFT_TIER_2 = 2000000L;   // 20 Lakh
    public static long GIFT_TIER_3 = 5000000L;   // 50 Lakh
    public static long GIFT_TIER_4 = 10000000L;  // 1 Crore
    public static long GIFT_TIER_5 = 20000000L;  // 2 Crore
    public static long GIFT_TIER_6 = 50000000L;  // 5 Crore
    public static long GIFT_TIER_7 = 100000000L; // 10 Crore

    /**
     * FINAL RULE: Gift -> Energy Economy
     * - Receiver earns 60% of total gift coins as ENERGY (0 Coins, 0 Diamonds)
     * - System/Platform cut = 40%
     */
    public static float ENERGY_AWARD_PERCENTAGE = 0.60f; // 60% Energy
    public static float SYSTEM_CUT_PERCENTAGE = 0.40f;   // 40% System Cut

    /**
     * Animation thresholds:
     * - Low-Value gifts: < 50,00,000 (50 Lakh)
     * - High-Value gifts: >= 50,00,000 (50 Lakh / 1 Crore)
     */
    public static long LOW_ANIMATION_THRESHOLD = 1000000L;  // 10 Lakh
    public static long HIGH_ANIMATION_THRESHOLD = 5000000L; // 50 Lakh

    /**
     * Required gift quantities for selection in Gift Panel
     */
    public static int[] GIFT_QUANTITY_OPTIONS = new int[]{1, 7, 77, 777};

    /**
     * Normal Spin coin costs
     */
    public static long[] NORMAL_SPIN_COSTS = new long[]{10L, 20L, 30L};

    /**
     * Coin to Energy exchange rate (1 Coin = 1 Energy)
     */
    public static float COIN_TO_ENERGY_RATE = 1.0f;

    /**
     * Validates if a gift price satisfies the 10 Lakh minimum rule.
     */
    public static boolean isValidGiftValue(long giftPrice) {
        return giftPrice >= MIN_GIFT_VALUE;
    }

    /**
     * Calculates the total coin cost for sending a gift.
     */
    public static long calculateTotalCost(long singlePrice, int quantity, int recipientCount) {
        if (singlePrice <= 0 || quantity <= 0 || recipientCount <= 0) return 0;
        return singlePrice * (long) quantity * (long) recipientCount;
    }

    /**
     * Calculates 30% Energy awarded to the receiver from total gift coins.
     * Example: 10,00,000 Total Gift Coins -> 3,00,000 Energy
     */
    public static long calculateEnergyAwarded(long totalGiftCoins) {
        if (totalGiftCoins <= 0) return 0;
        return (long) Math.floor(totalGiftCoins * ENERGY_AWARD_PERCENTAGE);
    }

    /**
     * Calculates the 70% system/platform cut from total gift coins.
     * Example: 10,00,000 Total Gift Coins -> 7,00,000 System Cut
     */
    public static long calculateSystemCut(long totalGiftCoins) {
        if (totalGiftCoins <= 0) return 0;
        return (long) Math.floor(totalGiftCoins * SYSTEM_CUT_PERCENTAGE);
    }
}
