package me.pakgamer5451.enchantplus.gui;

import me.pakgamer5451.enchantplus.EnchantPlus;
import me.pakgamer5451.enchantplus.spin.EnchantSpinManager;
import me.pakgamer5451.enchantplus.spin.EnchantSpinManager.Rarity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SpinMenuGUI implements Listener {

    private static final String GUI_TITLE = ChatColor.DARK_PURPLE + "Enchant Spin Menu";
    private static final String SPIN_TITLE = ChatColor.GOLD + "Spinning...";
    private static final Map<Integer, Rarity> slotToRarity = new HashMap<>();
    private static final Map<Rarity, Integer> rarityXpCost = new HashMap<>();
    private static final Set<UUID> spinningPlayers = new HashSet<>();

    static {
        rarityXpCost.put(Rarity.COMMON, 20);
        rarityXpCost.put(Rarity.RARE, 30);
        rarityXpCost.put(Rarity.EPIC, 40);
        rarityXpCost.put(Rarity.LEGENDARY, 60);
        rarityXpCost.put(Rarity.MYTHIC, 80);
    }

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, GUI_TITLE);

        setSlot(gui, 10, Material.WHITE_WOOL, "§fCommon Spin", Rarity.COMMON);
        setSlot(gui, 11, Material.BLUE_WOOL, "§9Rare Spin", Rarity.RARE);
        setSlot(gui, 13, Material.PURPLE_WOOL, "§5Epic Spin", Rarity.EPIC);
        setSlot(gui, 15, Material.ORANGE_WOOL, "§6Legendary Spin", Rarity.LEGENDARY);
        setSlot(gui, 16, Material.RED_WOOL, "§cMythic Spin", Rarity.MYTHIC);

        player.openInventory(gui);
    }

    private static void setSlot(Inventory gui, int slot, Material material, String name, Rarity rarity) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(List.of(
                ChatColor.GRAY + "Click to spin for a",
                ChatColor.LIGHT_PURPLE + rarity.name() + ChatColor.GRAY + " enchantment.",
                ChatColor.YELLOW + "Cost: " + rarityXpCost.get(rarity) + " XP Levels"
        ));
        item.setItemMeta(meta);
        gui.setItem(slot, item);
        slotToRarity.put(slot, rarity);
    }

    public static void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();
        if (!title.equals(GUI_TITLE) && !title.equals(SPIN_TITLE)) return;

        // Prevent all interaction during spinning
        event.setCancelled(true);

        if (title.equals(SPIN_TITLE)) {
            // Disallow item grabbing from spin GUI
            return;
        }

        int slot = event.getRawSlot();
        if (!slotToRarity.containsKey(slot)) return;

        Rarity rarity = slotToRarity.get(slot);
        int xpCost = rarityXpCost.get(rarity);

        if (spinningPlayers.contains(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You are already spinning!");
            return;
        }

        if (player.getLevel() < xpCost) {
            player.sendMessage(ChatColor.RED + "You need " + xpCost + " XP levels to spin.");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        player.setLevel(player.getLevel() - xpCost);
        startSpin(player, rarity);
    }

    public static void startSpin(Player player, Rarity rarity) {
        Inventory spinGui = Bukkit.createInventory(null, 27, SPIN_TITLE);
        spinningPlayers.add(player.getUniqueId());

        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < 27; i++) {
            spinGui.setItem(i, filler);
        }

        player.openInventory(spinGui);

        List<ItemStack> options = new ArrayList<>();
        EnchantSpinManager.getPossibleEnchantsForSpin(rarity).forEach(enchant ->
                options.add(EnchantSpinManager.createEnchantBook(enchant))
        );

        for (int i = 0; i < 3; i++) {
            ItemStack barrier = new ItemStack(Material.BARRIER);
            ItemMeta meta = barrier.getItemMeta();
            meta.setDisplayName("§cNo Reward");
            meta.setLore(List.of("§7Better luck next time."));
            barrier.setItemMeta(meta);
            options.add(barrier);
        }

        Random random = new Random();
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 40;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    spinningPlayers.remove(player.getUniqueId());
                    return;
                }

                if (ticks >= maxTicks) {
                    cancel();
                    spinningPlayers.remove(player.getUniqueId());

                    ItemStack reward = options.get(random.nextInt(options.size()));
                    spinGui.setItem(13, reward);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.closeInventory();

                            if (reward.getType() == Material.BARRIER) {
                                player.sendMessage("§cYou spun the wheel but got nothing!");
                                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                            } else {
                                player.getInventory().addItem(reward);
                                player.sendMessage("§aYou won: §e" + reward.getItemMeta().getDisplayName());
                                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);

                                if (rarity == Rarity.LEGENDARY || rarity == Rarity.MYTHIC) {
                                    Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + player.getName() +
                                            " has won a " + rarity.name() + " enchant: §e" +
                                            reward.getItemMeta().getDisplayName() + ChatColor.LIGHT_PURPLE + "!");
                                }
                            }
                        }
                    }.runTaskLater(EnchantPlus.getInstance(), 30L);
                } else {
                    spinGui.setItem(13, options.get(random.nextInt(options.size())));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.3f, 1.0f);
                    ticks++;
                }
            }
        }.runTaskTimer(EnchantPlus.getInstance(), 0L, 2L);
    }

    public static boolean isSpinning(Player player) {
        return spinningPlayers.contains(player.getUniqueId());
    }
}
