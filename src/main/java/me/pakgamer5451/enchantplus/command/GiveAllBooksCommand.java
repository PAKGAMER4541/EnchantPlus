package me.pakgamer5451.enchantplus.command;

import me.pakgamer5451.enchantplus.EnchantPlus;
import me.pakgamer5451.enchantplus.spin.EnchantSpinManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveAllBooksCommand implements CommandExecutor {
    
    public GiveAllBooksCommand(EnchantPlus plugin) {
        // Constructor - plugin parameter not currently used but kept for consistency
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this.");
            return true;
        }

        // ADD THIS BLOCK:
        if (!player.hasPermission("enchantplus.giveallenchantbooks")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        EnchantSpinManager.giveAllBooks(player);
        return true;
    }
}
