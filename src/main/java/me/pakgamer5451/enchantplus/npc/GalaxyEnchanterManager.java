package me.pakgamer5451.enchantplus.npc;

import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcData;
import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.events.NpcsLoadedEvent;
import me.pakgamer5451.enchantplus.EnchantPlus;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;

public class GalaxyEnchanterManager implements Listener {

    private static final String NPC_NAME_PREFIX = "GalaxyEnchanter_";
    // Skin from NameMC — paste your real base64 texture value here if you have one,
    // or use a player username string like "MHF_Steve" for a default skin
    private static final String SKIN = "MHF_Steve";

    private final EnchantPlus plugin;
    private final Map<UUID, Npc> activeNPCs = new HashMap<>();
    private boolean fancyNpcsAvailable = false;

    public GalaxyEnchanterManager(EnchantPlus plugin) {
        this.plugin = plugin;
        if (plugin.getServer().getPluginManager().getPlugin("FancyNpcs") != null) {
            fancyNpcsAvailable = true;
            plugin.getLogger().info("FancyNpcs found — Galaxy Enchanter NPC system ready.");
        } else {
            plugin.getLogger().warning("FancyNpcs not found — Galaxy Enchanter NPCs will not spawn.");
        }
    }

    // Called from EnchantPlus onEnable — listen for NpcsLoadedEvent then restore saved NPCs
    public void init() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onNpcsLoaded(NpcsLoadedEvent event) {
        loadAndRespawnNPCs();
    }

    public UUID spawnEnchanter(Location location) {
        if (!fancyNpcsAvailable) return null;

        UUID id = UUID.randomUUID();
        String npcName = NPC_NAME_PREFIX + id.toString().substring(0, 8);

        NpcData data = new NpcData(npcName, id, location);
        data.setSkin(SKIN);
        data.setDisplayName("<gradient:#a855f7:#60a5fa>✦ Galaxy Enchanter ✦</gradient>");
        data.setShowInTab(false);
        data.setTurnToPlayer(true);

        Npc npc = FancyNpcsPlugin.get().getNpcAdapter().apply(data);
        FancyNpcsPlugin.get().getNpcManager().registerNpc(npc);
        npc.create();
        npc.spawnForAll();

        activeNPCs.put(id, npc);
        saveNPCLocations();
        plugin.getLogger().info("Spawned Galaxy Enchanter NPC: " + npcName);
        return id;
    }

    public boolean removeEnchanter(UUID id) {
        Npc npc = activeNPCs.remove(id);
        if (npc == null) return false;
        npc.removeForAll();
        FancyNpcsPlugin.get().getNpcManager().removeNpc(npc);
        saveNPCLocations();
        return true;
    }

    public UUID getNearestEnchanter(Location location, double maxDistance) {
        UUID nearest = null;
        double nearestDist = maxDistance;
        for (Map.Entry<UUID, Npc> entry : activeNPCs.entrySet()) {
            Location npcLoc = entry.getValue().getData().getLocation();
            if (!npcLoc.getWorld().equals(location.getWorld())) continue;
            double dist = npcLoc.distance(location);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = entry.getKey();
            }
        }
        return nearest;
    }

    // Check if a given FancyNpcs Npc is one of ours
    public boolean isGalaxyEnchanter(Npc npc) {
        return activeNPCs.containsValue(npc);
    }

    public void removeAll() {
        for (Npc npc : activeNPCs.values()) {
            npc.removeForAll();
            FancyNpcsPlugin.get().getNpcManager().removeNpc(npc);
        }
        activeNPCs.clear();
    }

    private void saveNPCLocations() {
        plugin.getConfig().set("npc-locations", null);
        int i = 0;
        for (Map.Entry<UUID, Npc> entry : activeNPCs.entrySet()) {
            Location loc = entry.getValue().getData().getLocation();
            String path = "npc-locations." + i;
            plugin.getConfig().set(path + ".uuid", entry.getKey().toString());
            plugin.getConfig().set(path + ".world", loc.getWorld().getName());
            plugin.getConfig().set(path + ".x", loc.getX());
            plugin.getConfig().set(path + ".y", loc.getY());
            plugin.getConfig().set(path + ".z", loc.getZ());
            plugin.getConfig().set(path + ".yaw", loc.getYaw());
            plugin.getConfig().set(path + ".pitch", loc.getPitch());
            i++;
        }
        plugin.saveConfig();
        plugin.getLogger().info("Saved " + i + " NPC location(s) to config.");
    }

    private void loadAndRespawnNPCs() {
        if (!fancyNpcsAvailable) return;
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("npc-locations");
        if (section == null) return;
        int count = 0;
        for (String key : section.getKeys(false)) {
            String uuidStr = section.getString(key + ".uuid");
            String worldName = section.getString(key + ".world");
            double x = section.getDouble(key + ".x");
            double y = section.getDouble(key + ".y");
            double z = section.getDouble(key + ".z");
            float yaw = (float) section.getDouble(key + ".yaw");
            float pitch = (float) section.getDouble(key + ".pitch");
            org.bukkit.World world = plugin.getServer().getWorld(worldName);
            if (world == null || uuidStr == null) continue;
            Location loc = new Location(world, x, y, z, yaw, pitch);
            UUID savedId = UUID.fromString(uuidStr);

            String npcName = NPC_NAME_PREFIX + savedId.toString().substring(0, 8);
            NpcData data = new NpcData(npcName, savedId, loc);
            data.setSkin(SKIN);
            data.setDisplayName("<gradient:#a855f7:#60a5fa>✦ Galaxy Enchanter ✦</gradient>");
            data.setShowInTab(false);
            data.setTurnToPlayer(true);

            Npc npc = FancyNpcsPlugin.get().getNpcAdapter().apply(data);
            FancyNpcsPlugin.get().getNpcManager().registerNpc(npc);
            npc.create();
            npc.spawnForAll();

            activeNPCs.put(savedId, npc);
            count++;
            plugin.getLogger().info("Restored NPC at " + worldName + "," + x + "," + y + "," + z);
        }
        plugin.getLogger().info("Loaded and respawned " + count + " NPC(s) from config.");
    }
}
