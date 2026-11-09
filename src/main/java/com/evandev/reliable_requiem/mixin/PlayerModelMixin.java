package com.evandev.reliable_requiem.mixin;

import com.evandev.reliable_requiem.api.ICorpseRenderState;
import com.evandev.reliable_requiem.api.IJointedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDefinition;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.EnumSet;

import static com.evandev.reliable_requiem.corpse.ragdoll.CorpseRagdoll.*;

/**
 * Corpses bend at the elbows and knees, so each arm and leg gets two extra half-length parts: the upper half drawn
 * where the limb is, the lower half hanging from the joint. They are shown, and the whole limb hidden, only on corpses.
 */
@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin extends HumanoidModel<AvatarRenderState> implements IJointedModel {
    @Unique
    private static final String LAST_RITES$UPPER = "last_rites_upper", LAST_RITES$LOWER = "last_rites_lower", LAST_RITES$OVERLAY = "last_rites_overlay";

    @Shadow
    @Final
    public ModelPart leftSleeve;
    @Shadow
    @Final
    public ModelPart rightSleeve;
    @Shadow
    @Final
    public ModelPart leftPants;
    @Shadow
    @Final
    public ModelPart rightPants;

    /** Per limb (right arm, left arm, right leg, left leg); null for models without the extra parts, like armor. */
    @Unique
    private ModelPart[] lastRites$limbs, lastRites$overlays, lastRites$upper, lastRites$lower, lastRites$upperOverlay, lastRites$lowerOverlay;

    protected PlayerModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(method = "createMesh", at = @At("RETURN"))
    private static void lastRites$jointedLimbs(CubeDeformation scale, boolean slim, CallbackInfoReturnable<MeshDefinition> cir) {
        PartDefinition root = cir.getReturnValue().getRoot();
        float arm = slim ? 3 : 4;
        lastRites$split(root.getChild("right_arm"), 40, 16, 40, 32, slim ? -2 : -3, -2, arm, -1, 4, scale);
        lastRites$split(root.getChild("left_arm"), 32, 48, 48, 48, -1, -2, arm, 1, 4, scale);
        lastRites$split(root.getChild("right_leg"), 0, 16, 0, 32, -2, 0, 4, 0, 6, scale);
        lastRites$split(root.getChild("left_leg"), 16, 48, 0, 48, -2, 0, 4, 0, 6, scale);
    }

    /**
     * Adds the halves of a 12 pixel limb whose box starts at x y, texture at u v and overlay texture at su sv. The joint is
     * at jx jy from the limb's pivot, matching {@code CorpseRagdoll.PIVOT}.
     */
    @Unique
    private static void lastRites$split(PartDefinition limb, int u, int v, int su, int sv, float x, float y, float w, float jx, float jy, CubeDeformation scale) {
        CubeDeformation overlay = scale.extend(0.25F);
        PartDefinition upper = limb.addOrReplaceChild(LAST_RITES$UPPER, CubeListBuilder.create().texOffs(u, v).addBox(x, y, -2, w, 6, 4, scale), PartPose.ZERO);
        upper.addOrReplaceChild(LAST_RITES$OVERLAY, CubeListBuilder.create().texOffs(su, sv).addBox(x, y, -2, w, 6, 4, overlay), PartPose.ZERO);
        PartDefinition lower = limb.addOrReplaceChild(LAST_RITES$LOWER, lastRites$lowerHalf(u, v, x - jx, w, scale), PartPose.offset(jx, jy, 0));
        lower.addOrReplaceChild(LAST_RITES$OVERLAY, lastRites$lowerHalf(su, sv, x - jx, w, overlay), PartPose.ZERO);
    }

    /** A limb's lower half: sides from the lower half of the limb's texture, ends from the limb's own end faces. */
    @Unique
    private static CubeListBuilder lastRites$lowerHalf(int u, int v, float x, float w, CubeDeformation grow) {
        CubeListBuilder builder = CubeListBuilder.create();
        var cubes = ((CubeListBuilderAccessor) builder).lastRites$cubes();
        cubes.add(new CubeDefinition(null, u, v + 6, x, 0, -2, w, 6, 4, grow, false, 1, 1,
                EnumSet.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)));
        cubes.add(new CubeDefinition(null, u, v, x, 0, -2, w, 6, 4, grow, false, 1, 1, EnumSet.of(Direction.UP, Direction.DOWN)));
        return builder;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void lastRites$findHalves(ModelPart root, boolean slim, CallbackInfo ci) {
        if (!rightArm.hasChild(LAST_RITES$UPPER)) return;
        lastRites$limbs = new ModelPart[]{rightArm, leftArm, rightLeg, leftLeg};
        lastRites$overlays = new ModelPart[]{rightSleeve, leftSleeve, rightPants, leftPants};
        lastRites$upper = new ModelPart[4];
        lastRites$lower = new ModelPart[4];
        lastRites$upperOverlay = new ModelPart[4];
        lastRites$lowerOverlay = new ModelPart[4];
        for (int i = 0; i < 4; i++) {
            lastRites$upper[i] = lastRites$limbs[i].getChild(LAST_RITES$UPPER);
            lastRites$lower[i] = lastRites$limbs[i].getChild(LAST_RITES$LOWER);
            lastRites$upperOverlay[i] = lastRites$upper[i].getChild(LAST_RITES$OVERLAY);
            lastRites$lowerOverlay[i] = lastRites$lower[i].getChild(LAST_RITES$OVERLAY);
        }
        lastRites$wholeLimbs();
    }

    /** Poses a corpse from its ragdoll. */
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("TAIL"))
    private void lastRites$ragdoll(AvatarRenderState state, CallbackInfo ci) {
        boolean ragdoll = ((ICorpseRenderState) state).lastRites$hasRagdoll();
        lastRites$jointed(ragdoll);
        if (!ragdoll) return;
        float[][] parts = ((ICorpseRenderState) state).lastRites$ragdollPose().parts;
        body.resetPose();
        lastRites$pose(head, parts[HEAD]);
        lastRites$pose(rightArm, parts[R_ARM]);
        lastRites$pose(leftArm, parts[L_ARM]);
        lastRites$pose(rightLeg, parts[R_THIGH]);
        lastRites$pose(leftLeg, parts[L_THIGH]);
        if (lastRites$lower == null) return;
        lastRites$pose(lastRites$lower[0], parts[R_FOREARM]);
        lastRites$pose(lastRites$lower[1], parts[L_FOREARM]);
        lastRites$pose(lastRites$lower[2], parts[R_SHIN]);
        lastRites$pose(lastRites$lower[3], parts[L_SHIN]);
    }

    @Override
    public void lastRites$wholeLimbs() {
        lastRites$jointed(false);
    }

    /** Draws the limbs as halves, or whole. The halves' overlays follow the limb's own (sleeves and pants settings). */
    @Unique
    private void lastRites$jointed(boolean jointed) {
        if (lastRites$limbs == null) return;
        for (int i = 0; i < 4; i++) {
            lastRites$limbs[i].skipDraw = jointed;
            lastRites$upper[i].visible = jointed;
            lastRites$lower[i].visible = jointed;
            boolean overlay = jointed && lastRites$overlays[i].visible;
            lastRites$upperOverlay[i].visible = overlay;
            lastRites$lowerOverlay[i].visible = overlay;
            if (jointed) lastRites$overlays[i].visible = false;
        }
    }

    @Unique
    private static void lastRites$pose(ModelPart part, float[] pose) {
        part.resetPose();
        part.x = pose[0];
        part.y = pose[1];
        part.z = pose[2];
        part.setRotation(pose[3], pose[4], pose[5]);
    }
}
