package me.pakgamer5451.enchantplus.manager;

import java.util.*;

public class EnchantManager {

    // Singleton instance
    private static final EnchantManager INSTANCE = new EnchantManager();

    // Tracks owned enchants per player UUID
    private final Map<UUID, Set<String>> ownedEnchantments = new HashMap<>();

    // Private constructor to prevent external instantiation
    private EnchantManager() {}

    // Access point for the singleton
    public static EnchantManager getInstance() {
        return INSTANCE;
    }

    // Adds an enchant to the player's list
    public void addEnchant(UUID player, String enchantName) {
        ownedEnchantments
            .computeIfAbsent(player, k -> new HashSet<>())
            .add(enchantName.toLowerCase());
    }

    // Checks if a player has a specific enchant
    public boolean hasEnchant(UUID player, String enchantName) {
        return ownedEnchantments
            .getOrDefault(player, Collections.emptySet())
            .contains(enchantName.toLowerCase());
    }

    // Gets all enchants a player owns
    public Set<String> getPlayerEnchants(UUID player) {
        return ownedEnchantments
            .getOrDefault(player, Collections.emptySet());
    }

    // Optional: Clear all enchants for a player (for testing/reset purposes)
    public void clearEnchants(UUID player) {
        ownedEnchantments.remove(player);
    }
}
