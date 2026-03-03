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
    // Entries are resolved on first use and include fallback logic, so
    // each entry already points to a file that is guaranteed to exist.
    private static final Map<String, ResourceLocation> MODEL_CACHE     = new HashMap<>();
    private static final Map<String, ResourceLocation> TEXTURE_CACHE   = new HashMap<>();
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
                    "falling back to '{}'.", p, FALLBACK);
            return new ResourceLocation(Crittercolonies.MODID,
                    "geo/entity/citizen/" + FALLBACK + ".geo.json");
        });
    }

    @Override
    public ResourceLocation getTextureResource(ReplacedCitizenAnim animatable) {
        // Key includes gender so male/female variants are cached independently.
        String key    = activeProfession + "_" + activeGender;
        String gender = activeGender; // capture for use inside lambda
        return TEXTURE_CACHE.computeIfAbsent(key, k -> {
            ResourceLocation loc = new ResourceLocation(Crittercolonies.MODID,
                    "textures/entity/citizen/" + k + ".png");
            if (resourceExists(loc)) return loc;

            LOGGER.warn("[CritterColonies] Missing texture file '{}', " +
                    "falling back to '{}'.", k, FALLBACK + "_" + gender);
            return new ResourceLocation(Crittercolonies.MODID,
                    "textures/entity/citizen/" + FALLBACK + "_" + gender + ".png");
        });
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
