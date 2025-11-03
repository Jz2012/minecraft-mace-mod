package com.jz2012.macemod.mixin;

import com.jz2012.macemod.enchantment.LightningEnchantment;
import com.jz2012.macemod.enchantment.NukeEnchantment;
import com.jz2012.macemod.enchantment.ShockwaveEnchantment;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MaceItem;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to hook into the MaceItem's postHit method to detect mace smash attacks
 * and trigger custom enchantments.
 */
@Mixin(MaceItem.class)
public class MaceItemMixin {
    
    /**
     * Injects into the postHit method to detect when a mace attack hits an entity.
     * This is where we check for smash attacks and trigger enchantments.
     */
    @Inject(method = "postHit", at = @At("TAIL"))
    private void onPostHit(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        if (target.getWorld().isClient || !(attacker instanceof PlayerEntity player)) {
            return;
        }

        // Check if this was a smash attack (player falling with significant velocity)
        // Mace smash attacks occur when the player is falling
        if (player.fallDistance <= 1.5f) {
            return; // Not a smash attack
        }

        ServerWorld world = (ServerWorld) target.getWorld();
        
        // Calculate the damage dealt (for shockwave)
        // We approximate based on the fall distance
        float damage = calculateSmashDamage(player.fallDistance);

        // Check enchantments on the mace
        ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments(stack);
        
        for (RegistryEntry<Enchantment> entry : enchantments.getEnchantments()) {
            Enchantment enchantment = entry.value();
            
            // Check each custom enchantment
            if (enchantment instanceof NukeEnchantment) {
                NukeEnchantment.activate(target);
            } else if (enchantment instanceof ShockwaveEnchantment) {
                ShockwaveEnchantment.activate(target, attacker, damage);
            } else if (enchantment instanceof LightningEnchantment) {
                LightningEnchantment.activate(target, attacker);
            }
        }
    }

    /**
     * Calculates the approximate damage dealt by a mace smash attack based on fall distance.
     */
    private float calculateSmashDamage(float fallDistance) {
        // Mace damage scales with fall distance
        // Base damage + bonus damage from fall
        return 7.0f + (fallDistance * 4.0f);
    }
}
