package me.pakgamer5451.enchantplus.listener;

import me.pakgamer5451.enchantplus.enchant.EnderShiftEffect;
import me.pakgamer5451.enchantplus.enchant.FarmerBeesEffect;
import me.pakgamer5451.enchantplus.enchant.FlameKingEffect;
import me.pakgamer5451.enchantplus.enchant.InfernoCoreEffect;
import me.pakgamer5451.enchantplus.enchant.PhoenixAuraEffect;
import me.pakgamer5451.enchantplus.enchant.SoulboundEffect;
import me.pakgamer5451.enchantplus.enchant.ThunderlordEffect;
import me.pakgamer5451.enchantplus.gui.SpinMenuGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * Cleans up player-specific data on disconnect to prevent memory leaks
 */
public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        
        // Clear soulbound saved items for disconnected player
        SoulboundEffect.clearSavedItems(uuid);
        
        // Clear spinning state
        SpinMenuGUI.clearSpinning(uuid);
        
        // Clear Neural Overload electrify tasks and cooldowns
        ThunderlordEffect.cancelElectrify(uuid);
        ThunderlordEffect.clearCooldown(uuid);
        
        // Clear Inferno Core buff tasks
        InfernoCoreEffect.cancelBuff(uuid);
        
        // Clear Farmer Bees tasks and entities
        FarmerBeesEffect.cleanupPlayer(uuid);
        
        // Clear Reactive Aura cooldown
        FlameKingEffect.clearCooldown(uuid);
        
        // Clear Phoenix Aura saved XP
        PhoenixAuraEffect.cleanupXP(uuid);
        
        // Clear EnderShift death location
        EnderShiftEffect.clearDeathLocation(uuid);
        
        // Note: BlazingAura cooldownMap will naturally expire over time
        // For a thorough fix, expose a static clear method there too
    }
}
