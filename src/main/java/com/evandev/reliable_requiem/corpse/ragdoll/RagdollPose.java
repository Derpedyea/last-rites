package com.evandev.reliable_requiem.corpse.ragdoll;

import org.joml.Quaternionf;
import org.joml.Vector3f;

/** A ragdoll's pose for one frame: where its torso is and how it and each limb are turned. */
public final class RagdollPose {
    /** Torso center, relative to the entity. */
    public final Vector3f center = new Vector3f();
    /** Turns the standing body (+x right, +y up, +z back) into the torso's orientation. */
    public final Quaternionf rotation = new Quaternionf();
    /** Model part angles (x, y, z in radians), relative to the torso. */
    public final Vector3f head = new Vector3f();
    public final Vector3f rightArm = new Vector3f();
    public final Vector3f leftArm = new Vector3f();
    public final Vector3f rightLeg = new Vector3f();
    public final Vector3f leftLeg = new Vector3f();
}
