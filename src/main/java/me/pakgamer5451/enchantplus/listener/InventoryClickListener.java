package me.pakgamer5451.enchantplus.listener;

import me.pakgamer5451.enchantplus.EnchantPlus;
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

        if (!isValidItemType(clicked.getType(), data.itemType)) {
            player.sendMessage(ChatColor.RED + "You can only apply this enchant to: " + ChatColor.YELLOW + data.itemType);
            event.setCancelled(true);
            return;
        }

        List<String> existingEnchants = EnchantUtils.getAllEnchantIds(clicked);

        if (existingEnchants.contains(enchantId)) {
            player.sendMessage(ChatColor.YELLOW + "This item already has " + data.name + ".");
            event.setCancelled(true);
            return;
        }

        // Check if trying to apply veinminer and forgedtouch on same item
        if (conflictingEnchants.contains(enchantId.toLowerCase())) {
            for (String existing : existingEnchants) {
                if (conflictingEnchants.contains(existing.toLowerCase())) {
                    player.sendMessage(ChatColor.RED + "You cannot apply " + enchantId + " on an item that already has " + existing + ".");
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // Check general incompatibility
        if (!EnchantUtils.canApplyEnchant(clicked, enchantId)) {
            player.sendMessage(ChatColor.RED + "This enchant conflicts with another enchantment on this item.");
            event.setCancelled(true);
            return;
        }

        EnchantUtils.addEnchant(clicked, enchantId);
        player.sendMessage(ChatColor.GREEN + "Successfully applied " + ChatColor.LIGHT_PURPLE + data.name + ChatColor.GREEN + " to your item!");

        if (player.getGameMode() != GameMode.CREATIVE) {
            cursor.setAmount(cursor.getAmount() - 1);
        }

        player.setItemOnCursor(cursor.getAmount() > 0 ? cursor : null);
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack cursor = player.getItemOnCursor();
        if (cursor == null || cursor.getType() != Material.ENCHANTED_BOOK || !cursor.hasItemMeta()) return;

        ItemMeta bookMeta = cursor.getItemMeta();
        PersistentDataContainer container = bookMeta.getPersistentDataContainer();
        if (!container.has(enchantKey, PersistentDataType.STRING)) return;

        String enchantId = container.get(enchantKey, PersistentDataType.STRING);
        if (enchantId == null) return;

        EnchantSpinManager.EnchantData data = EnchantSpinManager.getEnchantData(enchantId);
        if (data == null) return;

        for (int slot : event.getRawSlots()) {
            ItemStack target = event.getView().getItem(slot);
            if (target == null || target.getType() == Material.AIR) continue;

            if (!isValidItemType(target.getType(), data.itemType)) {
                player.sendMessage(ChatColor.RED + "You can only apply this enchant to: " + ChatColor.YELLOW + data.itemType);
                event.setCancelled(true);
                return;
            }

            List<String> existingEnchants = EnchantUtils.getAllEnchantIds(target);

            if (existingEnchants.contains(enchantId)) {
                player.sendMessage(ChatColor.YELLOW + "This item already has " + data.name + ".");
                event.setCancelled(true);
                return;
            }

            // Check if trying to apply veinminer and forgedtouch on same item
            if (conflictingEnchants.contains(enchantId.toLowerCase())) {
                for (String existing : existingEnchants) {
                    if (conflictingEnchants.contains(existing.toLowerCase())) {
                        player.sendMessage(ChatColor.RED + "You cannot apply " + enchantId + " on an item that already has " + existing + ".");
                        event.setCancelled(true);
                        return;
                    }
                }
            }

            if (!EnchantUtils.canApplyEnchant(target, enchantId)) {
                player.sendMessage(ChatColor.RED + "This enchant conflicts with another enchantment on this item.");
                event.setCancelled(true);
                return;
            }

            EnchantUtils.addEnchant(target, enchantId);
            player.sendMessage(ChatColor.GREEN + "Successfully applied " + ChatColor.LIGHT_PURPLE + data.name + ChatColor.GREEN + " to your item!");

            if (player.getGameMode() != GameMode.CREATIVE) {
                cursor.setAmount(cursor.getAmount() - 1);
            }

            player.setItemOnCursor(cursor.getAmount() > 0 ? cursor : null);
            event.setCancelled(true);
            return;
        }
    }

    private boolean isValidItemType(String itemName, String typeString) {
        String[] validTypes = typeString.toUpperCase().replace(" ", "").split(",");
        for (String type : validTypes) {
            if (
                itemName.endsWith(type) || itemName.equals(type) ||
                (type.equals("TOOLS") && (itemName.contains("PICKAXE") || itemName.contains("AXE") || itemName.contains("SHOVEL") || itemName.contains("HOE"))) ||
                (type.equals("WEAPONS") && (itemName.contains("SWORD") || itemName.contains("TRIDENT") || itemName.contains("BOW") || itemName.contains("CROSSBOW"))) ||
                (type.equals("ARMOR") && (itemName.contains("HELMET") || itemName.contains("CHESTPLATE") || itemName.contains("LEGGINGS") || itemName.contains("BOOTS"))) ||
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
}
