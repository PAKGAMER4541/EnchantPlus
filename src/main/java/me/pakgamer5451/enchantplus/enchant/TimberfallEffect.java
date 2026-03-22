package me.pakgamer5451.enchantplus.enchant;

import me.pakgamer5451.enchantplus.util.EnchantUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class TimberfallEffect implements Listener {

    private static final Set<Material> LOGS = EnumSet.of(
        Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG,
        Material.JUNGLE_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG,
        Material.MANGROVE_LOG, Material.CHERRY_LOG, Material.CRIMSON_STEM,
        Material.WARPED_STEM,
        // Stripped variants
        Material.STRIPPED_OAK_LOG, Material.STRIPPED_SPRUCE_LOG, Material.STRIPPED_BIRCH_LOG,
        Material.STRIPPED_JUNGLE_LOG, Material.STRIPPED_ACACIA_LOG, Material.STRIPPED_DARK_OAK_LOG,
        Material.STRIPPED_MANGROVE_LOG, Material.STRIPPED_CHERRY_LOG, Material.STRIPPED_CRIMSON_STEM,
        Material.STRIPPED_WARPED_STEM
    );

    private static final BlockFace[] DIRECTIONS = {
        BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH,
        BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST
    };

    private static final int MAX_BLOCKS = 100;
    
    // Guard flag to prevent recursion crash
    private static final Set<UUID> activeBreakers = new HashSet<>();

    @EventHandler
    public void onLogBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block origin = event.getBlock();
        ItemStack tool = player.getInventory().getItemInMainHand();

        // If this event was triggered BY our own breakNaturally call, skip it
        if (activeBreakers.contains(player.getUniqueId())) return;

        if (!EnchantUtils.hasEnchant(tool, "timberfall")) return;
        if (!EnchantUtils.isEnchantActive(player, tool)) return;
        if (!LOGS.contains(origin.getType())) return;

        // Axe check
        String typeName = tool.getType().name().toLowerCase();
        if (!typeName.contains("axe")) return;

        activeBreakers.add(player.getUniqueId());
        try {
            breakTree(origin, player, tool);
        } finally {
            activeBreakers.remove(player.getUniqueId()); // always remove, even on error
        }
    }

    private void breakTree(Block origin, Player player, ItemStack tool) {
        Queue<Block> queue = new LinkedList<>();
        Set<Block> visited = new HashSet<>();
        queue.add(origin);

        while (!queue.isEmpty() && visited.size() < MAX_BLOCKS) {
            Block block = queue.poll();
            if (!visited.add(block)) continue;
            if (!LOGS.contains(block.getType())) continue;

            block.breakNaturally(tool);

            for (BlockFace face : DIRECTIONS) {
                Block neighbor = block.getRelative(face);
                if (!visited.contains(neighbor) && LOGS.contains(neighbor.getType())) {
                    queue.add(neighbor);
                }
            }
        }
    }
}
