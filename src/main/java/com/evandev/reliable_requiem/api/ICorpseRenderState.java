package com.evandev.reliable_requiem.api;

import com.evandev.reliable_requiem.corpse.ragdoll.RagdollPose;

/** Client render state for avatars that are corpses. */
public interface ICorpseRenderState {
    boolean lastRites$isCorpse();

    void lastRites$setCorpse(boolean corpse);

    /** Whether {@link #lastRites$ragdollPose()} holds this frame's pose; if not, the corpse is drawn lying flat. */
    boolean lastRites$hasRagdoll();

    void lastRites$setHasRagdoll(boolean hasRagdoll);

    /** The state's ragdoll pose, reused from frame to frame. */
    RagdollPose lastRites$ragdollPose();
}
