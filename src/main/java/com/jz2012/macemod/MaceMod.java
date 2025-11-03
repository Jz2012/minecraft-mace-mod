package com.jz2012.macemod;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MaceMod implements ModInitializer {
    public static final String MOD_ID = "macemod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Mace Mod");
        ModEnchantments.register();
    }
}
