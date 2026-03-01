package net.felixlotionstein.crittercolonies.anim;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;
import software.bernie.geckolib.animatable.GeoReplacedEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class ReplacedCitizenAnim implements GeoReplacedEntity {
    public static final ReplacedCitizenAnim INSTANCE = new ReplacedCitizenAnim();

    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");

    // Updated each frame by ReplacedCitizenRenderer before GeckoLib animates,
    // because state.isMoving() always returns false for a non-LivingEntity shim.
    public boolean moving = false;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private ReplacedCitizenAnim() {}

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "walk_controller", 5, state -> {
            if (moving) {
                return state.setAndContinue(WALK);
            }
            return state.setAndContinue(IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public EntityType<?> getReplacingEntityType() {
        return ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("minecolonies", "citizen"));
    }
}
