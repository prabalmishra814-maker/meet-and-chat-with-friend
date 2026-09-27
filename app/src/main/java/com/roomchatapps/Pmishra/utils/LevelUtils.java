package com.roomchatapps.Pmishra.utils;

import com.roomchatapps.Pmishra.R;

public class LevelUtils {

    // 1 Coin Spent = 1 XP
    public static final long COINS_PER_XP = 1L;

    // Cumulative XP required to reach each level (Index 0 = Level 1 (100 XP), Index 1 = Level 2 (300 XP), etc.)
    // For Level 0: 0 XP (requires 100 XP to reach Level 1)
    private static final long[] LEVEL_THRESHOLDS = {
            100L,      // Level 1: 100 XP (100 coins)
            300L,      // Level 2: 300 XP (300 coins total)
            600L,      // Level 3: 600 XP (600 coins total)
            1000L,     // Level 4: 1,000 XP (1,000 coins total)
            2000L,     // Level 5: 2,000 XP
            3500L,     // Level 6: 3,500 XP
            5500L,     // Level 7: 5,500 XP
            8000L,     // Level 8: 8,000 XP
            11000L     // Level 9: 11,000 XP
    };

    public static final long COINS_PER_LEVEL_AFTER_9 = 10000L;
    public static final long COINS_PER_LEVEL = 10000L;

    /**
     * Calculates user level based on total coins spent.
     * New user (0 coins spent) starts at Level 0.
     */
    public static long calculateLevel(long coinsSpent) {
        if (coinsSpent < 100) return 0;

        for (int i = LEVEL_THRESHOLDS.length - 1; i >= 0; i--) {
            if (coinsSpent >= LEVEL_THRESHOLDS[i]) {
                if (i == LEVEL_THRESHOLDS.length - 1) {
                    // Level 9 and above
                    long extraCoins = coinsSpent - LEVEL_THRESHOLDS[i];
                    return 9 + (extraCoins / COINS_PER_LEVEL_AFTER_9);
                } else {
                    return i + 1;
                }
            }
        }
        return 0;
    }

    /**
     * Total XP is 1:1 with coins spent.
     */
    public static long calculateTotalXp(long coinsSpent) {
        if (coinsSpent < 0) coinsSpent = 0;
        return coinsSpent;
    }

    /**
     * Calculates total XP required to reach the start of a given level.
     * Level 0 starts at 0 XP.
     * Level 1 starts at 100 XP.
     */
    public static long getXpRequiredForLevel(long level) {
        if (level <= 0) return 0L;
        if (level <= LEVEL_THRESHOLDS.length) {
            return LEVEL_THRESHOLDS[(int) level - 1];
        } else {
            long baseLevel9Xp = LEVEL_THRESHOLDS[LEVEL_THRESHOLDS.length - 1];
            return baseLevel9Xp + (level - 9) * COINS_PER_LEVEL_AFTER_9;
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
     * Calculates XP progress within the current level (e.g. 50 / 100 XP).
     */
    public static int calculateCurrentXpInLevel(long coinsSpent) {
        if (coinsSpent < 0) coinsSpent = 0;
        long currentLevel = calculateLevel(coinsSpent);
        long startXp = getXpRequiredForLevel(currentLevel);
        long xpInLevel = coinsSpent - startXp;
        return (int) Math.max(0, xpInLevel);
    }

    /**
     * Calculates progress percentage (0 - 100) within the current level for ProgressBar.
     */
    public static int calculateXpPercentageInLevel(long coinsSpent) {
        if (coinsSpent < 0) coinsSpent = 0;
        long currentLevel = calculateLevel(coinsSpent);
        long startXp = getXpRequiredForLevel(currentLevel);
        long nextXp = getXpRequiredForLevel(currentLevel + 1);
        long totalNeededInLevel = nextXp - startXp;
        if (totalNeededInLevel <= 0) return 0;
        long xpInLevel = coinsSpent - startXp;
        return (int) Math.min(100, Math.max(0, (xpInLevel * 100) / totalNeededInLevel));
    }

    /**
     * Calculates coins needed to reach the next level.
     */
    public static long getCoinsNeededForNextLevel(long coinsSpent) {
        if (coinsSpent < 0) coinsSpent = 0;
        long currentLevel = calculateLevel(coinsSpent);
        long nextXp = getXpRequiredForLevel(currentLevel + 1);
        return Math.max(0, nextXp - coinsSpent);
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
