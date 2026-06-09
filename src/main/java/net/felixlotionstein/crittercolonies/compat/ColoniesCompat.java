package net.felixlotionstein.crittercolonies.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Central access point for whichever colonies mod is installed.
 *
 * Call {@link #init()} once during mod construction (after GeckoLib.initialize()).
 * All other methods are safe to call from any client thread after that.
 *
 * Supported backends:
 *   "minecolonies"  → MineColoniesBackend  (direct API, pirates included)
 *   "slimcolonies"  → SlimColoniesBackend  (reflection only, no pirates)
 */
public final class ColoniesCompat {

    private static IColoniesBackend backend;

    private ColoniesCompat() {}

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    /**
     * Detects which colonies mod is loaded and initialises the appropriate
     * backend. Throws if neither is present so the user gets a clear error.
     */
    public static void init() {
        if (ModList.get().isLoaded("minecolonies")) {
            // MineColoniesBackend references com.minecolonies.* classes.
            // This branch is only reached when minecolonies is on the classpath,
            // so the class load is safe.
            backend = new MineColoniesBackend();
        } else if (ModList.get().isLoaded("slimcolonies")) {
            // SlimColoniesBackend uses pure reflection — no SlimColonies imports.
            backend = new SlimColoniesBackend();
        } else {
            throw new IllegalStateException(
                    "[CritterColonies] Neither 'minecolonies' nor 'slimcolonies' is loaded. " +
                    "CritterColonies requires exactly one of them.");
        }
    }

    // -------------------------------------------------------------------------
    // Entity type access
    // -------------------------------------------------------------------------

    public static EntityType<?> getCitizenType() {
        return backend.citizenType();
    }

    /**
     * Returns {@code true} when a pirate/raider EntityType is available.
     * Always {@code false} with SlimColonies.
     */
    public static boolean hasPirates() {
        return backend.pirateType() != null;
    }

    /** Returns the pirate EntityType, or {@code null} when {@link #hasPirates()} is false. */
    public static EntityType<?> getPirateType() {
        return backend.pirateType();
    }

    // -------------------------------------------------------------------------
    // Per-entity data helpers
    // -------------------------------------------------------------------------

    /** Returns {@code "female"} or {@code "male"} for the given citizen entity. */
    public static String getGender(LivingEntity entity) {
        return backend.gender(entity);
    }

    /**
     * Returns the cleaned profession key for the given citizen entity.
     * Falls back to {@code "unemployed"} on any failure.
     */
    public static String getProfession(LivingEntity entity) {
        return backend.profession(entity);
    }

    /**
     * Returns the texture path of the icon currently displayed above this citizen's
     * head (recruit, hungry, raided, missing tool, etc.), or {@code null} if no
     * icon is currently visible. Returning {@code null} suppresses the head icon.
     */
    @Nullable
    public static ResourceLocation getStatusIcon(LivingEntity entity) {
        return backend.statusIcon(entity);
    }

    // -------------------------------------------------------------------------
    // Shared string utility (used by both backends)
    // -------------------------------------------------------------------------

    /**
     * Normalises a raw job string from any colonies mod into a short lowercase key.
     * Handles all common formats:
     *   "com.minecolonies.core.colony.jobs.JobAlchemist" → "alchemist"
     *   "JobAlchemist"                                   → "alchemist"
     *   "minecolonies:alchemist"                         → "alchemist"
     *   "alchemist"                                      → "alchemist"
     */
    public static String cleanJobString(String raw) {
        if (raw == null || raw.isBlank()) return "unemployed";

        // Strip package prefix up to the last dot
        int lastDot = raw.lastIndexOf('.');
        String simple = (lastDot >= 0 ? raw.substring(lastDot + 1) : raw)
                .toLowerCase(Locale.ROOT);

        // Strip namespace prefix (e.g. "minecolonies:")
        int colon = simple.indexOf(':');
        if (colon >= 0) simple = simple.substring(colon + 1);

        // Strip "job" prefix added by some class names
        if (simple.startsWith("job")) simple = simple.substring(3);

        return simple.isBlank() ? "unemployed" : simple;
    }
}
