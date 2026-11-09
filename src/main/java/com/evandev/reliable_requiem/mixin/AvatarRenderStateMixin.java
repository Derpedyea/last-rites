package com.evandev.reliable_requiem.mixin;

import com.evandev.reliable_requiem.api.ICorpseRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AvatarRenderState.class)
public class AvatarRenderStateMixin implements ICorpseRenderState {
    @Unique
    private boolean lastRites$corpse;

    @Override
    public boolean lastRites$isCorpse() {
        return lastRites$corpse;
    }

    @Override
    public void lastRites$setCorpse(boolean corpse) {
        lastRites$corpse = corpse;
    }
}
