package me.pakgamer5451.enchantplus.enchant;

import me.pakgamer5451.enchantplus.util.EnchantUtils;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

public class ThunderlordEffect implements Listener {

    @EventHandler
    public void onBowCrit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow arrow)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        ProjectileSource source = arrow.getShooter();
        if (!(source instanceof Player player)) return;

        ItemStack bow = player.getInventory().getItemInMainHand();
        if (!EnchantUtils.hasEnchant(bow, "thunderlord")) return;
        if (!EnchantUtils.isEnchantActive(player, bow)) return;

        // Only activate if it's a critical bow hit
        if (!arrow.isCritical()) return;

        // Strike lightning at the entity's location
        Location loc = target.getLocation();
        loc.getWorld().strikeLightning(loc);
    }
}
