package me.pakgamer5451.enchantplus.command;

import me.pakgamer5451.enchantplus.npc.EnchantMasterTrait;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RemoveNPCCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        double maxDistance = 5; // Remove NPCs within 5 blocks
        boolean removed = false;

        for (NPC npc : CitizensAPI.getNPCRegistry()) {
            if (npc.isSpawned() && npc.getEntity().getWorld().equals(player.getWorld())) {
                if (npc.hasTrait(EnchantMasterTrait.class) && 
                    npc.getEntity().getLocation().distance(player.getLocation()) <= maxDistance) {

                    npc.destroy();
                    player.sendMessage(ChatColor.GREEN + "Enchant Master NPC removed.");
                    removed = true;
                    break;
                }
            }
        }

        if (!removed) {
            player.sendMessage(ChatColor.RED + "No nearby Enchant Master NPC found.");
        }

        return true;
    }
}
