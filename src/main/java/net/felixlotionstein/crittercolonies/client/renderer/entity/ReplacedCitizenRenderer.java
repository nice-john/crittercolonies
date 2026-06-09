package net.felixlotionstein.crittercolonies.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.felixlotionstein.crittercolonies.anim.ReplacedCitizenAnim;
import net.felixlotionstein.crittercolonies.client.model.entity.ReplacedCitizenModel;
import net.felixlotionstein.crittercolonies.client.renderer.CritterRenderTypes;
import net.felixlotionstein.crittercolonies.client.renderer.layer.CitizenOverlayLayer;
import net.felixlotionstein.crittercolonies.compat.ColoniesCompat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
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

    // -------------------------------------------------------------------------
    // Status icon — recreates the MineColonies head-icon rendering that we
    // lose by replacing the entire renderer. Without this, the alert / recruit
    // / hungry / raided icons would never appear on critter-citizens.
    // -------------------------------------------------------------------------

    /** Distance squared past which the icon is too small to be useful — culling cutoff. */
    private static final double ICON_RENDER_DISTANCE_SQR = 4096.0;

    /** Y-offset added to the name-tag height, in world units. */
    private static final float ICON_Y_OFFSET = 0.3f;

    /** Scale used by MineColonies for the icon quad (negative flips it to face camera). */
    private static final float ICON_SCALE = -0.025f;

    /** Half-width of the icon quad in (post-scale) pixels. */
    private static final float ICON_HALF = 5f;

    /** Full height of the icon quad in (post-scale) pixels. */
    private static final float ICON_HEIGHT = 10f;

    @Override
    protected void renderNameTag(LivingEntity entity, Component name, PoseStack poseStack,
                                  MultiBufferSource bufferSource, int packedLight) {
        super.renderNameTag(entity, name, poseStack, bufferSource, packedLight);

        ResourceLocation icon = ColoniesCompat.getStatusIcon(entity);
        if (icon == null) return;

        double distSqr = this.entityRenderDispatcher.distanceToSqr(
                entity.getX(), entity.getY(), entity.getZ());
        if (distSqr > ICON_RENDER_DISTANCE_SQR) return;

        poseStack.pushPose();
        poseStack.translate(0, entity.getNameTagOffsetY() + ICON_Y_OFFSET, 0);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.scale(ICON_SCALE, ICON_SCALE, ICON_SCALE);

        RenderType type    = CritterRenderTypes.statusIcon(icon);
        VertexConsumer buf = bufferSource.getBuffer(type);
        Matrix4f pose      = poseStack.last().pose();

        // POSITION_TEX quad: top-left, bottom-left, bottom-right, top-right (CCW).
        buf.vertex(pose, -ICON_HALF,           0f, 0f).uv(0f, 0f).endVertex();
        buf.vertex(pose, -ICON_HALF, ICON_HEIGHT, 0f).uv(0f, 1f).endVertex();
        buf.vertex(pose,  ICON_HALF, ICON_HEIGHT, 0f).uv(1f, 1f).endVertex();
        buf.vertex(pose,  ICON_HALF,           0f, 0f).uv(1f, 0f).endVertex();

        poseStack.popPose();
    }
}
