package com.TNTStudios.viceentidades.entity.diamantado;

import com.TNTStudios.viceentidades.entity.common.ApproachTargetGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class DiamantadoEntity extends PathAwareEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private boolean attacking = false;
    private String currentAnim = "idle";
    private final ServerBossBar bossBar = new ServerBossBar(
            Text.of("§cDiamantado"),
            ServerBossBar.Color.RED,
            ServerBossBar.Style.PROGRESS
    );

    public DiamantadoEntity(EntityType<? extends PathAwareEntity> type, World world) {
        super(type, world);
        // Mostrar bossbar a todos los jugadores activos
        if (world.getServer() != null) {
            for (ServerPlayerEntity p : world.getServer().getPlayerManager().getPlayerList()) {
                bossBar.addPlayer(p);
            }
        }
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 400.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.35)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 3.5);
    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<DiamantadoEntity> controller = new AnimationController<>(
                this, "controller", 0, this::predicate
        );
        controller.triggerableAnim("animation.diamantado.attack",
                RawAnimation.begin().thenPlay("animation.diamantado.attack"));
        controller.triggerableAnim("animation.diamantado.die",
                RawAnimation.begin().thenPlay("animation.diamantado.die"));
        controllers.add(controller);
    }

    private <E extends GeoEntity> PlayState predicate(AnimationState<E> state) {
        if (this.isDead()) {
            currentAnim = "die";
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.diamantado.die"));
        }
        if (currentAnim.equals("attack")) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.diamantado.attack"));
        }
        if (attacking && state.isMoving()) {
            currentAnim = "run";
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.diamantado.run"));
        }
        if (state.isMoving()) {
            currentAnim = "walk";
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.diamantado.walk"));
        }
        currentAnim = "idle";
        return state.setAndContinue(RawAnimation.begin().thenLoop("animation.diamantado.idle"));
    }

    public void startAttack(LivingEntity target) {
        this.attacking = true;
        this.setTarget(target);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new ApproachTargetGoal(this, 1.5, 1.5F) {
            @Override
            public boolean canStart() {
                return attacking && super.canStart();
            }

            @Override
            public boolean shouldContinue() {
                return attacking && super.shouldContinue();
            }
        });
        this.goalSelector.add(2, new net.minecraft.entity.ai.goal.WanderAroundFarGoal(this, 1.0));
    }

    @Override
    public void tick() {
        super.tick();

        // Actualizar bossbar
        bossBar.setPercent(this.getHealth() / this.getMaxHealth());

        // Ataque tipo terremoto, solo si está lo suficientemente cerca del objetivo
        if (attacking && this.getTarget() instanceof LivingEntity) {
            LivingEntity tgt = (LivingEntity) this.getTarget();
            if (this.squaredDistanceTo(tgt) < 4) {

                // Aplicamos daño y knockback a todos los jugadores cercanos (distancia 4 bloques)
                for (PlayerEntity player : this.getWorld().getPlayers()) {
                    if (!(player instanceof ServerPlayerEntity serverPlayer)) continue;
                    if (serverPlayer.squaredDistanceTo(this) > 16) continue;

                    // Fuente de daño: mob directo (esto asegura retroceso y sonido correcto)
                    DamageSource source = this.getDamageSources().mobAttack(this);

                    // Dañamos y empujamos con fuerza radial
                    serverPlayer.damage(source, 8.0F);
                    Vec3d knock = serverPlayer.getPos().subtract(this.getPos()).normalize().multiply(2);
                    serverPlayer.takeKnockback(1.5F, knock.x, knock.z);
                }

                // Disparamos animación de ataque
                currentAnim = "attack";
                this.triggerAnim("controller", "animation.diamantado.attack");

                // Terminamos la fase de persecución
                attacking = false;
                this.getNavigation().stop();
                this.setTarget(null);
            }
        }



        // Eliminar bossbar al morir
        if (!this.isAlive()) {
            for (ServerPlayerEntity p : bossBar.getPlayers()) {
                bossBar.removePlayer(p);
            }
        }
    }
}
