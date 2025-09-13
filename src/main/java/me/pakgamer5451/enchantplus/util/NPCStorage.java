package me.pakgamer5451.enchantplus.util;

import me.pakgamer5451.enchantplus.EnchantPlus;
import me.pakgamer5451.enchantplus.listener.NPCInteractListener.EnchantMasterTrait;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.EntityType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class NPCStorage {

    private final EnchantPlus plugin;

    // Keep track of Enchant Master NPCs by UUID
    private final Set<UUID> trackedNPCs = new HashSet<>();

    public NPCStorage(EnchantPlus plugin) {
        this.plugin = plugin;
    }

    // Spawn a new Enchant Master NPC at the player's location
    public void spawnNPC(String name, org.bukkit.Location location) {
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, name);
        npc.addTrait(EnchantMasterTrait.class);
        npc.setName(name);
        npc.spawn(location);

        trackedNPCs.add(npc.getUniqueId());
        plugin.getLogger().info("Enchant Master NPC spawned at " + location);
    }

    // Respawn any NPCs that are missing (Citizens normally handles persistence)
    public void respawnAll() {
        for (NPC npc : CitizensAPI.getNPCRegistry()) {
            if (npc.hasTrait(EnchantMasterTrait.class) && !npc.isSpawned()) {
                npc.spawn(npc.getStoredLocation());
            }
        }
    }

    // Remove nearby NPC with EnchantMasterTrait (used in /removenpc)
    public boolean removeNearbyNPC(org.bukkit.entity.Player player, double radius) {
        for (NPC npc : CitizensAPI.getNPCRegistry()) {
            if (npc.hasTrait(EnchantMasterTrait.class) && npc.isSpawned()) {
                if (npc.getEntity().getLocation().distance(player.getLocation()) <= radius) {
                    npc.destroy();
                    plugin.getLogger().info("Removed nearby Enchant Master NPC.");
                    return true;
                }
            }
        }
        return false;
    }
}
