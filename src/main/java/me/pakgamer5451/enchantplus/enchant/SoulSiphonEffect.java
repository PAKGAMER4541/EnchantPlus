package me.pakgamer5451.enchantplus.enchant;

import me.pakgamer5451.enchantplus.util.ActionBarUtil;
import me.pakgamer5451.enchantplus.util.EnchantUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class SoulSiphonEffect implements Listener {

    private static final Random random = new Random();

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer == null || !killer.isOnline()) return;

        ItemStack weapon = killer.getInventory().getItemInMainHand();
        if (weapon == null) return;

        if (!EnchantUtils.hasEnchant(weapon, "soul_siphon")) return;
        if (!EnchantUtils.isEnchantActive(killer, weapon)) return;

        // 15% chance to activate
        if (random.nextDouble() >= 0.15) return;

        double maxHealth = killer.getMaxHealth();
        double newHealth = Math.min(killer.getHealth() + 4.0, maxHealth); // 4 HP = 2 hearts
        killer.setHealth(newHealth);

        ActionBarUtil.send(killer, "§dSoul Siphon restored §c2 §dhearts!");
    }
}
