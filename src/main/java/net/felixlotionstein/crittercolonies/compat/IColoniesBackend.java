package net.felixlotionstein.crittercolonies.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

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

    /**
     * Returns the ResourceLocation of the status/interaction icon that should be drawn
     * above this citizen's head, or {@code null} when no icon is currently visible.
     *
     * MineColonies 1.20.1 calls this an "interaction icon" — it appears when the
     * citizen has a problem the player needs to address (recruit, missing tool,
     * hungry, raided, etc.). Both colonies mods expose the same conceptual data.
     */
    @Nullable ResourceLocation statusIcon(LivingEntity entity);
}
