package net.felixlotionstein.crittercolonies.client.renderer.entity;

import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.felixlotionstein.crittercolonies.anim.ReplacedCitizenAnim;
import net.felixlotionstein.crittercolonies.client.model.entity.ReplacedCitizenModel;
import net.felixlotionstein.crittercolonies.client.renderer.layer.CitizenOverlayLayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoReplacedEntityRenderer;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ReplacedCitizenRenderer
        extends GeoReplacedEntityRenderer<AbstractEntityCitizen, ReplacedCitizenAnim> {

    // Per-entity swing window — renderer is a singleton so this must be a map.
    private final Map<Integer, Integer> swingEndTickMap = new HashMap<>();

    public ReplacedCitizenRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new ReplacedCitizenModel(), ReplacedCitizenAnim.INSTANCE);
        addRenderLayer(new CitizenOverlayLayer(this));
    }

    @Override
    public void render(AbstractEntityCitizen entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        int id = entity.getId();

        // Model / texture selection — both read synced entity data, always valid client-side.
        ReplacedCitizenModel.activeGender     = entity.isFemale() ? "female" : "male";
        ReplacedCitizenModel.activeProfession = professionName(entity.getCitizenDataView());

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

    /**
     * Derives a short profession key from the data view's job string.
     * Handles all common formats:
     *   "com.minecolonies.core.colony.jobs.JobAlchemist" → "alchemist"
     *   "JobAlchemist"                                   → "alchemist"
     *   "minecolonies:alchemist"                         → "alchemist"
     *   "alchemist"                                      → "alchemist"
     */
    private static String professionName(@Nullable ICitizenDataView dataView) {
        if (dataView == null) return "unemployed";
        String raw = dataView.getJob();
        if (raw == null || raw.isBlank()) return "unemployed";
        return cleanJobString(raw);
    }

    private static String cleanJobString(String raw) {
        int lastDot = raw.lastIndexOf('.');
        String simple = (lastDot >= 0 ? raw.substring(lastDot + 1) : raw)
                .toLowerCase(Locale.ROOT);
        int colon = simple.indexOf(':');
        if (colon >= 0) simple = simple.substring(colon + 1);
        if (simple.startsWith("job")) simple = simple.substring(3);
        return simple.isBlank() ? "unemployed" : simple;
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
