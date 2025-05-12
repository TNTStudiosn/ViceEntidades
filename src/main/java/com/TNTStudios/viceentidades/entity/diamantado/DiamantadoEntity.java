package com.TNTStudios.viceentidades.entity.diamantado;

import com.TNTStudios.viceentidades.entity.common.ApproachTargetGoal;
import com.TNTStudios.viceentidades.entity.diamita.DiamitaEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import com.TNTStudios.viceentidades.registry.ViceEntityTypes;
import java.util.EnumSet;
import java.util.List;

public class DiamantadoEntity extends PathAwareEntity implements GeoEntity {

    private static final int ATTACK_COOLDOWN_TICKS = 20 * 10;
    private static final double ATTACK_RANGE = 10.0;

    private long lastAttackTime = 0;
    private BossPhase phase = BossPhase.IDLE;
    private String currentAnim = "idle";

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossBar bossBar = new ServerBossBar(
            Text.of("Diamanto"),
            ServerBossBar.Color.BLUE,
            ServerBossBar.Style.PROGRESS
    );

    private enum BossPhase { IDLE, CHASING }

    public DiamantadoEntity(EntityType<? extends PathAwareEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 1200.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.35)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 20.0);
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
        if ("attack".equals(currentAnim)) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.diamantado.attack"));
        }
        if (phase == BossPhase.CHASING && state.isMoving()) {
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
        long worldTime = this.getWorld().getTime();
        this.setTarget(target);
        this.phase = BossPhase.CHASING;
        // Forzar cooldown expirado:
        this.lastAttackTime = worldTime - ATTACK_COOLDOWN_TICKS;
    }


    private void performEarthquake() {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return;

        serverWorld.playSound(null, this.getBlockPos(),
                SoundEvents.ENTITY_ENDER_DRAGON_GROWL,
                SoundCategory.HOSTILE, 2.0f, 1.0f);
        serverWorld.spawnParticles(ParticleTypes.EXPLOSION,
                this.getX(), this.getY(), this.getZ(),
                30, 4, 1, 4, 0.3);

        double rangeSq = ATTACK_RANGE * ATTACK_RANGE;
        for (PlayerEntity player : this.getWorld().getPlayers()) {
            if (!(player instanceof ServerPlayerEntity serverPlayer)) continue;
            if (serverPlayer.squaredDistanceTo(this) > rangeSq) continue;

            serverPlayer.damage(this.getDamageSources().mobAttack(this), 12.0F);
            Vec3d direction = serverPlayer.getPos().subtract(this.getPos()).normalize();
            serverPlayer.takeKnockback(10.0F, direction.x * 4, direction.z * 4);
            serverPlayer.sendMessage(Text.of("§c¡Diamanto desata un terremoto brutal!"), true);
        }

        currentAnim = "attack";
        this.triggerAnim("controller", "animation.diamantado.attack");
    }

    @Override
    protected void initGoals() {
        // 0: salto automático si choca contra un bloque durante la persecución
        this.goalSelector.add(0, new net.minecraft.entity.ai.goal.Goal() {
            {
                // Solo controla el movimiento
                this.setControls(EnumSet.of(Control.MOVE));
            }
            @Override
            public boolean canStart() {
                // Cuando estamos persiguiendo y hay colisión horizontal
                return phase == BossPhase.CHASING && DiamantadoEntity.this.horizontalCollision;
            }
            @Override
            public void tick() {
                // Ejecutar el salto
                DiamantadoEntity.this.jump();
            }
        });

        // 1: perseguir al jugador en fase CHASING desde cualquier distancia
        this.goalSelector.add(1, new ApproachTargetGoal(this, 1.5, Float.MAX_VALUE) {
            @Override
            public boolean canStart() {
                return phase == BossPhase.CHASING;
            }
            @Override
            public boolean shouldContinue() {
                return phase == BossPhase.CHASING;
            }
        });

        // 2: deambular cuando no está en fase de persecución
        this.goalSelector.add(2, new net.minecraft.entity.ai.goal.WanderAroundFarGoal(this, 1.0));
    }



    @Override
    public void tick() {
        super.tick();
        bossBar.setPercent(this.getHealth() / this.getMaxHealth());

        if (this.isAlive() && this.getWorld() instanceof ServerWorld serverWorld) {
            for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                double distanceSq = player.squaredDistanceTo(this);

                if (distanceSq < 80 * 80 && player.isAlive()) {
                    if (!bossBar.getPlayers().contains(player)) {
                        bossBar.addPlayer(player);
                    }
                } else {
                    if (bossBar.getPlayers().contains(player)) {
                        bossBar.removePlayer(player);
                    }
                }
            }
        }


        long worldTime = this.getWorld().getTime();

        // ⏱ Solo si está atacando (tras comando) invoca secuaces cada 20 segundos
        if (phase == BossPhase.CHASING && worldTime % 400 == 0 && !this.isDead() && !this.isRemoved()) {
            if (this.getWorld() instanceof ServerWorld serverWorld) {
                // Contar entidades activas de cada tipo
                long diamitasActivas = serverWorld.getEntitiesByType(
                        ViceEntityTypes.DIAMITA,
                        e -> e.isAlive()
                ).size();

                long actionsActivas = serverWorld.getEntitiesByType(
                        ViceEntityTypes.ACTION,
                        e -> e.isAlive()
                ).size();

                // 🧨 Spawnea Diamitas si no se excede el límite
                int diamitasParaSpawnear = Math.max(0, 15 - (int) diamitasActivas);
                for (int i = 0; i < Math.min(5, diamitasParaSpawnear); i++) {
                    DiamitaEntity minion = new DiamitaEntity(ViceEntityTypes.DIAMITA, serverWorld);
                    minion.refreshPositionAndAngles(
                            this.getX() + (random.nextDouble() - 0.5) * 8,
                            this.getY(),
                            this.getZ() + (random.nextDouble() - 0.5) * 8,
                            this.getYaw(),
                            this.getPitch()
                    );
                    serverWorld.spawnEntity(minion);
                }

                // 💥 Spawnea Actions si no se excede el límite
                int actionsParaSpawnear = Math.max(0, 15 - (int) actionsActivas);
                for (int i = 0; i < Math.min(3, actionsParaSpawnear); i++) {
                    ActionEntity brute = new ActionEntity(ViceEntityTypes.ACTION, serverWorld);
                    brute.refreshPositionAndAngles(
                            this.getX() + (random.nextDouble() - 0.5) * 10,
                            this.getY(),
                            this.getZ() + (random.nextDouble() - 0.5) * 10,
                            this.getYaw(),
                            this.getPitch()
                    );
                    serverWorld.spawnEntity(brute);
                }

                // 🗯️ Mensaje en actionbar
                for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                    player.sendMessage(Text.of("§6¡Diamanto invoca a sus secuaces!"), true);
                }
            }
        }


        if (phase == BossPhase.CHASING) {
            Entity targetEntity = this.getTarget();
            if (targetEntity instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) targetEntity;
                double distanceSq = this.squaredDistanceTo(target);
                if (distanceSq < ATTACK_RANGE * ATTACK_RANGE
                        && worldTime - lastAttackTime >= ATTACK_COOLDOWN_TICKS) {
                    performEarthquake();
                    this.lastAttackTime = worldTime;

                    // Buscar siguiente jugador
                    if (worldTime % 40 == 0) { // Solo cada 2 segundos
                        PlayerEntity nearest = this.getWorld().getPlayers().stream()
                                .filter(PlayerEntity::isAlive)
                                .min((a, b) -> Double.compare(this.squaredDistanceTo(a), this.squaredDistanceTo(b)))
                                .orElse(null);

                        if (nearest != null) {
                            this.setTarget(nearest);
                        } else {
                            this.phase = BossPhase.IDLE;
                            this.setTarget(null);
                            this.getNavigation().stop();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);

        // Copia segura para evitar ConcurrentModificationException
        for (ServerPlayerEntity player : List.copyOf(bossBar.getPlayers())) {
            bossBar.removePlayer(player);
        }
    }

}