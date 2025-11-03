# Implementation Details

This document provides technical details about how the custom mace enchantments are implemented.

## Project Structure

```
minecraft-mace-mod/
├── src/main/
│   ├── java/com/jz2012/macemod/
│   │   ├── MaceMod.java                    # Main mod class
│   │   ├── ModEnchantments.java            # Enchantment registry
│   │   ├── enchantment/
│   │   │   ├── NukeEnchantment.java       # Nuke enchantment implementation
│   │   │   ├── ShockwaveEnchantment.java  # Shockwave enchantment implementation
│   │   │   └── LightningEnchantment.java  # Lightning enchantment implementation
│   │   └── mixin/
│   │       └── MaceItemMixin.java         # Mixin to hook mace attacks
│   └── resources/
│       ├── fabric.mod.json                 # Mod metadata
│       ├── macemod.mixins.json            # Mixin configuration
│       └── assets/macemod/lang/
│           └── en_us.json                  # English translations
├── build.gradle                            # Build configuration
├── gradle.properties                       # Project properties
├── settings.gradle                         # Gradle settings
└── README.md                               # Main documentation
```

## Core Components

### 1. Main Mod Class (MaceMod.java)

```java
public class MaceMod implements ModInitializer {
    public static final String MOD_ID = "macemod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Mace Mod");
        ModEnchantments.register();
    }
}
```

**Purpose:** Entry point for the mod. Initializes and registers all enchantments.

### 2. Enchantment Registry (ModEnchantments.java)

```java
public class ModEnchantments {
    public static final RegistryKey<Enchantment> NUKE = RegistryKey.of(
        RegistryKeys.ENCHANTMENT, 
        Identifier.of(MaceMod.MOD_ID, "nuke")
    );
    public static final RegistryKey<Enchantment> SHOCKWAVE = RegistryKey.of(
        RegistryKeys.ENCHANTMENT, 
        Identifier.of(MaceMod.MOD_ID, "shockwave")
    );
    public static final RegistryKey<Enchantment> LIGHTNING = RegistryKey.of(
        RegistryKeys.ENCHANTMENT, 
        Identifier.of(MaceMod.MOD_ID, "lightning")
    );

    public static void register() {
        Registry.register(Registries.ENCHANTMENT, NUKE.getValue(), new NukeEnchantment());
        Registry.register(Registries.ENCHANTMENT, SHOCKWAVE.getValue(), new ShockwaveEnchantment());
        Registry.register(Registries.ENCHANTMENT, LIGHTNING.getValue(), new LightningEnchantment());
    }
}
```

**Purpose:** Centralizes enchantment registration and provides registry keys for all custom enchantments.

## Enchantment Implementations

### Nuke Enchantment

**File:** `enchantment/NukeEnchantment.java`

**Properties:**
- Rarity: VERY_RARE
- Target: WEAPON
- Max Level: 1
- Min Power: 15
- Max Power: 50

**Activation Logic:**
```java
public static void activate(Entity target) {
    if (target == null || target.getWorld().isClient) {
        return;
    }

    Vec3d targetPos = target.getPos();
    
    // Spawn 100 TNT entities and explode them instantly
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
```

**Technical Details:**
- Creates 100 `TntEntity` objects at the exact position of the target
- Sets fuse to 0 for instant explosion
- Only activates on server side (checks `isClient`)
- Causes massive terrain destruction

**Performance Considerations:**
- 100 simultaneous explosions can cause significant lag
- May crash clients with lower-end hardware
- Consider reducing TNT count for production use

### Shockwave Enchantment

**File:** `enchantment/ShockwaveEnchantment.java`

**Properties:**
- Rarity: VERY_RARE
- Target: WEAPON
- Max Level: 1
- Area of Effect: 10×10×10 blocks
- Min Power: 15
- Max Power: 50

**Activation Logic:**
```java
public static void activate(Entity target, Entity attacker, float damage) {
    if (target == null || target.getWorld().isClient) {
        return;
    }

    // Create a bounding box around the target
    Box box = new Box(
        target.getX() - RADIUS, target.getY() - RADIUS, target.getZ() - RADIUS,
        target.getX() + RADIUS, target.getY() + RADIUS, target.getZ() + RADIUS
    );

    // Get all living entities in the radius
    List<LivingEntity> entities = target.getWorld().getEntitiesByClass(
        LivingEntity.class, 
        box, 
        entity -> entity != attacker && entity.isAlive()
    );

    // Create appropriate damage source
    DamageSource damageSource;
    if (attacker instanceof PlayerEntity player) {
        damageSource = player.getDamageSources().playerAttack(player);
    } else if (attacker instanceof LivingEntity living) {
        damageSource = living.getDamageSources().mobAttack(living);
    } else {
        damageSource = target.getDamageSources().generic();
    }

    // Deal damage to each entity
    for (LivingEntity entity : entities) {
        entity.damage(damageSource, damage);
    }
}
```

