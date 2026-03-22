package me.pakgamer5451.enchantplus.util;

import me.pakgamer5451.enchantplus.EnchantPlus;
import me.pakgamer5451.enchantplus.spin.EnchantSpinManager;
import me.pakgamer5451.enchantplus.spin.EnchantSpinManager.EnchantData;
import me.pakgamer5451.enchantplus.spin.Rarity;
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
    private static final NamespacedKey ENCHANT_LEVELS_KEY = new NamespacedKey(EnchantPlus.getInstance(), "enchant_levels");
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
        if (player == null || item == null || item.getType() == Material.AIR) return false;
        String typeName = item.getType().name();
        
        if (typeName.contains("HELMET"))     return item.equals(player.getInventory().getHelmet());
        if (typeName.contains("CHESTPLATE")) return item.equals(player.getInventory().getChestplate());
        if (typeName.contains("LEGGINGS"))   return item.equals(player.getInventory().getLeggings());
        if (typeName.contains("BOOTS"))      return item.equals(player.getInventory().getBoots());
        
        // Default: must be in main hand (tools, weapons)
        return item.equals(player.getInventory().getItemInMainHand());
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
                    int level = getEnchantLevel(item, id);
                    String levelStr = level > 1 ? " " + toRoman(level) : "";
                    String line = "§7" + data.name + levelStr + " §8[" + data.rarity.display() + "]";
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

    private static String toRoman(int level) {
        return switch (level) {
            case 1 -> "I"; case 2 -> "II"; case 3 -> "III";
            case 4 -> "IV"; case 5 -> "V"; default -> String.valueOf(level);
        };
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

        // Clean up level entry
        setEnchantLevel(item, enchantId, 1);

        item.setItemMeta(meta);
        updateEnchantLore(item);
    }

    // Get the level of a specific enchant on an item (returns 1 if not leveled)
    public static int getEnchantLevel(ItemStack item, String enchantId) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return 1;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        String stored = pdc.get(ENCHANT_LEVELS_KEY, PersistentDataType.STRING);
        if (stored == null || stored.isEmpty()) return 1;
        for (String entry : stored.split(";")) {
            String[] parts = entry.split(":");
            if (parts.length == 2 && parts[0].equalsIgnoreCase(enchantId)) {
                try { return Integer.parseInt(parts[1]); } catch (NumberFormatException e) { return 1; }
            }
        }
        return 1;
    }

    // Set or update the level of an enchant on an item
    public static void setEnchantLevel(ItemStack item, String enchantId, int level) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String stored = pdc.getOrDefault(ENCHANT_LEVELS_KEY, PersistentDataType.STRING, "");
        
        // Build new string, replacing existing entry if present
        StringBuilder result = new StringBuilder();
        boolean found = false;
        if (!stored.isEmpty()) {
            for (String entry : stored.split(";")) {
                String[] parts = entry.split(":");
                if (parts.length == 2 && parts[0].equalsIgnoreCase(enchantId)) {
                    if (level > 1) {
                        if (result.length() > 0) result.append(";");
                        result.append(enchantId.toLowerCase()).append(":").append(level);
                    }
                    found = true;
                } else {
                    if (result.length() > 0) result.append(";");
                    result.append(entry);
                }
            }
        }
        if (!found && level > 1) {
            if (result.length() > 0) result.append(";");
            result.append(enchantId.toLowerCase()).append(":").append(level);
        }
        
        if (result.length() > 0) {
            pdc.set(ENCHANT_LEVELS_KEY, PersistentDataType.STRING, result.toString());
        } else {
            pdc.remove(ENCHANT_LEVELS_KEY);
        }
        item.setItemMeta(meta);
    }

    // Get book level from a book ItemStack
    public static int getBookLevel(ItemStack book) {
        if (book == null || !book.hasItemMeta()) return 1;
        PersistentDataContainer pdc = book.getItemMeta().getPersistentDataContainer();
        NamespacedKey levelKey = new NamespacedKey(EnchantPlus.getInstance(), "custom_enchant_level");
        return pdc.getOrDefault(levelKey, PersistentDataType.INTEGER, 1);
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
}