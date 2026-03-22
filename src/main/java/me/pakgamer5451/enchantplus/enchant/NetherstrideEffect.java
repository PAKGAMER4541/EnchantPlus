package me.pakgamer5451.enchantplus.enchant;

import me.pakgamer5451.enchantplus.EnchantPlus;
import me.pakgamer5451.enchantplus.util.ActionBarUtil;
import me.pakgamer5451.enchantplus.util.EnchantUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class NetherstrideEffect implements Listener {

    private final Set<Location> tempBlocks = new HashSet<>();

    @EventHandler
    public void onLavaStep(PlayerMoveEvent event) {
        // Early exit: only fire when stepping onto new XZ block (PERFORMANCE FIX)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Player player = event.getPlayer();
        ItemStack boots = player.getInventory().getBoots();
        if (boots == null || boots.getType() == Material.AIR) return;
        if (!boots.getType().name().toLowerCase().contains("boots")) return;
        if (!EnchantUtils.hasEnchant(boots, "netherstride")) return;

        // Get level for branching behavior
        int level = EnchantUtils.getEnchantLevel(boots, "netherstride");

        Location below = player.getLocation().clone().subtract(0, 1, 0);
        Block block = below.getBlock();
        if (block.getType() != Material.LAVA) return;

        Location loc = block.getLocation();
        if (tempBlocks.contains(loc)) return;

        // Use MAGMA_BLOCK instead of BARRIER
        block.setType(Material.MAGMA_BLOCK);
        tempBlocks.add(loc);

        ActionBarUtil.send(player, "§6Netherstride activated!");

        // Revert delay based on level
        long revertTicks = switch (level) { case 3 -> 20 * 10L; case 2 -> 20 * 5L; default -> 20 * 2L; };

        Bukkit.getScheduler().runTaskLater(EnchantPlus.getInstance(), () -> {
            if (tempBlocks.remove(loc)) {
                Block b = loc.getBlock();
                if (b.getType() == Material.MAGMA_BLOCK) {
                    b.setType(Material.LAVA);
                }
            }
        }, revertTicks);
    }

    // NEW: Fire damage immunity (Level II+): already handled by onMagmaDamage for HOT_FLOOR
    // Expand for Level II: also cancel FIRE and FIRE_TICK (but NOT for Inferno Core users)
    @EventHandler
    public void onFireDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        
        ItemStack boots = player.getInventory().getBoots();
        if (boots == null || !EnchantUtils.hasEnchant(boots, "netherstride")) return;
        
        int level = EnchantUtils.getEnchantLevel(boots, "netherstride");
        
        EntityDamageEvent.DamageCause cause = event.getCause();
        
        // Level I: only HOT_FLOOR (magma block immunity)
        if (level == 1 && cause == EntityDamageEvent.DamageCause.HOT_FLOOR) {
            event.setCancelled(true);
        }
        
        // Level II+: HOT_FLOOR + FIRE + FIRE_TICK
        if (level >= 2 && (cause == EntityDamageEvent.DamageCause.HOT_FLOOR ||
                            cause == EntityDamageEvent.DamageCause.FIRE ||
                            cause == EntityDamageEvent.DamageCause.FIRE_TICK)) {
            event.setCancelled(true);
        }
        
        // Level III: + LAVA
        if (level == 3 && cause == EntityDamageEvent.DamageCause.LAVA) {
            event.setCancelled(true);
        }
    }

    // NEW: Call this from EnchantPlus.onDisable() to prevent world corruption
    public void cleanup() {
        for (Location loc : tempBlocks) {
            Block b = loc.getBlock();
            if (b.getType() == Material.MAGMA_BLOCK) {
                b.setType(Material.LAVA);
            }
        }
        tempBlocks.clear();
    }
}