**Technical Details:**
- Creates a 20×20×20 block cube (radius 10 from center)
- Filters entities to exclude the attacker
- Damage scales with fall distance: `7.0 + (fallDistance × 4.0)`
- Uses appropriate damage source based on attacker type
- Respects damage invulnerability frames

**Damage Examples:**
- 3 block fall: 19 damage (9.5 hearts)
- 5 block fall: 27 damage (13.5 hearts)
- 10 block fall: 47 damage (23.5 hearts)

### Lightning Enchantment

**File:** `enchantment/LightningEnchantment.java`

**Properties:**
- Rarity: VERY_RARE
- Target: WEAPON
- Max Level: 1
- Area of Effect: 10×10×10 blocks
- Min Power: 15
- Max Power: 50

**Activation Logic:**
```java
public static void activate(Entity target, Entity attacker) {
    if (target == null || target.getWorld().isClient) {
        return;
    }

    // Create a bounding box around the target
    Box box = new Box(
        target.getX() - RADIUS, target.getY() - RADIUS, target.getZ() - RADIUS,
        target.getX() + RADIUS, target.getY() + RADIUS, target.getZ() + RADIUS
    );

    // Get all entities in the radius (excluding attacker)
    List<Entity> entities = target.getWorld().getOtherEntities(
        attacker, 
        box, 
        entity -> entity.isAlive()
    );

    // Strike each entity with lightning
    for (Entity entity : entities) {
        Vec3d pos = entity.getPos();
        LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(target.getWorld());
        if (lightning != null) {
            lightning.refreshPositionAfterTeleport(pos);
            target.getWorld().spawnEntity(lightning);
        }
    }
}
```

**Technical Details:**
- Creates actual `LightningEntity` objects
- Each lightning bolt deals 5 damage (2.5 hearts)
- Lightning can set entities on fire
- Can create charged creepers if they survive
- Visual and sound effects included
- Does not require thunderstorm

**Side Effects:**
- Sets blocks on fire near strike point
- Can create charged creepers
- Provides visual and audio feedback
- May cause performance issues with many entities

## Mixin Implementation

### MaceItemMixin

**File:** `mixin/MaceItemMixin.java`

**Purpose:** Intercepts mace attacks to detect smash attacks and trigger enchantments.

**Hook Point:**
```java
@Inject(method = "postHit", at = @At("TAIL"))
private void onPostHit(ItemStack stack, LivingEntity target, LivingEntity attacker, 
                       CallbackInfoReturnable<Boolean> cir)
```

**Detection Logic:**
```java
// Must be server-side
if (target.getWorld().isClient) return;

// Must be a player
if (!(attacker instanceof PlayerEntity player)) return;

// Must be falling (smash attack detection)
if (player.fallDistance <= 1.5f) return;
```

**Enchantment Activation:**
```java
ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments(stack);

for (RegistryEntry<Enchantment> entry : enchantments.getEnchantments()) {
    Enchantment enchantment = entry.value();
    
    if (enchantment instanceof NukeEnchantment) {
        NukeEnchantment.activate(target);
    } else if (enchantment instanceof ShockwaveEnchantment) {
        float damage = calculateSmashDamage(player.fallDistance);
        ShockwaveEnchantment.activate(target, attacker, damage);
    } else if (enchantment instanceof LightningEnchantment) {
        LightningEnchantment.activate(target, attacker);
    }
}
```

**Damage Calculation:**
```java
private float calculateSmashDamage(float fallDistance) {
    return 7.0f + (fallDistance * 4.0f);
}
```

## Configuration Files

### fabric.mod.json

```json
{
  "schemaVersion": 1,
  "id": "macemod",
  "version": "${version}",
  "name": "Mace Mod",
  "description": "Custom enchantments for the Mace weapon",
  "authors": ["Jz2012"],
  "license": "MIT",
  "environment": "*",
  "entrypoints": {
    "main": ["com.jz2012.macemod.MaceMod"]
  },
  "mixins": ["macemod.mixins.json"],
  "depends": {
    "fabricloader": ">=0.16.0",
    "minecraft": "~1.21",
    "java": ">=21",
    "fabric-api": "*"
  }
}
```

### macemod.mixins.json

```json
{
  "required": true,
  "minVersion": "0.8",
  "package": "com.jz2012.macemod.mixin",
  "compatibilityLevel": "JAVA_21",
  "mixins": ["MaceItemMixin"],
  "client": [],
  "injectors": {
    "defaultRequire": 1
  }
}
```

## Build Configuration

