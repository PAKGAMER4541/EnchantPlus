package me.pakgamer5451.enchantplus.enchant;

import me.pakgamer5451.enchantplus.EnchantPlus;
import me.pakgamer5451.enchantplus.util.ActionBarUtil;
import me.pakgamer5451.enchantplus.util.EnchantUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.UUID;

public class BlazingAuraEffect implements Listener {

    private static final Random random = new Random();
    private static final long COOLDOWN_MS = 5000L;
    private final NamespacedKey cooldownKey = new NamespacedKey(EnchantPlus.getInstance(), "blazing_aura_cooldown");

    @EventHandler
    public void onPlayerDamaged(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player defender)) return;

        ItemStack chestplate = defender.getInventory().getChestplate();
        if (chestplate == null || chestplate.getType() == Material.AIR) return;

        // Ensure it's a chestplate
        if (!chestplate.getType().name().toLowerCase().contains("chestplate")) return;

        // Check for blazing_aura enchant
        if (!EnchantUtils.hasEnchant(chestplate, "blazing_aura")) return;
        if (!EnchantUtils.isEnchantActive(defender, chestplate)) return;

        // Get level for branching behavior
        int level = EnchantUtils.getEnchantLevel(chestplate, "blazing_aura");
        
        double chance  = switch (level) { case 3 -> 0.50; case 2 -> 0.35; default -> 0.25; };
        long cooldownMs = switch (level) { case 3 -> 2000L; case 2 -> 3000L; default -> 5000L; };

        // Check cooldown
        long now = System.currentTimeMillis();
        ItemMeta cooldownMeta = chestplate.getItemMeta();
        PersistentDataContainer pdc = cooldownMeta.getPersistentDataContainer();
        long lastUsed = pdc.getOrDefault(cooldownKey, PersistentDataType.LONG, 0L);
        if (now - lastUsed < cooldownMs) return;
        pdc.set(cooldownKey, PersistentDataType.LONG, now);
        chestplate.setItemMeta(cooldownMeta);

        if (random.nextDouble() > chance) return;

        // Apply effects to attacker
        Entity attacker = event.getDamager();
        if (attacker instanceof LivingEntity living) {
            living.setFireTicks(60); // 3 seconds of fire
            living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 0)); // Slowness I
            
            // Level II+: +1 extra heart direct damage to attacker
            if (level >= 2 && attacker instanceof LivingEntity living2) {
                living2.damage(2.0); // 1 heart (bypasses via normal damage, affected by armor — intentional)
            }
            
            // Level III: nearby mobs also ignited (exclude players to prevent griefing)
            if (level == 3) {
                defender.getWorld().getNearbyEntities(defender.getLocation(), 4, 4, 4).forEach(nearby -> {
                    if (nearby instanceof LivingEntity ne 
                            && !(nearby instanceof Player)   // ← FIX: Exclude players
                            && !ne.equals(defender) 
                            && !ne.equals(attacker)) {
                        ne.setFireTicks(60);
                    }
                });
            }
        }

        // Particles at chest level
        Location loc = defender.getLocation().add(0, 1.2, 0);
        defender.getWorld().spawnParticle(Particle.FLAME, loc, 40, 0.5, 1, 0.5);

        // Feedback with level-specific message
        String feedback = switch (level) {
            case 3 -> "§cBlazing Aura III §8» §fBurned attacker and nearby mobs!";
            case 2 -> "§cBlazing Aura II §8» §fBurned attacker!";
            default -> "§cBlazing Aura §8» §fBurned attacker!";
        };
        ActionBarUtil.send(defender, feedback);
    }
}
