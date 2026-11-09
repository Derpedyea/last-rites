package com.evandev.reliable_requiem.mixin;

import com.evandev.reliable_requiem.api.ICorpseRenderState;
import com.evandev.reliable_requiem.api.IRagdollHolder;
import com.evandev.reliable_requiem.corpse.Corpses;
import com.evandev.reliable_requiem.corpse.ragdoll.CorpseRagdoll;
import com.evandev.reliable_requiem.corpse.ragdoll.RagdollPose;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.decoration.Mannequin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {
    /** Set first: the shadow is worked out while the render state is filled in. */
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("HEAD"))
    private void lastRites$markCorpse(Avatar entity, AvatarRenderState state, float partialTicks, CallbackInfo ci) {
        ((ICorpseRenderState) state).lastRites$setCorpse(entity instanceof Mannequin && Corpses.isCorpseName(entity.getCustomName()));
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
    private void lastRites$ragdollPose(Avatar entity, AvatarRenderState state, float partialTicks, CallbackInfo ci) {
        ICorpseRenderState corpse = (ICorpseRenderState) state;
        CorpseRagdoll ragdoll = corpse.lastRites$isCorpse() && entity instanceof IRagdollHolder holder ? holder.lastRites$ragdoll() : null;
        corpse.lastRites$setHasRagdoll(ragdoll != null);
        if (ragdoll != null) ragdoll.pose(partialTicks, entity.getPosition(partialTicks), corpse.lastRites$ragdollPose());
    }

    /** A ragdoll places and turns the whole body itself, in place of the lying pose. */
    @Inject(method = "setupRotations(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V", at = @At("HEAD"), cancellable = true)
    private void lastRites$ragdollRotations(AvatarRenderState state, PoseStack poseStack, float bodyRot, float entityScale, CallbackInfo ci) {
        ICorpseRenderState corpse = (ICorpseRenderState) state;
        if (!corpse.lastRites$hasRagdoll()) return;
        RagdollPose pose = corpse.lastRites$ragdollPose();
        poseStack.translate(pose.center.x, pose.center.y, pose.center.z);
        poseStack.rotate(pose.rotation);
        poseStack.translate(0.0F, -CorpseRagdoll.TORSO_CENTER_Y, 0.0F);
        ci.cancel();
    }
}
