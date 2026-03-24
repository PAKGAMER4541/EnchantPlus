package me.pakgamer5451.enchantplus.enchant;

import me.pakgamer5451.enchantplus.util.EnchantUtils;
import org.bukkit.Material;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class AnglerEffect implements Listener {

    private static final Random RANDOM = new Random();
    private static final Material[] TREASURES = {
        Material.ENCHANTED_BOOK, Material.BOW, 
        Material.FISHING_ROD, Material.NAME_TAG, Material.SADDLE
    };

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        // Only affect fishing start
        if (event.getState() != PlayerFishEvent.State.FISHING) return;

        Player player = event.getPlayer();
        FishHook hook = event.getHook();

        // Check player is holding fishing rod with angler enchant
        ItemStack rod = player.getInventory().getItemInMainHand();
        if (rod == null || rod.getType() != Material.FISHING_ROD) return;
        if (!EnchantUtils.hasEnchant(rod, "angler")) return;
        if (!EnchantUtils.isEnchantActive(player, rod)) return;

        // Get level and apply wait time reduction
        int level = EnchantUtils.getEnchantLevel(rod, "angler");
        double reduction = switch (level) { 
            case 3 -> 0.40; 
            case 2 -> 0.55; 
            default -> 0.75; 
        };
        // Level I = keep 75% (25% faster), II = keep 55% (45% faster), III = keep 40% (60% faster)
        hook.setWaitTime((int)(hook.getWaitTime() * reduction));
    }

    @EventHandler
    public void onFishCaught(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        if (event.getCaught() == null) return;

        Player player = event.getPlayer();
        ItemStack rod = player.getInventory().getItemInMainHand();
        if (rod == null || rod.getType() != Material.FISHING_ROD) return;
        if (!EnchantUtils.hasEnchant(rod, "angler")) return;
        if (!EnchantUtils.isEnchantActive(player, rod)) return;

        int level = EnchantUtils.getEnchantLevel(rod, "angler");
        
        // Level III: guaranteed treasure-quality loot
        if (level == 3 && event.getCaught() instanceof Item caughtEntity) {
            Material type = caughtEntity.getItemStack().getType();
            boolean isFish = (type == Material.COD || type == Material.SALMON || 
                              type == Material.TROPICAL_FISH || type == Material.PUFFERFISH);
            if (isFish) {
                // Replace with a treasure item
                Material treasure = TREASURES[RANDOM.nextInt(TREASURES.length)];
                caughtEntity.setItemStack(new ItemStack(treasure));
            }
        }
    }
}
//Test needed 