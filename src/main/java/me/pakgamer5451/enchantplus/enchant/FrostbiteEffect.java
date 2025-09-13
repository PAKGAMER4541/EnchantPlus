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

        if (random.nextDouble() < 0.25) {
            // Apply Slowness IV for 5 seconds
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 3));

            // Mark entity with metadata
            target.setMetadata("frostbite_slowness", new FixedMetadataValue(
                player.getServer().getPluginManager().getPlugin("EnchantPlus"), player.getName())
            );

            // Visual: Snowflake burst
            target.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().add(0, 1, 0), 30, 0.3, 0.5, 0.3);

            // Audio: Glass breaking (chilling feedback)
            target.getWorld().playSound(target.getLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 0.5f);

            // Notify player
            ActionBarUtil.send(player, "§bFrostbite §8» §fSlowness IV applied!");
        }
    }
}
