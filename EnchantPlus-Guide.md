# EnchantPlus Complete Guide

## 🎰 Spin System Overview

The EnchantPlus spin system allows players to obtain custom enchantments through a randomized wheel spinning mechanic. Players spend XP to spin for enchantments of different rarity tiers.

### 💰 XP Costs by Rarity
- **COMMON**: 20 XP
- **RARE**: 30 XP  
- **EPIC**: 40 XP
- **LEGENDARY**: 60 XP
- **MYTHIC**: 80 XP

### 📢 Broadcast System
- **LEGENDARY and MYTHIC** wins are broadcast to all players
- Other rarities are private to the spinning player

---

## 🔮 Enchantment List by Rarity

### ⚪ COMMON Enchantments

#### **Auto Replant** (Level I-III)
- **Description**: Fully grown crops replant themselves on break
- **Applies to**: Hoe
- **Effect**: When breaking fully grown crops, they automatically replant using 1 seed from inventory
- **Level Scaling**: 
  - Level I: Basic replant
  - Level II: 10% chance to save seed
  - Level III: 20% chance to save seed + 5% bonus crop yield
- **Conflicts**: None
- **Notes**: Supports wheat, carrots, potatoes, beetroots, and nether wart
- **Flavor**: *"The soil remembers the seed."*
- **Bless**: The land serves those who tend it.

#### **Angler** (Level I-III)
- **Description**: Faster fishing bite time
- **Applies to**: Fishing Rod
- **Effect**: Reduces fishing wait time when enchant is active
- **Level Scaling**: 
  - Level I: 25% faster bite time
  - Level II: 35% faster bite time + 10% treasure chance
  - Level III: 50% faster bite time + 15% treasure chance
- **Conflicts**: None
- **Notes**: Perfect for efficient fishing
- **Flavor**: *"Patience is the oldest hook."*
- **Bless**: The water yields to the still hand.

#### **Villager's Deal** (Level I-III)
- **Description**: Throw at a villager for trade discount (30 mins)
- **Applies to**: Splash Potion
- **Effect**: Splash potion applies discount to villager trades for 30 minutes
- **Level Scaling**: 
  - Level I: 50% discount
  - Level II: 60% discount + 5 min duration
  - Level III: 75% discount + 10 min duration
- **Conflicts**: None
- **Notes**: Potion becomes locked after enchant application
- **Flavor**: *"Gold speaks, but kindness speaks louder."*
- **Bless**: One gift. Thirty minutes of gratitude.

#### **Farmer Bees** (Level I-III)
- **Description**: Summons worker bees to tend nearby crops
- **Applies to**: Diamond Hoe, Netherite Hoe
- **Effect**: Spawns worker bees that bonemeal crops for 60 seconds
- **Level Scaling**: 
  - Level I: 3 bees, 60s duration, 24h cooldown
  - Level II: 4 bees, 90s duration, 18h cooldown  
  - Level III: 5 bees, 120s duration, 12h cooldown
- **Conflicts**: None
- **Notes**: Durability cost system with exponential scaling
- **Flavor**: *"The hive serves those who serve the land."*
- **Bless**: Patience grows faster than wheat.

---

### 🔵 RARE Enchantments

#### **Forge Touch** (Level I-III)
- **Description**: Auto-smelts ores instantly to inventory
- **Applies to**: Pickaxe
- **Effect**: When mining ores, they are automatically smelted and placed directly in your inventory
- **Level Scaling**: 
  - Level I: Basic auto-smelt
  - Level II: 10% chance for double smelt
  - Level III: 20% chance for double smelt + 5% XP bonus
- **Conflicts**: None
- **Notes**: Perfect for efficient mining without furnace trips
- **Flavor**: *"Fire is merely another tool."*
- **Bless**: The furnace bows to those who carry it.

#### **Timberfall** (Level I-III)
- **Description**: Fells entire tree when one log is broken
- **Applies to**: Axe
- **Effect**: Breaking any log of a tree will cause the entire tree to break
- **Level Scaling**: 
  - Level I: Basic tree felling
  - Level II: 15% chance for extra logs
  - Level III: 30% chance for extra logs + stripped log support
- **Conflicts**: None
- **Notes**: Great for large-scale wood gathering, now supports stripped logs
- **Flavor**: *"The forest remembers every tree that falls."*
- **Curse**: One cut calls them all.

