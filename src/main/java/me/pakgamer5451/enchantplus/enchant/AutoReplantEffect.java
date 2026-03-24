package me.pakgamer5451.enchantplus.enchant;

import me.pakgamer5451.enchantplus.EnchantPlus;
import me.pakgamer5451.enchantplus.util.ActionBarUtil;
import me.pakgamer5451.enchantplus.util.EnchantUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class AutoReplantEffect implements Listener {

    private static final Map<Material, Material> CROP_TO_SEED = Map.of(
        Material.WHEAT,       Material.WHEAT_SEEDS,
        Material.CARROTS,     Material.CARROT,
        Material.POTATOES,    Material.POTATO,
        Material.BEETROOTS,   Material.BEETROOT_SEEDS,
        Material.NETHER_WART, Material.NETHER_WART
    );

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material cropType = block.getType();

        // Check if this is a crop we handle
        if (!CROP_TO_SEED.containsKey(cropType)) return;

        // Check player is holding a hoe with auto_replant enchant
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool == null || !EnchantUtils.hasEnchant(tool, "auto_replant")) return;
        if (!EnchantUtils.isEnchantActive(player, tool)) return;

        // Check crop is fully grown
        if (!(block.getBlockData() instanceof Ageable ageable)) return;
        if (ageable.getAge() < ageable.getMaximumAge()) return;

        // Determine required seed
        Material seedMaterial = CROP_TO_SEED.get(cropType);

        // Check player has at least 1 seed
        if (!player.getInventory().containsAtLeast(new ItemStack(seedMaterial), 1)) {
            ActionBarUtil.send(player, "§cAuto Replant §8» §fNo " + seedMaterial.name().toLowerCase().replace("_", " ") + " in inventory!");
            return;
        }

        // Special case: Nether Wart requires soul sand
        if (cropType == Material.NETHER_WART) {
            if (block.getRelative(org.bukkit.block.BlockFace.DOWN).getType() != Material.SOUL_SAND) {
                ActionBarUtil.send(player, "§cAuto Replant §8» §fNether Wart must be planted on Soul Sand!");
                return;
            }
        }

        // Remove 1 seed from inventory
        ItemStack seedToRemove = new ItemStack(seedMaterial, 1);
        player.getInventory().removeItem(seedToRemove);

        // Schedule replanting after the break
        new BukkitRunnable() {
            @Override
            public void run() {
                // Re-check block is still air/farmland (wasn't replaced)
                if (block.getType() == Material.AIR || block.getType() == Material.FARMLAND) {
                    block.setType(cropType);
                    Ageable fresh = (Ageable) block.getBlockData();
                    fresh.setAge(0);
                    block.setBlockData(fresh);
                    
                    ActionBarUtil.send(player, "§aAuto Replant §8» §fCrop replanted!");
                }
            }
        }.runTaskLater(EnchantPlus.getInstance(), 1L);
    }
}
// i think too OP lol maybe 