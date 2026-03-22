package me.pakgamer5451.enchantplus.listener;

import me.pakgamer5451.enchantplus.EnchantPlus;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.io.*;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks player-placed blocks to prevent duplication exploits
 * Used by MidasTouchEffect to ensure only natural blocks are affected
 * Now persists data across server restarts with async auto-save
 */
public class PlayerPlacedBlockTracker implements Listener {

    // Static so MidasTouchEffect can access it
    private static final Set<Location> playerPlacedBlocks = 
        Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final File DATA_FILE = new File(EnchantPlus.getInstance().getDataFolder(), "placed_blocks.dat");

    public static void load() {
        if (!DATA_FILE.exists()) return;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    World world = Bukkit.getWorld(parts[0]);
                    if (world != null) {
                        try {
                            int x = Integer.parseInt(parts[1]);
                            int y = Integer.parseInt(parts[2]);
                            int z = Integer.parseInt(parts[3]);
                            playerPlacedBlocks.add(new Location(world, x, y, z));
                        } catch (NumberFormatException e) {
                            // Skip malformed lines
                        }
                    }
                }
            }
        } catch (IOException e) {
            EnchantPlus.getInstance().getLogger().warning("Failed to load placed blocks data: " + e.getMessage());
        }
    }

    public static void save() {
        try {
            DATA_FILE.getParentFile().mkdirs();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE))) {
                for (Location loc : playerPlacedBlocks) {
                    writer.write(loc.getWorld().getName() + "," + 
                               loc.getBlockX() + "," + 
                               loc.getBlockY() + "," + 
                               loc.getBlockZ());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            EnchantPlus.getInstance().getLogger().warning("Failed to save placed blocks data: " + e.getMessage());
        }
    }

    public static void startAutoSave() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(
            EnchantPlus.getInstance(),
            PlayerPlacedBlockTracker::save,
            6000L, 6000L // every 5 minutes
        );
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        playerPlacedBlocks.add(event.getBlock().getLocation());
        // NO file I/O here - handled by async auto-save
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        // Clean up when any block is broken (not just by Midas)
        playerPlacedBlocks.remove(event.getBlock().getLocation());
        // NO file I/O here - handled by async auto-save
    }

    public static boolean isPlayerPlaced(Location loc) {
        return playerPlacedBlocks.contains(loc);
    }
}
