package com.evandev.reliable_requiem.mixin;

import com.evandev.reliable_requiem.modules.RequiemModules;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if <26.1 {
/*import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
*///?}

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Inject(method = "setRespawnPosition", at = @At("HEAD"), cancellable = true)
    //? if >=26.1 {
    private void reliableRequiem$onSetRespawnPosition(ServerPlayer.RespawnConfig respawnConfig, boolean showMessage, CallbackInfo ci) {
    //?} else {
    /*private void reliableRequiem$onSetRespawnPosition(ResourceKey<Level> dimension, BlockPos position, float angle, boolean forced, boolean sendMessage, CallbackInfo ci) {
    *///?}
        if (RequiemModules.onSetSpawn()) {
            ci.cancel();
        }
    }
}
