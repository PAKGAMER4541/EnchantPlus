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

    public static class EnchantData {
        public final String id, name, description, itemType;
        public final Rarity rarity;
        public final String flavorQuote;
        public final String curseOrBlessing; // starts with "§cCurse: " or "§aBless: "
        public final int level;

        public EnchantData(String id, String name, String description, Rarity rarity, String itemType) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.rarity = rarity;
            this.itemType = itemType;
            this.flavorQuote = "";
            this.curseOrBlessing = "";
            this.level = 1;
        }

        public EnchantData(String id, String name, String description, Rarity rarity, String itemType, String flavorQuote, String curseOrBlessing) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.rarity = rarity;
            this.itemType = itemType;
            this.flavorQuote = flavorQuote;
            this.curseOrBlessing = curseOrBlessing;
            this.level = 1;
        }

        public EnchantData(String id, String name, String description, Rarity rarity, String itemType, String flavorQuote, String curseOrBlessing, int level) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.rarity = rarity;
            this.itemType = itemType;
            this.flavorQuote = flavorQuote;
            this.curseOrBlessing = curseOrBlessing;
            this.level = level;
        }
    }

    private static final Map<Rarity, List<EnchantData>> enchantPool = new HashMap<>();
    private static final Map<String, EnchantData> enchantMap = new HashMap<>();
    private static final Random rand = new Random();

    // Enchant level distribution sets
    private static final Set<String> singleLevelEnchants = Set.of(
        "auto_replant", "villagers_deal", "timberfall", "terraformer",
        "void_strike", "thunderlord", "soulbound", "ender_shift"
    );
    
    private static final Set<String> twoLevelEnchants = Set.of("forge_touch");

    public static int rollLevel(EnchantData data) {
        // Enchants with no level system always give level 1
        if (singleLevelEnchants.contains(data.id)) return 1;
        
        // Enchants with only 2 levels
        if (twoLevelEnchants.contains(data.id)) {
            // 65% Level I, 35% Level II
            return rand.nextDouble() < 0.65 ? 1 : 2;
        }
        
        // 3-level enchants: 50% I, 35% II, 15% III
        double roll = rand.nextDouble();
        if (roll < 0.50) return 1;
        if (roll < 0.85) return 2;
        return 3;
    }

    static {
        for (Rarity r : Rarity.values()) {
            enchantPool.put(r, new ArrayList<>());
        }

        // ---- FIXED itemType entries ----
        add("soul_siphon", "Soul Siphon", "Kill a player to gain 2 absorption hearts for 2 mins", Rarity.MYTHIC, "SWORD,AXE", "Every kill feeds the blade.", "§cCurse: §fOnly a warrior's death sates it.");
        add("frostbite", "Frostbite", "25% chance to apply Slowness IV (5s)", Rarity.EPIC, "SWORD,TRIDENT", "Cold finds every gap in every armour.", "§cCurse: §fThe slow die cold.");
        add("void_strike", "Void Strike", "+300% damage vs End mobs (15% chance)", Rarity.LEGENDARY, "SWORD,AXE,BOW,CROSSBOW,TRIDENT", "The void sees those who look into it.", "§cCurse: §fEnd mobs smell the void on your blade.");
        add("thunderlord", "Storm's Fetters", "Electrifies weakened targets — surges every 3s", Rarity.LEGENDARY, "BOW", "When the blood flows, the heavens notice.", "§cCurse: §fThe tethered cannot outrun the storm.");

        add("forge_touch", "Forge Touch", "Auto-smelts ores instantly to inventory", Rarity.RARE, "PICKAXE", "Fire is merely another tool.", "§aBless: §fThe furnace bows to those who carry it.");
        add("timberfall", "Timberfall", "Fells entire tree when one log is broken", Rarity.RARE, "AXE", "The forest remembers every tree that falls.", "§cCurse: §fOne cut calls them all.");
        add("terraformer", "Terraformer", "Breaks 3x3 block area (horizontal)", Rarity.RARE, "SHOVEL,PICKAXE", "The earth yields to those who persist.", "§cCurse: §fThe ground does not forget what you took.");
        add("soulbound", "Soulbound", "Keeps item on death (up to 3 times)", Rarity.LEGENDARY, "SWORD,AXE,PICKAXE,SHOVEL,TRIDENT,CHESTPLATE,BOOTS,HELMET,LEGGINGS", "Some prices are paid after death.", "§cCurse: §fEach life spent dims the enchant.");

        add("phoenix_aura", "Phoenix Aura", "Auto-revive on death (24h cooldown)", Rarity.MYTHIC, "CHESTPLATE", "Resurrection is never truly free.", "§cCurse: §fThe flame remembers. It will not burn twice.");
        add("netherstride", "Netherstride", "Walk on lava for 10s with visual platform", Rarity.LEGENDARY, "BOOTS", "Lava is just slow ground.", "§aBless: §fThe Nether opens a path for the bold.");
        add("blazing_aura", "Blazing Aura", "25% chance to set attackers on fire and slow them for 2s (5s cooldown)", Rarity.EPIC, "CHESTPLATE", "To wear flame is to invite it.", "§cCurse: §fYour fire is their warning.");

        add("ender_shift", "Ender Shift", "Teleport to last death location (1 use)", Rarity.LEGENDARY, "COMPASS", "Death leaves a door ajar.", "§cCurse: §fYou only get one chance to go back.");
        add("midas_touch", "Midas Touch", "5% chance for double drops,Wont work with Silktouch", Rarity.RARE, "PICKAXE,SHOVEL,AXE", "Gold hungers only for more gold.", "§cCurse: §fGreed is its own reward.");
        add("flame_king", "Reactive Aura", "Absorbs movement debuffs and grants Speed II for 3s (12s cooldown)", Rarity.LEGENDARY, "CHESTPLATE", "Motion cannot be caged.", "§aBless: §fEvery chain becomes a launchpad.");
    add("inferno_core", "Inferno Core", "Burn to gain Strength I + Haste I. Fire immunity while active.", Rarity.LEGENDARY, "LEGGINGS", "What burns you makes you stronger.", "§cCurse: §fThe fire must be fed.");

        // New COMMON enchants
        add("auto_replant", "Auto Replant", "Fully grown crops replant themselves on break", Rarity.COMMON, "HOE", "The soil remembers the seed.", "§aBless: §fThe land serves those who tend it.");
        add("angler", "Angler", "25% faster fishing bite time", Rarity.COMMON, "FISHING_ROD", "Patience is the oldest hook.", "§aBless: §fThe water yields to the still hand.");
        add("villagers_deal", "Villager's Deal", "Throw at a villager for 50% trade discount (30 mins)", Rarity.COMMON, "SPLASH_POTION", "Gold speaks, but kindness speaks louder.", "§aBless: §fOne gift. Thirty minutes of gratitude.");
        add("farmer_bees", "Farmer Bees", "Summons worker bees to tend nearby crops for 1 minute", Rarity.COMMON, "DIAMOND_HOE,NETHERITE_HOE", "The hive serves those who serve the land.", "§aBless: §fPatience grows faster than wheat.");

        // TODO: add 2 more COMMON enchants
        // add("placeholder_1", "TBD", "TBD", Rarity.COMMON, "TBD");
        // add("placeholder_2", "TBD", "TBD", Rarity.COMMON, "TBD");
    }

    private static void add(String id, String name, String desc, Rarity rarity, String appliesTo) {
        EnchantData data = new EnchantData(id, name, desc, rarity, appliesTo);
        enchantPool.get(rarity).add(data);
        enchantMap.put(id.toLowerCase(), data);
    }

    private static void add(String id, String name, String desc, Rarity rarity, String appliesTo, String flavorQuote, String curseOrBlessing) {
        EnchantData data = new EnchantData(id, name, desc, rarity, appliesTo, flavorQuote, curseOrBlessing);
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

        // Check if pool is empty to prevent crash
        List<EnchantData> possibleEnchants = getPossibleEnchantsForSpin(tier);
        if (possibleEnchants.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No enchants available for this tier.");
            player.setLevel(player.getLevel() + xpCost); // Refund XP
            return false;
        }

        player.setLevel(player.getLevel() - xpCost);
        EnchantData selected = rollEnchant(tier);
        int level = rollLevel(selected);
        ItemStack book = createEnchantBook(selected, level);
        player.getInventory().addItem(book);

        player.sendMessage(ChatColor.GREEN + "You received: " + ChatColor.LIGHT_PURPLE + selected.name + " " + toRoman(level));
        return true;
    }

    private static EnchantData rollEnchant(Rarity tier) {

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

    public static ItemStack createEnchantBook(EnchantData data, int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();
        
        // Display name includes Roman numeral if level > 1
        String levelSuffix = " " + toRoman(level); // always show level
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + data.name + levelSuffix);
        
        // Lore same as before
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + data.description);
        if (!data.flavorQuote.isEmpty()) {
            lore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "\"" + data.flavorQuote + "\"");
        }
        if (!data.curseOrBlessing.isEmpty()) {
            lore.add(data.curseOrBlessing);
        }
        lore.add(ChatColor.DARK_GRAY + "Applicable to: " + data.itemType);
        lore.add(data.rarity.display() + " Rarity");
        lore.add(ChatColor.YELLOW + "Right-click an item to apply.");
        meta.setLore(lore);
        
        // Store enchant ID and level in PDC
        NamespacedKey enchantKey = new NamespacedKey(EnchantPlus.getInstance(), "custom_enchant");
        NamespacedKey levelKey = new NamespacedKey(EnchantPlus.getInstance(), "custom_enchant_level");
        meta.getPersistentDataContainer().set(enchantKey, PersistentDataType.STRING, data.id);
        meta.getPersistentDataContainer().set(levelKey, PersistentDataType.INTEGER, level);
        
        book.setItemMeta(meta);
        return book;
    }

    // Keep old signature for backward compat (defaults to level 1)
    public static ItemStack createEnchantBook(EnchantData data) {
        return createEnchantBook(data, 1);
    }

    private static String toRoman(int level) {
        return switch (level) {
            case 1 -> "I"; case 2 -> "II"; case 3 -> "III";
            case 4 -> "IV"; case 5 -> "V"; default -> String.valueOf(level);
        };
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
