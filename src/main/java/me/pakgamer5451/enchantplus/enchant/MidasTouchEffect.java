package me.pakgamer5451.enchantplus.enchant;

import me.pakgamer5451.enchantplus.listener.PlayerPlacedBlockTracker;
import me.pakgamer5451.enchantplus.util.ActionBarUtil;
import me.pakgamer5451.enchantplus.util.EnchantUtils;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Random;

public class MidasTouchEffect implements Listener {

    private static final Random random = new Random();

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool == null || tool.getType().isAir()) return;

        if (!EnchantUtils.hasEnchant(tool, "midas_touch")) return;
        if (!EnchantUtils.isEnchantActive(player, tool)) return;

        // Skip player-placed blocks to prevent duplication exploit
        if (PlayerPlacedBlockTracker.isPlayerPlaced(block.getLocation())) return;

        // Get level for branching behavior
        int level = EnchantUtils.getEnchantLevel(tool, "midas_touch");
        
        double chance = switch (level) { case 3 -> 0.20; case 2 -> 0.12; default -> 0.05; };

        if (random.nextDouble() < chance) {
            // Ancient Debris: only allowed at Level III
            if (block.getType() == Material.ANCIENT_DEBRIS && level < 3) {
                return; // skip Midas effect on Ancient Debris for Level I and II
            }
            
            event.setDropItems(false);
            Collection<ItemStack> drops = block.getDrops(tool);
            
            for (ItemStack drop : drops) {
                if (drop == null || drop.getAmount() == 0) continue;
                ItemStack doubled = drop.clone();
                doubled.setAmount(drop.getAmount() * 2);
                block.getWorld().dropItemNaturally(block.getLocation(), doubled);
            }
            
            // Level III: 10% chance for triple instead of double
            if (level == 3 && random.nextDouble() < 0.10) {
                // Drop one more set
                for (ItemStack drop : drops) {
                    if (drop == null || drop.getAmount() == 0) continue;
                    ItemStack extra = drop.clone();
                    block.getWorld().dropItemNaturally(block.getLocation(), extra);
                }
                ActionBarUtil.send(player, "§6Midas Touch §8» §eTriple drop!");
            } else {
                ActionBarUtil.send(player, "§6Midas Touch §8» §eYour drops turned to gold!");
            }

            // Visual: Gold sparkle
            block.getWorld().spawnParticle(Particle.FIREWORK, block.getLocation().add(0.5, 0.5, 0.5), 25, 0.3, 0.3, 0.3);

            // Sound: Experience pickup (reward feeling)
            block.getWorld().playSound(block.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 1.5f);
        }
    }
}
