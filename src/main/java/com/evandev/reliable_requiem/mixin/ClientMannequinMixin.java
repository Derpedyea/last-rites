package com.evandev.reliable_requiem.mixin;

import com.evandev.reliable_requiem.api.IRagdollHolder;
import com.evandev.reliable_requiem.corpse.Corpses;
import com.evandev.reliable_requiem.corpse.ragdoll.CorpseRagdoll;
import net.minecraft.client.entity.ClientMannequin;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientMannequin.class)
public abstract class ClientMannequinMixin extends Mannequin implements IRagdollHolder {
    @Unique
    private @Nullable CorpseRagdoll lastRites$ragdoll;

    protected ClientMannequinMixin(EntityType<Mannequin> type, Level level) {
        super(type, level);
    }

    @Override
    public @Nullable CorpseRagdoll lastRites$ragdoll() {
        if (lastRites$ragdoll == null && Corpses.isCorpseName(getCustomName())) {
            lastRites$ragdoll = CorpseRagdoll.create(getId(), getUUID(), position(), getYRot(), level());
        }
        return lastRites$ragdoll;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void lastRites$tickRagdoll(CallbackInfo ci) {
        boolean made = lastRites$ragdoll != null;
        CorpseRagdoll ragdoll = lastRites$ragdoll();
        if (ragdoll != null && made) ragdoll.tick(level());
    }
}
