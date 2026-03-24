package me.pakgamer5451.enchantplus.enchant;

import me.pakgamer5451.enchantplus.EnchantPlus;
import me.pakgamer5451.enchantplus.util.EnchantUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SoulboundEffect implements Listener {

    private static final NamespacedKey CHARGES_KEY = new NamespacedKey(EnchantPlus.getInstance(), "soulbound_charges");
    private static final String SOULBOUND_ID = "soulbound";
    private static final Map<UUID, List<ItemStack>> savedSoulboundItems = new HashMap<>();
    private static final File SOULBOUND_FILE = new File(
        EnchantPlus.getInstance().getDataFolder(), "soulbound_pending.yml");

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
            savePending(player.getUniqueId(), saved); // persist to disk
        }
    }

    private static void savePending(UUID uuid, List<ItemStack> items) {
        List<ItemStack> itemsCopy = new ArrayList<>(items);
        Bukkit.getScheduler().runTaskAsynchronously(EnchantPlus.getInstance(), () -> {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(SOULBOUND_FILE);
            yaml.set(uuid.toString(), itemsCopy);
            try { yaml.save(SOULBOUND_FILE); } 
            catch (IOException e) {
                EnchantPlus.getInstance().getLogger().warning("Failed to save soulbound pending: " + e.getMessage());
            }
        });
    }

    private static List<ItemStack> loadPending(UUID uuid) {
        if (!SOULBOUND_FILE.exists()) return null;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(SOULBOUND_FILE);
        if (!yaml.contains(uuid.toString())) return null;
        List<?> raw = yaml.getList(uuid.toString());
        if (raw == null) return null;
        List<ItemStack> items = new ArrayList<>();
        for (Object obj : raw) {
            if (obj instanceof ItemStack) items.add((ItemStack) obj);
        }
        return items.isEmpty() ? null : items;
    }

    private static void removePending(UUID uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(EnchantPlus.getInstance(), () -> {
            if (!SOULBOUND_FILE.exists()) return;
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(SOULBOUND_FILE);
            yaml.set(uuid.toString(), null);
            try { yaml.save(SOULBOUND_FILE); }
            catch (IOException e) {
                EnchantPlus.getInstance().getLogger().warning("Failed to clear soulbound pending: " + e.getMessage());
            }
        });
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        List<ItemStack> items = savedSoulboundItems.remove(uuid);
        if (items == null) {
            // Not in memory — player may have disconnected, check disk
            items = loadPending(uuid);
        }
        if (items != null) {
            removePending(uuid); // clean up disk entry
            for (ItemStack item : items) {
                Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
                for (ItemStack overflow : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), overflow);
                }
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

    // Add this static method so that quit listener can call it
    public static void clearSavedItems(UUID uuid) {
        savedSoulboundItems.remove(uuid);
        // Do NOT call removePending here — disk entry must survive disconnects
    }
}