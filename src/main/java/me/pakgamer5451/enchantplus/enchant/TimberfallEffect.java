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
        Material.WARPED_STEM
    );

    private static final BlockFace[] DIRECTIONS = {
        BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH,
        BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST
    };

    private static final int MAX_BLOCKS = 100;

    @EventHandler
    public void onLogBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block origin = event.getBlock();
        ItemStack tool = player.getInventory().getItemInMainHand();

        if (!EnchantUtils.hasEnchant(tool, "timberfall")) return;
        if (!EnchantUtils.isEnchantActive(player, tool)) return;
        if (!LOGS.contains(origin.getType())) return;

        // Axe check
        String typeName = tool.getType().name().toLowerCase();
        if (!typeName.contains("axe")) return;

        Set<Block> visited = new HashSet<>();
        breakTree(origin, player, visited, tool);
    }

    private void breakTree(Block block, Player player, Set<Block> visited, ItemStack tool) {
        if (visited.size() > MAX_BLOCKS) return;
        if (!LOGS.contains(block.getType())) return;
        if (!visited.add(block)) return;

        block.breakNaturally(tool);

        for (BlockFace face : DIRECTIONS) {
            Block neighbor = block.getRelative(face);
            breakTree(neighbor, player, visited, tool);
        }
    }
}
