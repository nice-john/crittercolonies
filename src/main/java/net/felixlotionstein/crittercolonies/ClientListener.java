package net.felixlotionstein.crittercolonies;

import net.felixlotionstein.crittercolonies.client.renderer.entity.ReplacedCitizenRenderer;
import net.felixlotionstein.crittercolonies.client.renderer.entity.ReplacedPirateRenderer;
import net.felixlotionstein.crittercolonies.compat.ColoniesCompat;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Crittercolonies.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientListener {
    private ClientListener() {}

    // LOWEST priority ensures we register after MineColonies / SlimColonies and any
    // other mods, so our renderer is never overwritten regardless of mod load order.
    @SuppressWarnings("unchecked")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        // Pirates only exist in MineColonies — SlimColonies has no raider entities.
        if (ColoniesCompat.hasPirates()) {
            event.registerEntityRenderer(
                    (EntityType) ColoniesCompat.getPirateType(),
                    ReplacedPirateRenderer::new);
        }
        event.registerEntityRenderer(
                (EntityType) ColoniesCompat.getCitizenType(),
                ReplacedCitizenRenderer::new);
    }
}
