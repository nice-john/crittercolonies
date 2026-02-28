package net.felixlotionstein.crittercolonies.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.minecolonies.api.entity.mobs.pirates.AbstractEntityPirate;
import net.felixlotionstein.crittercolonies.anim.ReplacedPirateAnim;
import net.felixlotionstein.crittercolonies.client.model.entity.ReplacedPirateModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoReplacedEntityRenderer;

import java.util.HashMap;
import java.util.Map;

public class ReplacedPirateRenderer
        extends GeoReplacedEntityRenderer<AbstractEntityPirate, ReplacedPirateAnim> {

    // Per-entity attack window, keyed by entity ID.
    // The renderer is a singleton (one instance for all pirates), so this must be a map.
    private final Map<Integer, Integer> attackEndTickMap = new HashMap<>();

    public ReplacedPirateRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new ReplacedPirateModel(), ReplacedPirateAnim.INSTANCE);
    }

    @Override
    public void render(AbstractEntityPirate entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        int id = entity.getId();

        ReplacedPirateAnim.INSTANCE.movingMap.put(
                id, entity.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4);

        if (entity.swinging) {
            attackEndTickMap.put(id, entity.tickCount + 20);
        }
        ReplacedPirateAnim.INSTANCE.attackRemainingMap.put(
                id, Math.max(0, attackEndTickMap.getOrDefault(id, 0) - entity.tickCount));

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack,
                                    ReplacedPirateAnim animatable, BakedGeoModel model, boolean isReRender,
                                    float partialTick, int packedLight, int packedOverlay) {
        super.scaleModelForRender(widthScale, heightScale, poseStack, animatable, model,
                isReRender, partialTick, packedLight, packedOverlay);
        poseStack.mulPose(Axis.YP.rotationDegrees(-90));
    }
}