#### **Terraformer** (Level I-III)
- **Description**: Breaks 3x3 block area (horizontal)
- **Applies to**: Shovel, Pickaxe
- **Effect**: Breaking a block breaks all adjacent blocks in 3x3 pattern
- **Level Scaling**: 
  - Level I: 3x3 area
  - Level II: 3x3 area + 10% chance to save durability
  - Level III: 3x3 area + 20% chance to save durability
- **Conflicts**: None
- **Notes**: Excellent for large excavation projects
- **Flavor**: *"The earth yields to those who persist."*
- **Curse**: The ground does not forget what you took.

#### **Midas Touch** (Level I-III)
- **Description**: Chance for double drops, won't work with Silk Touch
- **Applies to**: Pickaxe, Shovel, Axe
- **Effect**: Chance to double block drops on break
- **Level Scaling**: 
  - Level I: 5% chance
  - Level II: 8% chance
  - Level III: 12% chance
- **Conflicts**: Silk Touch (won't activate with it)
- **Notes**: Small chance for bonus resources
- **Flavor**: *"Gold hungers only for more gold."*
- **Curse**: Greed is its own reward.

---

### 🟣 EPIC Enchantments

#### **Storm's Fetters** (Level I-III) (formerly Neural Overload)
- **Description**: Electrifies weakened targets — surges every 3s
- **Applies to**: Bow
- **Effect**: 
  - Activates on arrow hit when target is ≤ 75% health
  - Applies "Electrified" state for 9 seconds
  - Every 3 seconds: 50% chance for either 1-2 heart magic damage OR Slowness IV + Mining Fatigue II for 2s
  - Visual effects: purple particles + zap sounds
  - 10-second internal cooldown per shooter
- **Level Scaling**: 
  - Level I: Base effects
  - Level II: 12s duration + 25% stronger effects
  - Level III: 15s duration + 50% stronger effects
- **Conflicts**: None
- **Notes**: Powerful debuff enchant for sustained bow combat
- **Flavor**: *"When the blood flows, the heavens notice."*
- **Curse**: The tethered cannot outrun the storm.

#### **Blazing Aura** (Level I-III)
- **Description**: Chance to set attackers on fire and slow them
- **Applies to**: Chestplate
- **Effect**: When attacked, chance to ignite and slow attacker
- **Level Scaling**: 
  - Level I: 25% chance, 2s duration, 5s cooldown
  - Level II: 30% chance, 3s duration, 4s cooldown
  - Level III: 35% chance, 4s duration, 3s cooldown + nearby ignition (players excluded)
- **Conflicts**: None
- **Notes**: Defensive fire damage enchant, Level III no longer griefs players
- **Flavor**: *"To wear flame is to invite it."*
- **Curse**: Your fire is their warning.

#### **Frostbite** (Level I-III)
- **Description**: Chance to apply Slowness IV
- **Applies to**: Sword, Trident
- **Effect**: On hit, chance to apply Slowness IV
- **Level Scaling**: 
  - Level I: 25% chance, 5s duration
  - Level II: 30% chance, 6s duration
  - Level III: 35% chance, 7s duration + 10% weakness chance
- **Conflicts**: None
- **Notes**: Crowd control for melee combat
- **Flavor**: *"Cold finds every gap in every armour."*
- **Curse**: The slow die cold.

---

### 🟠 LEGENDARY Enchantments

#### **Void Strike** (Level I-III)
- **Description**: Bonus damage vs End mobs
- **Applies to**: Sword, Axe, Bow, Crossbow, Trident
- **Effect**: Chance to deal bonus damage against End dimension mobs
- **Level Scaling**: 
  - Level I: 15% chance for 4x damage
  - Level II: 20% chance for 5x damage
  - Level III: 25% chance for 6x damage
- **Conflicts**: None
- **Notes**: Essential for End dimension combat
- **Flavor**: *"The void sees those who look into it."*
- **Curse**: End mobs smell the void on your blade.

#### **Soulbound** (Level I-III)
- **Description**: Keeps item on death (charges system)
- **Applies to**: Sword, Axe, Pickaxe, Shovel, Trident, Chestplate, Boots, Helmet, Leggings
- **Effect**: Item stays in inventory on death (consumes 1 charge per death)
- **Level Scaling**: 
  - Level I: 3 charges
  - Level II: 4 charges
  - Level III: 5 charges + overflow protection
- **Conflicts**: None
- **Notes**: Prevents losing valuable gear, handles full inventory safely
- **Flavor**: *"Some prices are paid after death."*
- **Curse**: Each life spent dims the enchant.

#### **Netherstride** (Level I-III)
- **Description**: Walk on lava for 2 seconds per block
- **Applies to**: Boots
- **Effect**: Passive — auto-triggers when walking over lava, creates temporary platform
- **Level Scaling**: 
  - Level I: 2s per block
  - Level II: 3s per block + fire resistance
  - Level III: 4s per block + fire resistance + 10% speed boost
- **Conflicts**: None
- **Notes**: Safe lava traversal in Nether
- **Flavor**: *"Lava is just slow ground."*
- **Bless**: The Nether opens a path for the bold.

#### **Ender Shift** (Level I-III)
- **Description**: Teleport to last death location
- **Applies to**: Compass
- **Effect**: Right-click to teleport to your last death location
- **Level Scaling**: 
  - Level I: 1 use, 30min expiry
  - Level II: 2 uses, 45min expiry
  - Level III: 3 uses, 60min expiry
- **Conflicts**: None
- **Notes**: Quick recovery from death locations, memory leak fixed
- **Flavor**: *"Death leaves a door ajar."*
- **Curse**: You only get limited chances to go back.

#### **Reactive Aura** (Level I-III) (formerly Flame King)
- **Description**: Absorbs movement debuffs and grants Speed II
- **Applies to**: Chestplate
- **Effect**: 
  - Absorbs incoming Slowness, Levitation, Mining Fatigue effects
  - Grants Speed II for 3 seconds when absorbing
  - 12-second cooldown per player
- **Level Scaling**: 
  - Level I: Base effects
  - Level II: 4s duration + Speed III
  - Level III: 5s duration + Speed III + jump boost
- **Conflicts**: None
- **Notes**: Defensive mobility enchant
- **Flavor**: *"Motion cannot be caged."*
- **Bless**: Every chain becomes a launchpad.

#### **Inferno Core** (Level I-III)
- **Description**: Burn to gain Strength + Haste. Fire immunity while active.
- **Applies to**: Leggings
- **Effect**: 
  - Fire immunity when not protected by other sources
  - When on fire: gain Strength and Haste buffs
  - Buffs refresh every 2 seconds while burning
  - Buffs stop when fire stops
- **Level Scaling**: 
  - Level I: Strength I + Haste I
  - Level II: Strength I + Haste II + 5% damage reduction
  - Level III: Strength II + Haste II + 10% damage reduction
- **Conflicts**: None (works with other fire protection)
- **Notes**: Turn fire damage into power buffs
- **Flavor**: *"What burns you makes you stronger."*
- **Curse**: The fire must be fed.

---

### 🔴 MYTHIC Enchantments

#### **Soul Siphon** (Level I-III)
- **Description**: On kill: grants absorption hearts
- **Applies to**: Sword, Axe
- **Effect**: Guaranteed activation when a PLAYER or MOB is killed. Grants absorption hearts lasting 2 minutes. Absorption cannot stack beyond 4 hearts (8 HP) total.
- **Level Scaling**: 
  - Level I: 2 hearts on player kill
  - Level II: 3 hearts on player kill, 2 hearts on mob kill
  - Level III: 4 hearts on player kill, 3 hearts on mob kill
- **Conflicts**: None
- **Notes**: Sustainable combat healing for PvP and PvE
- **Flavor**: *"Every kill feeds the blade."*
- **Curse**: Only a warrior's death sates it.

#### **Phoenix Aura** (Level I-III)
- **Description**: Auto-revive on death with cooldown
- **Applies to**: Chestplate
- **Effect**: Automatic resurrection on death with full health
- **Level Scaling**: 
  - Level I: 24h cooldown, basic revive
  - Level II: 18h cooldown, 5s invincibility
  - Level III: 12h cooldown, 10s invincibility + 50% XP retention
- **Cooldown**: Stored on item, displays in lore
- **Conflicts**: None
- **Notes**: Ultimate life-saving enchant with cooldown tracking
- **Flavor**: *"Resurrection is never truly free."*
- **Curse**: The flame remembers. It will not burn twice.

---

## ⚔️ Combat Enchant Categories

### **Offensive Enchants**
- **Melee**: Void Strike, Frostbite, Soul Siphon
- **Ranged**: Storm's Fetters, Void Strike
- **Utility**: Midas Touch, Forge Touch

### **Defensive Enchants**
- **Armor**: Reactive Aura, Blazing Aura, Inferno Core, Phoenix Aura, Soulbound
- **Mobility**: Netherstride, Ender Shift

### **Tool Enchants**
- **Mining**: Forge Touch, Terraformer, Midas Touch
- **Woodcutting**: Timberfall
- **Farming**: Auto Replant, Farmer Bees

### **Special Enchants**
- **Trading**: Villager's Deal
- **Fishing**: Angler

---

## 🔄 Level System Features

### **Progressive Enhancement**
- All 22 enchantments now have Level I-III variants
- Each level provides meaningful stat improvements
- Higher levels maintain core mechanics with enhanced effects

### **Balance Considerations**
- Level I: Base functionality
- Level II: 20-30% improvement + minor bonus
- Level III: 40-60% improvement + significant bonus effect

### **Cooldown Management**
- Many enchants feature reduced cooldowns at higher levels
- Cooldowns tracked in item lore for transparency
- Smart cooldown system prevents exploits

---

## 🐛 Recent Bug Fixes & Performance Updates

### **Critical Fixes**
- ✅ **Farmer Bees**: Fixed cooldown saving and 24h reset logic
- ✅ **Blazing Aura**: Fixed Level III player griefing issue
- ✅ **SpinMenuGUI**: Fixed inventory overflow data loss
- ✅ **ActionBarUtil**: Fixed § color code rendering
- ✅ **EnderShift**: Fixed memory leaks with 30min expiry
- ✅ **Soulbound**: Added full inventory overflow protection

### **Performance Optimizations**
- ✅ **PlayerPlacedBlockTracker**: Async auto-save, thread-safe operations
- ✅ **EnchantGalleryGUI**: Cached dev skull prevents API lookups
- ✅ **All Systems**: Memory leak prevention and cleanup

### **Safety Improvements**
- ✅ **Crash Prevention**: Empty pool checks, defensive programming
- ✅ **Data Persistence**: Block tracking survives server restarts
- ✅ **Thread Safety**: Concurrent operations properly handled

---

## 💡 Pro Tips

1. **Level Strategy**: Higher levels provide significant advantages for investment
2. **XP Management**: Save XP for higher rarity spins - LEGENDARY and MYTHIC enchants are game-changing
3. **Armor Sets**: Combine defensive enchants (Reactive Aura + Inferno Core + Soulbound)
4. **Tool Specialization**: Use different tools for different tasks (Forge Touch for ores, Terraformer for clearing)
5. **Combat Strategy**: Storm's Fetters for bow PvP, Frostbite for melee crowd control
6. **Exploration**: Netherstride for Nether travel, Ender Shift for death recovery
7. **Farming**: Auto Replant for automated farming, Farmer Bees for rapid crop growth
8. **Trading**: Use Villager's Deal potions for discounted trades
9. **Cooldown Tracking**: Watch item lore for cooldown status
10. **Level Investment**: Focus on enchants you use most frequently

---

## 📊 Rarity Distribution

- **COMMON**: Basic utility and farming enchants (Level I-III)
- **RARE**: Quality of life improvements (Level I-III)
- **EPIC**: Combat and specialized utility (Level I-III)
- **LEGENDARY**: Game-changing abilities (Level I-III)
- **MYTHIC**: Ultimate powers with unique mechanics (Level I-III)

---

## 🎨 Lore System

Every enchant book now features:
- **Level Indicator**: Clear level designation (I, II, III)
- **Flavor Quote**: Thematic text in gray italic
- **Curse/Blessing**: Red curse or green blessing text
- **Enhanced Lore**: More detailed descriptions and mechanics
- **Cooldown Display**: Real-time cooldown status for applicable enchants

This adds depth and storytelling to each enchantment!

---

## 🚀 Performance & Stability

### **Optimized Systems**
- Async disk operations prevent TPS drops
- Thread-safe data structures prevent crashes
- Memory leak prevention ensures long-term stability
- Defensive programming prevents runtime exceptions

### **Data Safety**
- Persistent block tracking across restarts
- Overflow protection prevents item loss
- Cooldown persistence maintains game balance
- Backup systems protect player data

---

*Last Updated: EnchantPlus v2.0.0 Level System + Critical Bug Fixes*
*Total Enchantments: 22 (All with Level I-III variants)*
*Features: Complete Level System, Performance Optimizations, Bug Fixes*
*Status: Production Ready*
