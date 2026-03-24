package me.pakgamer5451.enchantplus.enchant;

import me.pakgamer5451.enchantplus.util.ActionBarUtil;
import me.pakgamer5451.enchantplus.util.EnchantUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ForgeTouchEffect implements Listener {

    private static final Map<Material, Material> smeltMap = new HashMap<>() {{
        put(Material.IRON_ORE, Material.IRON_INGOT);
        put(Material.GOLD_ORE, Material.GOLD_INGOT);
        put(Material.COPPER_ORE, Material.COPPER_INGOT);
        put(Material.DEEPSLATE_IRON_ORE, Material.IRON_INGOT);
        put(Material.DEEPSLATE_GOLD_ORE, Material.GOLD_INGOT);
        put(Material.DEEPSLATE_COPPER_ORE, Material.COPPER_INGOT);
        put(Material.NETHER_GOLD_ORE, Material.GOLD_INGOT);
        put(Material.RAW_IRON_BLOCK, Material.IRON_INGOT);
        put(Material.RAW_GOLD_BLOCK, Material.GOLD_INGOT);
        put(Material.RAW_COPPER_BLOCK, Material.COPPER_INGOT);
        put(Material.ANCIENT_DEBRIS, Material.NETHERITE_SCRAP);
    }};

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        ItemStack tool = player.getInventory().getItemInMainHand();

        if (tool == null || !EnchantUtils.hasEnchant(tool, "forge_touch")) return;
        if (!EnchantUtils.isEnchantActive(player, tool)) return;

        // Only pickaxes
        if (!tool.getType().name().toLowerCase().contains("pickaxe")) return;

        // Respect Silk Touch
        if (tool.containsEnchantment(Enchantment.SILK_TOUCH)) return;

        Material type = block.getType();
        if (!smeltMap.containsKey(type)) return;

        // Get level for branching behavior
        int level = EnchantUtils.getEnchantLevel(tool, "forge_touch");

        event.setDropItems(false); // Cancel default drops

        Material smeltedType = smeltMap.get(type);
        int amount = 1;

        // Apply Fortune
        if (tool.containsEnchantment(Enchantment.FORTUNE)) {
            int fortuneLevel = tool.getEnchantmentLevel(Enchantment.FORTUNE);
            amount += new Random().nextInt(fortuneLevel + 1);
        }

        ItemStack result = new ItemStack(smeltedType, amount);

        if (level >= 2) {
            // Direct to inventory
            PlayerInventory inv = player.getInventory();
            HashMap<Integer, ItemStack> leftover = inv.addItem(result);
            for (ItemStack item : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
            ActionBarUtil.send(player, "§6Forge Touch §8» §fSmelted and sent to inventory!");
        } else {
            // Drop on ground
            block.getWorld().dropItemNaturally(block.getLocation(), result);
            ActionBarUtil.send(player, "§6Forge Touch §8» §fOre smelted!");
        }
    }
}
