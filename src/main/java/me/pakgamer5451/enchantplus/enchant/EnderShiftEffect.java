package me.pakgamer5451.enchantplus.enchant;

import me.pakgamer5451.enchantplus.util.ActionBarUtil;
import me.pakgamer5451.enchantplus.util.EnchantUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EnderShiftEffect implements Listener {

    private record DeathRecord(Location location, long timestamp) {}
    
    private static final Map<UUID, DeathRecord> lastDeathLocations = new HashMap<>();
    private static final long EXPIRY_MS = 30 * 60 * 1000L; // 30 minutes
    private final JavaPlugin plugin;

    public EnderShiftEffect(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        lastDeathLocations.put(player.getUniqueId(), new DeathRecord(player.getLocation(), System.currentTimeMillis()));
    }

    @EventHandler
    public void onCompassUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.COMPASS) return;
        if (!EnchantUtils.hasEnchant(item, "ender_shift")) return;
        if (!EnchantUtils.isEnchantActive(player, item)) return;

        // Right click only
        Action action = event.getAction();
        if (!(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) return;

        DeathRecord record = lastDeathLocations.get(player.getUniqueId());
        if (record == null) {
            ActionBarUtil.send(player, "§cNo previous death location saved.");
            return;
        }
        
        if (System.currentTimeMillis() - record.timestamp() > EXPIRY_MS) {
            lastDeathLocations.remove(player.getUniqueId());
            ActionBarUtil.send(player, "§cDeath location expired (30 minute limit).");
            return;
        }
        
        Location location = record.location();

        World deathWorld = Bukkit.getWorld(location.getWorld().getName());
        if (deathWorld == null) {
            ActionBarUtil.send(player, "§cError: Death world not found.");
            return;
        }

        if (deathWorld.getEnvironment() == World.Environment.THE_END && location.getY() <= 0) {
            ActionBarUtil.send(player, "§cCannot teleport: death location was in the void.");
            return;
        }

        Location safeLoc = findSafeLocationNear(location, 3, 64); // radius 3, depth 64
        if (safeLoc == null) {
            ActionBarUtil.send(player, "§cCannot teleport: no nearby safe ground found.");
            return;
        }

        // Teleport and grant invincibility
        player.teleport(safeLoc.add(0.5, 1, 0.5)); // center on block
        player.setInvulnerable(true);
        player.playSound(safeLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
        ActionBarUtil.send(player, "§bEnder Shift teleported you to your last death location!");

        lastDeathLocations.remove(player.getUniqueId());
        item.setAmount(0); // Consume the compass

        // Remove invincibility after 5 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                player.setInvulnerable(false);
                ActionBarUtil.send(player, "§7Your invincibility has worn off.");
            }
        }.runTaskLater(plugin, 20 * 5);
    }

    private Location findSafeLocationNear(Location center, int radiusXZ, int depthY) {
        World world = center.getWorld();
        int centerY = center.getBlockY();

        for (int dx = -radiusXZ; dx <= radiusXZ; dx++) {
            for (int dz = -radiusXZ; dz <= radiusXZ; dz++) {
                int x = center.getBlockX() + dx;
                int z = center.getBlockZ() + dz;

                for (int dy = 0; dy <= depthY; dy++) {
                    int y = centerY - dy;
                    if (y < world.getMinHeight()) break;

                    Location check = new Location(world, x, y, z);
                    Block ground = check.getBlock();
                    Block above = check.clone().add(0, 1, 0).getBlock();

                    if (ground.getType().isSolid() && above.getType() == Material.AIR) {
                        return ground.getLocation();
                    }
                }
            }
        }

        return null; // no safe spot found
    }

    public static void clearDeathLocation(UUID uuid) {
        lastDeathLocations.remove(uuid);
    }
}
