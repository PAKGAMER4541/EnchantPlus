package me.pakgamer5451.enchantplus.npc;

import me.pakgamer5451.enchantplus.EnchantPlus;
import me.pakgamer5451.enchantplus.gui.MainMenuGUI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EnchantMasterTrait extends Trait implements Listener {

    private final EnchantPlus plugin;

    public EnchantMasterTrait() {
        super("enchantmaster");
        this.plugin = EnchantPlus.getInstance();
    }

    @Override
    public void onSpawn() {
        // Optional setup, like logging or setting metadata
    }

    @EventHandler
    public void onRightClick(NPCRightClickEvent event) {
        if (!event.getNPC().equals(this.getNPC())) return;

        Player player = event.getClicker();
        new MainMenuGUI().open(player);  // Call your GUI opener here
    }
}
