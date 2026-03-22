package me.pakgamer5451.enchantplus.enchant;

import me.pakgamer5451.enchantplus.util.ActionBarUtil;
import me.pakgamer5451.enchantplus.util.EnchantUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FlameKingEffect implements Listener {

    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_MS = 12000L; // 12 seconds (default for Level I)

    public static void clearCooldown(UUID uuid) {
        cooldowns.remove(uuid);
    }

    @EventHandler
    public void onPotionEffectAdd(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getAction() != EntityPotionEffectEvent.Action.ADDED) return;

        ItemStack chestplate = player.getInventory().getChestplate();
        if (chestplate == null || !EnchantUtils.hasEnchant(chestplate, "flame_king")) return;
        if (!EnchantUtils.isEnchantActive(player, chestplate)) return;

        // Get level for branching behavior
        int level = EnchantUtils.getEnchantLevel(chestplate, "flame_king");
        
        long cooldownMs = switch (level) { case 3 -> 5000L; case 2 -> 8000L; default -> 12000L; };

        // Effects to absorb
        boolean shouldAbsorb = switch (level) {
            case 3 -> true; // absorb ANY negative effect
            case 2 -> event.getNewEffect().getType() == PotionEffectType.SLOWNESS ||
                      event.getNewEffect().getType() == PotionEffectType.LEVITATION ||
                      event.getNewEffect().getType() == PotionEffectType.MINING_FATIGUE ||
                      event.getNewEffect().getType() == PotionEffectType.WEAKNESS ||
                      event.getNewEffect().getType() == PotionEffectType.POISON;
            default -> event.getNewEffect().getType() == PotionEffectType.SLOWNESS ||
                       event.getNewEffect().getType() == PotionEffectType.LEVITATION ||
                       event.getNewEffect().getType() == PotionEffectType.MINING_FATIGUE;
        };

        if (!shouldAbsorb) return;

        // Check cooldown (after shouldAbsorb check)
        long now = System.currentTimeMillis();
        Long lastUsed = cooldowns.get(player.getUniqueId());
        if (lastUsed != null && now - lastUsed < cooldownMs) return;

        // Apply counter-buff
        int speedDuration = (level == 3) ? 80 : 60; // 4s vs 3s
        event.setCancelled(true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, speedDuration, 1)); // Speed II

        if (level == 3) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 80, 0)); // Strength I 4s
        }

        // Apply cooldown
        cooldowns.put(player.getUniqueId(), now);

        // Feedback with level-specific message
        String feedback = switch (level) {
            case 3 -> "§6Reactive Aura III §8» §eEffect absorbed — Speed + Strength boost!";
            case 2 -> "§6Reactive Aura II §8» §eEffect absorbed — Speed boost!";
            default -> "§6Reactive Aura §8» §eEffect absorbed — Speed boost!";
        };
        ActionBarUtil.send(player, feedback);
    }
}
