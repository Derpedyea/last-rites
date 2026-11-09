package com.evandev.reliable_requiem.mixin;

import com.evandev.reliable_requiem.api.ICorpseRenderState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin extends HumanoidModel<AvatarRenderState> {
    protected PlayerModelMixin(ModelPart root) {
        super(root);
    }

    /** A corpse lies limp: head rolled to one side, arms and legs slack and a little apart. */
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("TAIL"))
    private void lastRites$limp(AvatarRenderState state, CallbackInfo ci) {
        if (!((ICorpseRenderState) state).lastRites$isCorpse()) return;
        head.xRot = 0.15F;
        head.yRot = 0.7F;
        head.zRot = 0.0F;
        rightArm.xRot = 0.0F;
        rightArm.yRot = 0.0F;
        rightArm.zRot = 0.3F;
        leftArm.xRot = 0.0F;
        leftArm.yRot = 0.0F;
        leftArm.zRot = -0.2F;
        rightLeg.xRot = 0.0F;
        rightLeg.zRot = 0.1F;
        leftLeg.xRot = 0.0F;
        leftLeg.zRot = -0.06F;
    }
}
