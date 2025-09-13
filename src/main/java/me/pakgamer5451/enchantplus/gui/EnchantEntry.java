package me.pakgamer5451.enchantplus.gui;

import org.bukkit.Material;

public class EnchantEntry {
    public final String name;
    public final String description;
    public final Material icon;
    public final String rarity;
    public final String appliesTo;

    public EnchantEntry(String name, String description, Material icon, String rarity, String appliesTo) {
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.rarity = rarity;
        this.appliesTo = appliesTo;
    }
}
