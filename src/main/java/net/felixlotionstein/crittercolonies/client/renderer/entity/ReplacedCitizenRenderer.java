package net.felixlotionstein.crittercolonies.client.renderer.entity;

import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.felixlotionstein.crittercolonies.anim.ReplacedCitizenAnim;
import net.felixlotionstein.crittercolonies.client.model.entity.ReplacedCitizenModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoReplacedEntityRenderer;

import java.util.Locale;

public class ReplacedCitizenRenderer
        extends GeoReplacedEntityRenderer<AbstractEntityCitizen, ReplacedCitizenAnim> {

    public ReplacedCitizenRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new ReplacedCitizenModel(), ReplacedCitizenAnim.INSTANCE);
    }

    @Override
    public void render(AbstractEntityCitizen entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // isFemale() and getCitizenDataView() both read synced entity data — always valid client-side.
        // getCitizenData() is null on the client, so we use getCitizenDataView() for the profession.
        ReplacedCitizenModel.activeGender     = entity.isFemale() ? "female" : "male";
        ReplacedCitizenModel.activeProfession = professionName(entity.getCitizenDataView());

        ReplacedCitizenAnim.INSTANCE.moving =
                entity.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4;
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    /**
     * Derives a short profession key from the data view's job string.
     *
     * ICitizenDataView.getJob() returns a synced String whose format may vary:
     *   full class name  → "com.minecolonies.core.colony.jobs.JobAlchemist"
     *   simple class     → "JobAlchemist"
     *   registry key     → "minecolonies:alchemist"
     *   already clean    → "alchemist"
     *
     * All four cases produce "alchemist", which maps to the asset filenames.
     */
    private static String professionName(@Nullable ICitizenDataView dataView) {
        if (dataView == null) return "unemployed";
        String raw = dataView.getJob();
        if (raw == null || raw.isBlank()) return "unemployed";
        return cleanJobString(raw);
    }

    private static String cleanJobString(String raw) {
        // Strip package prefix: "com.example.jobs.JobAlchemist" → "JobAlchemist"
        int lastDot = raw.lastIndexOf('.');
        String simple = (lastDot >= 0 ? raw.substring(lastDot + 1) : raw)
                .toLowerCase(Locale.ROOT);

        // Strip registry namespace: "minecolonies:alchemist" → "alchemist"
        int colon = simple.indexOf(':');
        if (colon >= 0) simple = simple.substring(colon + 1);

        // Strip conventional "job" prefix: "jobalchemist" → "alchemist"
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
