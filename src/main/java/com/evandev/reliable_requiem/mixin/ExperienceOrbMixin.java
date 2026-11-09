package com.evandev.reliable_requiem.mixin;

import com.evandev.reliable_requiem.config.ModConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin extends Entity {

    public ExperienceOrbMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    //? if >=26.1 {
    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;I)V", at = @At("TAIL"))
    private void reliableRequiem$condenseExperience(Level level, Vec3 pos, Vec3 roughly, int value, CallbackInfo ci) {
    //?} else {
    /*@Inject(method = "<init>(Lnet/minecraft/world/level/Level;DDDI)V", at = @At("TAIL"))
    private void reliableRequiem$condenseExperience(Level level, double x, double y, double z, int value, CallbackInfo ci) {
    *///?}
        reliableRequiem$applySpreadMultiplier(level);
    }

    @Unique
    private void reliableRequiem$applySpreadMultiplier(Level level) {
        if (ModConfig.get().enabled) {
            double multiplier = ModConfig.get().itemSpreadMultiplier;
            if (multiplier != 1.0) {
                boolean isFromDeath = !level.getEntitiesOfClass(Player.class,
                                this.getBoundingBox().inflate(0.5)).stream()
                        .filter(Player::isDeadOrDying)
                        .toList().isEmpty();

                if (isFromDeath) {
                    Vec3 delta = this.getDeltaMovement();
                    this.setDeltaMovement(delta.x * multiplier, delta.y * multiplier, delta.z * multiplier);
                }
            }
        }
    }
}
