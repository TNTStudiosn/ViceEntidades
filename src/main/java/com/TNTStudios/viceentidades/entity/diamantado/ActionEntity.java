// src/main/java/com/TNTStudios/viceentidades/entity/action/ActionEntity.java
package com.TNTStudios.viceentidades.entity.diamantado;

import com.TNTStudios.viceentidades.entity.common.ApproachTargetGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * ActionEntity es un mob más grande que persigue y golpea al jugador.
 * Tiene 3 animaciones: idle, walk y attack.
 */
public class ActionEntity extends PathAwareEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private String currentAnim = "idle";

    public ActionEntity(EntityType<? extends PathAwareEntity> type, World world) {
        super(type, world);
    }

    // Atributos: vida y daño balanceados para armadura de hierro + prot II (≈73% reducción)
    public static DefaultAttributeContainer.Builder createAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0)        // 15 corazones
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.23)    // velocidad moderada
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8.0)      // daño base
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 2.0);  // ligero retroceso
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<ActionEntity> controller = new AnimationController<>(
                this, "action_controller", 0, this::predicate
        );
        // Animación de ataque disparada manualmente
        controller.triggerableAnim("animation.action.attack", RawAnimation.begin().thenPlay("animation.action.attack"));
        controllers.add(controller);
    }

    private <E extends GeoEntity> PlayState predicate(AnimationState<E> state) {
        ActionEntity animEntity = (ActionEntity) state.getAnimatable();

        // Si se está reproduciendo el ataque
        if (animEntity.currentAnim.equals("attack")) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.action.attack"));
        }

        // Si se mueve, reproducir walk
        if (state.isMoving()) {
            currentAnim = "walk";
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.action.walk"));
        }

        // Por defecto, idle
        currentAnim = "idle";
        return state.setAndContinue(RawAnimation.begin().thenLoop("animation.action.idle"));
    }

    @Override
    protected void initGoals() {
        // Persigue constantemente al jugador
        this.goalSelector.add(1, new ApproachTargetGoal(this, 1.4, 1.2F));
        // Ataque cuerpo a cuerpo con pausa reducida y alcance extendido
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.2, true) {
            @Override
            protected double getSquaredMaxAttackDistance(LivingEntity target) {
                // dobla el alcance efectivo
                float reach = this.mob.getWidth() * 2.0F + target.getWidth();
                return reach * reach;
            }
        });
        // Patrulla cuando no tiene objetivo
        this.goalSelector.add(3, new WanderAroundFarGoal(this, 1.0));
        // Observa jugadores cercanos
        this.goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        // Selecciona como objetivo a jugadores vivos
        this.targetSelector.add(1, new ActiveTargetGoal<>(
                this,
                PlayerEntity.class,
                64, false, false,
                living -> !living.isSpectator() && living.isAlive()
        ));
    }



    @Override
    public boolean tryAttack(net.minecraft.entity.Entity target) {
        boolean result = super.tryAttack(target);
        if (result) {
            // Disparar la animación de ataque
            this.currentAnim = "attack";
            this.triggerAnim("action_controller", "animation.action.attack");
        }
        return result;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        // Ignorar daño de explosiones, si quieres
        String name = source.getName();
        if (name.equals("explosion") || name.equals("explosion.player")) {
            return false;
        }
        return super.damage(source, amount);
    }

    @Override
    protected void playStepSound(BlockPos pos, net.minecraft.block.BlockState state) {
        this.playSound(state.getSoundGroup().getStepSound(), 1.0F, 1.0F);
    }
}
