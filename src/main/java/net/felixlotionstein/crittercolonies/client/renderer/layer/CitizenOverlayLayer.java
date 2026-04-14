package net.felixlotionstein.crittercolonies.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.felixlotionstein.crittercolonies.Crittercolonies;
import net.felixlotionstein.crittercolonies.anim.ReplacedCitizenAnim;
import net.felixlotionstein.crittercolonies.client.model.entity.ReplacedCitizenModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Renders up to three translucent overlay textures on top of the base citizen model,
 * stacked in this order:
 *
 *   1. textures/entity/citizen/{gender}_overlay.png
 *        Applied to every citizen of that gender. Use for sex-specific body features
 *        that are shared across all professions.
 *
 *   2. textures/entity/citizen/{profession}_overlay.png
 *        Gender-neutral profession clothing/accessories. This is what you will use
 *        for most professions.
 *
 *   3. textures/entity/citizen/{profession}_{gender}_overlay.png
 *        Profession clothing that needs to differ between male and female.
 *        Only create this when the neutral layer isn't sufficient.
 *
 * Each layer is fully optional. If the PNG doesn't exist the layer is silently skipped.
 * Existence is checked exactly once per unique key per game session (result is cached).
 */
public class CitizenOverlayLayer extends GeoRenderLayer<ReplacedCitizenAnim> {

    /**
     * Maps an overlay key (the filename stem, e.g. "female", "baker", "baker_male")
     * to either:
     *   Optional.of(loc)   — texture exists, use it
     *   Optional.empty()   — texture absent, skip silently
     *
     * Using Optional as the value type lets us store the "not found" result so we
     * never hit the ResourceManager more than once per key.
     */
    private static final Map<String, Optional<ResourceLocation>> CACHE = new HashMap<>();

    public CitizenOverlayLayer(GeoRenderer<ReplacedCitizenAnim> renderer) {
        super(renderer);
    }

    // -------------------------------------------------------------------------
    // GeoRenderLayer entry point
    // -------------------------------------------------------------------------

    @Override
    public void render(PoseStack poseStack, ReplacedCitizenAnim animatable,
                       BakedGeoModel bakedModel, RenderType renderType,
                       MultiBufferSource bufferSource, VertexConsumer buffer,
                       float partialTick, int packedLight, int packedOverlay) {

        String profession = ReplacedCitizenModel.activeProfession;
        String gender     = ReplacedCitizenModel.activeGender;

        // Layer 1 — gender base (e.g. female_overlay.png)
        renderOverlay(gender,
                bakedModel, poseStack, bufferSource, animatable,
                partialTick, packedLight, packedOverlay);

        // Layer 2 — profession neutral (e.g. baker_overlay.png)
        renderOverlay(profession,
                bakedModel, poseStack, bufferSource, animatable,
                partialTick, packedLight, packedOverlay);

        // Layer 3 — profession + gender specific (e.g. baker_female_overlay.png)
        renderOverlay(profession + "_" + gender,
                bakedModel, poseStack, bufferSource, animatable,
                partialTick, packedLight, packedOverlay);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Resolves the key and, if the texture exists, re-renders the model with it. */
    private void renderOverlay(String key,
                                BakedGeoModel bakedModel, PoseStack poseStack,
                                MultiBufferSource bufferSource, ReplacedCitizenAnim animatable,
                                float partialTick, int packedLight, int packedOverlay) {
        resolve(key).ifPresent(tex -> {
            RenderType overlayType    = RenderType.entityTranslucent(tex);
            VertexConsumer overlayBuf = bufferSource.getBuffer(overlayType);
            // reRender bypasses scaleModelForRender, so apply the same -180° Y
            // rotation that the base renderer uses, otherwise overlays face backwards.
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(-180));
            this.renderer.reRender(bakedModel, poseStack, bufferSource, animatable,
                    overlayType, overlayBuf,
                    partialTick, packedLight, packedOverlay,
                    0xFFFFFFFF);
            poseStack.popPose();
        });
    }

    /**
     * Checks once per key whether {@code textures/entity/citizen/{key}_overlay.png}
     * exists, then permanently caches the result.
     */
    private static Optional<ResourceLocation> resolve(String key) {
        return CACHE.computeIfAbsent(key, k -> {
            ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(Crittercolonies.MODID,
                    "textures/entity/citizen/" + k + "_overlay.png");
            return Minecraft.getInstance().getResourceManager()
                    .getResource(loc).isPresent()
                    ? Optional.of(loc)
                    : Optional.empty();
        });
    }
}
