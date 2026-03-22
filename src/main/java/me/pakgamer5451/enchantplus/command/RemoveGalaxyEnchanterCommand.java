package me.pakgamer5451.enchantplus.command;

import me.pakgamer5451.enchantplus.EnchantPlus;
import me.pakgamer5451.enchantplus.npc.GalaxyEnchanterManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Command to remove Galaxy Enchanter NPC
 * Usage: /removeenchanter
 */
public class RemoveGalaxyEnchanterCommand implements CommandExecutor {
    
    private final EnchantPlus plugin;
    private final GalaxyEnchanterManager npcManager;
    
    public RemoveGalaxyEnchanterCommand(EnchantPlus plugin, GalaxyEnchanterManager npcManager) {
        this.plugin = plugin;
        this.npcManager = npcManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Only players can use this command
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Check permission
        if (!player.hasPermission("enchantplus.removeenchanter")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        
        // Remove nearest NPC within 10 blocks
        UUID nearest = npcManager.getNearestEnchanter(player.getLocation(), 10.0);
        if (nearest != null && npcManager.removeEnchanter(nearest)) {
            player.sendMessage(ChatColor.GREEN + "Galaxy Enchanter removed.");
        } else {
            player.sendMessage(ChatColor.RED + "No Galaxy Enchanter found nearby.");
        }
        
        return true;
    }
}
