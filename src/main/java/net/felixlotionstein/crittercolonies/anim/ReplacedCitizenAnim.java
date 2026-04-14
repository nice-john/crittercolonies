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

public final class ReplacedCitizenAnim implements GeoReplacedEntity {
    public static final ReplacedCitizenAnim INSTANCE = new ReplacedCitizenAnim();

    private static final RawAnimation IDLE  = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK  = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation SWING = RawAnimation.begin().then("swing", LoopType.PLAY_ONCE);
    private static final RawAnimation SLEEP = RawAnimation.begin().thenLoop("sleep");

    // Per-entity maps keyed by entity ID — written each frame by ReplacedCitizenRenderer.
    public final Map<Integer, Boolean> movingMap       = new ConcurrentHashMap<>();
    public final Map<Integer, Integer> swingRemainingMap = new ConcurrentHashMap<>();
    public final Map<Integer, Boolean> sleepingMap     = new ConcurrentHashMap<>();

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private ReplacedCitizenAnim() {}

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 5, state -> {
            int id = state.getData(DataTickets.ENTITY).getId();

            if (INSTANCE.sleepingMap.getOrDefault(id, false))  return state.setAndContinue(SLEEP);
            if (INSTANCE.swingRemainingMap.getOrDefault(id, 0) > 0) return state.setAndContinue(SWING);
            if (INSTANCE.movingMap.getOrDefault(id, false))    return state.setAndContinue(WALK);
            return state.setAndContinue(IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public EntityType<?> getReplacingEntityType() {
        return ModEntities.CITIZEN;
    }
}
