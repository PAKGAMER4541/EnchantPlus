package me.pakgamer5451.enchantplus.enchant;

import me.pakgamer5451.enchantplus.EnchantPlus;
import me.pakgamer5451.enchantplus.util.ActionBarUtil;
import me.pakgamer5451.enchantplus.util.EnchantUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InfernoCoreEffect implements Listener {

    private static final Map<UUID, BukkitTask> activeTasks = new HashMap<>();

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        
        // Check for fire/lava damage
        if (event.getCause() != EntityDamageEvent.DamageCause.FIRE && 
            event.getCause() != EntityDamageEvent.DamageCause.FIRE_TICK && 
            event.getCause() != EntityDamageEvent.DamageCause.LAVA) return;

        ItemStack leggings = player.getInventory().getLeggings();
        if (leggings == null || !EnchantUtils.hasEnchant(leggings, "inferno_core")) return;
        if (!EnchantUtils.isEnchantActive(player, leggings)) return;

        // Check activation conditions
        if (!hasFireProtection(player)) {
            event.setCancelled(true); // Fire immunity
        }
    }

    @EventHandler
    public void onFireStart(EntityCombustEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack leggings = player.getInventory().getLeggings();
        if (leggings == null || !EnchantUtils.hasEnchant(leggings, "inferno_core")) return;
        if (!EnchantUtils.isEnchantActive(player, leggings)) return;

        // Check activation conditions
        if (hasFireProtection(player)) return;

        // Start buff task if not already active
        if (!activeTasks.containsKey(player.getUniqueId())) {
            startBuffTask(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        cancelBuff(event.getPlayer().getUniqueId());
    }

    private boolean hasFireProtection(Player player) {
        // Check for Fire Resistance potion effect
        if (player.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) return true;

        // Check all armor slots for FIRE_PROTECTION enchant
        ItemStack[] armor = new ItemStack[] {
            player.getInventory().getHelmet(),
            player.getInventory().getChestplate(),
            player.getInventory().getLeggings(),
            player.getInventory().getBoots()
        };

        for (ItemStack piece : armor) {
            if (piece != null && piece.containsEnchantment(Enchantment.FIRE_PROTECTION)) {
                return true;
            }
        }
        return false;
    }

    private void startBuffTask(Player player) {
        // Get level for branching behavior
        ItemStack leggings = player.getInventory().getLeggings();
        int level = EnchantUtils.getEnchantLevel(leggings, "inferno_core");
        
        String message = switch (level) {
            case 3 -> "§c🔥 Inferno Core III §8» §eHeat Sync active!";
            case 2 -> "§c🔥 Inferno Core II §8» §eHeat Sync active!";
            default -> "§c🔥 Inferno Core §8» §eHeat Sync active!";
        };
        ActionBarUtil.send(player, message);

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || player.getFireTicks() <= 0) {
                    // Remove buffs and cancel task
                    player.removePotionEffect(PotionEffectType.STRENGTH);
                    player.removePotionEffect(PotionEffectType.HASTE);
                    if (level >= 2) player.removePotionEffect(PotionEffectType.RESISTANCE);
                    if (level == 3) player.removePotionEffect(PotionEffectType.SPEED);
                    cancelBuff(player.getUniqueId());
                    return;
                }

                // Refresh buffs every 2 seconds while burning
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, 0)); // Strength I all levels
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 60, 0)); // Haste I all levels
                
                if (level >= 2) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 0)); // Resistance I
                }
                if (level == 3) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 0)); // Speed I
                }
            }
        }.runTaskTimer(EnchantPlus.getInstance(), 0L, 40L); // Every 2 seconds (40 ticks)

        activeTasks.put(player.getUniqueId(), task);
    }

    // Static method for PlayerQuitListener cleanup
    public static void cancelBuff(UUID uuid) {
        BukkitTask task = activeTasks.remove(uuid);
        if (task != null) task.cancel();
    }
}
