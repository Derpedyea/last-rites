package com.evandev.reliable_requiem.mixin;

import com.evandev.reliable_requiem.api.ICorpseRenderState;
import com.evandev.reliable_requiem.corpse.ragdoll.RagdollPose;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AvatarRenderState.class)
public class AvatarRenderStateMixin implements ICorpseRenderState {
    @Unique
    private boolean lastRites$corpse;
    @Unique
    private boolean lastRites$hasRagdoll;
    @Unique
    private final RagdollPose lastRites$pose = new RagdollPose();

    @Override
    public boolean lastRites$isCorpse() {
        return lastRites$corpse;
    }

    @Override
    public void lastRites$setCorpse(boolean corpse) {
        lastRites$corpse = corpse;
    }

    @Override
    public boolean lastRites$hasRagdoll() {
        return lastRites$hasRagdoll;
    }

    @Override
    public void lastRites$setHasRagdoll(boolean hasRagdoll) {
        lastRites$hasRagdoll = hasRagdoll;
    }

    @Override
    public RagdollPose lastRites$ragdollPose() {
        return lastRites$pose;
    }
}
