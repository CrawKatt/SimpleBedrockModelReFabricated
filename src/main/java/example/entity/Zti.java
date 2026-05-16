package example.entity;

import example.animation.ZtiAnimationInstance;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class Zti extends PathAwareEntity {
    private static final TrackedData<Integer> ATTACK_ANIMATION_TICKS = DataTracker.registerData(Zti.class, TrackedDataHandlerRegistry.INTEGER);

    private ZtiAnimationInstance animationInstance;

    public Zti(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 80.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8.0D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0D)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0D)
                .add(EntityAttributes.GENERIC_ARMOR, 20.0D);
    }

    @Override
    protected void initDataTracker(DataTracker.@NotNull Builder builder) {
        super.initDataTracker(builder);
        builder.add(ATTACK_ANIMATION_TICKS, 0);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(0, new SwimGoal(this));
        goalSelector.add(1, new MeleeAttackGoal(this, 1.0D, false));
        goalSelector.add(2, new WanderAroundFarGoal(this, 0.8D));
        goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        goalSelector.add(4, new LookAroundGoal(this));

        targetSelector.add(0, new RevengeGoal(this));
        targetSelector.add(1, new ActiveTargetGoal<>(this, IronGolemEntity.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (!getWorld().isClient && isAttackAnimationActive()) {
            dataTracker.set(ATTACK_ANIMATION_TICKS, dataTracker.get(ATTACK_ANIMATION_TICKS) - 1);
        }
    }

    @Override
    public boolean tryAttack(@NotNull Entity target) {
        dataTracker.set(ATTACK_ANIMATION_TICKS, 10);
        return super.tryAttack(target);
    }

    public boolean isAttackAnimationActive() {
        return dataTracker.get(ATTACK_ANIMATION_TICKS) > 0;
    }

    @Environment(EnvType.CLIENT)
    public ZtiAnimationInstance getAnimationInstance() {
        if (animationInstance == null) {
            animationInstance = new ZtiAnimationInstance(this);
        }
        return animationInstance;
    }
}
