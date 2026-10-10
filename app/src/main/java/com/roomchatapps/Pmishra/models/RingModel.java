package com.roomchatapps.Pmishra.models;

public class RingModel {

    private String id;
    private String name;
    private int iconRes;
    private String svgaPath;
    private long priceCoins;
    private boolean isSelected;

    public RingModel(String id, String name, int iconRes, String svgaPath, long priceCoins, boolean isSelected) {
        this.id = id;
        this.name = name;
        this.iconRes = iconRes;
        this.svgaPath = svgaPath;
        this.priceCoins = priceCoins;
        this.isSelected = isSelected;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getIconRes() {
        return iconRes;
    }

    public String getSvgaPath() {
        return svgaPath;
    }

    public long getPriceCoins() {
        return priceCoins;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
