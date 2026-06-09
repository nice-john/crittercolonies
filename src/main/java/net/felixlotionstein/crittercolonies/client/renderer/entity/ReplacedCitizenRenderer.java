package net.felixlotionstein.crittercolonies.client.renderer.entity;

import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.felixlotionstein.crittercolonies.anim.ReplacedCitizenAnim;
import net.felixlotionstein.crittercolonies.client.model.entity.ReplacedCitizenModel;
import net.felixlotionstein.crittercolonies.client.renderer.CritterRenderTypes;
import net.felixlotionstein.crittercolonies.client.renderer.layer.CitizenOverlayLayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
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

    // -------------------------------------------------------------------------
    // Status icon — recreates the MineColonies head-icon rendering that we
    // lose by replacing the entire renderer. Without this, the alert / recruit
    // / hungry / raided icons would never appear on critter-citizens.
    // -------------------------------------------------------------------------

    /** Distance squared past which the icon is too small to be useful — culling cutoff. */
    private static final double ICON_RENDER_DISTANCE_SQR = 4096.0;

    /** Y-offset added to the name-tag attachment height, in world units. */
    private static final float ICON_Y_OFFSET = 0.9f;

    /** Pose scale for the icon quad (X stays positive, Y flips, matches MineColonies 1.21.1). */
    private static final float ICON_SCALE = 0.025f;

    /** Half-width of the icon quad in (post-scale) pixels. */
    private static final float ICON_HALF = 5f;

    /** Full height of the icon quad in (post-scale) pixels. */
    private static final float ICON_HEIGHT = 10f;

    @Override
    protected void renderNameTag(AbstractEntityCitizen entity, Component name, PoseStack poseStack,
                                  MultiBufferSource bufferSource, int packedLight, float partialTick) {
        super.renderNameTag(entity, name, poseStack, bufferSource, packedLight, partialTick);

        ICitizenDataView dataView = entity.getCitizenDataView();
        if (dataView == null || !dataView.hasVisibleStatus()) return;

        double distSqr = this.entityRenderDispatcher.distanceToSqr(
                entity.getX(), entity.getY(), entity.getZ());
        if (distSqr > ICON_RENDER_DISTANCE_SQR) return;

        ResourceLocation icon = dataView.getStatusIcon();
        if (icon == null) return;

        // 1.21+ moved name-tag positioning into entity attachments. Defensive fallback
        // in case an entity somehow lacks the NAME_TAG attachment — never seen on
        // citizens but cheaper than crashing.
        Vec3 attach = entity.getAttachments()
                .getNullable(EntityAttachment.NAME_TAG, 0, entity.getViewYRot(partialTick));
        if (attach == null) attach = Vec3.ZERO;

        poseStack.pushPose();
        poseStack.translate(attach.x, attach.y + ICON_Y_OFFSET, attach.z);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.scale(ICON_SCALE, -ICON_SCALE, ICON_SCALE);

        RenderType type    = CritterRenderTypes.statusIcon(icon);
        VertexConsumer buf = bufferSource.getBuffer(type);
        Matrix4f pose      = poseStack.last().pose();

        // POSITION_TEX quad: top-left, bottom-left, bottom-right, top-right (CCW).
        buf.addVertex(pose, -ICON_HALF,           0f, 0f).setUv(0f, 0f);
        buf.addVertex(pose, -ICON_HALF, ICON_HEIGHT, 0f).setUv(0f, 1f);
        buf.addVertex(pose,  ICON_HALF, ICON_HEIGHT, 0f).setUv(1f, 1f);
        buf.addVertex(pose,  ICON_HALF,           0f, 0f).setUv(1f, 0f);

        poseStack.popPose();
    }
}
