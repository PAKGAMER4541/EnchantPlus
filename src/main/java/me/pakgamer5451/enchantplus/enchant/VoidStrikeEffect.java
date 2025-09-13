package me.pakgamer5451.enchantplus.enchant;

import me.pakgamer5451.enchantplus.util.ActionBarUtil;
import me.pakgamer5451.enchantplus.util.EnchantUtils;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class VoidStrikeEffect implements Listener {

    private static final Random RANDOM = new Random();
    private static final double ACTIVATION_CHANCE = 0.15;

    @EventHandler
    public void onEntityHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        Entity target = event.getEntity();
        if (!isVoidEntity(target)) return;

        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon == null || !EnchantUtils.hasEnchant(weapon, "voidstrike")) return;
        if (!EnchantUtils.isEnchantActive(player, weapon)) return;

        if (RANDOM.nextDouble() < ACTIVATION_CHANCE) {
            // Activate effect
            event.setDamage(event.getDamage() * 4.0); // +300% damage

            // Visuals & Sound
            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 1f, 1.2f);
            target.getWorld().spawnParticle(Particle.PORTAL, target.getLocation().add(0, 1, 0), 50, 0.3, 0.5, 0.3, 0.05);

            // Player feedback
            ActionBarUtil.send(player, "§5Void Strike §8» §dDevastating blow! +300% damage");
        } else {
            // Optional: uncomment to show feedback even on miss
            // ActionBarUtil.send(player, "§7Void Strike failed to trigger.");
        }
    }

    private boolean isVoidEntity(Entity entity) {
        return entity instanceof Enderman
            || entity instanceof EnderDragon
            || entity instanceof Shulker;
    }
}
