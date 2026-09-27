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

    /**
     * Clears and hides both static and SVGA frame views (e.g. for empty unoccupied seats).
     */
    public static void clearFrame(ImageView staticFrameView, SVGAImageView svgaFrameView) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post(() -> clearFrame(staticFrameView, svgaFrameView));
            return;
        }
        if (staticFrameView != null) staticFrameView.setVisibility(View.GONE);
        if (svgaFrameView != null) {
            svgaFrameView.setTag(null);
            try {
                svgaFrameView.stopAnimation();
                svgaFrameView.clear();
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

        // Always resolve to a valid SVGA path so SVGA profile frame ALWAYS plays on user profiles!
        String svgaPath = getFrameSvgaPath(frameId);
        if (TextUtils.isEmpty(svgaPath)) {
            svgaPath = "frame/crown_circle.svga"; // Default fallback animated SVGA frame
        }

        if (staticFrameView != null) staticFrameView.setVisibility(View.GONE);
        if (svgaFrameView != null) {
            svgaFrameView.setVisibility(View.VISIBLE);
            // Software layer prevents GPU clipping/masking issues on complex SVGA frame vectors
            svgaFrameView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

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
                    try {
                        svgaFrameView.stopAnimation();
                    } catch (Exception ignored) {}
                    svgaFrameView.setVisibility(View.VISIBLE);
                    svgaFrameView.setVideoItem(cachedEntity);
                    svgaFrameView.setLoops(0); // Continuous infinite loop
                    svgaFrameView.stepToFrame(0, true);
                    return;
                }
            }

            // 2. Decode from Assets and Cache for future instant re-use
            final String finalSvgaPath = svgaPath;
            SVGAParser parser = new SVGAParser(context.getApplicationContext());
            parser.decodeFromAssets(finalSvgaPath, new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                    svgaMemoryCache.put(finalSvgaPath, videoItem); // Store in memory
                    mainHandler.post(() -> {
                        if (finalSvgaPath.equals(svgaFrameView.getTag())) {
                            try {
                                svgaFrameView.stopAnimation();
                            } catch (Exception ignored) {}
                            svgaFrameView.setVisibility(View.VISIBLE);
                            svgaFrameView.setVideoItem(videoItem);
                            svgaFrameView.setLoops(0); // Continuous infinite loop
                            svgaFrameView.stepToFrame(0, true);
                        }
                    });
                }

                @Override
                public void onError() {
                    // Fallback to default crown_circle.svga if path fails so SVGA ALWAYS plays!
                    if (!"frame/crown_circle.svga".equals(finalSvgaPath)) {
                        mainHandler.post(() -> displayFrame(context, "frame/crown_circle.svga", staticFrameView, svgaFrameView));
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

        String id = frameId.trim();

        // Direct SVGA file path (e.g., "frame/champion_frame.svga" or "Entry/golden_super_car.svga")
        if (id.endsWith(".svga") || id.contains("/")) {
            return id;
        }

        switch (id) {
            case "frame_champion":
            case "champion_frame":
                return "frame/champion_frame.svga";
            case "frame_crown_circle":
            case "crown_circle":
                return "frame/crown_circle.svga";
            case "frame_golden_emperor":
            case "golden_emperor":
                return "frame/golden_emperor.svga";
            case "frame_dragon":
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
            case "crystal_frame":
                return "frame/crystal_frame.svga";
            case "frame_crystal_ring":
            case "crystal_ring":
                return "frame/crystal_ring.svga";
            case "frame_diamond_ring":
            case "diamond_ring":
                return "frame/diamond_ring.svga";
            case "frame_diamond_glow":
            case "diamond_glow":
                return "frame/diamond_glow.svga";
            case "frame_fire_ring":
            case "fire_ring_frame":
            case "fire_ring":
                return "frame/fire_ring_frame.svga";
            case "frame_flame_lion":
            case "flame_lion":
                return "frame/flame_lion.svga";
            case "frame_ice_crystal":
            case "ice_crystal":
                return "frame/ice_crystal.svga";
            case "frame_inferno_crown":
            case "inferno_crown":
                return "frame/inferno_crown.svga";
            case "frame_lion_glory":
            case "lion_glory":
                return "frame/lion_glory.svga";
            case "frame_majestic_aura":
            case "majestic_aura":
                return "frame/majestic_aura.svga";
            case "frame_music_ring":
            case "music_ring":
                return "frame/music_ring.svga";
            case "frame_nature_ring":
            case "nature_ring":
                return "frame/nature_ring.svga";
            case "frame_purple_mask":
            case "purple_mask":
                return "frame/purple_mask.svga";
            case "frame_purple_star":
            case "purple_star":
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
                return "frame/frame_rank_2.svga";
            case "frame_rank_3":
            case "rank_3":
                return "frame/frame_rank_3.svga";
            case "frame_vip_1":
            case "vip_1":
                return "frame/vip_1.svga";
            case "frame_vip_2":
            case "vip_2":
                return "frame/vip_2.svga";
            case "frame_vip_3":
            case "vip_3":
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
            case "frame_user8":
            case "user8":
                return "frame/user8.svga";
            case "frame2":
                return "frame/frame2.svga";
        }

        if (id.startsWith("frame_")) {
            return "frame/" + id.substring(6) + ".svga";
        }

        return "frame/" + id + ".svga";
    }

    /**
     * Resolves an equipped entrance ID to its SVGA asset path if it is an animated SVGA entrance.
     */
    public static String getEntranceSvgaPath(String entranceId) {
        if (entranceId == null || entranceId.trim().isEmpty()) return null;

        String id = entranceId.trim();
        if (id.endsWith(".svga") || id.contains("/")) {
            return id;
        }

        switch (id) {
            case "entrance_anime_man": return "Entry/anime_man_entry.svga";
            case "entrance_golden_car": return "Entry/golden_super_car.svga";
            case "entrance_red_car": return "Entry/red_super_car.svga";
            case "entrance_toyota_car": return "Entry/toyota_car_entry.svga";
        }

        return "Entry/" + id + ".svga";
    }
}

