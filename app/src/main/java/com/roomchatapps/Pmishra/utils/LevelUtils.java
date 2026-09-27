package com.roomchatapps.Pmishra.utils;

public class LevelUtils {

    public static final long COINS_PER_LEVEL = 10000L;
    public static final long COINS_PER_XP = 100L;
    public static final int MAX_XP_PER_LEVEL = 100;

    /**
     * Calculates the user level based on total coins spent.
     * Rule: 10,000 coins spent = +1 Level (Starts at Level 1).
     */
    public static long calculateLevel(long coinsSpent) {
        if (coinsSpent < 0) coinsSpent = 0;
        return (coinsSpent / COINS_PER_LEVEL) + 1;
    }

    /**
     * Calculates total accumulated XP based on total coins spent.
     * Rule: 100 coins spent = 1 XP (so 10,000 coins spent = 100 XP = 1 Level).
     */
    public static long calculateTotalXp(long coinsSpent) {
        if (coinsSpent < 0) coinsSpent = 0;
        return coinsSpent / COINS_PER_XP;
    }

    /**
     * Calculates current XP progress within the current level (0 - 99 XP).
     */
    public static int calculateCurrentXpInLevel(long coinsSpent) {
        if (coinsSpent < 0) coinsSpent = 0;
        return (int) ((coinsSpent % COINS_PER_LEVEL) / COINS_PER_XP);
    }

    /**
     * Calculates coins spent towards the current level (0 - 9999 coins).
     */
    public static long calculateCoinsInCurrentLevel(long coinsSpent) {
        if (coinsSpent < 0) coinsSpent = 0;
        return coinsSpent % COINS_PER_LEVEL;
    }

    /**
     * Calculates coins required to reach the next level.
     */
    public static long getCoinsNeededForNextLevel(long coinsSpent) {
        if (coinsSpent < 0) coinsSpent = 0;
        return COINS_PER_LEVEL - calculateCoinsInCurrentLevel(coinsSpent);
    }

    public static String getLevelBadgeText(long level) {
        return "Lv." + level;
    }

    public static String getXpProgressText(long coinsSpent) {
        return calculateCurrentXpInLevel(coinsSpent) + " / " + MAX_XP_PER_LEVEL + " XP";
    }

    public static String getCoinsProgressText(long coinsSpent) {
        long currentCoinsInLevel = calculateCoinsInCurrentLevel(coinsSpent);
        return String.format("%,d / %,d Coins", currentCoinsInLevel, COINS_PER_LEVEL);
    }
}
