package me.pakgamer5451.enchantplus.util;

import me.pakgamer5451.enchantplus.EnchantPlus;
import me.pakgamer5451.enchantplus.spin.EnchantSpinManager;
import me.pakgamer5451.enchantplus.spin.EnchantSpinManager.EnchantData;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

/**
 * Utility class to handle custom enchant logic, storage, and lore.
 */
public class EnchantUtils {

    private static final NamespacedKey ENCHANT_KEY = new NamespacedKey(EnchantPlus.getInstance(), "custom_enchants");
    private static final NamespacedKey SOULBOUND_CHARGES_KEY = new NamespacedKey(EnchantPlus.getInstance(), "soulbound_charges");
    private static final NamespacedKey ITEM_UUID_KEY = new NamespacedKey(EnchantPlus.getInstance(), "item_uuid");

    // Enchantments that cannot be combined together on one item
    private static final Set<String> CONFLICTING_MINING_ENCHANTS = Set.of("veinminer", "forgetouch", "midastouch");

    public static boolean hasEnchant(ItemStack item, String enchantId) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return false;
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        String stored = container.get(ENCHANT_KEY, PersistentDataType.STRING);
        return stored != null && Arrays.asList(stored.split(";")).contains(enchantId);
    }

    public static boolean isEnchantActive(Player player, ItemStack item) {
        return player != null && item != null
                && item.getType() != Material.AIR
                && player.getInventory().getItemInMainHand().equals(item);
    }

    /**
     * Prevents application of enchantments that are mutually exclusive.
     */
    public static boolean areEnchantsIncompatible(String enchant1, String enchant2) {
        return CONFLICTING_MINING_ENCHANTS.contains(enchant1.toLowerCase())
                && CONFLICTING_MINING_ENCHANTS.contains(enchant2.toLowerCase())
                && !enchant1.equalsIgnoreCase(enchant2);
    }

    /**
     * Checks whether a given enchant can be safely applied to the item.
     */
    public static boolean canApplyEnchant(ItemStack item, String newEnchantId) {
        for (String existing : getAllEnchantIds(item)) {
            if (areEnchantsIncompatible(newEnchantId, existing)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds a custom enchant to an item.
     */
    public static void addEnchant(ItemStack item, String enchantId) {
        if (item == null || item.getType() == Material.AIR) return;

        if (!canApplyEnchant(item, enchantId)) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        String existing = container.getOrDefault(ENCHANT_KEY, PersistentDataType.STRING, "");
        List<String> ids = new ArrayList<>(Arrays.asList(existing.isEmpty() ? new String[0] : existing.split(";")));

        if (ids.contains(enchantId)) return;

        ids.add(enchantId);
        container.set(ENCHANT_KEY, PersistentDataType.STRING, String.join(";", ids));

        if (enchantId.equalsIgnoreCase("soulbound")) {
            container.set(SOULBOUND_CHARGES_KEY, PersistentDataType.INTEGER, 3);
        }

        item.setItemMeta(meta);
        updateEnchantLore(item);
    }

    public static void updateEnchantLore(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        List<String> lore = new ArrayList<>();

        String stored = container.get(ENCHANT_KEY, PersistentDataType.STRING);
        if (stored != null && !stored.isEmpty()) {
            for (String id : stored.split(";")) {
                EnchantData data = EnchantSpinManager.getEnchantData(id);
                if (data != null) {
                    String line = "§7" + data.name + " §8[" + data.rarity + "]";
                    if (id.equalsIgnoreCase("soulbound")) {
                        int charges = container.getOrDefault(SOULBOUND_CHARGES_KEY, PersistentDataType.INTEGER, 3);
                        line += " §7(" + charges + "x)";
                    }
                    lore.add(line);
                }
            }
        }

        // Removed the problematic lore addition for soulbound charges without enchant
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public static List<String> getAllEnchantIds(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return List.of();
        String stored = item.getItemMeta().getPersistentDataContainer().get(ENCHANT_KEY, PersistentDataType.STRING);
        return (stored != null && !stored.isEmpty()) ? Arrays.asList(stored.split(";")) : List.of();
    }

    public static void removeEnchant(ItemStack item, String enchantId) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String existing = container.getOrDefault(ENCHANT_KEY, PersistentDataType.STRING, "");
        List<String> ids = new ArrayList<>(Arrays.asList(existing.split(";")));

        if (!ids.remove(enchantId)) return;

        container.set(ENCHANT_KEY, PersistentDataType.STRING, String.join(";", ids));

        if (enchantId.equalsIgnoreCase("soulbound")) {
            container.remove(SOULBOUND_CHARGES_KEY);
        }

        item.setItemMeta(meta);
        updateEnchantLore(item);
    }

    public static UUID getOrCreateItemUUID(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return null;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        String existing = container.get(ITEM_UUID_KEY, PersistentDataType.STRING);
        try {
            if (existing != null) return UUID.fromString(existing);
        } catch (IllegalArgumentException ignored) {}

        UUID newId = UUID.randomUUID();
        container.set(ITEM_UUID_KEY, PersistentDataType.STRING, newId.toString());
        item.setItemMeta(meta);
        return newId;
    }

    public static String formatEnchantDisplay(EnchantData data) {
        return data.name + " (" + data.rarity.name() + ")";
    }

    @Deprecated
    public static String getEnchantId(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(ENCHANT_KEY, PersistentDataType.STRING);
    }
}