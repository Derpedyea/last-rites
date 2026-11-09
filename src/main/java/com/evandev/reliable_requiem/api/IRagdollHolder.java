package com.evandev.reliable_requiem.api;

import com.evandev.reliable_requiem.corpse.ragdoll.CorpseRagdoll;
import org.jetbrains.annotations.Nullable;

/** Client mannequins that are corpses carry their ragdoll. */
public interface IRagdollHolder {
    /** The corpse's ragdoll; null if this mannequin isn't a corpse, or while its body is still being settled. */
    @Nullable CorpseRagdoll lastRites$ragdoll();
}
