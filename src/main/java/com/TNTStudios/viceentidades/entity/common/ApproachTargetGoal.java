package com.TNTStudios.viceentidades.entity.common;

import com.TNTStudios.viceentidades.entity.diamita.DiamitaEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;

public class ApproachTargetGoal extends Goal {
    private final MobEntity mob;
    private final double speed;
    private final float stopDistance;
    private int updateCooldown = 0;

    public ApproachTargetGoal(MobEntity mob, double speed, float stopDistance) {
        this.mob = mob;
        this.speed = speed;
        this.stopDistance = stopDistance;
        this.setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        if (mob instanceof DiamitaEntity diamita && diamita.isExploding()) return false;
        LivingEntity target = mob.getTarget();
        return target != null && target.isAlive() && mob.squaredDistanceTo(target) > (stopDistance * stopDistance);
    }

    @Override
    public void tick() {
        if (--updateCooldown <= 0) {
            LivingEntity target = mob.getTarget();
            if (target != null) {
                Vec3d targetPos = target.getPos();
                if (mob.getNavigation().isIdle() || mob.squaredDistanceTo(target) > 1.0) {
                    mob.getNavigation().startMovingTo(targetPos.x, targetPos.y, targetPos.z, speed);
                }
            }
            updateCooldown = 5; // solo actualiza dirección cada 5 ticks
        }
    }

    @Override
    public boolean shouldContinue() {
        if (mob instanceof DiamitaEntity diamita && diamita.isExploding()) return false;
        LivingEntity target = mob.getTarget();
        return target != null && target.isAlive() && mob.squaredDistanceTo(target) > (stopDistance * stopDistance);
    }
}
