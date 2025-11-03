package com.jz2012.macemod;

import com.jz2012.macemod.enchantment.LightningEnchantment;
import com.jz2012.macemod.enchantment.NukeEnchantment;
import com.jz2012.macemod.enchantment.ShockwaveEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModEnchantments {
    public static final RegistryKey<Enchantment> NUKE = RegistryKey.of(RegistryKeys.ENCHANTMENT, 
        Identifier.of(MaceMod.MOD_ID, "nuke"));
    public static final RegistryKey<Enchantment> SHOCKWAVE = RegistryKey.of(RegistryKeys.ENCHANTMENT, 
        Identifier.of(MaceMod.MOD_ID, "shockwave"));
    public static final RegistryKey<Enchantment> LIGHTNING = RegistryKey.of(RegistryKeys.ENCHANTMENT, 
        Identifier.of(MaceMod.MOD_ID, "lightning"));

    public static void register() {
        MaceMod.LOGGER.info("Registering enchantments for " + MaceMod.MOD_ID);
        
        Registry.register(Registries.ENCHANTMENT, NUKE.getValue(), new NukeEnchantment());
        Registry.register(Registries.ENCHANTMENT, SHOCKWAVE.getValue(), new ShockwaveEnchantment());
        Registry.register(Registries.ENCHANTMENT, LIGHTNING.getValue(), new LightningEnchantment());
    }
}
