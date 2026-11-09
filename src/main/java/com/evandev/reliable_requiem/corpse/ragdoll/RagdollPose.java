package com.evandev.reliable_requiem.corpse.ragdoll;

import org.joml.Quaternionf;
import org.joml.Vector3f;

/** A ragdoll's pose for one frame: where its torso is, and how each model part sits. */
public final class RagdollPose {
    /** Torso center, relative to the entity. */
    public final Vector3f center = new Vector3f();
    /** Turns the standing body (+x right, +y up, +z back) into the torso's orientation. */
    public final Quaternionf rotation = new Quaternionf();
    /**
     * Per bone ({@link CorpseRagdoll#BONES}): its model part's pivot (pixels) and angles x y z (radians), relative to the
     * part it hangs from ({@link CorpseRagdoll#PARENT}). The torso's entry is unused.
     */
    public final float[][] parts = new float[CorpseRagdoll.BONES][6];
}
