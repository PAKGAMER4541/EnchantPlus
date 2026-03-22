package me.pakgamer5451.enchantplus.enchant;

import me.pakgamer5451.enchantplus.util.ActionBarUtil;
import me.pakgamer5451.enchantplus.util.EnchantUtils;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class FrostbiteEffect implements Listener {

    private static final Random random = new Random();

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon == null) return;
        if (!EnchantUtils.hasEnchant(weapon, "frostbite")) return;
        if (!EnchantUtils.isEnchantActive(player, weapon)) return;

        // Get level for branching behavior
        int level = EnchantUtils.getEnchantLevel(weapon, "frostbite");
        
        double chance = switch (level) { case 2 -> 0.35; case 3 -> 0.35; default -> 0.25; };

        if (random.nextDouble() < chance) {
            // Base: always apply Slowness
            int slownessAmplifier = (level == 3) ? 4 : 3; // Slowness V vs IV (amplifier = level - 1)
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, slownessAmplifier));
            
            // Level II+: add Weakness I
            if (level >= 2) {
                int weaknessAmplifier = (level == 3) ? 1 : 0; // Weakness II vs I
                target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, weaknessAmplifier));
            }
            
            // Level III: add Mining Fatigue I
            if (level == 3) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 100, 0));
            }

            // Mark entity with metadata
            target.setMetadata("frostbite_slowness", new FixedMetadataValue(
                player.getServer().getPluginManager().getPlugin("EnchantPlus"), player.getUniqueId().toString())
            );

            // Visual: Snowflake burst
            target.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().add(0, 1, 0), 30, 0.3, 0.5, 0.3);

            // Audio: Glass breaking (chilling feedback)
            target.getWorld().playSound(target.getLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 0.5f);

            // Notify player with level-specific message
            String message = switch (level) {
                case 3 -> "§bFrostbite III §8» §fSlowness V + Weakness II + Mining Fatigue I applied!";
                case 2 -> "§bFrostbite II §8» §fSlowness IV + Weakness I applied!";
                default -> "§bFrostbite §8» §fSlowness IV applied!";
            };
            ActionBarUtil.send(player, message);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.hasMetadata("frostbite_slowness")) {
            entity.removeMetadata("frostbite_slowness",
                entity.getServer().getPluginManager().getPlugin("EnchantPlus"));
        }
    }
}
