package com.evandev.reliable_requiem.api;

/** Player models with extra parts that split each arm and leg at the elbow or knee, shown only on corpses. */
public interface IJointedModel {
    /** Back to whole limbs, as the first-person hand needs. */
    void lastRites$wholeLimbs();
}
