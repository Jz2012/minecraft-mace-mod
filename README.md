# Minecraft Mace Mod

A Fabric mod for Minecraft 1.21.1 that adds three powerful custom enchantments for the Mace weapon. Each enchantment activates only during a mace smash attack (when falling and striking an enemy).

## Features

### Custom Enchantments

1. **Nuke** - Spawns and instantly explodes 100 TNT at the target's location
   - Rarity: Very Rare
   - Max Level: 1
   - Creates massive destruction at impact point

2. **Shockwave** - Deals the same damage as the smash attack to all mobs and players in a 10×10 radius
   - Rarity: Very Rare
   - Max Level: 1
   - Excludes the attacker from damage
   - Damage scales with fall distance

3. **Lightning** - Strikes all entities in a 10-block radius with lightning
   - Rarity: Very Rare
   - Max Level: 1
   - Excludes the attacker
   - Summons actual lightning bolts

## Requirements

- Minecraft 1.21.1
- Fabric Loader 0.16.5 or higher
- Fabric API 0.105.0+1.21.1 or higher
- Java 21 or higher

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/)
2. Download [Fabric API](https://modrinth.com/mod/fabric-api)
3. Place both Fabric API and this mod in your `.minecraft/mods` folder
4. Launch Minecraft with the Fabric profile

## Building from Source

```bash
./gradlew build
```

The compiled jar will be in `build/libs/`

## Usage Examples

### In-Game Usage

1. **Obtaining Enchantments:**
   - Use an enchanting table with a Mace
   - Use an anvil to combine enchanted books with your Mace
   - Use creative mode: `/enchantedmace nuke`

2. **Activating Enchantments:**
   - Jump or fall from a height (at least 1.5 blocks)
   - Strike an enemy with your enchanted Mace mid-fall
   - The enchantment effect triggers on impact

### Commands

Give yourself an enchanted mace:

```
/enchantedmace nuke
/enchantedmace lightning
/enchantedmace shockwave
```

Enchant a held mace:

```
/enchant @p macemod:nuke 1
/enchant @p macemod:shockwave 1
/enchant @p macemod:lightning 1
```

### Code Usage Examples

#### Registering Enchantments

The mod automatically registers all enchantments on initialization:

```java
public class MaceMod implements ModInitializer {
    @Override
    public void onInitialize() {
        ModEnchantments.register();
    }
}
```

#### Custom Enchantment Implementation

Each enchantment extends the base `Enchantment` class and provides a static `activate()` method:

```java
// Nuke Enchantment
NukeEnchantment.activate(target);

// Shockwave Enchantment
ShockwaveEnchantment.activate(target, attacker, damage);

// Lightning Enchantment
LightningEnchantment.activate(target, attacker);
```

#### Mixin Hook

The `MaceItemMixin` hooks into the mace's `postHit` method to detect smash attacks:

```java
@Inject(method = "postHit", at = @At("TAIL"))
private void onPostHit(ItemStack stack, LivingEntity target, LivingEntity attacker, 
                       CallbackInfoReturnable<Boolean> cir) {
    // Check if smash attack (fall distance > 1.5 blocks)
    if (player.fallDistance <= 1.5f) return;
    
    // Trigger enchantments based on what's on the mace
    if (hasNukeEnchantment(stack)) {
        NukeEnchantment.activate(target);
    }
    // ... other enchantments
}
```

## Technical Details

### Enchantment Activation

All enchantments only activate when:
1. The attacker is a player
2. The player has fallen at least 1.5 blocks
3. The mace successfully hits a living entity
4. The enchantment is present on the mace

### Damage Calculation

The Shockwave enchantment calculates damage based on fall distance:
```
damage = 7.0 + (fallDistance × 4.0)
```

### Area of Effect

Both Shockwave and Lightning use a 10-block radius (10×10×10 cube) centered on the target.

## License

MIT

## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues.

## Credits

- Built with [Fabric](https://fabricmc.net/)
- Uses [Fabric API](https://github.com/FabricMC/fabric)
