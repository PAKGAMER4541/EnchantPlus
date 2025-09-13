package me.pakgamer5451.enchantplus.listener;

import me.pakgamer5451.enchantplus.gui.MainMenuGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        MainMenuGUI.handleClick(event);
    }
}
