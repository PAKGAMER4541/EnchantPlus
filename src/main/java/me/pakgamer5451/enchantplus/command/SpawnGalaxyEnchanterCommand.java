package me.pakgamer5451.enchantplus.command;

import me.pakgamer5451.enchantplus.EnchantPlus;
import me.pakgamer5451.enchantplus.npc.GalaxyEnchanterManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Command to spawn Galaxy Enchanter NPC
 * Usage: /spawnenchanter
 */
public class SpawnGalaxyEnchanterCommand implements CommandExecutor {
    
    private final EnchantPlus plugin;
    private final GalaxyEnchanterManager npcManager;
    
    public SpawnGalaxyEnchanterCommand(EnchantPlus plugin, GalaxyEnchanterManager npcManager) {
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
        if (!player.hasPermission("enchantplus.spawnenchanter")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        
        // Get spawn location (player's location)
        Location spawnLocation = player.getLocation();
        
        // Spawn NPC
        UUID id = npcManager.spawnEnchanter(spawnLocation);
        
        if (id != null) {
            player.sendMessage(ChatColor.GREEN + "Galaxy Enchanter spawned!");
            plugin.getLogger().info("Player " + player.getName() + " spawned Galaxy Enchanter NPC");
        } else {
            player.sendMessage(ChatColor.RED + "FancyNpcs not available.");
        }
        
        return true;
    }
}
