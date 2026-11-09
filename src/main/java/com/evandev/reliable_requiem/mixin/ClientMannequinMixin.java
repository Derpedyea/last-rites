package com.evandev.reliable_requiem.mixin;

import com.evandev.reliable_requiem.api.IRagdollHolder;
import com.evandev.reliable_requiem.corpse.Corpses;
import com.evandev.reliable_requiem.corpse.ragdoll.CorpseRagdoll;
import com.evandev.reliable_requiem.corpse.ragdoll.LevelRagdollWorld;
import com.evandev.reliable_requiem.corpse.ragdoll.Ragdolls;
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

import java.util.concurrent.CompletableFuture;

@Mixin(ClientMannequin.class)
public abstract class ClientMannequinMixin extends Mannequin implements IRagdollHolder {
    @Unique
    private @Nullable CorpseRagdoll lastRites$ragdoll;
    /** A corpse first seen after it fell: its body being settled in the background. */
    @Unique
    private @Nullable CompletableFuture<CorpseRagdoll> lastRites$settling;
    @Unique
    private @Nullable LevelRagdollWorld lastRites$world;

    protected ClientMannequinMixin(EntityType<Mannequin> type, Level level) {
        super(type, level);
    }

    @Override
    public @Nullable CorpseRagdoll lastRites$ragdoll() {
        if (lastRites$ragdoll != null || !Corpses.isCorpseName(getCustomName())) return lastRites$ragdoll;
        if (lastRites$settling == null) {
            if (Ragdolls.takeFresh(getId())) {
                lastRites$ragdoll = Ragdolls.start(this);
            } else {
                lastRites$settling = Ragdolls.settle(level(), this);
            }
        } else if (lastRites$settling.isDone()) {
            // On failure fall back to letting it fall in view.
            lastRites$ragdoll = lastRites$settling.isCompletedExceptionally() ? Ragdolls.start(this) : lastRites$settling.join();
            lastRites$settling = null;
        }
        return lastRites$ragdoll;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void lastRites$tickRagdoll(CallbackInfo ci) {
        boolean ready = lastRites$ragdoll != null;
        CorpseRagdoll ragdoll = lastRites$ragdoll();
        if (ragdoll == null || !ready) return;
        if (lastRites$world == null) lastRites$world = new LevelRagdollWorld(level());
        ragdoll.tick(lastRites$world);
    }
}
