package me.pakgamer5451.enchantplus.enchant;

import me.pakgamer5451.enchantplus.util.EnchantUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.metadata.MetadataValue;

public class FlameKingEffect implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        ItemStack chestplate = player.getInventory().getChestplate();

        // Activation: Must be wearing chestplate with "flame_king"
        if (chestplate == null) return;
        if (!EnchantUtils.hasEnchant(chestplate, "flame_king")) return;
        if (!EnchantUtils.isEnchantActive(player, chestplate)) return;

        // Check if player has slowness from Frostbite
        if (player.hasPotionEffect(PotionEffectType.SLOW) && player.hasMetadata("frostbite_slowness")) {
            String attackerName = "Unknown";
            for (MetadataValue meta : player.getMetadata("frostbite_slowness")) {
                if (meta.getOwningPlugin().getName().equalsIgnoreCase("EnchantPlus")) {
                    attackerName = meta.asString();
                }
            }

            player.removePotionEffect(PotionEffectType.SLOW);
            player.removeMetadata("frostbite_slowness", player.getServer().getPluginManager().getPlugin("EnchantPlus"));

            player.sendMessage("§6Flame King §7burned away Frostbite's effect!");
            Player attacker = player.getServer().getPlayerExact(attackerName);
            if (attacker != null && attacker.isOnline()) {
                attacker.sendMessage("§7Your §bFrostbite §7was negated by §6Flame King§7!");
            }
        }
    }
}
