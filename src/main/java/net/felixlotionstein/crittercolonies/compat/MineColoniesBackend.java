package net.felixlotionstein.crittercolonies.compat;

import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

/**
 * IColoniesBackend implementation backed by the real MineColonies API.
 * This class directly imports com.minecolonies.* and must ONLY be
 * instantiated when the "minecolonies" mod is confirmed to be loaded.
 */
public final class MineColoniesBackend implements IColoniesBackend {

    @Override
    public EntityType<?> citizenType() {
        return ModEntities.CITIZEN;
    }

    @Override
    public EntityType<?> pirateType() {
        return ModEntities.CAMP_PIRATE;
    }

    @Override
    public String gender(LivingEntity entity) {
        return ((AbstractEntityCitizen) entity).isFemale() ? "female" : "male";
    }

    @Override
    public String profession(LivingEntity entity) {
        ICitizenDataView dataView = ((AbstractEntityCitizen) entity).getCitizenDataView();
        if (dataView == null) return "unemployed";
        String raw = dataView.getJob();
        return ColoniesCompat.cleanJobString(raw != null ? raw : "");
    }
}
