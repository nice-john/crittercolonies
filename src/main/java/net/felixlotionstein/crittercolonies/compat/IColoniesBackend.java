package net.felixlotionstein.crittercolonies.compat;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

/**
 * Contract for whichever colonies mod is present at runtime.
 * Implementations must be safe to instantiate only when their
 * respective mod is confirmed loaded.
 */
public interface IColoniesBackend {

    /** The citizen EntityType registered by the colonies mod. */
    EntityType<?> citizenType();

    /**
     * The pirate/raider EntityType, or {@code null} if the mod has no raiders.
     * Callers must null-check before using.
     */
    EntityType<?> pirateType();

    /** Returns {@code "female"} or {@code "male"} for the given citizen entity. */
    String gender(LivingEntity entity);

    /**
     * Returns the cleaned profession key for the given citizen entity
     * (e.g. {@code "baker"}, {@code "miner"}, {@code "unemployed"}).
     */
    String profession(LivingEntity entity);
}
