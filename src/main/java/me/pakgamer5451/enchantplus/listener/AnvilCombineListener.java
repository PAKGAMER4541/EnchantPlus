package me.pakgamer5451.enchantplus.listener;

import me.pakgamer5451.enchantplus.spin.EnchantSpinManager;
import me.pakgamer5451.enchantplus.util.EnchantUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import me.pakgamer5451.enchantplus.EnchantPlus;

public class AnvilCombineListener implements Listener {

    private final NamespacedKey enchantKey = new NamespacedKey(EnchantPlus.getInstance(), "custom_enchant");
    private final NamespacedKey levelKey = new NamespacedKey(EnchantPlus.getInstance(), "custom_enchant_level");

    @EventHandler
    public void onAnvilPrepare(PrepareAnvilEvent event) {
        AnvilInventory anvil = event.getInventory();
        ItemStack first = anvil.getItem(0);
        ItemStack second = anvil.getItem(1);

        if (first == null || second == null) return;
        if (first.getType() != Material.ENCHANTED_BOOK) return;
        if (second.getType() != Material.ENCHANTED_BOOK) return;
        if (!first.hasItemMeta() || !second.hasItemMeta()) return;

        PersistentDataContainer pdc1 = first.getItemMeta().getPersistentDataContainer();
        PersistentDataContainer pdc2 = second.getItemMeta().getPersistentDataContainer();

        if (!pdc1.has(enchantKey, PersistentDataType.STRING)) return;
        if (!pdc2.has(enchantKey, PersistentDataType.STRING)) return;

        String id1 = pdc1.get(enchantKey, PersistentDataType.STRING);
        String id2 = pdc2.get(enchantKey, PersistentDataType.STRING);

        // Must be same enchant
        if (id1 == null || !id1.equals(id2)) return;

        int level1 = pdc1.getOrDefault(levelKey, PersistentDataType.INTEGER, 1);
        int level2 = pdc2.getOrDefault(levelKey, PersistentDataType.INTEGER, 1);

        // Must be same level
        if (level1 != level2) {
            event.setResult(null); // block combine — different levels can't merge
            return;
        }

        // Max level is 3 — can't combine two level 3 books
        if (level1 >= 3) {
            event.setResult(null);
            return;
        }

        int newLevel = level1 + 1;
        EnchantSpinManager.EnchantData data = EnchantSpinManager.getEnchantData(id1);
        if (data == null) return;

        // Create upgraded book
        ItemStack result = EnchantSpinManager.createEnchantBook(data, newLevel);

        // Block name changes — force the result name to match the book, not player input
        anvil.setRepairCost(1); // small XP cost for combining
        event.setResult(result);
    }
}
