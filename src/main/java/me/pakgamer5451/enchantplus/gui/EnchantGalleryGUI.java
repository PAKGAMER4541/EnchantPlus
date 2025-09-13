package me.pakgamer5451.enchantplus.gui;

import me.pakgamer5451.enchantplus.spin.EnchantSpinManager;
import me.pakgamer5451.enchantplus.spin.EnchantSpinManager.EnchantData;
import me.pakgamer5451.enchantplus.spin.EnchantSpinManager.Rarity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EnchantGalleryGUI implements Listener {

    private static final String GUI_TITLE = ChatColor.DARK_PURPLE + "Enchant Gallery";

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, GUI_TITLE);

        int index = 0;
        for (Rarity rarity : Rarity.values()) {
            for (EnchantData data : EnchantSpinManager.getEnchantsByRarity(rarity)) {
                if (index >= 54) break;

                ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
                ItemMeta meta = book.getItemMeta();
                meta.setDisplayName(ChatColor.LIGHT_PURPLE + data.name);
                meta.setLore(java.util.Arrays.asList(
                    ChatColor.GRAY + data.description,
                    ChatColor.DARK_GRAY + "Applies to: " + data.itemType,
                    ChatColor.DARK_PURPLE + "Rarity: " + rarity.name()
                ));
                book.setItemMeta(meta);
                gui.setItem(index++, book);
            }
        }

        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(GUI_TITLE)) {
            event.setCancelled(true);
        }
    }
}
