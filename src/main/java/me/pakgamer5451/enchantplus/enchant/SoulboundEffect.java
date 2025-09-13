package me.pakgamer5451.enchantplus.enchant;

import me.pakgamer5451.enchantplus.EnchantPlus;
import me.pakgamer5451.enchantplus.util.EnchantUtils;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class SoulboundEffect implements Listener {

    private static final NamespacedKey CHARGES_KEY = new NamespacedKey(EnchantPlus.getInstance(), "soulbound_charges");
    private static final String SOULBOUND_ID = "soulbound";
    private final Map<UUID, List<ItemStack>> savedSoulboundItems = new HashMap<>();

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        List<ItemStack> drops = event.getDrops();
        List<ItemStack> saved = new ArrayList<>();

        Iterator<ItemStack> iterator = drops.iterator();
        while (iterator.hasNext()) {
            ItemStack item = iterator.next();
            if (item == null || !item.hasItemMeta()) continue;
            
            // Case-insensitive soulbound check
            boolean hasSoulbound = false;
            for (String enchant : EnchantUtils.getAllEnchantIds(item)) {
                if (enchant.equalsIgnoreCase(SOULBOUND_ID)) {
                    hasSoulbound = true;
                    break;
                }
            }
            if (!hasSoulbound) continue;

            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            int charges = container.getOrDefault(CHARGES_KEY, PersistentDataType.INTEGER, 3);

            if (charges <= 0) continue;

            iterator.remove(); // Remove from death drops
            charges--; // Decrement BEFORE processing

            if (charges <= 0) {
                // Remove enchant using standardized ID
                EnchantUtils.removeEnchant(item, SOULBOUND_ID);
                player.sendMessage(ChatColor.RED + "§cSoulbound has worn off from one of your items.");
            } else {
                // Update charges only if not depleted
                container.set(CHARGES_KEY, PersistentDataType.INTEGER, charges);
                // Update meta immediately
                item.setItemMeta(meta);
            }

            // Always update lore to reflect changes
            EnchantUtils.updateEnchantLore(item);
            saved.add(item);
        }

        if (!saved.isEmpty()) {
            savedSoulboundItems.put(player.getUniqueId(), saved);
        }
    }

    // Rest of the class remains unchanged
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        List<ItemStack> items = savedSoulboundItems.remove(uuid);
        if (items != null) {
            for (ItemStack item : items) {
                player.getInventory().addItem(item);
            }
            player.sendMessage(ChatColor.GOLD + "§eYour soulbound items have been returned to you.");
        }
    }

    public static void initializeCharges(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        
        // Only initialize if soulbound exists
        boolean hasSoulbound = false;
        for (String enchant : EnchantUtils.getAllEnchantIds(item)) {
            if (enchant.equalsIgnoreCase("soulbound")) {
                hasSoulbound = true;
                break;
            }
        }
        if (!hasSoulbound) return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (!container.has(CHARGES_KEY, PersistentDataType.INTEGER)) {
            container.set(CHARGES_KEY, PersistentDataType.INTEGER, 3);
            item.setItemMeta(meta);
        }
    }

    public static int getRemainingCharges(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.getOrDefault(CHARGES_KEY, PersistentDataType.INTEGER, 0);
    }
}