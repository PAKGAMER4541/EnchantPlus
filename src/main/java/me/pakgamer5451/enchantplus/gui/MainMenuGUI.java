package me.pakgamer5451.enchantplus.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import me.pakgamer5451.enchantplus.gui.SpinMenuGUI;

public class MainMenuGUI implements Listener {

    private static final String GUI_TITLE = "§6EnchantPlus Menu";

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, GUI_TITLE);

        gui.setItem(12, createMenuItem(Material.BOOK, "§bSpin Enchant"));
        gui.setItem(14, createMenuItem(Material.ENCHANTED_BOOK, "§aView Enchants"));
       

        player.openInventory(gui);
    }

    private static ItemStack createMenuItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    public static void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String title = clicked.getItemMeta().getDisplayName();
        switch (title) {
    case "§bSpin Enchant" -> {
        player.closeInventory();
        me.pakgamer5451.enchantplus.gui.SpinMenuGUI.open(player);
    }
    case "§aView Enchants" -> {
    player.closeInventory();
    EnchantGalleryGUI.open(player);
}

    case "§eXP Info" -> player.sendMessage("§7XP required depends on rarity tier.");
}

    }
}
