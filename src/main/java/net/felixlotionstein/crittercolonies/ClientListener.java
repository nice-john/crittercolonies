package net.felixlotionstein.crittercolonies;

import com.minecolonies.api.entity.ModEntities;
import net.felixlotionstein.crittercolonies.client.renderer.entity.ReplacedCitizenRenderer;
import net.felixlotionstein.crittercolonies.client.renderer.entity.ReplacedPirateRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Crittercolonies.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
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
