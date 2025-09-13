package me.pakgamer5451.enchantplus.enchant;

import me.pakgamer5451.enchantplus.EnchantPlus;
import me.pakgamer5451.enchantplus.util.ActionBarUtil;
import me.pakgamer5451.enchantplus.util.EnchantUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class NetherstrideEffect implements Listener {

    private final Set<Location> tempBlocks = new HashSet<>();

    @EventHandler
    public void onLavaStep(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Step 1: Check if boots exist and have Netherstride
        ItemStack boots = player.getInventory().getBoots();
        if (boots == null || boots.getType() == Material.AIR) return;
        if (!boots.getType().name().toLowerCase().contains("boots")) return;
        if (!EnchantUtils.hasEnchant(boots, "netherstride")) return;

        // Step 2: Get block under player
        Location below = player.getLocation().clone().subtract(0, 1, 0);
        Block block = below.getBlock();

        // Step 3: Only transform LAVA blocks (not already barrier)
        if (block.getType() != Material.LAVA) return;

        Location loc = block.getLocation();
        if (tempBlocks.contains(loc)) return;

        // Step 4: Transform lava to barrier and track
        block.setType(Material.BARRIER);
        tempBlocks.add(loc);

        ActionBarUtil.send(player, "§6Netherstride activated!");

        // Step 5: Revert block back to lava after 2 seconds
        Bukkit.getScheduler().runTaskLater(EnchantPlus.getInstance(), () -> {
            if (tempBlocks.remove(loc)) {
                Block b = loc.getBlock();
                if (b.getType() == Material.BARRIER) {
                    b.setType(Material.LAVA);
                }
            }
        }, 20 * 2); // 2 seconds delay
    }
}
