package me.pakgamer5451.enchantplus.command;

import me.pakgamer5451.enchantplus.EnchantPlus;
import me.pakgamer5451.enchantplus.npc.EnchantMasterTrait;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class SpawnNPCCommand implements CommandExecutor {

    private final EnchantPlus plugin;

    public SpawnNPCCommand(EnchantPlus plugin) {
        this.plugin = plugin;
    }

   @Override
public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player player)) {
        sender.sendMessage(ChatColor.RED + "Only players can use this command!");
        return true;
    }

    Location location = player.getLocation();

    // Create and spawn Citizens NPC
    NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, ChatColor.GREEN + "Enchant Master");
    npc.spawn(location);

    // Add custom trait
    npc.getOrAddTrait(EnchantMasterTrait.class);

    // Set skin using persistent data key (safer and supported)
    npc.data().setPersistent("player-skin-name", "LibrarianSteve");

    player.sendMessage(ChatColor.YELLOW + "Enchant Master NPC spawned with Librarian-like skin!");
    return true;
}

}
