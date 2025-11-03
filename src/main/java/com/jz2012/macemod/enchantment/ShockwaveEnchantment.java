package com.jz2012.macemod.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;

import java.util.List;

/**
 * Shockwave Enchantment: Deals the same damage as the smash attack to all mobs
 * and players in a 10×10 radius, excluding the attacker.
 */
public class ShockwaveEnchantment extends Enchantment {
    private static final double RADIUS = 10.0;

    public ShockwaveEnchantment() {
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
     * Activates the shockwave effect around the target location.
     * Deals the same damage to all entities in radius except the attacker.
     * 
     * @param target The entity that was hit by the mace smash
     * @param attacker The player who performed the smash attack
     * @param damage The amount of damage dealt by the smash attack
     */
    public static void activate(Entity target, Entity attacker, float damage) {
        if (target == null || target.getWorld().isClient) {
            return;
        }

        // Create a box around the target position
        Box box = new Box(
            target.getX() - RADIUS, target.getY() - RADIUS, target.getZ() - RADIUS,
            target.getX() + RADIUS, target.getY() + RADIUS, target.getZ() + RADIUS
        );

        // Get all living entities in the radius
        List<LivingEntity> entities = target.getWorld().getEntitiesByClass(
            LivingEntity.class, box, entity -> entity != attacker && entity.isAlive()
        );

        // Deal damage to each entity
        DamageSource damageSource;
        if (attacker instanceof PlayerEntity player) {
            damageSource = player.getDamageSources().playerAttack(player);
        } else if (attacker instanceof LivingEntity living) {
            damageSource = living.getDamageSources().mobAttack(living);
        } else {
            damageSource = target.getDamageSources().generic();
        }

        for (LivingEntity entity : entities) {
            entity.damage(damageSource, damage);
        }
    }
}
