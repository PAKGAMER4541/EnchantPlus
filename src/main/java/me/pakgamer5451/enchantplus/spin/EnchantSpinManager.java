package me.pakgamer5451.enchantplus.spin;

import me.pakgamer5451.enchantplus.EnchantPlus;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class EnchantSpinManager {

    public enum Rarity {
        COMMON, RARE, EPIC, LEGENDARY, MYTHIC
    }

    public static class EnchantData {
        public final String id, name, description, itemType;
        public final Rarity rarity;

        public EnchantData(String id, String name, String description, Rarity rarity, String itemType) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.rarity = rarity;
            this.itemType = itemType;
        }
    }

    private static final Map<Rarity, List<EnchantData>> enchantPool = new HashMap<>();
    private static final Map<String, EnchantData> enchantMap = new HashMap<>();

    static {
        for (Rarity r : Rarity.values()) {
            enchantPool.put(r, new ArrayList<>());
        }

        // ---- FIXED itemType entries ----
        add("soul_siphon", "Soul Siphon", "15% chance to heal 1 on kill", Rarity.MYTHIC, "SWORD,AXE");
        add("frostbite", "Frostbite", "30% chance to apply Slowness IV (5s)", Rarity.EPIC, "SWORD,TRIDENT");
        add("void_strike", "Void Strike", "+300% damage vs End mobs (15% chance)", Rarity.LEGENDARY, "SWORD,AXE,BOW,CROSSBOW,TRIDENT");
        add("thunderlord", "Thunderlord", " summon lightning on crit", Rarity.EPIC, "BOW,CROSSBOW");

        add("forge_touch", "Forge Touch", "Auto-smelts ores instantly to inventory", Rarity.COMMON, "PICKAXE");
        add("timberfall", "Timberfall", "Fells entire tree when one log is broken", Rarity.RARE, "AXE");
        add("terraformer", "Terraformer", "Breaks 3x3 block area (horizontal)", Rarity.EPIC, "SHOVEL,PICKAXE");
        add("soulbound", "Soulbound", "Keeps item on death (up to 3 times)", Rarity.LEGENDARY, "SWORD,AXE,PICKAXE,SHOVEL,TRIDENT,CHESTPLATE,BOOTS,HELMET,LEGGINGS");

        add("phoenix_aura", "Phoenix Aura", "Auto-revive on death (24h cooldown)", Rarity.MYTHIC, "CHESTPLATE");
        add("netherstride", "Netherstride", "Walk on lava for 10s with visual platform", Rarity.LEGENDARY, "BOOTS");
        add("blazing_aura", "Blazing Aura", "25% chance to set attackers on fire and slow them for 2s (5s cooldown)", Rarity.EPIC, "CHESTPLATE");

        add("ender_shift", "Ender Shift", "Teleport to last death location (1 use)", Rarity.LEGENDARY, "COMPASS");
        add("midas_touch", "Midas Touch", "5% chance for double drops,Wont work with Silktouch", Rarity.EPIC, "PICKAXE,SHOVEL,AXE");
        add("flame_king", "Flame King", "Negates Slowness effects automatically", Rarity.EPIC, "CHESTPLATE");
    }

    private static void add(String id, String name, String desc, Rarity rarity, String appliesTo) {
        EnchantData data = new EnchantData(id, name, desc, rarity, appliesTo);
        enchantPool.get(rarity).add(data);
        enchantMap.put(id.toLowerCase(), data);
    }

    public static EnchantData getEnchantData(String id) {
        return enchantMap.get(id.toLowerCase());
    }

    public static Map<String, EnchantData> getAllEnchantments() {
        return enchantMap;
    }

    public static boolean trySpin(Player player, Rarity tier, int xpCost) {
        if (player.getLevel() < xpCost) {
            player.sendMessage(ChatColor.RED + "You need " + xpCost + " XP levels to spin.");
            return false;
        }

        player.setLevel(player.getLevel() - xpCost);
        EnchantData selected = rollEnchant(tier);
        ItemStack book = createEnchantBook(selected);
        player.getInventory().addItem(book);

        player.sendMessage(ChatColor.GREEN + "You received: " + ChatColor.LIGHT_PURPLE + selected.name);
        return true;
    }

    private static EnchantData rollEnchant(Rarity tier) {
        Random rand = new Random();

        Map<Rarity, Double> weights = switch (tier) {
            case COMMON -> Map.of(Rarity.COMMON, 1.0);
            case RARE -> Map.of(Rarity.COMMON, 0.5, Rarity.RARE, 0.5);
            case EPIC -> Map.of(Rarity.RARE, 0.4, Rarity.EPIC, 0.6);
            case LEGENDARY -> Map.of(Rarity.EPIC, 0.3, Rarity.LEGENDARY, 0.7);
            case MYTHIC -> Map.of(Rarity.LEGENDARY, 0.5, Rarity.MYTHIC, 0.5);
        };

        List<EnchantData> pool = new ArrayList<>();
        for (Map.Entry<Rarity, Double> entry : weights.entrySet()) {
            List<EnchantData> sublist = enchantPool.get(entry.getKey());
            for (EnchantData data : sublist) {
                for (int i = 0; i < (int) (entry.getValue() * 10); i++) {
                    pool.add(data);
                }
            }
        }

        return pool.get(rand.nextInt(pool.size()));
    }

    public static ItemStack createEnchantBook(EnchantData data) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();

        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + data.name);
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + data.description,
                ChatColor.DARK_GRAY + "Applicable to: " + data.itemType,
                ChatColor.DARK_PURPLE + "Rarity: " + data.rarity.name(),
                ChatColor.YELLOW + "Drop this book onto an item to apply."
        ));

        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(EnchantPlus.getInstance(), "custom_enchant");
        container.set(key, PersistentDataType.STRING, data.id);

        book.setItemMeta(meta);
        return book;
    }

    public static List<EnchantData> getEnchantsByRarity(Rarity rarity) {
        return enchantPool.getOrDefault(rarity, Collections.emptyList());
    }

    public static List<EnchantData> getPossibleEnchantsForSpin(Rarity tier) {
        Map<Rarity, Double> weights = switch (tier) {
            case COMMON -> Map.of(Rarity.COMMON, 1.0);
            case RARE -> Map.of(Rarity.COMMON, 0.5, Rarity.RARE, 0.5);
            case EPIC -> Map.of(Rarity.RARE, 0.4, Rarity.EPIC, 0.6);
            case LEGENDARY -> Map.of(Rarity.EPIC, 0.3, Rarity.LEGENDARY, 0.7);
            case MYTHIC -> Map.of(Rarity.LEGENDARY, 0.5, Rarity.MYTHIC, 0.5);
        };

        List<EnchantData> combined = new ArrayList<>();
        for (Map.Entry<Rarity, Double> entry : weights.entrySet()) {
            combined.addAll(enchantPool.getOrDefault(entry.getKey(), Collections.emptyList()));
        }
        return combined;
    }

    public static String getDisplayNameForEnchant(String id) {
        EnchantData data = getEnchantData(id);
        return data != null ? data.name : id;
    }

    // 🔧 FOR TESTING: Give all books to player
    public static void giveAllBooks(Player player) {
        for (EnchantData data : enchantMap.values()) {
            player.getInventory().addItem(createEnchantBook(data));
        }
        player.sendMessage(ChatColor.GOLD + "All custom enchant books have been added to your inventory.");
    }
}
