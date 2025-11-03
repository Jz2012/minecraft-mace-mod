package com.jz2012.macemod.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.TntEntity;
import net.minecraft.util.math.Vec3d;

/**
 * Nuke Enchantment: Spawns and instantly explodes 100 TNT at the target's location
 * during a mace smash attack.
 */
public class NukeEnchantment extends Enchantment {
    public NukeEnchantment() {
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
     * Activates the nuke effect at the target location.
     * Spawns 100 TNT entities and explodes them instantly.
     * 
     * @param target The entity that was hit by the mace smash
     */
    public static void activate(Entity target) {
        if (target == null || target.getWorld().isClient) {
            return;
        }

        Vec3d targetPos = target.getPos();
        
        // Spawn 100 TNT entities and explode them instantly
        for (int i = 0; i < 100; i++) {
            TntEntity tnt = new TntEntity(target.getWorld(), targetPos.x, targetPos.y, targetPos.z, null);
            tnt.setFuse(0); // Explode immediately
            target.getWorld().spawnEntity(tnt);
        }
    }
}
