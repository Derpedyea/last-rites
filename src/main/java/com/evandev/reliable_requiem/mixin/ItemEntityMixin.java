package com.evandev.reliable_requiem.mixin;

import com.evandev.reliable_requiem.api.IRequiemItem;
import com.evandev.reliable_requiem.config.ModConfig;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//? if >=26.1 {
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//?} else {
/*import net.minecraft.nbt.CompoundTag;
*///?}

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin implements IRequiemItem {

    @Shadow
    private int age;

    @Unique
    private boolean reliableRequiem$droppedOnDeath = false;

    @Override
    public boolean reliableRequiem$isDroppedOnDeath() {
        return reliableRequiem$droppedOnDeath;
    }

    @Override
    public void reliableRequiem$setDroppedOnDeath(boolean droppedOnDeath) {
        this.reliableRequiem$droppedOnDeath = droppedOnDeath;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void reliableRequiem$onTick(CallbackInfo ci) {
        ItemEntity item = (ItemEntity) (Object) this;
        if (!item.level().isClientSide() && ModConfig.get().enabled && this.reliableRequiem$droppedOnDeath) {
            if (ModConfig.get().neverDespawnDeathDrops) {
                if (this.age > 4000) {
                    this.age = 0;
                }
            } else {
                int configuredTimeInTicks = ModConfig.get().dropDespawnTime * 20;
                if (configuredTimeInTicks > 0 && this.age >= configuredTimeInTicks) {
                    item.discard();
                }
            }
        }
    }

    //? if >=26.1 {
    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void reliableRequiem$onHurt(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
    //?} else {
    /*@Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void reliableRequiem$onHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
    *///?}
        if (ModConfig.get().enabled &&
                ModConfig.get().explosionResistantDeathDrops &&
                this.reliableRequiem$droppedOnDeath &&
                source.is(DamageTypeTags.IS_EXPLOSION)) {

            cir.setReturnValue(false);
        }
    }

    //? if >=26.1 {
    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void reliableRequiem$onSave(ValueOutput output, CallbackInfo ci) {
        if (this.reliableRequiem$droppedOnDeath) {
            output.putBoolean("ReliableRequiem_DeathDrop", true);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void reliableRequiem$onRead(ValueInput input, CallbackInfo ci) {
        this.reliableRequiem$droppedOnDeath = input.getBooleanOr("ReliableRequiem_DeathDrop", false);
    }
    //?} else {
    /*@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void reliableRequiem$onSave(CompoundTag compound, CallbackInfo ci) {
        if (this.reliableRequiem$droppedOnDeath) {
            compound.putBoolean("ReliableRequiem_DeathDrop", true);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void reliableRequiem$onRead(CompoundTag compound, CallbackInfo ci) {
        this.reliableRequiem$droppedOnDeath = compound.getBoolean("ReliableRequiem_DeathDrop");
    }
    *///?}
}
