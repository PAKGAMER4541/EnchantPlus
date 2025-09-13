package me.pakgamer5451.enchantplus.enchant;

import me.pakgamer5451.enchantplus.util.ActionBarUtil;
import me.pakgamer5451.enchantplus.util.EnchantUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TerraformerEffect implements Listener {

    // Valid blocks for pickaxe and shovel
    private static final Set<Material> SHOVELABLE = new HashSet<>(Arrays.asList(
            Material.DIRT, Material.GRASS_BLOCK, Material.COARSE_DIRT, Material.SAND,
            Material.RED_SAND, Material.GRAVEL, Material.PODZOL, Material.MYCELIUM,
            Material.ROOTED_DIRT, Material.MUD, Material.SOUL_SAND, Material.SOUL_SOIL
    ));

    private static final Set<Material> MINEABLE = new HashSet<>(Arrays.asList(
            Material.STONE, Material.ANDESITE, Material.DIORITE, Material.GRANITE,
            Material.DEEPSLATE, Material.NETHERRACK, Material.END_STONE,
            Material.CALCITE, Material.BLACKSTONE, Material.BASALT, Material.TUFF
    ));

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        Block center = event.getBlock();

        if (tool == null || tool.getType() == Material.AIR) return;
        if (!EnchantUtils.hasEnchant(tool, "terraformer")) return;
        if (!EnchantUtils.isEnchantActive(player, tool)) return;
        if (player.isSneaking()) return;

        boolean isPickaxe = tool.getType().name().endsWith("_PICKAXE");
        boolean isShovel = tool.getType().name().endsWith("_SHOVEL");

        if (!isPickaxe && !isShovel) return;

        Material centerType = center.getType();
        if ((isPickaxe && !MINEABLE.contains(centerType)) || (isShovel && !SHOVELABLE.contains(centerType))) return;

        int broken = 0;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Block relative = center.getRelative(dx, 0, dz);
                if (relative.equals(center)) continue;

                Material relType = relative.getType();
                if (relType == Material.AIR || !relType.isBlock()) continue;
                if (relative.getType().name().contains("CHEST") || relType == Material.BARRIER) continue;

                // Only break blocks appropriate for the tool
                if ((isPickaxe && MINEABLE.contains(relType)) || (isShovel && SHOVELABLE.contains(relType))) {
                    boolean success = relative.breakNaturally(tool); // Uses tool durability and enchants
                    if (success) broken++;
                }
            }
        }

        if (broken > 0) {
            ActionBarUtil.send(player, "§eTerraformer cleared the area!");
        }
    }
}
