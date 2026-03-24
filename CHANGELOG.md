# EnchantPlus Changelog

## Version 2.1.0 - Critical Bug Fixes + Performance Enhancements

### 🔴 Critical Fixes
- **SpinMenuGUI Level System**: Fixed entire level distribution bypass - rollLevel() now properly integrated
- **InventoryClickListener**: Prevented item corruption by moving potion validation before enchant application
- **VillagersDealEffect**: Fixed memory leak from uncanceled countdown timers
- **VillagersDealEffect**: Added EntityDeathEvent handler to clean up dead villagers

### 🟠 High Priority Fixes
- **FarmerBees**: Fixed countdown display showing wildly incorrect time values
- **SoulboundEffect**: Moved YAML I/O operations off main thread to prevent potential freezing

### 🔵 Minor Fixes
- **EnchantSpinManager**: Eliminated unnecessary Random object creation by using static field
- **Code Quality**: Removed stale comments and cleaned up dead code

### 🚀 Performance Improvements
- **Memory Management**: Proper timer tracking and cleanup prevents resource leaks
- **Thread Safety**: Asynchronous file operations prevent main thread blocking
- **Resource Optimization**: Eliminated redundant object creation

---

## Version 2.0.0 - Level System + Major Features

### ✨ Major Features
- **Complete Level System**: All enchantments now support Level I-III variants
- **Anvil Combining**: Same enchant + same level = next level (max 3)
- **Farmer Bees**: Smooth bee movement with roaming mode for full 60-second lifespan
- **Villager's Deal**: Enhanced with potion restrictions and discount refresh mechanics
- **Soulbound Items**: Persistent storage prevents item loss on disconnect

### 🎮 Gameplay Improvements
- **Level Distribution**: 50% Level I, 35% Level II, 15% Level III for 3-tier enchants
- **Visual Enhancements**: Improved particle effects and sound feedback
- **Balance Adjustments**: Refined enchantment power levels and cooldowns

### 🔧 Technical Improvements
- **Performance**: Optimized event handling and reduced memory footprint
- **Reliability**: Enhanced error handling and data persistence
- **Compatibility**: Updated for Paper 1.21.1

---

## Version 1.0.0 - Initial Release

### 🎯 Core Features
- **22 Custom Enchantments**: Complete enchantment system with unique effects
- **Galaxy Enchanter**: Custom NPC with animated spawning mechanics
- **Spin Menu GUI**: Interactive enchantment gambling system
- **Administrative Tools**: Full command suite for server management

### 🛡️ Enchantment Categories
- **Combat**: Thunderlord, Void Strike, Phoenix Aura, Flame King
- **Utility**: Ender Shift, Auto Replant, Villager's Deal, Soulbound
- **Resource**: Farmer Bees, Forge Touch, Midas Touch, Timberfall
- **Movement**: Netherstride, Blazing Aura, Inferno Core
- **Special**: Reactive Aura, Neural Overload, Terraformer

### 🏗️ Architecture
- **Modular Design**: Clean separation of concerns
- **Event-Driven**: Efficient Bukkit event handling
- **Persistent Data**: Reliable data storage across restarts
- **Performance Optimized**: Minimal server impact
