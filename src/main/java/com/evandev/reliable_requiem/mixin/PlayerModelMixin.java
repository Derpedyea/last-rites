package com.evandev.reliable_requiem.mixin;

import com.evandev.reliable_requiem.api.ICorpseRenderState;
import com.evandev.reliable_requiem.corpse.ragdoll.RagdollPose;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin extends HumanoidModel<AvatarRenderState> {
    protected PlayerModelMixin(ModelPart root) {
        super(root);
    }

    /** Poses a corpse's limbs from its ragdoll. */
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("TAIL"))
    private void lastRites$ragdoll(AvatarRenderState state, CallbackInfo ci) {
        ICorpseRenderState corpse = (ICorpseRenderState) state;
        if (!corpse.lastRites$hasRagdoll()) return;
        RagdollPose pose = corpse.lastRites$ragdollPose();
        body.resetPose();
        lastRites$pose(head, pose.head);
        lastRites$pose(rightArm, pose.rightArm);
        lastRites$pose(leftArm, pose.leftArm);
        lastRites$pose(rightLeg, pose.rightLeg);
        lastRites$pose(leftLeg, pose.leftLeg);
    }

    @Unique
    private static void lastRites$pose(ModelPart part, Vector3f angles) {
        part.resetPose();
        part.setRotation(angles.x, angles.y, angles.z);
    }
}
