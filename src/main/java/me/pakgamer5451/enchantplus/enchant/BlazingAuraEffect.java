package me.pakgamer5451.enchantplus.enchant;

import me.pakgamer5451.enchantplus.util.ActionBarUtil;
import me.pakgamer5451.enchantplus.util.EnchantUtils;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class BlazingAuraEffect implements Listener {

    private static final Random random = new Random();
    private static final Map<UUID, Long> cooldownMap = new HashMap<>();
    private static final long COOLDOWN_MS = 5000; // 5 seconds

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

        // Check cooldown
        UUID itemUUID = EnchantUtils.getOrCreateItemUUID(chestplate);
        long now = System.currentTimeMillis();
        if (cooldownMap.containsKey(itemUUID)) {
            long last = cooldownMap.get(itemUUID);
            if (now - last < COOLDOWN_MS) return;
        }
        cooldownMap.put(itemUUID, now);

        // 25% chance
        if (random.nextDouble() > 0.25) return;

        // Apply effects to attacker
        Entity attacker = event.getDamager();
        if (attacker instanceof LivingEntity living) {
            living.setFireTicks(60); // 3 seconds of fire
            living.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 0)); // Slowness I
        }

        // Particles at chest level
        Location loc = defender.getLocation().add(0, 1.2, 0);
        defender.getWorld().spawnParticle(Particle.FLAME, loc, 40, 0.5, 1, 0.5);

        // Feedback
        ActionBarUtil.send(defender, "§cBlazing Aura burned your attacker!");
    }
}
