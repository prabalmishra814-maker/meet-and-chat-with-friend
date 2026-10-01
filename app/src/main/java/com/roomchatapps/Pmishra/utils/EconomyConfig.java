package com.roomchatapps.Pmishra.utils;

public class EconomyConfig {

    /**
     * Minimum Gift Value = 30,000 Coins.
     */
    public static long MIN_GIFT_VALUE = 30000L;

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
     * - Targeted receiver earns 30% of total gift coins as ENERGY
     * - Room-wide gifts distribute 70% of total gift coins as ENERGY among eligible room members
     * - Platform/Remaining portion = 70% for targeted / 30% for room
     */
    // GIFT COIN/ENERGY FIX
    public static float TARGETED_ENERGY_PERCENTAGE = 0.30f; // 30% Energy for selected receiver
    // GIFT COIN/ENERGY FIX
    public static float ROOM_ENERGY_PERCENTAGE = 0.70f;     // 70% Energy for room-wide distribution
    public static float ENERGY_AWARD_PERCENTAGE = 0.30f;     // Default 30% Energy
    public static float SYSTEM_CUT_PERCENTAGE = 0.70f;       // Default 70% System Cut

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
     * Validates if a gift price is valid (greater than 0).
     */
    public static boolean isValidGiftValue(long giftPrice) {
        return giftPrice > 0;
    }

    /**
     * Calculates the total coin cost for sending a gift.
     */
    // GIFT COIN/ENERGY FIX
    public static long calculateTotalCost(long singlePrice, int quantity, int recipientCount, boolean isRoomGift) {
        if (singlePrice <= 0 || quantity <= 0) return 0;
        if (isRoomGift) {
            return singlePrice * (long) quantity;
        } else {
            return singlePrice * (long) quantity * (long) Math.max(1, recipientCount);
        }
    }

    // GIFT COIN/ENERGY FIX
    public static long calculateTotalCost(long singlePrice, int quantity, int recipientCount) {
        return calculateTotalCost(singlePrice, quantity, recipientCount, false);
    }

    /**
     * Calculates 30% Energy awarded to the selected receiver from total gift coins.
     * Example: 100 Total Gift Coins -> 30 Energy
     */
    // GIFT COIN/ENERGY FIX
    public static long calculateEnergyAwarded(long totalGiftCoins) {
        if (totalGiftCoins <= 0) return 0;
        return (long) Math.floor(totalGiftCoins * TARGETED_ENERGY_PERCENTAGE);
    }

    /**
     * Calculates 70% Energy distributed across the room from total gift coins.
     * Example: 100 Total Gift Coins -> 70 Energy Total Room Distribution
     */
    // GIFT COIN/ENERGY FIX
    public static long calculateRoomEnergyDistributed(long totalGiftCoins) {
        if (totalGiftCoins <= 0) return 0;
        return (long) Math.floor(totalGiftCoins * ROOM_ENERGY_PERCENTAGE);
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
