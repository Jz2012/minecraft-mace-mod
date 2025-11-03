package com.jz2012.macemod.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LightningEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * Lightning Enchantment: Strikes all entities in a 10-block radius with lightning,
 * excluding the attacker.
 */
public class LightningEnchantment extends Enchantment {
    private static final double RADIUS = 10.0;

    public LightningEnchantment() {
        super(Enchantment.Rarity.VERY_RARE, EnchantmentTarget.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMinPower(int level) {
        return 15;
    }

    @Override
    public int getMaxPower(int level) {
        return 50;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    /**
     * Activates the lightning effect around the target location.
     * Summons lightning bolts on all entities in radius except the attacker.
     * 
     * @param target The entity that was hit by the mace smash
     * @param attacker The player who performed the smash attack
     */
    public static void activate(Entity target, Entity attacker) {
        if (target == null || target.getWorld().isClient) {
            return;
        }

        // Create a box around the target position
        Box box = new Box(
            target.getX() - RADIUS, target.getY() - RADIUS, target.getZ() - RADIUS,
            target.getX() + RADIUS, target.getY() + RADIUS, target.getZ() + RADIUS
        );

        // Get all entities in the radius
        List<Entity> entities = target.getWorld().getOtherEntities(
            attacker, box, entity -> entity.isAlive()
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
}
