package me.pakgamer5451.enchantplus.enchant;

import me.pakgamer5451.enchantplus.EnchantPlus;
import me.pakgamer5451.enchantplus.util.ActionBarUtil;
import me.pakgamer5451.enchantplus.util.EnchantUtils;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class ThunderlordEffect implements Listener {

    private static final Random RANDOM = new Random();
    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final Map<UUID, BukkitTask> electrifyTasks = new HashMap<>();

    @EventHandler
    public void onArrowHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow arrow)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        if (!(arrow.getShooter() instanceof Player player)) return;

        ItemStack bow = player.getInventory().getItemInMainHand();
        if (bow == null || !EnchantUtils.hasEnchant(bow, "thunderlord")) return;
        if (!EnchantUtils.isEnchantActive(player, bow)) return;

        // Check if target is weakened (≤ 75% health)
        if (target.getHealth() > target.getMaxHealth() * 0.75) return;

        // Check cooldown (10 seconds per shooter)
        long now = System.currentTimeMillis();
        Long lastShot = cooldowns.get(player.getUniqueId());
        if (lastShot != null && now - lastShot < 10000L) return;

        // Apply cooldown
        cooldowns.put(player.getUniqueId(), now);

        // Start electrify effect for 15 seconds
        startElectrify(target);
    }

    private void startElectrify(LivingEntity target) {
        // Cancel existing task if any
        BukkitTask existing = electrifyTasks.get(target.getUniqueId());
        if (existing != null) existing.cancel();

        // Schedule electrify ticks every 3 seconds for 15 seconds total
        BukkitTask task = new BukkitRunnable() {
            private int ticks = 0;

            @Override
            public void run() {
                if (!target.isValid() || target.isDead()) {
                    cancelElectrify(target.getUniqueId());
                    return;
                }

                ticks += 3; // 3 seconds passed
                if (ticks > 15) {
                    cancelElectrify(target.getUniqueId());
                    return;
                }

                // 50/50 surge roll
                if (RANDOM.nextBoolean()) {
                    // Surge A — Internal Burn (1.5-3.5 hearts magic damage)
                    EntityDamageEvent dmgEvent = new EntityDamageEvent(
                        target, EntityDamageEvent.DamageCause.MAGIC, RANDOM.nextDouble() * 2 + 1.5);
                    Bukkit.getPluginManager().callEvent(dmgEvent);
                    if (!dmgEvent.isCancelled()) {
                        target.damage(dmgEvent.getFinalDamage());
                    }
                    target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, 
                        target.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.1);
                    target.getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_BURN, 0.5f, 1.0f);
                } else {
                    // Surge B — Neural Numb (Slowness IV + Mining Fatigue II for 2s)
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 3)); // Slowness IV
                    target.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 40, 1)); // Mining Fatigue II
                    target.getWorld().spawnParticle(Particle.LARGE_SMOKE, 
                        target.getLocation().add(0, 1, 0), 8, 0.2, 0.2, 0.2, 0.05);
                    target.getWorld().playSound(target.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.3f, 0.8f);
                }

                // Visual lightning effect (no damage)
                target.getWorld().strikeLightningEffect(target.getLocation());
            }
        }.runTaskTimer(EnchantPlus.getInstance(), 0L, 60L); // Every 3 seconds (60 ticks)

        electrifyTasks.put(target.getUniqueId(), task);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        cancelElectrify(entity.getUniqueId());
    }

    // Static method for PlayerQuitListener cleanup
    public static void cancelElectrify(UUID uuid) {
        BukkitTask task = electrifyTasks.remove(uuid);
        if (task != null) task.cancel();
    }

    public static void clearCooldown(UUID uuid) {
        cooldowns.remove(uuid);
    }
}
