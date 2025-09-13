package me.pakgamer5451.enchantplus;

import me.pakgamer5451.enchantplus.command.GiveAllBooksCommand;
import me.pakgamer5451.enchantplus.command.RemoveNPCCommand;
import me.pakgamer5451.enchantplus.command.SpawnNPCCommand;
import me.pakgamer5451.enchantplus.enchant.*;
import me.pakgamer5451.enchantplus.gui.EnchantGalleryGUI;
import me.pakgamer5451.enchantplus.gui.MainMenuGUI;
import me.pakgamer5451.enchantplus.gui.SpinMenuGUI;
import me.pakgamer5451.enchantplus.listener.GUIListener;
import me.pakgamer5451.enchantplus.listener.InventoryClickListener;
import me.pakgamer5451.enchantplus.listener.NPCInteractListener;
import me.pakgamer5451.enchantplus.npc.EnchantMasterTrait;
import me.pakgamer5451.enchantplus.util.EnchantUtils;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class EnchantPlus extends JavaPlugin {

    private static EnchantPlus instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Register Citizens trait
        if (Bukkit.getPluginManager().isPluginEnabled("Citizens")) {
            CitizensAPI.getTraitFactory().registerTrait(
                TraitInfo.create(EnchantMasterTrait.class).withName("enchantmaster")
            );
            getLogger().info("[EnchantPlus] EnchantMasterTrait registered with Citizens.");
        } else {
            getLogger().severe("[EnchantPlus] Citizens plugin not found! NPCs will not function.");
        }

        // Register commands
        if (getCommand("spawnnpc") != null)
            getCommand("spawnnpc").setExecutor(new SpawnNPCCommand(this));
        if (getCommand("removenpc") != null)
            getCommand("removenpc").setExecutor(new RemoveNPCCommand());
        if (getCommand("giveallenchantbooks") != null)
            getCommand("giveallenchantbooks").setExecutor(new GiveAllBooksCommand());

        // Register GUI + listeners
        getServer().getPluginManager().registerEvents(new GUIListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(), this);
        getServer().getPluginManager().registerEvents(new MainMenuGUI(), this);
        getServer().getPluginManager().registerEvents(new SpinMenuGUI(), this);
        getServer().getPluginManager().registerEvents(new EnchantGalleryGUI(), this);

        // Register enchant effect listeners
        getServer().getPluginManager().registerEvents(new BlazingAuraEffect(), this);
        getServer().getPluginManager().registerEvents(new EnderShiftEffect(this), this);
        getServer().getPluginManager().registerEvents(new FlameKingEffect(), this);
        getServer().getPluginManager().registerEvents(new ForgeTouchEffect(), this);
        getServer().getPluginManager().registerEvents(new FrostbiteEffect(), this);
        getServer().getPluginManager().registerEvents(new MidasTouchEffect(), this);
        getServer().getPluginManager().registerEvents(new NetherstrideEffect(), this);
        getServer().getPluginManager().registerEvents(new SoulSiphonEffect(), this);
        getServer().getPluginManager().registerEvents(new SoulboundEffect(), this);
        getServer().getPluginManager().registerEvents(new TerraformerEffect(), this);
        getServer().getPluginManager().registerEvents(new ThunderlordEffect(), this);
        getServer().getPluginManager().registerEvents(new TimberfallEffect(), this);
        getServer().getPluginManager().registerEvents(new VoidStrikeEffect(), this);
        getServer().getPluginManager().registerEvents(new PhoenixAuraEffect(), this);

        // Update PhoenixAura lore on startup
        for (Player player : Bukkit.getOnlinePlayers()) {
            ItemStack chest = player.getInventory().getChestplate();
            if (chest != null && EnchantUtils.hasEnchant(chest, "phoenix_aura")) {
                PhoenixAuraEffect.updateCooldownLore(chest, 0);
            }
        }

        // Register NPC interact listener after Citizens is ready
        getServer().getScheduler().runTaskLater(this, () -> {
            getServer().getPluginManager().registerEvents(new NPCInteractListener(), this);
            getLogger().info("[EnchantPlus] NPCInteractListener registered.");
        }, 20L);

        getLogger().info("[EnchantPlus] Plugin enabled successfully.");
    }

    @Override
    public void onDisable() {
        getLogger().info("[EnchantPlus] Plugin disabled.");
    }

    public static EnchantPlus getInstance() {
        return instance;
    }
}
