package me.pakgamer5451.enchantplus.listener;

import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.events.NpcInteractEvent;
import me.pakgamer5451.enchantplus.EnchantPlus;
import me.pakgamer5451.enchantplus.gui.MainMenuGUI;
import me.pakgamer5451.enchantplus.npc.GalaxyEnchanterManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class GalaxyEnchanterListener implements Listener {

    private final EnchantPlus plugin;
    private final GalaxyEnchanterManager manager;

    public GalaxyEnchanterListener(EnchantPlus plugin, GalaxyEnchanterManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onNpcInteract(NpcInteractEvent event) {
        Npc npc = event.getNpc();
        if (!manager.isGalaxyEnchanter(npc)) return;

        Player player = event.getPlayer();
        
        player.closeInventory();
        MainMenuGUI.open(player);
    }
}
