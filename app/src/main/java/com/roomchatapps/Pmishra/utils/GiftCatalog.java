package com.roomchatapps.Pmishra.utils;

import com.roomchatapps.Pmishra.R;
import com.roomchatapps.Pmishra.adapters.GiftStoreAdapter;

import java.util.ArrayList;
import java.util.List;

// STORE COLLECTION INTEGRATION
public class GiftCatalog {

    public static List<GiftStoreAdapter.GiftStoreItem> getAllGifts() {
        List<GiftStoreAdapter.GiftStoreItem> giftList = new ArrayList<>();

        // 1. Gift Category
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Golden Tea", "gift/golden_tea.svga", R.drawable.gift_golden_tea, 200000, "Gift"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Doraemon Gift", "gift/doraemon_gift.svga", R.drawable.gift_doraemon, 100000, "Gift"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Flag Gift", "gift/rose.svga", R.drawable.room_gift_ic, 100000, "Gift"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Birthday Cake", "gift/birthday_cake.svga", R.drawable.gift_baklava, 250000, "Gift"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Gold Ring", "gift/blue_ring_love.svga", R.drawable.gift_blue_ring, 200000, "Gift"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Umbrella", "gift/umbrella.svga", R.drawable.gift_umbrella, 1300000, "Gift"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Love Pure", "gift/love_pure.svga", R.drawable.gift_love, 500000, "Gift"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Smoke Effect", "gift/smoke.svga", R.drawable.gift_smoke, 300000, "Gift"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Love Gift Box", "gift/love_gift_box.svga", R.drawable.gift_love_gift_box, 2000000, "Gift"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Blue Princess Gown", "gift/blue_princess_gown.svga", R.drawable.gift_blue_gown, 1500000, "Gift"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Makeup Box", "gift/makeup_box.svga", R.drawable.gift_makeup_box, 1000000, "Gift"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Pearls Necklace", "gift/pearls_necklace.svga", R.drawable.gift_pearls_necklace, 600000, "Gift"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Love Fireworks", "gift/love_fireworks.svga", R.drawable.gift_love_fireworks, 500000, "Gift"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Fire Rocket", "gift/fire_rocket.svga", R.drawable.gift_fire_rocket, 99999, "Gift"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Cosmic Float", "gift/cosmic_float.svga", R.drawable.gift_cosmic_float, 4000000, "Gift"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Love Proposal", "gift/love_proposal.svga", R.drawable.gift_love_proposal, 3000000, "Gift"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Love Couple", "gift/love_couple.svga", R.drawable.gift_love_couple, 1500000, "Gift"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Fantasy Castle", "gift/fantasy_castle.svga", R.drawable.gift_fantasy_castle, 1000000, "Gift"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Wedding Hall", "gift/wedding_proposal.svga", R.drawable.gift_wedding_proposal, 500000, "Gift"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Royal Couple", "gift/royal_couple.svga", R.drawable.gift_royal_couple, 1999999, "Gift"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Hassan II Mosque", "gift/hassan_mosque.svga", R.drawable.gift_hassan_mosque, 5000000, "Gift"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Popcorn", "gift/popcorn.svga", R.drawable.gift_popcorn, 200000, "Gift"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Baklava", "gift/baklava.svga", R.drawable.gift_baklava, 100000, "Gift"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Party Popper", "gift/party_popper.svga", R.drawable.gift_party_popper, 290000, "Gift"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Money Stack", "gift/money.svga", R.drawable.gift_money, 99999, "Gift"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Gold Bar", "gift/gold_bar.svga", R.drawable.gift_gold_bar, 500000, "Gift"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Magic Gift", "gift/magic_gift.svga", R.drawable.gift_magic_gift, 300000, "Gift"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Crystal Rose", "gift/crystal_rose.svga", R.drawable.gift_crystal_rose, 1800000, "Gift"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Glass Glow Rose", "gift/glass_glow_rose.svga", R.drawable.gift_glass_glow_rose, 999999, "Gift"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Refrigerator", "gift/refrigerator.svga", R.drawable.gift_refrigerator, 77777, "Gift"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Angel Bride", "gift/angel_bride.svga", R.drawable.gift_angel_bride, 600000, "Gift"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Angel Queen Crown", "gift/angel_queen_crown.svga", R.drawable.gift_angel_queen, 300000, "Gift"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Forever Couple", "gift/forever_couple.svga", R.drawable.gift_forever_couple, 500000, "Gift"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Magic Sword", "gift/magic_sword.svga", R.drawable.gift_magic_sword, 700000, "Gift"));

        // 2. Relationship Category
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Blue Love Ring", "gift/blue_love_ring.svga", R.drawable.gift_blue_ring, 1000000, "Relationship"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Perfume", "gift/parfume.svga", R.drawable.gift_parfume, 1700000, "Relationship"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Love Confession", "gift/love_confession.svga", R.drawable.gift_love_confession, 2000000, "Relationship"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("CP Celebration", "gift/cp_celebration.svga", R.drawable.gift_cp_celebration, 6000000, "Relationship"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Diamond Ring", "gift/diamond_ring_gift.svga", R.drawable.gift_golden_rings, 1500000, "Relationship"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Forever Love", "gift/forever_love.svga", R.drawable.gift_forever_love, 500000, "Relationship"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Wedding Proposal", "gift/wedding_proposal.svga", R.drawable.gift_wedding_proposal, 1200000, "Relationship"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Love City", "gift/love_city.svga", R.drawable.gift_love_city, 3500000, "Relationship"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Royal Banquet", "gift/royal_banquet.svga", R.drawable.gift_royal_banquet, 4500000, "Relationship"));

        // 3. Luxury Category
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Luxury Bag", "gift/luxury_bag.svga", R.drawable.gift_luxury_bag, 5500000, "Luxury"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Church", "gift/church.svga", R.drawable.gift_church, 3500000, "Luxury"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Royal Suit", "gift/royal_suit.svga", R.drawable.gift_royal_suit, 5000000, "Luxury"));
        giftList.add(new GiftStoreAdapter.GiftStoreItem("Floating Castle", "gift/floating_castle.svga", R.drawable.gift_floating_castle, 8000000, "Luxury"));

        return giftList;
    }

    public static GiftStoreAdapter.GiftStoreItem findGiftByName(String rawName) {
        if (rawName == null || rawName.trim().isEmpty()) return null;
        String cleanName = rawName.trim();

        // Direct exact match or regex strip "(x2)", "(Targeted)" etc.
        String baseName = cleanName.replaceAll("\\s*\\(.*?\\)", "").trim();

        for (GiftStoreAdapter.GiftStoreItem item : getAllGifts()) {
            if (item.name.equalsIgnoreCase(cleanName) || item.name.equalsIgnoreCase(baseName)) {
                return item;
            }
        }

        // Substring matching
        for (GiftStoreAdapter.GiftStoreItem item : getAllGifts()) {
            if (baseName.toLowerCase().contains(item.name.toLowerCase()) || item.name.toLowerCase().contains(baseName.toLowerCase())) {
                return item;
            }
        }
        return null;
    }
}
