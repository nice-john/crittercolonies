package net.felixlotionstein.crittercolonies;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;
import software.bernie.geckolib.GeckoLib;

@Mod(Crittercolonies.MODID)
public class Crittercolonies {

    public static final String MODID = "crittercolonies";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Crittercolonies() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        GeckoLib.initialize();

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("CritterColonies common setup complete.");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("CritterColonies server starting.");
    }
}
