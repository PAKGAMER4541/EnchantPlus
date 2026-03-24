package me.pakgamer5451.enchantplus.enchant;

import me.pakgamer5451.enchantplus.EnchantPlus;
import me.pakgamer5451.enchantplus.util.ActionBarUtil;
import me.pakgamer5451.enchantplus.util.EnchantUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Bee;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class FarmerBeesEffect implements Listener {

    private static final Set<Material> CROPS = Set.of(
        Material.WHEAT, Material.CARROTS, Material.POTATOES,
        Material.BEETROOTS, Material.NETHER_WART
    );

    private static final Set<Material> ALL_FARMABLE = Set.of(
        Material.WHEAT, Material.CARROTS, Material.POTATOES,
        Material.BEETROOTS, Material.NETHER_WART, Material.FARMLAND
    );

    private static final NamespacedKey USES_KEY = new NamespacedKey(EnchantPlus.getInstance(), "farmer_bees_uses");
    private static final NamespacedKey RESET_KEY = new NamespacedKey(EnchantPlus.getInstance(), "farmer_bees_reset_time");
    private static final NamespacedKey LAST_USE_KEY = new NamespacedKey(EnchantPlus.getInstance(), "farmer_bees_last_use");

    private final Map<UUID, List<BukkitRunnable>> activeTasks = new HashMap<>();
    private final Map<UUID, List<Bee>> activeBees = new HashMap<>();
    
    // Static instance for cleanup methods
    private static FarmerBeesEffect instance;
    
    public FarmerBeesEffect() {
        instance = this;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && 
            event.getAction() != Action.RIGHT_CLICK_AIR) return;

        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();

        // Check tool is diamond or netherite hoe with farmer_bees enchant
        if (tool == null || !EnchantUtils.hasEnchant(tool, "farmer_bees")) return;
        if (!EnchantUtils.isEnchantActive(player, tool)) return;

        Material toolType = tool.getType();
        if (toolType != Material.DIAMOND_HOE && toolType != Material.NETHERITE_HOE) return;

        // Add level branching
        int level = EnchantUtils.getEnchantLevel(tool, "farmer_bees");

        // Check and reset 24h usage counter for ALL levels
        checkAndResetIfNeeded(tool);

        int maxBeesForLevel = switch (level) { case 3 -> 3; case 2 -> 2; default -> 1; };
        int maxUsesPerDay   = switch (level) { case 3 -> Integer.MAX_VALUE; case 2 -> 10; default -> 10; };
        long cooldownMs     = switch (level) { case 3 -> 0L; case 2 -> 2 * 60 * 1000L; default -> 5 * 60 * 1000L; };
        boolean costsDurability = (level == 3);

        // Check cooldown (Level I and II only)
        if (level < 3) {
            PersistentDataContainer pdc = tool.getItemMeta().getPersistentDataContainer();
            long lastUse = pdc.getOrDefault(LAST_USE_KEY, PersistentDataType.LONG, 0L);
            long now = System.currentTimeMillis();
            if (now - lastUse < cooldownMs) {
                long remainingSecs = (cooldownMs - (now - lastUse)) / 1000;
                ActionBarUtil.send(player, "§cFarmer Bees §8» §fCooldown active! Ready in §e" + remainingSecs + "s.");
                return;
            }
        }

        // Check daily use limit (Level I and II only)
        if (level < 3) {
            int currentUses = getUses(tool);
            if (currentUses >= maxUsesPerDay) {
                long hoursUntilReset = getHoursUntilReset(tool);
                ActionBarUtil.send(player, "§cFarmer Bees §8» §fDaily limit reached. Resets in §e" + hoursUntilReset + "h.");
                return;
            }
        }

        // Prevent double-activation guard (only after tool/enchant checks)
        if (activeTasks.containsKey(player.getUniqueId())) {
            ActionBarUtil.send(player, "§cBees are already working!");
            return;
        }

        // If clicking a block, verify it's farmable
        Location activationLoc = player.getLocation();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block clicked = event.getClickedBlock();
            if (clicked == null) return;
            if (!ALL_FARMABLE.contains(clicked.getType())) return;
            activationLoc = clicked.getLocation();
        }

        // Check durability cost (Level III only)
        if (costsDurability) {
            int cost = getDurabilityCost(tool);
            ItemMeta meta = tool.getItemMeta();
            if (!(meta instanceof Damageable damageable)) return;

            int currentDamage = damageable.getDamage();
            int maxDurability = tool.getType().getMaxDurability();
            int remaining = maxDurability - currentDamage;

            if (remaining < cost) {
                ActionBarUtil.send(player, "§cFarmer Bees §8» §fNot enough durability!");
                return;
            }

            // Apply damage
            damageable.setDamage(currentDamage + cost);
            tool.setItemMeta(meta);
        }

        // Store last use time (Level I and II only)
        if (level < 3) {
            ItemMeta meta = tool.getItemMeta();
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(LAST_USE_KEY, PersistentDataType.LONG, System.currentTimeMillis());
            tool.setItemMeta(meta);
            incrementUses(tool);
        }

        // Find crops in 10x10x3 area
        List<Block> immatureCrops = findImmatureCrops(activationLoc);
        if (immatureCrops.isEmpty()) {
            ActionBarUtil.send(player, "§cNo crops found nearby.");
            // Refund durability if applicable
            if (costsDurability) {
                ItemMeta meta = tool.getItemMeta();
                if (meta instanceof Damageable damageable) {
                    damageable.setDamage(damageable.getDamage() - getDurabilityCost(tool));
                    tool.setItemMeta(meta);
                }
            }
            if (level < 3) decrementUses(tool);
            return;
        }

        // Spawn worker bees with level-based count
        spawnWorkerBees(player, activationLoc, immatureCrops, maxBeesForLevel, costsDurability);

        // Show feedback
        String feedback = "§6Farmer Bees §8» §eBees summoned!";
        if (level < 3) {
            int nextCost = (int) Math.pow(2, getUses(tool)) * 10;
            long hoursUntilReset = getHoursUntilReset(tool);
            feedback += " §7Next use costs §c" + nextCost + " §7durability. Resets in §e" + hoursUntilReset + "h.";
        }
        ActionBarUtil.send(player, feedback);
    }

    private void checkAndResetIfNeeded(ItemStack hoe) {
        ItemMeta meta = hoe.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        long now = System.currentTimeMillis();
        long lastReset = pdc.getOrDefault(RESET_KEY, PersistentDataType.LONG, 0L);
        if (now - lastReset >= 86_400_000L) {
            pdc.set(USES_KEY, PersistentDataType.INTEGER, 0);
            pdc.set(RESET_KEY, PersistentDataType.LONG, now);
            hoe.setItemMeta(meta);
        }
    }

    private int getDurabilityCost(ItemStack hoe) {
        ItemMeta meta = hoe.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        int uses = pdc.getOrDefault(USES_KEY, PersistentDataType.INTEGER, 0);
        return (int) Math.pow(2, uses) * 10; // 10, 20, 40, 80, 160...
    }

    private int getUses(ItemStack hoe) {
        ItemMeta meta = hoe.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.getOrDefault(USES_KEY, PersistentDataType.INTEGER, 0);
    }

    private void incrementUses(ItemStack hoe) {
        ItemMeta meta = hoe.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        int uses = pdc.getOrDefault(USES_KEY, PersistentDataType.INTEGER, 0);
        pdc.set(USES_KEY, PersistentDataType.INTEGER, uses + 1);
        hoe.setItemMeta(meta);
    }

    private void decrementUses(ItemStack hoe) {
        ItemMeta meta = hoe.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        int uses = pdc.getOrDefault(USES_KEY, PersistentDataType.INTEGER, 0);
        pdc.set(USES_KEY, PersistentDataType.INTEGER, Math.max(0, uses - 1));
        hoe.setItemMeta(meta);
    }

    private long getHoursUntilReset(ItemStack hoe) {
        ItemMeta meta = hoe.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        long lastReset = pdc.getOrDefault(RESET_KEY, PersistentDataType.LONG, 0L);
        long timeSinceReset = System.currentTimeMillis() - lastReset;
        long msUntilReset = 86_400_000L - timeSinceReset;
        return Math.max(0, msUntilReset / (1000 * 60 * 60));
    }

    private List<Block> findImmatureCrops(Location center) {
        List<Block> crops = new ArrayList<>();
        World world = center.getWorld();
        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();

        for (int x = centerX - 5; x <= centerX + 5; x++) {
            for (int y = centerY - 1; y <= centerY + 1; y++) {
                for (int z = centerZ - 5; z <= centerZ + 5; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (CROPS.contains(block.getType()) && block.getBlockData() instanceof Ageable ageable) {
                        if (ageable.getAge() < ageable.getMaximumAge()) {
                            crops.add(block);
                        }
                    }
                }
            }
        }
        return crops;
    }

    private void spawnWorkerBees(Player player, Location activationLoc, List<Block> immatureCrops, int maxBees, boolean costsDurability) {
        World world = activationLoc.getWorld();
        List<BukkitRunnable> tasks = new ArrayList<>();
        List<Bee> spawnedBees = new ArrayList<>();

        // Distribute crops across maxBees bees
        int cropsPerBee = Math.min(maxBees, (immatureCrops.size() + maxBees - 1) / maxBees); // Round up division

        for (int i = 0; i < maxBees; i++) {
            int startIndex = i * cropsPerBee;
            int endIndex = Math.min(startIndex + cropsPerBee, immatureCrops.size());
            if (startIndex >= immatureCrops.size()) break;

            List<Block> assignedCrops = immatureCrops.subList(startIndex, endIndex);

            // Spawn bee
            Bee bee = (Bee) world.spawnEntity(activationLoc, EntityType.BEE);
            bee.setCustomName("§6Worker Bee");
            bee.setCustomNameVisible(true);
            bee.setAware(false);       // disable normal AI
            bee.setInvulnerable(true); // cannot take damage
            bee.setAnger(0);           // not angry
            spawnedBees.add(bee);      // track for cleanup

            // Bee work task
            BukkitRunnable beeTask = new BukkitRunnable() {
                int cropIndex = 0;
                Location targetLoc = null;
                boolean roaming = false;

                @Override
                public void run() {
                    if (!bee.isValid()) {
                        cancel();
                        return;
                    }

                    // Once all crops are done, switch to roaming mode
                    if (cropIndex >= assignedCrops.size()) {
                        roaming = true;
                    }

                    if (roaming) {
                        // Fly randomly around the activation area
                        if (targetLoc == null || bee.getLocation().distance(targetLoc) < 0.8) {
                            // Pick a new random point within the activation area
                            double offsetX = (Math.random() - 0.5) * 8;
                            double offsetY = Math.random() * 2 + 0.5;
                            double offsetZ = (Math.random() - 0.5) * 8;
                            targetLoc = activationLoc.clone().add(offsetX, offsetY, offsetZ);
                        }

                        Location current = bee.getLocation();
                        double distance = current.distance(targetLoc);
                        if (distance > 0.5) {
                            double speed = 0.15;
                            double dx = (targetLoc.getX() - current.getX()) / distance * speed;
                            double dy = (targetLoc.getY() - current.getY()) / distance * speed;
                            double dz = (targetLoc.getZ() - current.getZ()) / distance * speed;
                            bee.setVelocity(new org.bukkit.util.Vector(dx, dy, dz));
                        }
                        return;
                    }

                    // Working mode — move toward and tend assigned crops
                    Block target = assignedCrops.get(cropIndex);
                    if (targetLoc == null) {
                        targetLoc = target.getLocation().add(0.5, 1.2, 0.5);
                    }

                    Location current = bee.getLocation();
                    double distance = current.distance(targetLoc);

                    if (distance > 0.5) {
                        double speed = 0.25;
                        double dx = (targetLoc.getX() - current.getX()) / distance * speed;
                        double dy = (targetLoc.getY() - current.getY()) / distance * speed;
                        double dz = (targetLoc.getZ() - current.getZ()) / distance * speed;
                        bee.setVelocity(new org.bukkit.util.Vector(dx, dy, dz));
                    } else {
                        bee.setVelocity(new org.bukkit.util.Vector(0, 0, 0));

                        if (target.getBlockData() instanceof Ageable ageable) {
                            int currentAge = ageable.getAge();
                            int maxAge = ageable.getMaximumAge();
                            if (currentAge < maxAge) {
                                ageable.setAge(Math.min(currentAge + 2, maxAge));
                                target.setBlockData(ageable);
                                target.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                                    target.getLocation().add(0.5, 0.5, 0.5), 5, 0.3, 0.3, 0.3);
                            }
                        }
                        cropIndex++;
                        targetLoc = null;
                    }
                }
            };
            beeTask.runTaskTimer(EnchantPlus.getInstance(), 0L, 3L); // every 3 ticks for smooth movement
            tasks.add(beeTask);
        }

        // General area boost task
        BukkitRunnable areaBoostTask = new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 4) { // Run 4 times (60 seconds total)
                    cancel();
                    return;
                }

                // Boost all crops in area
                for (Block crop : findImmatureCrops(activationLoc)) {
                    if (crop.getBlockData() instanceof Ageable ageable) {
                        int currentAge = ageable.getAge();
                        int maxAge = ageable.getMaximumAge();
                        if (currentAge < maxAge) {
                            ageable.setAge(Math.min(currentAge + 1, maxAge));
                            crop.setBlockData(ageable);
                        }
                    }
                }
                ticks++;
            }
        };
        areaBoostTask.runTaskTimer(EnchantPlus.getInstance(), 0L, 300L); // Every 15 seconds
        tasks.add(areaBoostTask);

        // Action bar notification task
        BukkitRunnable actionBarTask = new BukkitRunnable() {
            int secondsLeft = 60;

            @Override
            public void run() {
                if (secondsLeft <= 0) {
                    cancel();
                    return;
                }

                for (Player nearby : Bukkit.getOnlinePlayers()) {
                    if (nearby.getLocation().distanceSquared(activationLoc) <= 225) {
                        ActionBarUtil.send(nearby, "§6🐝 Farmer Bees are working! §e" + secondsLeft + "s remaining");
                    }
                }
                secondsLeft -= 5;
            }
        };
        actionBarTask.runTaskTimer(EnchantPlus.getInstance(), 0L, 100L); // Every 5 seconds
        tasks.add(actionBarTask);

        // Cleanup task
        BukkitRunnable cleanupTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Cancel all tasks with exception handling
                for (BukkitRunnable task : tasks) {
                    try { task.cancel(); } catch (IllegalStateException ignored) {}
                }
                // Remove all bee entities
                for (Bee bee : spawnedBees) {
                    if (bee.isValid()) bee.remove();
                }
                // Remove from active maps
                activeTasks.remove(player.getUniqueId());
                activeBees.remove(player.getUniqueId());
            }
        };
        cleanupTask.runTaskLater(EnchantPlus.getInstance(), 1200L); // 60 seconds
        tasks.add(cleanupTask);

        activeTasks.put(player.getUniqueId(), tasks);
        activeBees.put(player.getUniqueId(), spawnedBees);
    }

    // Cleanup method for player quit
    public static void cleanupPlayer(UUID playerId) {
        if (instance != null) {
            // Cancel all tasks for this player
            List<BukkitRunnable> tasks = instance.activeTasks.get(playerId);
            if (tasks != null) {
                for (BukkitRunnable task : tasks) {
                    try { task.cancel(); } catch (IllegalStateException ignored) {}
                }
                instance.activeTasks.remove(playerId);
            }
            
            // Remove all bees for this player
            List<Bee> bees = instance.activeBees.get(playerId);
            if (bees != null) {
                for (Bee bee : bees) {
                    if (bee.isValid()) bee.remove();
                }
                instance.activeBees.remove(playerId);
            }
        }
    }

    // Static cleanup method for onDisable
    public static void cleanupAll() {
        if (instance != null) {
            // Cancel all tasks
            for (List<BukkitRunnable> tasks : instance.activeTasks.values()) {
                for (BukkitRunnable task : tasks) {
                    try { task.cancel(); } catch (IllegalStateException ignored) {}
                }
            }
            
            // Remove all bees
            for (List<Bee> bees : instance.activeBees.values()) {
                for (Bee bee : bees) {
                    if (bee.isValid()) bee.remove();
                }
            }
            
            // Clear maps
            instance.activeTasks.clear();
            instance.activeBees.clear();
        }
    }
}
