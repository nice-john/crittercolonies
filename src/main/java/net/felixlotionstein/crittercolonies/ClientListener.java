package net.felixlotionstein.crittercolonies;

import com.minecolonies.api.entity.ModEntities;
import net.felixlotionstein.crittercolonies.client.renderer.entity.ReplacedCitizenRenderer;
import net.felixlotionstein.crittercolonies.client.renderer.entity.ReplacedPirateRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = Crittercolonies.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ClientListener {
    private ClientListener() {}

    // LOWEST priority ensures we register after Minecolonies and any other mods,
    // so our renderer is never overwritten regardless of mod load order.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.CAMP_PIRATE, ReplacedPirateRenderer::new);
        event.registerEntityRenderer(ModEntities.CITIZEN, ReplacedCitizenRenderer::new);
    }
}
