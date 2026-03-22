package me.pakgamer5451.enchantplus.gui;

import me.pakgamer5451.enchantplus.spin.EnchantSpinManager;
import me.pakgamer5451.enchantplus.spin.EnchantSpinManager.EnchantData;
import me.pakgamer5451.enchantplus.spin.Rarity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class EnchantGalleryGUI implements Listener {

    private static final String GALLERY_TITLE = ChatColor.DARK_PURPLE + "Enchant Gallery";
    private static final String ENCHANTS_TITLE_PREFIX = ChatColor.DARK_PURPLE + "Enchants — ";
    private static final ItemStack DEV_SKULL = buildDevSkull();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!(title.equals(GALLERY_TITLE) || title.startsWith(ENCHANTS_TITLE_PREFIX))) return;

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getRawSlot();

        // Handle gallery main screen clicks
        if (title.equals(GALLERY_TITLE)) {
            if (slot == 49) { // Back button (bottom-right corner)
                openGallery(player); // Re-open same screen (no action needed)
            } else if (slot >= 10 && slot <= 16) { // Rarity selection
                Rarity rarity = getRarityForSlot(slot);
                if (rarity != null) {
                    openRarityBrowser(player, rarity);
                }
            }
            return;
        }

        // Handle rarity browser clicks
        if (title.startsWith(ENCHANTS_TITLE_PREFIX)) {
            if (slot == 49) { // Back arrow
                openGallery(player);
            }
            // All other clicks are cancelled (read-only gallery)
        }
    }

    public static void openGallery(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, GALLERY_TITLE);

        // Rarity selection items
        gui.setItem(10, createRarityItem(Rarity.COMMON));
        gui.setItem(11, createRarityItem(Rarity.RARE));
        gui.setItem(13, createRarityItem(Rarity.EPIC));
        gui.setItem(15, createRarityItem(Rarity.LEGENDARY));
        gui.setItem(16, createRarityItem(Rarity.MYTHIC));

        // Developer skull in bottom-right corner
        gui.setItem(26, makeDevSkull());

        player.openInventory(gui);
    }

    private static ItemStack createRarityItem(Rarity rarity) {
        ItemStack item = new ItemStack(switch (rarity) {
            case COMMON -> Material.WHITE_WOOL;
            case RARE -> Material.BLUE_WOOL;
            case EPIC -> Material.PURPLE_WOOL;
            case LEGENDARY -> Material.ORANGE_WOOL;
            case MYTHIC -> Material.RED_WOOL;
        });

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(rarity.display());
        meta.setLore(List.of(ChatColor.GRAY + "Click to browse " + rarity.display() + ChatColor.GRAY + " enchants."));
        item.setItemMeta(meta);
        return item;
    }

    private static void openRarityBrowser(Player player, Rarity rarity) {
        Inventory gui = Bukkit.createInventory(null, 54, ENCHANTS_TITLE_PREFIX + rarity.display());

        List<EnchantData> enchants = EnchantSpinManager.getEnchantsByRarity(rarity);

        for (int i = 0; i < enchants.size() && i < 54; i++) {
            EnchantData data = enchants.get(i);
            ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
            ItemMeta meta = book.getItemMeta();
            meta.setDisplayName(rarity.colorCode + data.name);
            meta.setLore(List.of(
                ChatColor.GRAY + data.description,
                ChatColor.DARK_GRAY + "Applies to: " + data.itemType,
                rarity.display() + " Enchant"
            ));
            book.setItemMeta(meta);
            gui.setItem(i, book);
        }

        // Back arrow
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.GRAY + "« Back");
        backMeta.setLore(List.of(ChatColor.DARK_GRAY + "Return to rarity select"));
        back.setItemMeta(backMeta);
        gui.setItem(49, back);

        // Developer skull
        gui.setItem(53, makeDevSkull());

        player.openInventory(gui);
    }

    private static ItemStack buildDevSkull() {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer("PAKGAMER5451"));
        meta.setDisplayName(ChatColor.DARK_GRAY + "DEV: " + ChatColor.DARK_PURPLE + "PAKGAMER5451");
        meta.setLore(List.of(ChatColor.GRAY + "Plugin created by " + ChatColor.DARK_PURPLE + "PAKGAMER5451"));
        skull.setItemMeta(meta);
        return skull;
    }

    private static ItemStack makeDevSkull() {
        return DEV_SKULL.clone(); // return a clone so cached item isn't mutated
    }

    private static Rarity getRarityForSlot(int slot) {
        return switch (slot) {
            case 10 -> Rarity.COMMON;
            case 11 -> Rarity.RARE;
            case 13 -> Rarity.EPIC;
            case 15 -> Rarity.LEGENDARY;
            case 16 -> Rarity.MYTHIC;
            default -> null;
        };
    }
}
