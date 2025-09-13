// GiveAllBooksCommand.java
package me.pakgamer5451.enchantplus.command;

import me.pakgamer5451.enchantplus.spin.EnchantSpinManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveAllBooksCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this.");
            return true;
        }

        EnchantSpinManager.giveAllBooks(player);
        return true;
    }
}
