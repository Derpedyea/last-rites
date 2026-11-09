package com.evandev.reliable_requiem.api;

import com.evandev.reliable_requiem.corpse.CorpseInventory;

import java.util.UUID;

/**
 * Corpse state mixed into vanilla mannequins. A mannequin is a corpse when {@link #lastRites$getOwner()} is non-null.
 */
public interface ICorpse {
    UUID lastRites$getOwner();

    String lastRites$getOwnerName();

    long lastRites$getDeathTime();

    int lastRites$getXp();

    void lastRites$setXp(int xp);

    CorpseInventory lastRites$getInventory();

    void lastRites$makeCorpse(UUID owner, String ownerName, long deathTime);

    /** The invisible interaction entity over the body that makes it easy to click. */
    UUID lastRites$getHitbox();

    void lastRites$setHitbox(UUID hitbox);
}
