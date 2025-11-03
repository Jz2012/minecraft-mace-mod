# Usage Examples for Mace Enchantments

This document provides detailed examples of how to use the custom mace enchantments in Minecraft 1.21.1.

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Installation](#installation)
3. [Enchantment Commands](#enchantment-commands)
4. [Gameplay Examples](#gameplay-examples)
5. [Technical Implementation](#technical-implementation)

## Prerequisites

- Minecraft 1.21.1
- Fabric Loader 0.16.5+
- Fabric API 0.105.0+1.21.1
- This mod installed in your mods folder

## Installation

1. Download and install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.1
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) and place it in `.minecraft/mods`
3. Download this mod and place it in `.minecraft/mods`
4. Launch Minecraft with the Fabric profile

## Enchantment Commands

### Giving Enchanted Maces

Use these commands to give yourself a mace with each enchantment:

```mcfunction
# Nuke Enchantment
/give @p minecraft:mace{Enchantments:[{id:"macemod:nuke",lvl:1}]}

# Shockwave Enchantment
/give @p minecraft:mace{Enchantments:[{id:"macemod:shockwave",lvl:1}]}

# Lightning Enchantment
/give @p minecraft:mace{Enchantments:[{id:"macemod:lightning",lvl:1}]}

# Combination of all three (very powerful!)
/give @p minecraft:mace{Enchantments:[{id:"macemod:nuke",lvl:1},{id:"macemod:shockwave",lvl:1},{id:"macemod:lightning",lvl:1}]}
```

### Enchanting an Existing Mace

Hold a mace in your main hand and run:

```mcfunction
# Add Nuke enchantment
/enchant @p macemod:nuke 1

# Add Shockwave enchantment
/enchant @p macemod:shockwave 1

# Add Lightning enchantment
/enchant @p macemod:lightning 1
```

**Note:** You can use an anvil to combine enchanted books with your mace in survival mode.

## Gameplay Examples

### Example 1: Nuke Enchantment

**Scenario:** Clearing out a mob spawner room

1. Build a tall pillar (at least 5 blocks high) near the mob spawner
2. Equip your mace enchanted with Nuke
3. Jump off the pillar
4. While falling, aim at a mob near the center of the room
5. Strike the mob mid-fall
6. **Result:** 100 TNT entities spawn at the mob's location and explode instantly, destroying the spawner and all surrounding mobs

**Caution:** This will cause significant terrain destruction!

### Example 2: Shockwave Enchantment

**Scenario:** Fighting a horde of zombies

1. Build a 10-block tall tower
2. Equip your mace enchanted with Shockwave
3. Wait for zombies to gather around the base
4. Jump and strike one zombie while falling
5. **Result:** All zombies within a 10×10 block radius take the same damage as your smash attack (scales with fall distance)

**Tip:** The higher you fall from, the more damage the shockwave deals!

### Example 3: Lightning Enchantment

**Scenario:** Defeating the Ender Dragon's minions

1. Climb to a high point in the End
2. Equip your mace enchanted with Lightning
3. Jump and strike an enderman while falling
4. **Result:** Lightning bolts strike all entities within a 10-block radius (except you), dealing heavy damage

**Note:** Lightning can set mobs on fire and create charged creepers if they survive!

### Example 4: Combined Enchantments

**Scenario:** Ultimate destruction (creative mode recommended)

1. Create a mace with all three enchantments
2. Find an area you want to completely devastate
3. Jump from a significant height (20+ blocks)
4. Strike any mob
5. **Result:**
   - 100 TNT explodes at impact
   - All nearby entities take massive smash damage
   - Lightning strikes everything in range
   - Complete and utter chaos!

## Technical Implementation

### How Smash Attacks Work

The enchantments only activate during a **smash attack**, which occurs when:

1. You are falling (fall distance > 1.5 blocks)
2. You strike a living entity with the mace
3. You are playing as a player entity

### Damage Calculation

The Shockwave enchantment damage is calculated as:

```
damage = 7.0 + (fall_distance × 4.0)
```

**Examples:**
- 3 blocks fall = 7.0 + (3 × 4.0) = **19 damage** (9.5 hearts)
- 5 blocks fall = 7.0 + (5 × 4.0) = **27 damage** (13.5 hearts)
- 10 blocks fall = 7.0 + (10 × 4.0) = **47 damage** (23.5 hearts)

### Area of Effect

- **Nuke:** Point target (100 TNT at exact target location)
- **Shockwave:** 10×10×10 block cube centered on target
- **Lightning:** 10×10×10 block cube centered on target

### Exclusions

- **Shockwave:** Does not damage the attacker
- **Lightning:** Does not strike the attacker
- **Nuke:** May harm the attacker if too close to explosion

## Code Integration Examples

### Registering Custom Enchantments

The mod registers enchantments during initialization:

```java
public class ModEnchantments {
    public static final RegistryKey<Enchantment> NUKE = RegistryKey.of(
        RegistryKeys.ENCHANTMENT, 
        Identifier.of("macemod", "nuke")
    );
    
    public static void register() {
        Registry.register(
            Registries.ENCHANTMENT, 
            NUKE.getValue(), 
            new NukeEnchantment()
        );
        // ... register other enchantments
    }
}
```

### Mixin Hook Implementation

The mod uses a Mixin to inject into the MaceItem's postHit method:

```java
@Mixin(MaceItem.class)
public class MaceItemMixin {
    @Inject(method = "postHit", at = @At("TAIL"))
    private void onPostHit(ItemStack stack, LivingEntity target, 
                           LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        // Check for smash attack conditions
        if (player.fallDistance <= 1.5f) return;
        
        // Trigger enchantment effects
        ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments(stack);
        for (RegistryEntry<Enchantment> entry : enchantments.getEnchantments()) {
            // Check and activate each enchantment
        }
    }
}
```

### Creating Custom Enchantment Effects

Each enchantment extends the base Enchantment class:

```java
public class NukeEnchantment extends Enchantment {
    public NukeEnchantment() {
        super(
            Enchantment.Rarity.VERY_RARE, 
            EnchantmentTarget.WEAPON, 
            new EquipmentSlot[]{EquipmentSlot.MAINHAND}
        );
    }
    
    public static void activate(Entity target) {
        // Spawn 100 TNT at target location
        for (int i = 0; i < 100; i++) {
            TntEntity tnt = new TntEntity(
                target.getWorld(), 
                targetPos.x, targetPos.y, targetPos.z, 
                null
            );
            tnt.setFuse(0); // Explode immediately
            target.getWorld().spawnEntity(tnt);
        }
    }
}
```

## Tips and Tricks

1. **Fall Distance:** Higher falls = more damage with Shockwave
2. **Positioning:** Strike mobs at the center of groups for maximum Shockwave/Lightning effect
3. **Safety:** Use Feather Falling boots to survive high falls
4. **Combinations:** Multiple enchantments stack their effects
5. **Terrain Protection:** Use explosion-resistant blocks around valuable structures when using Nuke

## Troubleshooting

**Q: The enchantments aren't activating**
- Ensure you're falling at least 1.5 blocks before impact
- Check that you're hitting a living entity
- Verify the mod is installed correctly

**Q: The Nuke enchantment causes too much lag**
- This is expected with 100 TNT explosions
- Consider using fewer TNT or a different enchantment for better performance

**Q: Can I use these enchantments on other weapons?**
- No, they are specifically designed for the mace weapon only
- The code checks for MaceItem specifically

## License

MIT License - Feel free to modify and redistribute
