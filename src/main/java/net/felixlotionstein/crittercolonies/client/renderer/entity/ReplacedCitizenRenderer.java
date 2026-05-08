package net.felixlotionstein.crittercolonies.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.felixlotionstein.crittercolonies.anim.ReplacedCitizenAnim;
import net.felixlotionstein.crittercolonies.client.model.entity.ReplacedCitizenModel;
import net.felixlotionstein.crittercolonies.client.renderer.layer.CitizenOverlayLayer;
import net.felixlotionstein.crittercolonies.compat.ColoniesCompat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoReplacedEntityRenderer;

import java.util.HashMap;
import java.util.Map;

/**
 * Renderer for citizen entities from MineColonies or SlimColonies.
 *
 * Uses {@link LivingEntity} as the generic type so this single renderer
 * works with either colonies mod regardless of its citizen base class.
 * Profession and gender data are obtained through {@link ColoniesCompat}
 * which handles the API differences via the backend abstraction.
 */
public class ReplacedCitizenRenderer
        extends GeoReplacedEntityRenderer<LivingEntity, ReplacedCitizenAnim> {

    // Per-entity swing window — renderer is a singleton so this must be a map.
    private final Map<Integer, Integer> swingEndTickMap = new HashMap<>();

    public ReplacedCitizenRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new ReplacedCitizenModel(), ReplacedCitizenAnim.INSTANCE);
        addRenderLayer(new CitizenOverlayLayer(this));
    }

    @Override
    public void render(LivingEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        int id = entity.getId();

        // Model / texture selection via the compat layer — works for both colonies mods.
        ReplacedCitizenModel.activeGender     = ColoniesCompat.getGender(entity);
        ReplacedCitizenModel.activeProfession = ColoniesCompat.getProfession(entity);

        // Animation state — per-entity, written before GeckoLib reads them.
        ReplacedCitizenAnim.INSTANCE.movingMap.put(
                id, entity.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4);

        ReplacedCitizenAnim.INSTANCE.sleepingMap.put(id, entity.isSleeping());

        if (entity.swinging) {
            swingEndTickMap.put(id, entity.tickCount + 20);
        }
        ReplacedCitizenAnim.INSTANCE.swingRemainingMap.put(
                id, Math.max(0, swingEndTickMap.getOrDefault(id, 0) - entity.tickCount));

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack,
                                    ReplacedCitizenAnim animatable, BakedGeoModel model, boolean isReRender,
                                    float partialTick, int packedLight, int packedOverlay) {
        super.scaleModelForRender(widthScale, heightScale, poseStack, animatable, model,
                isReRender, partialTick, packedLight, packedOverlay);
        poseStack.mulPose(Axis.YP.rotationDegrees(-180));
    }
}
