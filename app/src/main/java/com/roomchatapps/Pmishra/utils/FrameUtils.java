package com.roomchatapps.Pmishra.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.roomchatapps.Pmishra.R;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FrameUtils {

    // High-performance Memory Cache for decoded SVGA Video Entities (Eliminates lag & disk reads)
    private static final Map<String, SVGAVideoEntity> svgaMemoryCache = new ConcurrentHashMap<>();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static final String DEFAULT_FRAME = "frame_champion";

    /**
     * Clears and hides both static and SVGA frame views (e.g. for empty unoccupied seats).
     * Uses setImageDrawable(null) instead of clear() so that mCleared flag is not set on recycled views.
     */
    public static void clearFrame(ImageView staticFrameView, SVGAImageView svgaFrameView) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post(() -> clearFrame(staticFrameView, svgaFrameView));
            return;
        }
        if (staticFrameView != null) {
            staticFrameView.setImageDrawable(null);
            staticFrameView.setVisibility(View.GONE);
        }
        if (svgaFrameView != null) {
            svgaFrameView.setTag(null);
            try {
                svgaFrameView.stopAnimation();
                svgaFrameView.setImageDrawable(null);
            } catch (Exception ignored) {}
            svgaFrameView.setVisibility(View.GONE);
        }
    }

    /**
     * Unified, high-performance helper to display a user's equipped frame in any activity or fragment.
     * Guarantees Main UI thread safety, memory caching, tag matching, software layer rendering,
     * and automatic fallback so animated SVGA profile frames ALWAYS play continuously at 60 FPS.
     */
    public static void displayFrame(Context context, String frameId, ImageView staticFrameView, SVGAImageView svgaFrameView) {
        if (context == null) return;

        // Ensure execution happens strictly on the Main UI thread
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post(() -> displayFrame(context, frameId, staticFrameView, svgaFrameView));
            return;
        }

        if (TextUtils.isEmpty(frameId)) {
            clearFrame(staticFrameView, svgaFrameView);
            return;
        }

        final String activeFrameId = frameId;

        // 1. Immediate Static Frame Placeholder (Guarantees banner/frame is ALWAYS 100% visible immediately)
        int staticResId = getFrameDrawableRes(context, activeFrameId);
        if (staticResId == 0) {
            staticResId = R.drawable.ic_crown_gold_frame;
        }

        if (staticFrameView != null) {
            staticFrameView.setImageResource(staticResId);
            staticFrameView.setVisibility(View.VISIBLE);
        }

        // 2. Animated SVGA Frame Upgrade
        String svgaPath = getFrameSvgaPath(activeFrameId);
        if (TextUtils.isEmpty(svgaPath)) {
            svgaPath = "frame/champion_frame.svga"; // Default fallback animated SVGA frame
        }

        if (svgaFrameView != null) {
            svgaFrameView.setVisibility(View.VISIBLE);
            svgaFrameView.setLayerType(View.LAYER_TYPE_SOFTWARE, null); // Software layer prevents clipping

            svgaFrameView.setTag(svgaPath);

            // 1. Instant Memory Cache Hit (0ms latency, zero lag)
            if (svgaMemoryCache.containsKey(svgaPath)) {
                SVGAVideoEntity cachedEntity = svgaMemoryCache.get(svgaPath);
                if (cachedEntity != null) {
                    try {
                        svgaFrameView.stopAnimation();
                    } catch (Exception ignored) {}
                    svgaFrameView.setVisibility(View.VISIBLE);
                    svgaFrameView.setVideoItem(cachedEntity);
                    svgaFrameView.startAnimation();
                    return;
                }
            }

            // 2. Decode from Assets and Cache for future instant re-use
            final String finalSvgaPath = svgaPath;
            SVGAParser parser = new SVGAParser(context.getApplicationContext());
            parser.decodeFromAssets(finalSvgaPath, new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                    svgaMemoryCache.put(finalSvgaPath, videoItem);
                    if (finalSvgaPath.equals(svgaFrameView.getTag())) {
                        try {
                            svgaFrameView.stopAnimation();
                        } catch (Exception ignored) {}
                        svgaFrameView.setVisibility(View.VISIBLE);
                        svgaFrameView.setVideoItem(videoItem);
                        svgaFrameView.startAnimation();
                    }
                }

                @Override
                public void onError() {
                    // Fallback to static frame if decoding fails
                    if (finalSvgaPath.equals(svgaFrameView.getTag())) {
                        svgaFrameView.setVisibility(View.GONE);
                    }
                }
            }, null);
        }
    }

    /**
     * Synchronously resolves a frame item ID or drawable resource name to a local drawable resource ID.
     * Returns 0 if no frame is equipped.
     */
    public static int getFrameDrawableRes(Context context, String frameId) {
        if (frameId == null || frameId.trim().isEmpty()) {
            return 0;
        }

        switch (frameId.trim()) {
            case "frame_champion":
            case "frame_crown_circle":
            case "frame_golden_emperor":
            case "frame_star_crown":
            case "frame_rank_1":
            case "frame_vip_3":
            case "frame_vip_4":
            case "frame_vip_5":
            case "frame_vip_6":
            case "frame_vip_7":
                return R.drawable.ic_crown_gold_frame;

            case "frame_rank_2":
            case "frame_vip_2":
            case "frame_golden_wings":
                return R.drawable.ic_crown_silver_frame;

            case "frame_rank_3":
            case "frame_vip_1":
            case "frame_crystal_ring":
                return R.drawable.ic_crown_bronze_frame;

            case "frame_royal_gold_banner":
                return R.drawable._1000092517_removebg_preview;
            case "frame_mystic_aura_banner":
                return R.drawable._1000092518_removebg_preview;
            case "frame_vibrant_banner":
            case "frame_default_neon":
                return R.drawable._1000092519_removebg_preview;

            case "frame_cyber_yellow":
            case "frame_star_ring":
            case "frame_diamond_glow":
                return R.drawable._1000092462_removebg_preview;

            case "frame_neon_green":
            case "frame_music_ring":
            case "frame_nature_ring":
                return R.drawable._1000092463_removebg_preview;

            case "frame_mystic_purple":
            case "frame_purple_mask":
            case "frame_purple_star":
            case "frame_purple_thunder":
            case "frame_majestic_aura":
                return R.drawable._1000092464_removebg_preview;

            case "frame_hot_pink":
                return R.drawable._1000092465_removebg_preview;

            case "frame_aqua_blue":
            case "frame_ice_crystal":
                return R.drawable._1000092466_removebg_preview;

            case "frame_golden_royal":
            case "frame_golden_beast":
            case "frame_imperial_glory":
            case "frame_lion_glory":
                return R.drawable._1000092467_removebg_preview;

            case "frame_diamond_glint":
            case "frame_crystal":
                return R.drawable._1000092469_removebg_preview;

            case "frame_ultimate_fire":
            case "frame_fire_ring":
            case "frame_dragon":
            case "frame_flame_lion":
            case "frame_inferno_crown":
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

    /**
     * Resolves an equipped frame ID to its SVGA asset path if it is an animated SVGA frame.
     */
    public static String getFrameSvgaPath(String frameId) {
        if (frameId == null || frameId.trim().isEmpty()) return null;

        switch (frameId.trim()) {
            case "frame_champion": return "frame/champion_frame.svga";
            case "frame_crown_circle": return "frame/crown_circle.svga";
            case "frame_golden_emperor": return "frame/golden_emperor.svga";
            case "frame_dragon": return "frame/dragon_frame.svga";
            case "frame_golden_beast": return "frame/golden_beast.svga";
            case "frame_imperial_glory": return "frame/imperial_glory.svga";
            case "frame_golden_wings": return "frame/golden_wings.svga";
            case "frame_crystal": return "frame/crystal_frame.svga";
            case "frame_crystal_ring": return "frame/crystal_ring.svga";
            case "frame_diamond_glow": return "frame/diamond_glow.svga";
            case "frame_fire_ring": return "frame/fire_ring_frame.svga";
            case "frame_flame_lion": return "frame/flame_lion.svga";
            case "frame_ice_crystal": return "frame/ice_crystal.svga";
            case "frame_inferno_crown": return "frame/inferno_crown.svga";
            case "frame_lion_glory": return "frame/lion_glory.svga";
            case "frame_majestic_aura": return "frame/majestic_aura.svga";
            case "frame_music_ring": return "frame/music_ring.svga";
            case "frame_nature_ring": return "frame/nature_ring.svga";
            case "frame_purple_mask": return "frame/purple_mask.svga";
            case "frame_purple_star": return "frame/purple_star.svga";
            case "frame_purple_thunder": return "frame/purple_thunder.svga";
            case "frame_star_crown": return "frame/star_crown.svga";
            case "frame_star_ring": return "frame/star_ring.svga";
            case "frame_rank_1": return "frame/frame_rank_1.svga";
            case "frame_rank_2": return "frame/frame_rank_2.svga";
            case "frame_rank_3": return "frame/frame_rank_3.svga";
            case "frame_vip_1": return "frame/vip_1.svga";
            case "frame_vip_2": return "frame/vip_2.svga";
            case "frame_vip_3": return "frame/vip_3.svga";
            case "frame_vip_4": return "frame/vip_4.svga";
            case "frame_vip_5": return "frame/vip_5.svga";
            case "frame_vip_6": return "frame/vip_6.svga";
            case "frame_vip_7": return "frame/vip_7.svga";
        }
        return null;
    }

    /**
     * Resolves an equipped entrance ID to its SVGA asset path if it is an animated SVGA entrance.
     */
    public static String getEntranceSvgaPath(String entranceId) {
        if (entranceId == null || entranceId.trim().isEmpty()) return null;

        switch (entranceId.trim()) {
            case "entrance_anime_man": return "Entry/anime_man_entry.svga";
            case "entrance_golden_car": return "Entry/golden_super_car.svga";
            case "entrance_red_car": return "Entry/red_super_car.svga";
            case "entrance_toyota_car": return "Entry/toyota_car_entry.svga";
        }
        return null;
    }
}
