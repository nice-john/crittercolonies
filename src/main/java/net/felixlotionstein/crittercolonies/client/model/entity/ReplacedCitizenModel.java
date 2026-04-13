package net.felixlotionstein.crittercolonies.client.model.entity;

import com.mojang.logging.LogUtils;
import net.felixlotionstein.crittercolonies.Crittercolonies;
import net.felixlotionstein.crittercolonies.anim.ReplacedCitizenAnim;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

import java.util.HashMap;
import java.util.Map;

public class ReplacedCitizenModel extends DefaultedEntityGeoModel<ReplacedCitizenAnim> {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Profession name used when a profession's asset file is missing. */
    private static final String FALLBACK = "unemployed";

    /** Single base texture shared by all citizens — overlays handle the visual variation. */
    private static final ResourceLocation BASE_TEXTURE = new ResourceLocation(
            Crittercolonies.MODID, "textures/entity/citizen/citizen.png");

    /**
     * Set by ReplacedCitizenRenderer before each render call.
     * Safe as plain statics because entity rendering is single-threaded.
     *
     * Model     → one per profession (gender-neutral):
     *   geo/entity/citizen/{profession}.geo.json
     *   fallback: geo/entity/citizen.geo.json
     *
     * Texture   → always citizen.png; overlays supply profession/gender variation.
     *
     * Animation → one per profession (gender-neutral):
     *   animations/entity/citizen/{profession}.animation.json
     */
    public static String activeGender     = "male";
    public static String activeProfession = "unemployed";

    // Caches so we don't allocate new ResourceLocations every frame.
    private static final Map<String, ResourceLocation> MODEL_CACHE     = new HashMap<>();
    private static final Map<String, ResourceLocation> ANIMATION_CACHE = new HashMap<>();

    public ReplacedCitizenModel() {
        super(new ResourceLocation(Crittercolonies.MODID, "citizen/" + FALLBACK));
    }

    // -------------------------------------------------------------------------
    // Resource-existence helper
    // -------------------------------------------------------------------------

    /**
     * Returns true if the given ResourceLocation resolves to an actual file
     * in the current resource pack stack (mod jars count as resource packs).
     * Must only be called on the client render thread.
     */
    private static boolean resourceExists(ResourceLocation location) {
        return Minecraft.getInstance().getResourceManager().getResource(location).isPresent();
    }

    // -------------------------------------------------------------------------
    // Asset getters — each falls back to the "unemployed" asset if the
    // profession-specific file is absent, logging a warning the first time.
    // -------------------------------------------------------------------------

    @Override
    public ResourceLocation getModelResource(ReplacedCitizenAnim animatable) {
        return MODEL_CACHE.computeIfAbsent(activeProfession, p -> {
            ResourceLocation loc = new ResourceLocation(Crittercolonies.MODID,
                    "geo/entity/citizen/" + p + ".geo.json");
            if (resourceExists(loc)) return loc;

            LOGGER.warn("[CritterColonies] Missing geo file for profession '{}', " +
                    "falling back to citizen.geo.json.", p);
            return new ResourceLocation(Crittercolonies.MODID, "geo/entity/citizen.geo.json");
        });
    }

    @Override
    public ResourceLocation getTextureResource(ReplacedCitizenAnim animatable) {
        return BASE_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(ReplacedCitizenAnim animatable) {
        return ANIMATION_CACHE.computeIfAbsent(activeProfession, p -> {
            ResourceLocation loc = new ResourceLocation(Crittercolonies.MODID,
                    "animations/entity/citizen/" + p + ".animation.json");
            if (resourceExists(loc)) return loc;

            LOGGER.warn("[CritterColonies] Missing animation file for profession '{}', " +
                    "falling back to '{}'.", p, FALLBACK);
            return new ResourceLocation(Crittercolonies.MODID,
                    "animations/entity/citizen/" + FALLBACK + ".animation.json");
        });
    }
}
