package me.pakgamer5451.enchantplus.enchant;

import me.pakgamer5451.enchantplus.EnchantPlus;
import me.pakgamer5451.enchantplus.util.EnchantUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VillagersDealEffect implements Listener {

    private static final Map<UUID, Long> discountedVillagers = new HashMap<>();
    private static final Map<UUID, org.bukkit.scheduler.BukkitRunnable> activeTimers = new HashMap<>();
    private static final NamespacedKey DISCOUNT_KEY = new NamespacedKey(EnchantPlus.getInstance(), "villagers_deal_discount");
    private static final NamespacedKey LOCKED_KEY = new NamespacedKey(EnchantPlus.getInstance(), "villagers_deal_locked");
    private static final NamespacedKey START_TIME_KEY = new NamespacedKey(EnchantPlus.getInstance(), "villagers_deal_start_time");

    public VillagersDealEffect() {
        // Startup recovery for existing discounted villagers
        recoverDiscountedVillagers();
    }

    private void recoverDiscountedVillagers() {
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (org.bukkit.entity.Entity entity : world.getEntities()) {
                if (entity instanceof Villager villager) {
                    PersistentDataContainer pdc = villager.getPersistentDataContainer();
                    if (pdc.has(DISCOUNT_KEY, PersistentDataType.STRING)) {
                        Long startTime = pdc.get(START_TIME_KEY, PersistentDataType.LONG);
                        if (startTime != null) {
                            long elapsed = System.currentTimeMillis() - startTime;
                            long remaining = 30 * 60 * 1000 - elapsed; // 30 minutes in millis
                            
                            if (remaining > 0) {
                                // Villager still has active discount
                                discountedVillagers.put(villager.getUniqueId(), startTime);
                                int minutesLeft = (int) (remaining / (60 * 1000));
                                villager.setCustomName("§a-50% §7| §e" + minutesLeft + ":00");
                                villager.setCustomNameVisible(true);
                                
                                // Restart countdown timer
                                restartCountdownTimer(villager, minutesLeft);
                            } else {
                                // Discount expired, restore prices
                                restoreDiscount(villager);
                            }
                        }
                    }
                }
            }
        }
    }

    private void restartCountdownTimer(Villager villager, int minutesLeft) {
        new BukkitRunnable() {
            int minutes = minutesLeft - 1;
            
            @Override
            public void run() {
                if (!villager.isValid() || minutes <= 0) {
                    restoreDiscount(villager);
                    cancel();
                    return;
                }
                villager.setCustomName("§a-50% §7| §e" + minutes + ":00");
                minutes--;
            }
        }.runTaskTimer(EnchantPlus.getInstance(), 0L, 1200L); // every 60 seconds
    }

    @EventHandler
    public void onPotionLand(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof ThrownPotion potion)) return;

        ItemStack item = potion.getItem();
        if (!EnchantUtils.hasEnchant(item, "villagers_deal")) return;

        // Check a villager was hit
        if (event.getHitEntity() == null || !(event.getHitEntity() instanceof Villager villager)) {
            // No villager hit — do NOT consume the potion (cancel consumption)
            // Unfortunately ThrownPotion can't easily be cancelled at this point;
            // just do nothing — the potion still breaks but has no effect
            return;
        }

        // Apply discount for 30 minutes
        applyDiscount(villager);
    }

    @EventHandler
    public void onBrew(BrewEvent event) {
        // Block brewing if any ingredient has villagers_deal_locked
        for (ItemStack ingredient : event.getContents().getContents()) {
            if (ingredient != null && ingredient.hasItemMeta()) {
                ItemMeta meta = ingredient.getItemMeta();
                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                if (pdc.has(LOCKED_KEY, PersistentDataType.BYTE)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    private void applyDiscount(Villager villager) {
        UUID villagerId = villager.getUniqueId();
        
        // Cancel existing timer if present
        org.bukkit.scheduler.BukkitRunnable existingTimer = activeTimers.remove(villagerId);
        if (existingTimer != null) {
            existingTimer.cancel();
        }

        // Check if already discounted
        if (discountedVillagers.containsKey(villagerId)) {
            // Already discounted — restore original prices first, then reapply fresh
            restoreDiscount(villager);
        }

        discountedVillagers.put(villagerId, System.currentTimeMillis());

        // Store original prices before modifying
        storeOriginalPrices(villager);
        
        // Store start time for recovery
        long startTime = System.currentTimeMillis();
        PersistentDataContainer pdc = villager.getPersistentDataContainer();
        pdc.set(START_TIME_KEY, PersistentDataType.LONG, startTime);

        // Reduce all recipe prices by 50%, floor at 5 emeralds
        List<MerchantRecipe> recipes = villager.getRecipes();
        for (MerchantRecipe recipe : recipes) {
            // Skip trades where the OUTPUT is emeralds (player selling TO villager)
            // We only discount trades where the PLAYER PAYS emeralds
            List<ItemStack> ingredients = new ArrayList<>(recipe.getIngredients());
            boolean changed = false;
            for (int i = 0; i < ingredients.size(); i++) {
                ItemStack ing = ingredients.get(i);
                if (ing != null && ing.getType() == Material.EMERALD) {
                    int original = ing.getAmount();
                    int discounted = Math.max(1, (int) Math.ceil(original * 0.5));
                    ing.setAmount(discounted);
                    ingredients.set(i, ing);
                    changed = true;
                }
            }
            if (changed) {
                recipe.setIngredients(ingredients);
            }
        }
        villager.setRecipes(recipes);

        // Floating text above villager using display name temporarily
        villager.setCustomName("§a-50% §7| §e30:00");
        villager.setCustomNameVisible(true);

        // Countdown timer updating every 60 seconds, remove after 30 mins
        org.bukkit.scheduler.BukkitRunnable timer = new org.bukkit.scheduler.BukkitRunnable() {
            int minutesLeft = 29;
            @Override
            public void run() {
                if (!villager.isValid() || minutesLeft <= 0) {
                    // Restore prices
                    restoreDiscount(villager);
                    cancel();
                    return;
                }
                villager.setCustomName("§a-50% §7| §e" + minutesLeft + ":00");
                minutesLeft--;
            }
        };
        timer.runTaskTimer(EnchantPlus.getInstance(), 1200L, 1200L); // every 60 seconds
        activeTimers.put(villagerId, timer);
    }

    private void storeOriginalPrices(Villager villager) {
        PersistentDataContainer pdc = villager.getPersistentDataContainer();
        StringBuilder prices = new StringBuilder();
        List<MerchantRecipe> recipes = villager.getRecipes();
        for (int r = 0; r < recipes.size(); r++) {
            List<ItemStack> ingredients = recipes.get(r).getIngredients();
            for (int i = 0; i < ingredients.size(); i++) {
                ItemStack ing = ingredients.get(i);
                if (ing != null && ing.getType() == Material.EMERALD) {
                    if (prices.length() > 0) prices.append(";");
                    prices.append(r).append(":").append(i).append(":").append(ing.getAmount());
                }
            }
        }
        pdc.set(DISCOUNT_KEY, PersistentDataType.STRING, prices.toString());
    }

    private void restoreDiscount(Villager villager) {
        UUID villagerId = villager.getUniqueId();
        discountedVillagers.remove(villagerId);
        activeTimers.remove(villagerId);
        villager.setCustomName(null);
        villager.setCustomNameVisible(false);
        
        // Restore original prices from PDC
        PersistentDataContainer pdc = villager.getPersistentDataContainer();
        String pricesStr = pdc.get(DISCOUNT_KEY, PersistentDataType.STRING);
        if (pricesStr != null && !pricesStr.isEmpty()) {
            List<MerchantRecipe> recipes = villager.getRecipes();
            for (String entry : pricesStr.split(";")) {
                String[] parts = entry.split(":");
                if (parts.length != 3) continue;
                try {
                    int recipeIdx = Integer.parseInt(parts[0]);
                    int ingIdx = Integer.parseInt(parts[1]);
                    int amount = Integer.parseInt(parts[2]);
                    if (recipeIdx >= recipes.size()) continue;
                    MerchantRecipe recipe = recipes.get(recipeIdx);
                    List<ItemStack> ingredients = new ArrayList<>(recipe.getIngredients());
                    if (ingIdx >= ingredients.size()) continue;
                    ingredients.get(ingIdx).setAmount(amount);
                    recipe.setIngredients(ingredients);
                } catch (NumberFormatException ignored) {}
            }
            villager.setRecipes(recipes);
            pdc.remove(DISCOUNT_KEY);
            pdc.remove(START_TIME_KEY);
        }
    }

    @EventHandler
    public void onVillagerDeath(org.bukkit.event.entity.EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Villager villager)) return;
        
        UUID villagerId = villager.getUniqueId();
        if (discountedVillagers.containsKey(villagerId)) {
            discountedVillagers.remove(villagerId);
            org.bukkit.scheduler.BukkitRunnable timer = activeTimers.remove(villagerId);
            if (timer != null) {
                timer.cancel();
            }
        }
    }

    // Static method to lock potions when enchant is applied
    public static void lockPotion(ItemStack potion) {
        ItemMeta meta = potion.getItemMeta();
        if (meta != null) {
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(LOCKED_KEY, PersistentDataType.BYTE, (byte) 1);
            potion.setItemMeta(meta);
        }
    }
}
