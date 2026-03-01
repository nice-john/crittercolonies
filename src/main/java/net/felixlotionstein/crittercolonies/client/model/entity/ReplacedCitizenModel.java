package net.felixlotionstein.crittercolonies.client.model.entity;

import net.felixlotionstein.crittercolonies.Crittercolonies;
import net.felixlotionstein.crittercolonies.anim.ReplacedCitizenAnim;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

import java.util.HashMap;
import java.util.Map;

public class ReplacedCitizenModel extends DefaultedEntityGeoModel<ReplacedCitizenAnim> {

    /**
     * Set by ReplacedCitizenRenderer before each render call.
     * Safe as plain statics because entity rendering is single-threaded.
     *
     * Model    → one per profession (gender-neutral):
     *   geo/entity/citizen/{profession}.geo.json
     *
     * Texture  → one per profession + gender:
     *   textures/entity/citizen/{profession}_{gender}.png
     *
     * Animation → one per profession (gender-neutral):
     *   animations/entity/citizen/{profession}.animation.json
     */
    public static String activeGender     = "male";
    public static String activeProfession = "unemployed";

    // Caches so we don't allocate new ResourceLocations every frame.
    private static final Map<String, ResourceLocation> MODEL_CACHE     = new HashMap<>();
    private static final Map<String, ResourceLocation> TEXTURE_CACHE   = new HashMap<>();
    private static final Map<String, ResourceLocation> ANIMATION_CACHE = new HashMap<>();

    public ReplacedCitizenModel() {
        super(new ResourceLocation(Crittercolonies.MODID, "citizen/unemployed"));
    }

    @Override
    public ResourceLocation getModelResource(ReplacedCitizenAnim animatable) {
        // Shared model — profession only, no gender.
        return MODEL_CACHE.computeIfAbsent(activeProfession, p ->
                new ResourceLocation(Crittercolonies.MODID,
                        "geo/entity/citizen/" + p + ".geo.json"));
    }

    @Override
    public ResourceLocation getTextureResource(ReplacedCitizenAnim animatable) {
        // Texture is gendered: male_alchemist.png / female_alchemist.png
        String key = activeProfession + "_" + activeGender;
        return TEXTURE_CACHE.computeIfAbsent(key, k ->
                new ResourceLocation(Crittercolonies.MODID,
                        "textures/entity/citizen/" + k + ".png"));
    }

    @Override
    public ResourceLocation getAnimationResource(ReplacedCitizenAnim animatable) {
        // Shared animation — profession only, no gender.
        return ANIMATION_CACHE.computeIfAbsent(activeProfession, p ->
                new ResourceLocation(Crittercolonies.MODID,
                        "animations/entity/citizen/" + p + ".animation.json"));
    }
}
