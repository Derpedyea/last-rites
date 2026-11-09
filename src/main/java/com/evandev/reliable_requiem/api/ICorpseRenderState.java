package com.evandev.reliable_requiem.api;

/** Client render state flag for avatars that are corpses. */
public interface ICorpseRenderState {
    boolean lastRites$isCorpse();

    void lastRites$setCorpse(boolean corpse);
}
