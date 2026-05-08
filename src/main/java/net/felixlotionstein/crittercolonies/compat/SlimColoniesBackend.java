package net.felixlotionstein.crittercolonies.compat;

import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * IColoniesBackend implementation for SlimColonies.
 * Uses pure reflection — no no.monopixel.* imports — so this class
 * compiles and loads correctly even without SlimColonies on the classpath.
 *
 * All SlimColonies class/method references are resolved once in the
 * constructor and cached. Failures fall back to safe defaults ("male",
 * "unemployed") rather than crashing.
 */
public final class SlimColoniesBackend implements IColoniesBackend {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String MOD_ENTITIES_CLASS    = "no.monopixel.slimcolonies.api.entity.ModEntities";
    private static final String ABSTRACT_CITIZEN_CLASS = "no.monopixel.slimcolonies.api.entity.citizen.AbstractEntityCitizen";
    private static final String DATA_VIEW_CLASS        = "no.monopixel.slimcolonies.api.colony.ICitizenDataView";

    /**
     * Resolved on first call to {@link #citizenType()} — NOT in the constructor.
     * SlimColonies populates ModEntities.CITIZEN during RegisterEvent, which fires
     * AFTER the mod constructor (where ColoniesCompat.init() runs). Reading the
     * field eagerly returned null and crashed the citizen renderer registration.
     * volatile + double-checked locking gives lock-free reads after the first call.
     */
    private volatile EntityType<?> citizenType;
    private volatile boolean       citizenTypeResolved;

    // Cached method handles — null if not found (defensive fallback).
    private final Method isFemaleMethod;
    private final Method getCitizenDataViewMethod;
    private final Method getJobMethod;

    public SlimColoniesBackend() {
        // Note: ModEntities.CITIZEN is resolved lazily in citizenType(). The Method
        // lookups below resolve class definitions (not registry contents), so they
        // are safe to do eagerly here.

        // Resolve isFemale()
        Method fem = null;
        try {
            Class<?> abstractCitizen = Class.forName(ABSTRACT_CITIZEN_CLASS);
            fem = abstractCitizen.getMethod("isFemale");
        } catch (Exception e) {
            LOGGER.warn("[CritterColonies] SlimColonies: isFemale() not found, gender will default to male");
        }
        isFemaleMethod = fem;

        // Resolve getCitizenDataView()
        Method cdv = null;
        try {
            Class<?> abstractCitizen = Class.forName(ABSTRACT_CITIZEN_CLASS);
            cdv = abstractCitizen.getMethod("getCitizenDataView");
        } catch (Exception e) {
            LOGGER.warn("[CritterColonies] SlimColonies: getCitizenDataView() not found, profession will default to unemployed");
        }
        getCitizenDataViewMethod = cdv;

        // Resolve getJob() on ICitizenDataView
        Method job = null;
        try {
            Class<?> dataView = Class.forName(DATA_VIEW_CLASS);
            job = dataView.getMethod("getJob");
        } catch (Exception e) {
            LOGGER.warn("[CritterColonies] SlimColonies: getJob() not found, profession will default to unemployed");
        }
        getJobMethod = job;
    }

    @Override
    public EntityType<?> citizenType() {
        return citizenType;
    }

    /** SlimColonies has no raiders. */
    @Override
    public EntityType<?> pirateType() {
        return null;
    }

    @Override
    public String gender(LivingEntity entity) {
        if (isFemaleMethod == null) return "male";
        try {
            return (boolean) isFemaleMethod.invoke(entity) ? "female" : "male";
        } catch (Exception e) {
            return "male";
        }
    }

    @Override
    public String profession(LivingEntity entity) {
        if (getCitizenDataViewMethod == null || getJobMethod == null) return "unemployed";
        try {
            Object dataView = getCitizenDataViewMethod.invoke(entity);
            if (dataView == null) return "unemployed";
            String raw = (String) getJobMethod.invoke(dataView);
            return ColoniesCompat.cleanJobString(raw != null ? raw : "");
        } catch (Exception e) {
            return "unemployed";
        }
    }
}