### gradle.properties

```properties
# Fabric Properties
minecraft_version=1.21.1
yarn_mappings=1.21.1+build.3
loader_version=0.16.5
loom_version=1.6

# Mod Properties
mod_version=1.0.0
maven_group=com.jz2012
archives_base_name=macemod

# Dependencies
fabric_version=0.105.0+1.21.1
```

### build.gradle

Key dependencies:
- `fabric-loom`: Gradle plugin for Fabric mod development
- `minecraft`: Minecraft game jar
- `yarn`: Deobfuscation mappings
- `fabric-loader`: Fabric mod loader
- `fabric-api`: Fabric API

## Testing Strategy

### Manual Testing Checklist

1. **Enchantment Registration**
   - [ ] All three enchantments appear in creative inventory
   - [ ] Enchantments can be applied via `/enchant` command
   - [ ] Enchantments appear in enchanting table

2. **Nuke Enchantment**
   - [ ] Activates only during smash attacks
   - [ ] Spawns 100 TNT at target location
   - [ ] TNT explodes immediately
   - [ ] Does not activate on client side

3. **Shockwave Enchantment**
   - [ ] Activates only during smash attacks
   - [ ] Damages entities in 10-block radius
   - [ ] Does not damage attacker
   - [ ] Damage scales with fall distance

4. **Lightning Enchantment**
   - [ ] Activates only during smash attacks
   - [ ] Strikes entities in 10-block radius
   - [ ] Does not strike attacker
   - [ ] Creates visual and sound effects

5. **Smash Attack Detection**
   - [ ] Does not activate with < 1.5 block fall
   - [ ] Activates with >= 1.5 block fall
   - [ ] Only works when player is attacker
   - [ ] Only works on living entities

### Unit Test Scenarios (if implementing)

```java
@Test
public void testSmashAttackDetection() {
    // Test fall distance threshold
    // Test player requirement
    // Test living entity requirement
}

@Test
public void testNukeActivation() {
    // Test TNT spawning count
    // Test TNT fuse time
    // Test server-side only activation
}

@Test
public void testShockwaveRadius() {
    // Test 10-block radius
    // Test attacker exclusion
    // Test damage calculation
}

@Test
public void testLightningStrike() {
    // Test lightning entity spawning
    // Test attacker exclusion
    // Test positioning
}
```

## Performance Considerations

### Potential Bottlenecks

1. **Nuke Enchantment**
   - 100 TNT entities cause significant lag
   - Explosion calculations are expensive
   - Terrain modification is resource-intensive
   - **Solution:** Consider reducing TNT count or adding cooldown

2. **Shockwave Enchantment**
   - Entity radius query can be slow with many entities
   - Damage calculation for each entity
   - **Solution:** Use efficient entity filtering, consider max entity limit

3. **Lightning Enchantment**
   - Multiple lightning entities spawn simultaneously
   - Each lightning has particle effects
   - **Solution:** Consider staggering lightning strikes

### Optimization Strategies

1. Add cooldown between enchantment activations
2. Reduce entity counts (e.g., 50 TNT instead of 100)
3. Add configuration options for effect intensity
4. Implement entity cap for area effects
5. Use asynchronous processing for non-critical effects

## Compatibility Notes

- **Minecraft Version:** 1.21.1
- **Fabric Loader:** >= 0.16.0
- **Java:** >= 21
- **Mixin:** 0.8+

### Known Incompatibilities

- May conflict with other mods that modify mace behavior
- Performance issues on lower-end hardware
- May not work with custom damage calculation mods

## Future Enhancements

1. Add configuration file for customizing:
   - TNT count for Nuke
   - Radius for Shockwave and Lightning
   - Damage multipliers
   - Cooldown times

2. Add visual effects:
   - Particle effects on activation
   - Custom sounds
   - Screen shake for Nuke

3. Add balancing features:
   - Cooldown system
   - Energy/durability cost
   - Progressive levels (I, II, III)

4. Add more enchantments:
   - Freeze: Freezes entities in place
   - Teleport: Teleports player away after smash
   - Healing: Heals player based on damage dealt

## Debugging

### Enable Debug Logging

Add to `log4j2.xml`:
```xml
<Logger level="debug" name="com.jz2012.macemod"/>
```

### Common Issues

1. **Enchantments not activating**
   - Check fall distance >= 1.5 blocks
   - Verify mod is loaded: `/fabric mods`
   - Check server logs for errors

2. **Build failures**
   - Verify Gradle version compatibility
   - Check Fabric Loom version
   - Ensure Java 21 is installed

3. **Mixin not applying**
   - Check mixin configuration JSON
   - Verify mixin package path
   - Check for conflicting mixins

## License

MIT License - See LICENSE file for details
