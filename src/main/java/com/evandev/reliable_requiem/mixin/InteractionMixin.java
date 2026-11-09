package com.evandev.reliable_requiem.mixin;

import com.evandev.reliable_requiem.corpse.Corpses;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** A corpse's hitbox passes right-clicks on to the corpse. */
@Mixin(Interaction.class)
public abstract class InteractionMixin {
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void lastRites$forwardToCorpse(Player player, InteractionHand hand, Vec3 location, CallbackInfoReturnable<InteractionResult> cir) {
        InteractionResult result = Corpses.interactHitbox((Interaction) (Object) this, player, hand);
        if (result != null) cir.setReturnValue(result);
    }
}
