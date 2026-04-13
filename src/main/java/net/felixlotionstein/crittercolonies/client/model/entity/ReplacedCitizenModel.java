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
     *   falls back to geo/entity/citizen.geo.json if missing
     *
     * Texture  → shared base for all citizens:
     *   textures/entity/citizen/citizen.png
     *   (visual variation is handled entirely by CitizenOverlayLayer)
     *
     * Animation → one per profession (gender-neutral):
     *   animations/entity/citizen/{profession}.animation.json
     */
    public static String activeGender     = "male";
    public static String activeProfession = "unemployed";

    /** Shared base texture applied to every citizen before overlays are drawn. */
    private static final ResourceLocation BASE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Crittercolonies.MODID,
                    "textures/entity/citizen/citizen.png");

    // Caches so we don't allocate new ResourceLocations every frame.
    // Entries are resolved on first use and include fallback logic, so
    // each entry already points to a file that is guaranteed to exist.
    private static final Map<String, ResourceLocation> MODEL_CACHE     = new HashMap<>();
    private static final Map<String, ResourceLocation> ANIMATION_CACHE = new HashMap<>();

    public ReplacedCitizenModel() {
        // "citizen" resolves to geo/entity/citizen.geo.json via DefaultedEntityGeoModel
        super(ResourceLocation.fromNamespaceAndPath(Crittercolonies.MODID, "citizen"));
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
            ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(Crittercolonies.MODID,
                    "geo/entity/citizen/" + p + ".geo.json");
            if (resourceExists(loc)) return loc;

            LOGGER.warn("[CritterColonies] Missing geo file for profession '{}', " +
                    "falling back to citizen.geo.json.", p);
            return ResourceLocation.fromNamespaceAndPath(Crittercolonies.MODID,
                    "geo/entity/citizen.geo.json");
        });
    }

    @Override
    public ResourceLocation getTextureResource(ReplacedCitizenAnim animatable) {
        // All citizens share one base texture. Visual variation comes entirely
        // from the overlay layers rendered on top by CitizenOverlayLayer.
        return BASE_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(ReplacedCitizenAnim animatable) {
        return ANIMATION_CACHE.computeIfAbsent(activeProfession, p -> {
            ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(Crittercolonies.MODID,
                    "animations/entity/citizen/" + p + ".animation.json");
            if (resourceExists(loc)) return loc;

            LOGGER.warn("[CritterColonies] Missing animation file for profession '{}', " +
                    "falling back to '{}'.", p, FALLBACK);
            return ResourceLocation.fromNamespaceAndPath(Crittercolonies.MODID,
                    "animations/entity/citizen/" + FALLBACK + ".animation.json");
        });
    }
}
