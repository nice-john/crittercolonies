package net.felixlotionstein.crittercolonies.anim;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.registries.BuiltInRegistries;
import software.bernie.geckolib.animatable.GeoReplacedEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.Animation.LoopType;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ReplacedPirateAnim implements GeoReplacedEntity {
    public static final ReplacedPirateAnim INSTANCE = new ReplacedPirateAnim();

    private static final RawAnimation WALK   = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation IDLE   = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation ATTACK = RawAnimation.begin().then("attack", LoopType.PLAY_ONCE);

    // Per-entity maps so each pirate animates independently.
    public final Map<Integer, Boolean> movingMap          = new ConcurrentHashMap<>();
    public final Map<Integer, Integer> attackRemainingMap = new ConcurrentHashMap<>();

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private ReplacedPirateAnim() {}

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "walk_controller", 5, state -> {
            int id = state.getData(DataTickets.ENTITY).getId();
            int remaining = INSTANCE.attackRemainingMap.getOrDefault(id, 0);
            boolean moving = INSTANCE.movingMap.getOrDefault(id, false);

            if (remaining > 0) return state.setAndContinue(ATTACK);
            if (moving)        return state.setAndContinue(WALK);
            return state.setAndContinue(IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public EntityType<?> getReplacingEntityType() {
        return BuiltInRegistries.ENTITY_TYPE
                .getOptional(ResourceLocation.fromNamespaceAndPath("minecolonies", "camppirate"))
                .orElseThrow();
    }
}
