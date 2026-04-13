package net.felixlotionstein.crittercolonies;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;
@Mod(Crittercolonies.MODID)
public class Crittercolonies {

    public static final String MODID = "crittercolonies";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Crittercolonies(IEventBus modBus) {
        modBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("CritterColonies common setup complete.");
    }

    @net.neoforged.bus.api.SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("CritterColonies server starting.");
    }
}
