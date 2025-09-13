package me.pakgamer5451.enchantplus.enchant;

import me.pakgamer5451.enchantplus.EnchantPlus;
import me.pakgamer5451.enchantplus.util.EnchantUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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

    private final NamespacedKey cooldownKey = new NamespacedKey(EnchantPlus.getInstance(), ENCHANT_KEY);

    @EventHandler
    public void onPlayerResurrect(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack chestplate = player.getInventory().getChestplate();
        if (chestplate == null || !EnchantUtils.hasEnchant(chestplate, "phoenix_aura")) return;

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

        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
        player.setFoodLevel(20);
        player.setFireTicks(0);
        player.getWorld().strikeLightningEffect(player.getLocation());
        player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1, 1);
        player.sendMessage("§6§lPhoenix Aura §7revived you from death!");

        new BukkitRunnable() {
            @Override
            public void run() {
                player.teleport(player.getLocation());
                player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
            }
        }.runTaskLater(EnchantPlus.getInstance(), 2L);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        // Just to prevent normal death from leaving item glitches
        Player player = event.getEntity();
        ItemStack chestplate = player.getInventory().getChestplate();
        if (chestplate != null && EnchantUtils.hasEnchant(chestplate, "phoenix_aura")) {
            updateCooldownLore(chestplate, getCooldown(chestplate));
        }
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
