package com.roomchatapps.Pmishra.utils;

import android.content.Context;
import com.roomchatapps.Pmishra.R;

public class FrameUtils {

    /**
     * Synchronously resolves a frame item ID or drawable resource name to a local drawable resource ID.
     * Returns 0 if no frame is equipped.
     */
    public static int getFrameDrawableRes(Context context, String frameId) {
        if (frameId == null || frameId.trim().isEmpty()) {
            return 0;
        }

        switch (frameId.trim()) {
            case "frame_default_neon":
                return R.drawable._1000092519_removebg_preview;
            case "frame_royal_gold_banner":
                return R.drawable._1000092517_removebg_preview;
            case "frame_mystic_aura_banner":
                return R.drawable._1000092518_removebg_preview;
            case "frame_vibrant_banner":
                return R.drawable._1000092519_removebg_preview;
            case "frame_cyber_yellow":
                return R.drawable._1000092462_removebg_preview;
            case "frame_neon_green":
                return R.drawable._1000092463_removebg_preview;
            case "frame_mystic_purple":
                return R.drawable._1000092464_removebg_preview;
            case "frame_hot_pink":
                return R.drawable._1000092465_removebg_preview;
            case "frame_aqua_blue":
                return R.drawable._1000092466_removebg_preview;
            case "frame_golden_royal":
                return R.drawable._1000092467_removebg_preview;
            case "frame_diamond_glint":
                return R.drawable._1000092469_removebg_preview;
            case "frame_ultimate_fire":
                return R.drawable._1000092470_removebg_preview;
        }

        if (context != null) {
            try {
                int resId = context.getResources().getIdentifier(frameId.trim(), "drawable", context.getPackageName());
                if (resId != 0) return resId;
            } catch (Exception ignored) {}
        }

        return 0;
    }
}
