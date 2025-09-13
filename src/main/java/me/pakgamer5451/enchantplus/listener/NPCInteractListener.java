package me.pakgamer5451.enchantplus.listener;

import me.pakgamer5451.enchantplus.gui.MainMenuGUI;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NPCInteractListener implements Listener {

    @EventHandler
    public void onNPCRightClick(NPCRightClickEvent event) {
        NPC npc = event.getNPC();
        if (npc.hasTrait(EnchantMasterTrait.class)) {
           MainMenuGUI.open(event.getClicker());

        }
    }

    // Trait class to mark NPCs that should open the enchant menu
    public static class EnchantMasterTrait extends Trait {
        public EnchantMasterTrait() {
            super("enchantmaster");
        }

        @Override
        public void onSpawn() {
            // Optional: customize NPC appearance or behavior here
        }
    }
}
