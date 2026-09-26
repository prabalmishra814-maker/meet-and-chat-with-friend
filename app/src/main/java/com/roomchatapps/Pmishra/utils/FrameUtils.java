package com.roomchatapps.Pmishra.utils;

import android.content.Context;
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

    /**
     * Unified, high-performance helper to display a user's equipped frame in any activity or fragment.
     * Uses hardware acceleration, memory caching, and tag-matching to achieve 60 FPS smooth seat switching with zero lag.
     */
    public static void displayFrame(Context context, String frameId, ImageView staticFrameView, SVGAImageView svgaFrameView) {
        if (context == null) return;

        if (TextUtils.isEmpty(frameId)) {
            if (staticFrameView != null) staticFrameView.setVisibility(View.GONE);
            if (svgaFrameView != null) {
                svgaFrameView.setTag(null);
                svgaFrameView.clear();
                svgaFrameView.setVisibility(View.GONE);
            }
            return;
        }

        String svgaPath = getFrameSvgaPath(frameId);
        if (!TextUtils.isEmpty(svgaPath)) {
            if (staticFrameView != null) staticFrameView.setVisibility(View.GONE);
            if (svgaFrameView != null) {
                svgaFrameView.setVisibility(View.VISIBLE);
                svgaFrameView.setLayerType(View.LAYER_TYPE_HARDWARE, null); // Hardware Accelerated GPU Rendering

                // Prevent redundant re-decoding if same frame is already loaded and playing
                Object currentTag = svgaFrameView.getTag();
                if (svgaPath.equals(currentTag) && svgaFrameView.isAnimating()) {
                    return;
                }

                svgaFrameView.setTag(svgaPath);

                // 1. Instant Memory Cache Hit (0ms latency, zero lag)
                if (svgaMemoryCache.containsKey(svgaPath)) {
                    SVGAVideoEntity cachedEntity = svgaMemoryCache.get(svgaPath);
                    if (cachedEntity != null) {
                        svgaFrameView.setVideoItem(cachedEntity);
                        svgaFrameView.startAnimation();
                        return;
                    }
                }

                // 2. Decode from Assets and Cache for future instant re-use
                SVGAParser parser = new SVGAParser(context.getApplicationContext());
                parser.decodeFromAssets(svgaPath, new SVGAParser.ParseCompletion() {
                    @Override
                    public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                        svgaMemoryCache.put(svgaPath, videoItem); // Store in memory
                        if (svgaPath.equals(svgaFrameView.getTag())) {
                            svgaFrameView.setVideoItem(videoItem);
                            svgaFrameView.startAnimation();
                        }
                    }

                    @Override
                    public void onError() {
                        if (svgaPath.equals(svgaFrameView.getTag())) {
                            svgaFrameView.setVisibility(View.GONE);
                            if (staticFrameView != null) {
                                int res = getFrameDrawableRes(context, frameId);
                                if (res != 0) {
                                    staticFrameView.setImageResource(res);
                                    staticFrameView.setVisibility(View.VISIBLE);
                                } else {
                                    staticFrameView.setVisibility(View.GONE);
                                }
                            }
                        }
                    }
                }, null);
            }
        } else {
            if (svgaFrameView != null) {
                svgaFrameView.setTag(null);
                svgaFrameView.clear();
                svgaFrameView.setVisibility(View.GONE);
            }
            if (staticFrameView != null) {
                int res = getFrameDrawableRes(context, frameId);
                if (res != 0) {
                    staticFrameView.setImageResource(res);
                    staticFrameView.setVisibility(View.VISIBLE);
                } else {
                    staticFrameView.setVisibility(View.GONE);
                }
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
            case "frame_diamond_ring": return "frame/diamond_ring.svga";
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
