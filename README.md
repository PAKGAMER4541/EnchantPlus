# EnchantPlus

A custom enchantment plugin for Paper 1.21 with a spin/gacha system, enchant level tiers, NPC integration, and 19 unique enchants.

## ✨ Features

**19 Custom Enchantments** across all rarity tiers (Common → Mythic), each with flavor text and curse/blessing lore.

**Enchant Level System** — 14 enchants have I / II / III tiers with meaningfully different mechanics per level. Apply a higher-level book to upgrade an existing enchant.

**Spin / Gacha System** — Spend XP levels at the Galaxy Enchanter NPC to spin for random enchant books. Rarity tiers have weighted drop pools. LEGENDARY and MYTHIC wins broadcast to the server.

**Enchant Gallery GUI** — Browse all enchants by rarity with descriptions, flavor quotes, and applicable items.

**Galaxy Enchanter NPC** — Powered by FancyNpcs (soft dependency). Spawns a custom-skinned NPC with a gradient display name.

**Persistent Block Tracker** — Tracks player-placed blocks across restarts to prevent Midas Touch duplication exploits.

## 📜 Commands

| Command | Description | Permission |
|---|---|---|
| `/spawnenchanter` | Spawns a Galaxy Enchanter NPC at your location | `enchantplus.spawnenchanter` |
| `/removeenchanter` | Removes the nearest Galaxy Enchanter within 10 blocks | `enchantplus.removeenchanter` |
| `/giveallenchantbooks` | Gives all enchant books (admin/testing) | `enchantplus.giveallenchantbooks` |

All permissions default to **op**.

## ⚙️ Requirements

- PaperMC 1.21+
- Java 17+
- [FancyNpcs](https://modrinth.com/plugin/fancynpcs) *(optional — plugin works without it, NPCs just won't spawn)*

## 📦 Enchants

| Enchant           | Rarity     | Levels     | Slot                                 |
|-------------------|------------|------------|--------------------------------------|
| Auto Replant      | Common     | I          | Hoe                                  |
| Angler            | Common     | I/II/III   | Fishing Rod                          |
| Villager's Deal   | Common     | I          | Splash Potion                        |
| Farmer Bees       | Common     | I/II/III   | Diamond/Netherite Hoe                |
| Forge Touch       | Rare       | I/II       | Pickaxe                              |
| Timberfall        | Rare       | I          | Axe                                  |
| Terraformer       | Rare       | I          | Shovel, Pickaxe                      |
| Midas Touch       | Rare       | I/II/III   | Pickaxe, Shovel, Axe                 |
| Frostbite         | Epic       | I/II/III   | Sword, Trident                       |
| Blazing Aura      | Epic       | I/II/III   | Chestplate                           |
| Void Strike       | Legendary  | I          | Sword, Axe, Bow, Crossbow, Trident   |
| Storm's Fetters   | Legendary  | I          | Bow                                  |
| Soulbound         | Legendary  | I          | Most items                           |
| Netherstride      | Legendary  | I/II/III   | Boots                                |
| Reactive Aura     | Legendary  | I/II/III   | Chestplate                           |
| Inferno Core      | Legendary  | I/II/III   | Leggings                             |
| Ender Shift       | Legendary  | I          | Compass                              |
| Soul Siphon       | Mythic     | I/II/III   | Sword, Axe                           |
| Phoenix Aura      | Mythic     | I/II/III   | Chestplate                           |

## 📂 Installation

1. Download the latest `.jar` from [Releases](../../releases)
2. Drop it into your `plugins/` folder
3. Restart your server
4. Optionally install FancyNpcs and use `/spawnenchanter` to place the NPC
```

## What's new in v2.0

### New enchants
- Auto Replant, Angler, Villager's Deal, Farmer Bees

### New features
- Enchant level system (I/II/III) on 14 enchants with unique mechanics per tier
- Persistent placed-block tracker (survives server restarts)
- Enchant books show flavor quotes and curse/blessing lore
- Ender Shift death location expires after 30 minutes

### Dependency change
- Removed Citizens dependency, now uses FancyNpcs (soft depend — optional)
- Updated to Paper 1.21

### Bug fixes
- Fixed all memory leaks and world corruption paths
- Fixed action bar § color codes showing as raw text
- Fixed spin reward being silently deleted on full inventory
- Fixed FarmerBees cooldown never saving
- Removed synchronous disk I/O from block events (was causing TPS drops)
