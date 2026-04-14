package net.felixlotionstein.crittercolonies.anim;

import com.minecolonies.api.entity.ModEntities;
import net.minecraft.world.entity.EntityType;
import software.bernie.geckolib.animatable.GeoReplacedEntity;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.Animation.LoopType;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ReplacedPirateAnim implements GeoReplacedEntity {
    public static final ReplacedPirateAnim INSTANCE = new ReplacedPirateAnim();

    private static final RawAnimation WALK   = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation IDLE   = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation ATTACK = RawAnimation.begin().then("attack", LoopType.PLAY_ONCE);

    // Per-entity maps so each pirate animates independently.
    // Key = entity ID (Entity#getId()). Written by ReplacedPirateRenderer each frame.
    public final Map<Integer, Boolean> movingMap       = new ConcurrentHashMap<>();
    public final Map<Integer, Integer> attackRemainingMap = new ConcurrentHashMap<>();

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private ReplacedPirateAnim() {}

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "walk_controller", 5, state -> {
            // DataTickets.ENTITY gives us the actual pirate being rendered right now.
            int id = state.getData(DataTickets.ENTITY).getId();
            int remaining = INSTANCE.attackRemainingMap.getOrDefault(id, 0);
            boolean moving = INSTANCE.movingMap.getOrDefault(id, false);

            if (remaining > 0)  return state.setAndContinue(ATTACK);
            if (moving)         return state.setAndContinue(WALK);
            return state.setAndContinue(IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public EntityType<?> getReplacingEntityType() {
        return ModEntities.CAMP_PIRATE;
    }
}
