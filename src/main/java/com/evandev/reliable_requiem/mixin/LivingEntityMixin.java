package com.evandev.reliable_requiem.mixin;

import com.evandev.reliable_requiem.corpse.Corpses;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=26.1 {
import com.evandev.reliable_requiem.api.IRequiemItem;
import com.evandev.reliable_requiem.config.ModConfig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//?}

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"))
    private void lastRites$beginCorpse(ServerLevel level, DamageSource source, CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayer player) Corpses.begin(player);
    }

    @Inject(method = "dropAllDeathLoot", at = @At("TAIL"))
    private void lastRites$finishCorpse(ServerLevel level, DamageSource source, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof ServerPlayer player) Corpses.finish(player);
        else if (Corpses.isCorpse(self)) Corpses.spill(self, level);
    }

    //? if >=26.1 {
    @Inject(method = "createItemStackToDrop", at = @At("RETURN"))
    private void reliableRequiem$tagDeathDrops(ItemStack itemStack, boolean randomly, boolean thrownFromHand, CallbackInfoReturnable<ItemEntity> cir) {
        ItemEntity droppedItem = cir.getReturnValue();
        LivingEntity entity = (LivingEntity) (Object) this;

        if (droppedItem != null && entity instanceof Player && entity.isDeadOrDying()) {
            ((IRequiemItem) droppedItem).reliableRequiem$setDroppedOnDeath(true);

            if (ModConfig.get().enabled) {
                double multiplier = ModConfig.get().itemSpreadMultiplier;
                if (multiplier != 1.0) {
                    Vec3 delta = droppedItem.getDeltaMovement();
                    droppedItem.setDeltaMovement(delta.x * multiplier, delta.y * multiplier, delta.z * multiplier);
                }
            }
        }
    }
    //?}
}
