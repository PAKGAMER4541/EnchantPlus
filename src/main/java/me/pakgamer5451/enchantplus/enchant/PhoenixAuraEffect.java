package me.pakgamer5451.enchantplus.enchant;

import me.pakgamer5451.enchantplus.EnchantPlus;
import me.pakgamer5451.enchantplus.util.EnchantUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class PhoenixAuraEffect implements Listener {

    private static final long COOLDOWN_MS = 24 * 60 * 60 * 1000L; // 24 hours
    private static final String ENCHANT_KEY = "phoenix_aura_cooldown";
    private static final Map<UUID, Integer> savedXP = new HashMap<>();

    private final NamespacedKey cooldownKey = new NamespacedKey(EnchantPlus.getInstance(), ENCHANT_KEY);

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        ItemStack chestplate = player.getInventory().getChestplate();
        if (chestplate == null || !EnchantUtils.hasEnchant(chestplate, "phoenix_aura")) return;
        
        int level = EnchantUtils.getEnchantLevel(chestplate, "phoenix_aura");
        
        // Update cooldown lore (existing behavior)
        updateCooldownLore(chestplate, getCooldown(chestplate));
        
        // Level III: save XP before it drops
        if (level == 3) {
            int totalXP = calculateTotalXP(player.getLevel(), player.getExp());
            savedXP.put(player.getUniqueId(), totalXP);
            event.setDroppedExp(0); // prevent XP orbs from spawning
        }
    }

    @EventHandler
    public void onPlayerResurrect(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack chestplate = player.getInventory().getChestplate();
        if (chestplate == null || !EnchantUtils.hasEnchant(chestplate, "phoenix_aura")) return;

        // Get level for branching behavior
        int level = EnchantUtils.getEnchantLevel(chestplate, "phoenix_aura");

        ItemMeta meta = chestplate.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        long now = System.currentTimeMillis();
        long lastUsed = container.has(cooldownKey, PersistentDataType.LONG)
                ? container.get(cooldownKey, PersistentDataType.LONG)
                : 0;

        long remaining = COOLDOWN_MS - (now - lastUsed);

        if (remaining > 0) {
            // Cooldown still active - allow totem
            return;
        }

        // Phoenix Aura will revive instead of totem
        event.setCancelled(true);

        container.set(cooldownKey, PersistentDataType.LONG, now);
        updateCooldownLore(chestplate, now);
        chestplate.setItemMeta(meta);

        // All levels: full health restore (unchanged)
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
        player.setFoodLevel(20);
        player.setFireTicks(0);
        player.getWorld().strikeLightningEffect(player.getLocation());
        player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1, 1);
        player.sendMessage("§6§lPhoenix Aura §7revived you from death!");

        // Level II+: 5 second invincibility after revive
        if (level >= 2) {
            player.setInvulnerable(true);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline()) {
                        player.setInvulnerable(false);
                        player.sendMessage("§6Phoenix Aura §8» §7Invincibility worn off.");
                    }
                }
            }.runTaskLater(EnchantPlus.getInstance(), 100L); // 5 seconds
        }

        // Level III: keep all XP on death
        if (level == 3) {
            Integer xp = savedXP.remove(player.getUniqueId());
            if (xp != null) {
                player.setTotalExperience(xp); // restores full XP bar
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                player.teleport(player.getLocation());
                player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
            }
        }.runTaskLater(EnchantPlus.getInstance(), 2L);
    }

    // Helper:
    private int calculateTotalXP(int level, float progress) {
        // Approximate total XP from level + progress
        int base = 0;
        for (int i = 0; i < level; i++) {
            base += (i < 16) ? (2 * i + 7) : (i < 31) ? (5 * i - 38) : (9 * i - 158);
        }
        return base + Math.round(progress * xpToNextLevel(level));
    }

    private int xpToNextLevel(int level) {
        if (level < 16) return 2 * level + 7;
        if (level < 31) return 5 * level - 38;
        return 9 * level - 158;
    }

    // Static method for PlayerQuitListener cleanup
    public static void cleanupXP(UUID uuid) {
        savedXP.remove(uuid);
    }

    public static void updateCooldownLore(ItemStack item, long cooldownStart) {
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

        lore.removeIf(line -> ChatColor.stripColor(line).startsWith("Cooldown:"));

        long now = System.currentTimeMillis();
        long end = cooldownStart + COOLDOWN_MS;
        long remaining = end - now;

        if (remaining > 0) {
            lore.add("§7Cooldown: §c" + formatTime(remaining));
        } else {
            lore.add("§7Cooldown: §aReady");
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    private static String formatTime(long ms) {
        long seconds = ms / 1000;
        long minutes = (seconds / 60) % 60;
        long hours = (seconds / 60) / 60;
        return hours + "h " + minutes + "m";
    }

    private long getCooldown(ItemStack item) {
        if (!item.hasItemMeta()) return 0;
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        return container.has(cooldownKey, PersistentDataType.LONG)
                ? container.get(cooldownKey, PersistentDataType.LONG)
                : 0;
    }
}
