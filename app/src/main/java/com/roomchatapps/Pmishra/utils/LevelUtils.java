package com.roomchatapps.Pmishra.utils;

import com.roomchatapps.Pmishra.R;

public class LevelUtils {

    // MANDATORY RULE: 10 Lakh Coins (10,00,000) = 10 XP (100,000 Coins = 1 XP)
    public static final long COINS_PER_XP = 100000L;

    // Cumulative XP required to reach each level (Index 0 = Level 1 (10 XP / 10L coins), Index 1 = Level 2 (30 XP / 30L coins), etc.)
    private static final long[] LEVEL_THRESHOLDS_XP = {
            10L,       // Level 1: 10 XP (10 Lakh coins)
            30L,       // Level 2: 30 XP (30 Lakh coins)
            60L,       // Level 3: 60 XP (60 Lakh coins)
            100L,      // Level 4: 100 XP (1 Crore coins)
            200L,      // Level 5: 200 XP (2 Crore coins)
            350L,      // Level 6: 350 XP (3.5 Crore coins)
            550L,      // Level 7: 550 XP (5.5 Crore coins)
            800L,      // Level 8: 800 XP (8 Crore coins)
            1100L      // Level 9: 1100 XP (11 Crore coins)
    };

    public static final long XP_PER_LEVEL_AFTER_9 = 1000L; // 1000 XP per level after Level 9 (10 Crore coins)
    public static final long COINS_PER_LEVEL = 100000000L; // 10 Crore coins

    /**
     * Calculates user level based on total coins spent.
     * New user (0 coins spent / < 10 Lakh coins) starts at Level 0.
     */
    public static long calculateLevel(long coinsSpent) {
        long totalXp = calculateTotalXp(coinsSpent);
        if (totalXp < LEVEL_THRESHOLDS_XP[0]) return 0;

        for (int i = LEVEL_THRESHOLDS_XP.length - 1; i >= 0; i--) {
            if (totalXp >= LEVEL_THRESHOLDS_XP[i]) {
                if (i == LEVEL_THRESHOLDS_XP.length - 1) {
                    // Level 9 and above
                    long extraXp = totalXp - LEVEL_THRESHOLDS_XP[i];
                    return 9 + (extraXp / XP_PER_LEVEL_AFTER_9);
                } else {
                    return i + 1;
                }
            }
        }
        return 0;
    }

    /**
     * Calculates total XP from coins spent (100,000 Coins = 1 XP).
     * 10 Lakh Coins = 10 XP.
     */
    public static long calculateTotalXp(long coinsSpent) {
        if (coinsSpent < 0) coinsSpent = 0;
        return coinsSpent / COINS_PER_XP;
    }

    /**
     * Calculates total XP required to reach the start of a given level.
     * Level 0 starts at 0 XP.
     * Level 1 starts at 10 XP.
     */
    public static long getXpRequiredForLevel(long level) {
        if (level <= 0) return 0L;
        if (level <= LEVEL_THRESHOLDS_XP.length) {
            return LEVEL_THRESHOLDS_XP[(int) level - 1];
        } else {
            long baseLevel9Xp = LEVEL_THRESHOLDS_XP[LEVEL_THRESHOLDS_XP.length - 1];
            return baseLevel9Xp + (level - 9) * XP_PER_LEVEL_AFTER_9;
        }
    }

    /**
     * Calculates XP required to go from current level to next level.
     */
    public static long getXpNeededForNextLevelFromStart(long level) {
        long currentLevelStart = getXpRequiredForLevel(level);
        long nextLevelStart = getXpRequiredForLevel(level + 1);
        return nextLevelStart - currentLevelStart;
    }

    /**
     * Calculates XP progress within the current level (e.g. 5 / 20 XP).
     */
    public static int calculateCurrentXpInLevel(long coinsSpent) {
        if (coinsSpent < 0) coinsSpent = 0;
        long totalXp = calculateTotalXp(coinsSpent);
        long currentLevel = calculateLevel(coinsSpent);
        long startXp = getXpRequiredForLevel(currentLevel);
        long xpInLevel = totalXp - startXp;
        return (int) Math.max(0, xpInLevel);
    }

    /**
     * Calculates coins spent within the current level.
     */
    public static long calculateCoinsInCurrentLevel(long coinsSpent) {
        if (coinsSpent < 0) coinsSpent = 0;
        long currentLevel = calculateLevel(coinsSpent);
        long startXp = getXpRequiredForLevel(currentLevel);
        long startCoins = startXp * COINS_PER_XP;
        return Math.max(0, coinsSpent - startCoins);
    }

    /**
     * Calculates progress percentage (0 - 100) within current level for ProgressBar.
     */
    public static int calculateXpPercentageInLevel(long coinsSpent) {
        if (coinsSpent < 0) coinsSpent = 0;
        long totalXp = calculateTotalXp(coinsSpent);
        long currentLevel = calculateLevel(coinsSpent);
        long startXp = getXpRequiredForLevel(currentLevel);
        long nextXp = getXpRequiredForLevel(currentLevel + 1);
        long totalNeededInLevel = nextXp - startXp;
        if (totalNeededInLevel <= 0) return 0;
        long xpInLevel = totalXp - startXp;
        return (int) Math.min(100, Math.max(0, (xpInLevel * 100) / totalNeededInLevel));
    }

    /**
     * Calculates coins needed to reach the next level.
     */
    public static long getCoinsNeededForNextLevel(long coinsSpent) {
        if (coinsSpent < 0) coinsSpent = 0;
        long currentLevel = calculateLevel(coinsSpent);
        long nextXp = getXpRequiredForLevel(currentLevel + 1);
        long nextLevelCoins = nextXp * COINS_PER_XP;
        return Math.max(0, nextLevelCoins - coinsSpent);
    }

    public static String getLevelBadgeText(long level) {
        return "Lv." + level;
    }

    public static int getLevelBadgeDrawable(long level) {
        if (level < 10) {
            return R.drawable.bg_level_badge_cyan;
        } else if (level < 25) {
            return R.drawable.bg_level_badge_purple;
        } else if (level < 50) {
            return R.drawable.bg_level_badge_pink;
        } else {
            return R.drawable.bg_level_badge_gold;
        }
    }
}
