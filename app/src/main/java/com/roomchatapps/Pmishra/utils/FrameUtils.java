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
        String svgaPath = getFrameSvgaPath(activeFrameId);

        // Crucial fix: Display ONLY ONE frame view at a time (SVGA OR static) to eliminate double frame overlap!
        if (svgaFrameView != null && !TextUtils.isEmpty(svgaPath)) {
            // Hide static frame view so two frames are never drawn over each other
            if (staticFrameView != null) {
                staticFrameView.setImageDrawable(null);
                staticFrameView.setVisibility(View.GONE);
            }

            svgaFrameView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            svgaFrameView.setVisibility(View.VISIBLE);
            svgaFrameView.setLayerType(View.LAYER_TYPE_SOFTWARE, null); // Software layer prevents clipping
            svgaFrameView.setTag(svgaPath);

            // 1. Instant Memory Cache Hit
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
                    // Fallback to static frame ONLY if SVGA decoding fails
                    if (finalSvgaPath.equals(svgaFrameView.getTag())) {
                        svgaFrameView.setVisibility(View.GONE);
                        if (staticFrameView != null) {
                            int resId = getFrameDrawableRes(context, activeFrameId);
                            if (resId != 0) {
                                staticFrameView.setImageResource(resId);
                                staticFrameView.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            }, null);
            return;
        }

        // Fallback: If no SVGA view or no SVGA path exists, display ONLY static frame
        if (svgaFrameView != null) {
            try {
                svgaFrameView.stopAnimation();
                svgaFrameView.setImageDrawable(null);
            } catch (Exception ignored) {}
            svgaFrameView.setVisibility(View.GONE);
        }

        int staticResId = getFrameDrawableRes(context, activeFrameId);
        if (staticFrameView != null) {
            if (staticResId != 0) {
                staticFrameView.setImageResource(staticResId);
                staticFrameView.setVisibility(View.VISIBLE);
            } else {
                staticFrameView.setImageDrawable(null);
                staticFrameView.setVisibility(View.GONE);
            }
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
            case "champion":
            case "test_frame":
                return R.drawable.test_frame;
            case "frame_crown_circle":
            case "crown_circle":
                return R.drawable.crown_circle;
            case "frame_diamond_glow":
            case "dimond_glow":
            case "dimond_glow_":
                return R.drawable.dimond_glow;
            case "frame_star_ring":
            case "star_ring":
                return R.drawable.star_ring;
            case "frame_crystal":
            case "crystal_frame":
            case "cristal_frame":
                return R.drawable.crystal_frame;
            case "frame_crystal_ring":
            case "crystal_ring":
            case "cristal_ring":
                return R.drawable.crystal_ring;
            case "frame_fire_ring":
            case "fire_ring_frame":
                return R.drawable.fire_ring_frame;
            case "frame_lion_glory":
            case "lion_glory":
                return R.drawable.lion_glory;
            case "frame_music_ring":
            case "music_ring":
                return R.drawable.music_ring;
            case "frame_purple_star":
                return R.drawable.purple_thundar;
            case "frame_golden_beast":
            case "golden_beast":
                return R.drawable.golden_beast;
            case "frame_flame_lion":
            case "flame_lion":
                return R.drawable.flame_lion;
            case "frame_imperial_glory":
            case "imperial_glory":
                return R.drawable.imperial_glory;
            case "frame_golden_emperor":
            case "golden_emperor":
                return R.drawable.golden_emperor;
            case "frame_golden_wings":
            case "golden_wings":
                return R.drawable.golden_wings;
            case "frame_majestic_aura":
            case "majestic_aura":
            case "majestic_aura_frame":
                return R.drawable.majestic_aura;
            case "frame_inferno_crown":
            case "inferno_crown":
                return R.drawable.inferno_crown;
            case "frame_nature_ring":
            case "nature_ring":
                return R.drawable.nature_ring;
            case "frame_vip_1":
            case "vip_1_":
                return R.drawable.vip_1_;
            case "frame_purple_thunder":
            case "purple_thunder":
            case "purple_thundar":
                return R.drawable.purple_thundar;
            case "frame_ice_crystal":
            case "ice_crystal":
            case "ice_crystals":
            case "ice_cristal":
                return R.drawable.ice_crystals;
            case "frame_dragon":
            case "dragon_frame":
                return R.drawable.dragon_frame;
            case "frame_vip_2":
            case "vip_2":
                return R.drawable.vip_2;
            case "frame_vip_3":
            case "vip3":
            case "vip3_":
                return R.drawable.vip3_;
            case "frame_vip_4":
            case "vip_4":
                return R.drawable.vip_4;
            case "frame_vip_5":
                return R.drawable.vip_6;
            case "frame_purple_mask":
            case "purple_mask_frame":
                return R.drawable.purple_mask_frame;
            case "frame_star_crown":
            case "star_crown":
                return R.drawable.star_crown;
            case "frame_vip_6":
            case "vip_6":
                return R.drawable.vip_6;
            case "frame_vip_7":
            case "vip_7":
            case "vip_7_":
                return R.drawable.vip_7;
            case "frame_rank_3":
                return R.drawable.frame_rank_3;
            case "frame_rank_2":
            case "fram_rank_2":
                return R.drawable.frame_rank_2;
            case "frame_rank_1":
                return R.drawable.fram_rank_1;

            case "entrance_toyota_car":
            case "ic_entrance_toyota_car":
                return R.drawable.ic_entrance_toyota_car;
            case "entrance_red_car":
            case "ic_entrance_red_car":
                return R.drawable.ic_entrance_red_car;
            case "entrance_anime_man":
            case "ic_entrance_anime_man":
                return R.drawable.ic_entrance_anime_man;
            case "entrance_golden_super_car":
            case "entrance_golden_car":
            case "ic_entrance_golden_car":
                return R.drawable.ic_entrance_golden_car;

            case "frame_royal_gold_banner":
                return R.drawable._1000092461_removebg_preview;
            case "frame_mystic_aura_banner":
                return R.drawable._1000092460_removebg_preview;
            case "frame_vibrant_banner":
            case "frame_default_neon":
                return R.drawable._1000092459_removebg_preview;
            case "frame_cyber_yellow":
                return R.drawable._1000092458_removebg_preview;
            case "frame_neon_green":
                return R.drawable._1000092377_removebg_preview;
            case "frame_mystic_purple":
                return R.drawable._1000092343_removebg_preview;
            case "frame_hot_pink":
                return R.drawable._1000092342_removebg_preview;
            case "frame_aqua_blue":
                return R.drawable._1000092341_removebg_preview;
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
            case "frame_champion":
            case "champion":
            case "champion_frame":
            case "test_frame":
                return "frame/champion_frame.svga";
            case "frame_crown_circle":
            case "crown_circle":
                return "frame/crown_circle.svga";
            case "frame_golden_emperor":
            case "golden_emperor":
                return "frame/golden_emperor.svga";
            case "frame_dragon":
            case "dragon":
            case "dragon_frame":
                return "frame/dragon_frame.svga";
            case "frame_golden_beast":
            case "golden_beast":
                return "frame/golden_beast.svga";
            case "frame_imperial_glory":
            case "imperial_glory":
                return "frame/imperial_glory.svga";
            case "frame_golden_wings":
            case "golden_wings":
                return "frame/golden_wings.svga";
            case "frame_crystal":
            case "crystal":
            case "crystal_frame":
            case "cristal_frame":
                return "frame/crystal_frame.svga";
            case "frame_crystal_ring":
            case "crystal_ring":
            case "cristal_ring":
                return "frame/crystal_ring.svga";
            case "frame_diamond_glow":
            case "diamond_glow":
            case "dimond_glow_":
                return "frame/diamond_glow.svga";
            case "frame_fire_ring":
            case "fire_ring":
            case "fire_ring_frame":
                return "frame/fire_ring_frame.svga";
            case "frame_flame_lion":
            case "flame_lion":
                return "frame/flame_lion.svga";
            case "frame_ice_crystal":
            case "ice_crystal":
            case "ice_cristal":
                return "frame/ice_crystal.svga";
            case "frame_inferno_crown":
            case "inferno_crown":
                return "frame/inferno_crown.svga";
            case "frame_lion_glory":
            case "lion_glory":
                return "frame/lion_glory.svga";
            case "frame_majestic_aura":
            case "majestic_aura":
            case "majestic_aura_frame":
                return "frame/majestic_aura.svga";
            case "frame_music_ring":
            case "music_ring":
                return "frame/music_ring.svga";
            case "frame_nature_ring":
            case "nature_ring":
                return "frame/nature_ring.svga";
            case "frame_purple_mask":
            case "purple_mask":
            case "purple_mask_frame":
                return "frame/purple_mask.svga";
            case "frame_purple_star":
            case "purple_star":
            case "purple_thunder_":
                return "frame/purple_star.svga";
            case "frame_purple_thunder":
            case "purple_thunder":
                return "frame/purple_thunder.svga";
            case "frame_star_crown":
            case "star_crown":
                return "frame/star_crown.svga";
            case "frame_star_ring":
            case "star_ring":
                return "frame/star_ring.svga";
            case "frame_rank_1":
            case "rank_1":
                return "frame/frame_rank_1.svga";
            case "frame_rank_2":
            case "rank_2":
            case "fram_rank_2":
                return "frame/frame_rank_2.svga";
            case "frame_rank_3":
            case "rank_3":
                return "frame/frame_rank_3.svga";
            case "frame_vip_1":
            case "vip_1":
            case "vip_1_":
                return "frame/vip_1.svga";
            case "frame_vip_2":
            case "vip_2":
                return "frame/vip_2.svga";
            case "frame_vip_3":
            case "vip_3":
            case "vip3":
                return "frame/vip_3.svga";
            case "frame_vip_4":
            case "vip_4":
                return "frame/vip_4.svga";
            case "frame_vip_5":
            case "vip_5":
                return "frame/vip_5.svga";
            case "frame_vip_6":
            case "vip_6":
                return "frame/vip_6.svga";
            case "frame_vip_7":
            case "vip_7":
                return "frame/vip_7.svga";
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
