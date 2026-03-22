package me.pakgamer5451.enchantplus.spin;

/**
 * Defines all rarity tiers for custom enchantments.
 * Used for: spin costs, lore display, broadcast thresholds,
 * drop rate weighting, and any future tier-gated features.
 *
 * To add a new tier: add it here AND update rollEnchant() weights in EnchantSpinManager.
 */
public enum Rarity {

    COMMON   (20,  "§f",     false),
    RARE     (30,  "§9",     false),
    EPIC     (40,  "§5",     false),
    LEGENDARY(60,  "§6",     true),   // broadcast on win
    MYTHIC   (80,  "§c",     true);   // broadcast on win

    public final int xpCost;
    public final String colorCode;
    public final boolean broadcastOnWin;

    Rarity(int xpCost, String colorCode, boolean broadcastOnWin) {
        this.xpCost = xpCost;
        this.colorCode = colorCode;
        this.broadcastOnWin = broadcastOnWin;
    }

    /** Returns the color + name for display in lore/GUI, e.g. "§6LEGENDARY" */
    public String display() {
        return colorCode + name();
    }
}
