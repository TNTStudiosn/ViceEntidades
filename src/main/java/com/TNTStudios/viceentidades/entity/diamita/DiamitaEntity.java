package com.TNTStudios.viceentidades.entity.diamita;

import com.TNTStudios.viceentidades.entity.common.ApproachTargetGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;



public class DiamitaEntity extends PathAwareEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int explosionTicks = -1;
    private boolean isExploding = false;
    private String currentAnimation = "idle";


    public DiamitaEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    // Animaciones vacías por ahora
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation.diamita.idle");

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<DiamitaEntity> controller = new AnimationController<>(this, "controller", 0, state -> {
            if (currentAnimation.equals("explotar")) {
                return PlayState.CONTINUE; // Dejamos que siga la animación de explotar
            }

            if (isExploding) {
                return PlayState.STOP; // Detenemos el cambio de animaciones
            }

            if (state.isMoving()) {
                currentAnimation = "run";
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.diamita.run"));
            }

            currentAnimation = "idle";
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.diamita.idle"));
        });

        controller.triggerableAnim("animation.diamita.explotar", RawAnimation.begin().thenPlay("animation.diamita.explotar"));
        controllers.add(controller);
    }

    @Override
    public boolean damage(net.minecraft.entity.damage.DamageSource source, float amount) {
        String name = source.getName();
        if (name.equals("explosion") || name.equals("explosion.player")) {
            return false;
        }

        return super.damage(source, amount);
    }

    @Override
    public void onDamaged(DamageSource source) {
        super.onDamaged(source);

        if (source.getAttacker() instanceof PlayerEntity player) {
            this.setTarget(player);
        }
    }



    @Override
    protected void playStepSound(net.minecraft.util.math.BlockPos pos, net.minecraft.block.BlockState state) {
        this.playSound(state.getSoundGroup().getStepSound(), 1.0F, 1.0F);
    }




    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25);
    }


    @Override
    protected void initGoals() {
        // Camina aleatoriamente si no ve jugadores
        this.goalSelector.add(1, new WanderAroundFarGoal(this, 1.0)); // bajamos un poco para balancear carga

        // Mira a jugadores cercanos solo si están realmente cerca
        this.goalSelector.add(2, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));

        // Sigue al jugador si tiene uno como objetivo
        this.goalSelector.add(3, new ApproachTargetGoal(this, 1.6, 1.5F));

        // Selecciona como objetivo a jugadores válidos
        this.targetSelector.add(1, new ActiveTargetGoal<>(
                this,
                PlayerEntity.class,
                64,               // distancia máxima
                false,             // check visibility
                false,            // no necesita navegación
                living -> !living.isSpectator() && living.isAlive()
        ));
    }

    @Override
    public void tick() {
        super.tick();

        // Lógica del lado del servidor
        if (!this.getWorld().isClient) {
            // Si aún no está explotando y el objetivo es un jugador a menos de 3 bloques...
            if (!isExploding && this.getTarget() instanceof PlayerEntity target && this.squaredDistanceTo(target) < 1) {
                isExploding = true;                  // Activamos el modo explosión
                explosionTicks = 0;                  // Reiniciamos contador de ticks
                this.getNavigation().stop();         // Detenemos navegación
                this.setVelocity(0, 0, 0);           // Cancelamos movimiento

                // Indicamos a GeckoLib que debe mantener la animación de explotar activa
                this.currentAnimation = "explotar";
                this.triggerAnim("controller", "animation.diamita.explotar");
            }

            if (isExploding) {
                this.setVelocity(0, 0, 0);           // Sigue inmóvil durante la secuencia
                explosionTicks++;

                if (explosionTicks == 18) {
                    this.getServer().getCommandManager().executeWithPrefix(
                            this.getCommandSource().withLevel(4).withSilent().withPosition(this.getPos()),
                            "summon immersive_aircraft:tiny_tnt ~ ~ ~"
                    );
                    this.discard(); // Removemos a Diamita del mundo
                }
            }
        }
    }



    public boolean isExploding() {
        return isExploding;
    }

}
