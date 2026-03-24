package me.pakgamer5451.enchantplus;

import me.pakgamer5451.enchantplus.command.*;
import me.pakgamer5451.enchantplus.enchant.*;
import me.pakgamer5451.enchantplus.gui.EnchantGalleryGUI;
import me.pakgamer5451.enchantplus.gui.MainMenuGUI;
import me.pakgamer5451.enchantplus.gui.SpinMenuGUI;
import me.pakgamer5451.enchantplus.listener.AnvilCombineListener;
import me.pakgamer5451.enchantplus.listener.GalaxyEnchanterListener;
import me.pakgamer5451.enchantplus.listener.InventoryClickListener;
import me.pakgamer5451.enchantplus.listener.PlayerPlacedBlockTracker;
import me.pakgamer5451.enchantplus.listener.PlayerQuitListener;
import me.pakgamer5451.enchantplus.npc.GalaxyEnchanterManager;
import me.pakgamer5451.enchantplus.util.EnchantUtils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class EnchantPlus extends JavaPlugin {

    private static EnchantPlus instance;
    private GalaxyEnchanterManager npcManager;
    private NetherstrideEffect netherstrideEffect;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Initialize FancyNpcs-based NPC Manager
        npcManager = new GalaxyEnchanterManager(this);
        npcManager.init();
        getServer().getPluginManager().registerEvents(new GalaxyEnchanterListener(this, npcManager), this);

        // Register commands
        if (getCommand("spawnenchanter") != null)
            getCommand("spawnenchanter").setExecutor(new SpawnGalaxyEnchanterCommand(this, npcManager));
        if (getCommand("removeenchanter") != null)
            getCommand("removeenchanter").setExecutor(new RemoveGalaxyEnchanterCommand(this, npcManager));
        if (getCommand("giveallenchantbooks") != null)
            getCommand("giveallenchantbooks").setExecutor(new GiveAllBooksCommand(this));

        // Register GUI + listeners
        getServer().getPluginManager().registerEvents(new InventoryClickListener(), this);
        getServer().getPluginManager().registerEvents(new MainMenuGUI(), this);
        getServer().getPluginManager().registerEvents(new SpinMenuGUI(), this);
        getServer().getPluginManager().registerEvents(new EnchantGalleryGUI(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);
        getServer().getPluginManager().registerEvents(new AnvilCombineListener(), this);

        // Register enchant effect listeners
        getServer().getPluginManager().registerEvents(new PlayerPlacedBlockTracker(), this);
        getServer().getPluginManager().registerEvents(new BlazingAuraEffect(), this);
        getServer().getPluginManager().registerEvents(new EnderShiftEffect(this), this);
        getServer().getPluginManager().registerEvents(new FlameKingEffect(), this);
        getServer().getPluginManager().registerEvents(new ForgeTouchEffect(), this);
        getServer().getPluginManager().registerEvents(new FrostbiteEffect(), this);
        getServer().getPluginManager().registerEvents(new MidasTouchEffect(), this);
        netherstrideEffect = new NetherstrideEffect();
        getServer().getPluginManager().registerEvents(netherstrideEffect, this);
        getServer().getPluginManager().registerEvents(new SoulSiphonEffect(), this);
        getServer().getPluginManager().registerEvents(new SoulboundEffect(), this);
        getServer().getPluginManager().registerEvents(new TerraformerEffect(), this);
        getServer().getPluginManager().registerEvents(new ThunderlordEffect(), this);
        getServer().getPluginManager().registerEvents(new TimberfallEffect(), this);
        getServer().getPluginManager().registerEvents(new VoidStrikeEffect(), this);
        getServer().getPluginManager().registerEvents(new PhoenixAuraEffect(), this);
        getServer().getPluginManager().registerEvents(new InfernoCoreEffect(), this);

        // Register new enchant effects
        getServer().getPluginManager().registerEvents(new AutoReplantEffect(), this);
        getServer().getPluginManager().registerEvents(new AnglerEffect(), this);
        getServer().getPluginManager().registerEvents(new VillagersDealEffect(), this);
        getServer().getPluginManager().registerEvents(new FarmerBeesEffect(), this);

        // Load PlayerPlacedBlockTracker data
        PlayerPlacedBlockTracker.load();
        PlayerPlacedBlockTracker.startAutoSave();

        // Update PhoenixAura lore on startup
        for (Player player : Bukkit.getOnlinePlayers()) {
            ItemStack chest = player.getInventory().getChestplate();
            if (chest != null && EnchantUtils.hasEnchant(chest, "phoenix_aura")) {
                PhoenixAuraEffect.updateCooldownLore(chest, 0);
            }
        }

        getLogger().info("[EnchantPlus] Simplified NMS-based Galaxy Enchanter system enabled successfully.");
        getLogger().info("[EnchantPlus] Plugin enabled successfully.");
    }

    @Override
    public void onDisable() {
        if (npcManager != null) {
            npcManager.removeAll();
        }
        if (netherstrideEffect != null) {
            netherstrideEffect.cleanup();
        }
        // Clean up Farmer Bees tasks and entities
        FarmerBeesEffect.cleanupAll();
        // Save PlayerPlacedBlockTracker data
        PlayerPlacedBlockTracker.save();
        getLogger().info("[EnchantPlus] All Galaxy Enchanter NPCs removed.");
        getLogger().info("[EnchantPlus] Plugin disabled.");
    }

    public static EnchantPlus getInstance() {
        return instance;
    }
}
