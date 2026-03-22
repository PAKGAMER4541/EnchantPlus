package me.pakgamer5451.enchantplus.enchant;

import me.pakgamer5451.enchantplus.EnchantPlus;
import me.pakgamer5451.enchantplus.util.ActionBarUtil;
import me.pakgamer5451.enchantplus.util.EnchantUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class SoulSiphonEffect implements Listener {

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        // Fires for all levels
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        ItemStack weapon = killer.getInventory().getItemInMainHand();
        if (!EnchantUtils.hasEnchant(weapon, "soul_siphon")) return;
        if (!EnchantUtils.isEnchantActive(killer, weapon)) return;
        
        int level = EnchantUtils.getEnchantLevel(weapon, "soul_siphon");
        double hearts = switch (level) { case 3 -> 4.0; case 2 -> 3.0; default -> 2.0; };
        long duration = (level == 3) ? 6000L : 2400L; // 5 mins vs 2 mins in ticks
        grantAbsorption(killer, hearts * 2, duration); // hearts * 2 because absorption uses HP not hearts
    }

    @EventHandler
    public void onMobKill(EntityDeathEvent event) {
        // Only fires for Level II+
        if (event.getEntity() instanceof Player) return; // handled by onPlayerKill
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        ItemStack weapon = killer.getInventory().getItemInMainHand();
        if (!EnchantUtils.hasEnchant(weapon, "soul_siphon")) return;
        if (!EnchantUtils.isEnchantActive(killer, weapon)) return;
        
        int level = EnchantUtils.getEnchantLevel(weapon, "soul_siphon");
        if (level < 2) return; // Level I: mob kills don't count
        
        double absorptionGrant = (level == 3) ? 4.0 * 2 : 1.0; // 4 hearts (Level III) vs 0.5 hearts (Level II)
        long duration = (level == 3) ? 6000L : 2400L;
        grantAbsorption(killer, absorptionGrant, duration);
    }

    private void grantAbsorption(Player player, double amount, long durationTicks) {
        double current = player.getAbsorptionAmount();
        double cap = 8.0; // 4 hearts max
        if (current >= cap) {
            ActionBarUtil.send(player, "§dSoul Siphon §8» §7Absorption full.");
            return;
        }
        double newAbs = Math.min(current + amount, cap);
        player.setAbsorptionAmount(newAbs);
        
        double granted = newAbs - current;
        Bukkit.getScheduler().runTaskLater(EnchantPlus.getInstance(), () -> {
            if (player.isOnline()) {
                player.setAbsorptionAmount(Math.max(0, player.getAbsorptionAmount() - granted));
            }
        }, durationTicks);
        
        double heartsGranted = granted / 2.0;
        ActionBarUtil.send(player, "§dSoul Siphon §8» §e+" + heartsGranted + " absorption hearts!");
    }
}
