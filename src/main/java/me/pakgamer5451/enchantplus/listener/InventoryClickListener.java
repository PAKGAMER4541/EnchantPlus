package me.pakgamer5451.enchantplus.listener;

import me.pakgamer5451.enchantplus.EnchantPlus;
import me.pakgamer5451.enchantplus.enchant.VillagersDealEffect;
import me.pakgamer5451.enchantplus.gui.MainMenuGUI;
import me.pakgamer5451.enchantplus.gui.SpinMenuGUI;
import me.pakgamer5451.enchantplus.spin.EnchantSpinManager;
import me.pakgamer5451.enchantplus.util.EnchantUtils;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import java.util.Set;

import java.util.List;

// ... (keep your existing imports)

public class InventoryClickListener implements Listener {

    private final NamespacedKey enchantKey = new NamespacedKey(EnchantPlus.getInstance(), "custom_enchant");

    private final Set<String> conflictingEnchants = Set.of("veinminer", "forgedtouch");

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        MainMenuGUI.handleClick(event);
        SpinMenuGUI.handleClick(event);

        if (event.getView().getTitle().contains("Spin") || event.getView().getTitle().contains("Enchants")) {
            event.setCancelled(true);
        }

        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack cursor = event.getCursor();
        ItemStack clicked = event.getCurrentItem();

        if (cursor == null || clicked == null || clicked.getType() == Material.AIR) return;
        if (cursor.getType() != Material.ENCHANTED_BOOK || !cursor.hasItemMeta()) return;

        ItemMeta bookMeta = cursor.getItemMeta();
        PersistentDataContainer container = bookMeta.getPersistentDataContainer();
        if (!container.has(enchantKey, PersistentDataType.STRING)) return;

        String enchantId = container.get(enchantKey, PersistentDataType.STRING);
        if (enchantId == null) return;

        EnchantSpinManager.EnchantData data = EnchantSpinManager.getEnchantData(enchantId);
        if (data == null) return;

        // Enhanced validation with better error messages
        if (!isValidItemType(clicked.getType(), data.itemType)) {
            player.sendMessage(ChatColor.RED + "❌ " + ChatColor.LIGHT_PURPLE + data.name + ChatColor.RED + " cannot be applied to " + ChatColor.YELLOW + clicked.getType().name().toLowerCase().replace("_", " "));
            player.sendMessage(ChatColor.GRAY + "This enchant can only be applied to: " + ChatColor.YELLOW + data.itemType.replace("_", " "));
            event.setCancelled(true);
            return;
        }

        List<String> existingEnchants = EnchantUtils.getAllEnchantIds(clicked);

        // Read book level
        int bookLevel = EnchantUtils.getBookLevel(cursor);

        // Get current level on the item
        int currentLevel = EnchantUtils.getEnchantLevel(clicked, enchantId);

        // If item already has this enchant at same or higher level — reject
        if (existingEnchants.contains(enchantId)) {
            if (bookLevel <= currentLevel) {
                player.sendMessage(ChatColor.RED + "❌ This item already has " +
                    ChatColor.LIGHT_PURPLE + data.name +
                    (currentLevel > 1 ? " " + toRoman(currentLevel) : "") +
                    ChatColor.RED + " — cannot downgrade.");
                event.setCancelled(true);
                return;
            }
            // bookLevel > currentLevel: upgrade allowed — fall through to apply
        }

        // Check if trying to apply veinminer and forgedtouch on same item
        if (conflictingEnchants.contains(enchantId.toLowerCase())) {
            for (String existing : existingEnchants) {
                if (conflictingEnchants.contains(existing.toLowerCase())) {
                    player.sendMessage(ChatColor.RED + "❌ Cannot apply " + ChatColor.LIGHT_PURPLE + data.name + ChatColor.RED + " - conflicts with existing enchantment.");
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // Check general incompatibility
        if (!EnchantUtils.canApplyEnchant(clicked, enchantId)) {
            player.sendMessage(ChatColor.RED + "❌ This enchant conflicts with another enchantment on this item.");
            event.setCancelled(true);
            return;
        }

        // Apply: addEnchant handles the enchant ID storage (already exists, just updating level)
        if (!existingEnchants.contains(enchantId)) {
            EnchantUtils.addEnchant(clicked, enchantId); // adds the base enchant
        }
        EnchantUtils.setEnchantLevel(clicked, enchantId, bookLevel); // sets the level
        EnchantUtils.updateEnchantLore(clicked);

        player.sendMessage(ChatColor.GREEN + "✅ Successfully applied " +
            ChatColor.LIGHT_PURPLE + data.name +
            (bookLevel > 1 ? " " + toRoman(bookLevel) : "") +
            ChatColor.GREEN + " to your item!");

        // Lock Villager's Deal potions to prevent further brewing
        if (enchantId.equals("villagers_deal") && clicked.getType() == Material.SPLASH_POTION) {
            VillagersDealEffect.lockPotion(clicked);
        }

        // Consume the book (prevent duplication)
        if (player.getGameMode() != GameMode.CREATIVE) {
            cursor.setAmount(cursor.getAmount() - 1);
        }

        player.setItemOnCursor(cursor.getAmount() > 0 ? cursor : null);
        event.setCancelled(true);
    }

    private boolean isValidItemType(String itemName, String typeString) {
        String[] validTypes = typeString.toUpperCase().replace(" ", "").split(",");
        for (String type : validTypes) {
            if (
                itemName.endsWith(type) || itemName.equals(type) ||
                (type.equals("TOOLS") && (itemName.contains("PICKAXE") || itemName.contains("AXE") || itemName.contains("SHOVEL") || itemName.contains("HOE"))) ||
                (type.equals("WEAPONS") && (itemName.contains("SWORD") || itemName.contains("TRIDENT") || itemName.contains("BOW") || itemName.contains("CROSSBOW"))) ||
                (type.equals("ARMOR") && (itemName.contains("HELMET") || itemName.contains("CHESTPLATE") || itemName.contains("LEGGINGS") || itemName.contains("BOOTS"))) ||
                (type.equals("SPLASH_POTION") && itemName.equals("SPLASH_POTION")) ||
                (type.equals("FISHING_ROD") && itemName.equals("FISHING_ROD")) ||
                (type.equals("ALL") || type.equals("ANY"))
            ) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidItemType(Material material, String typeString) {
        return isValidItemType(material.name(), typeString);
    }

    private static String toRoman(int level) {
        return switch (level) {
            case 1 -> "I"; case 2 -> "II"; case 3 -> "III";
            case 4 -> "IV"; case 5 -> "V"; default -> String.valueOf(level);
        };
    }
}
