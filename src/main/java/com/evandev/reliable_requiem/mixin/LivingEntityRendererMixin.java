package com.evandev.reliable_requiem.mixin;

import com.evandev.reliable_requiem.api.ICorpseRenderState;
import com.evandev.reliable_requiem.corpse.Corpses;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    /** Ashen: greyer and cooler, with the blood gone from the skin. Armor and held items keep their colors. */
    @Unique
    private static final int LAST_RITES$CORPSE_TINT = 0xFFA4B0A8;

    @Inject(method = "getModelTint", at = @At("RETURN"), cancellable = true)
    private void lastRites$ashenCorpse(LivingEntityRenderState state, CallbackInfoReturnable<Integer> cir) {
        if (state instanceof ICorpseRenderState corpse && corpse.lastRites$isCorpse()) {
            cir.setReturnValue(ARGB.multiply(cir.getReturnValue(), LAST_RITES$CORPSE_TINT));
        }
    }

    /** A corpse's shadow would sit under its feet, not under the body. */
    @Inject(method = "getShadowRadius(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;)F", at = @At("RETURN"), cancellable = true)
    private void lastRites$noCorpseShadow(LivingEntityRenderState state, CallbackInfoReturnable<Float> cir) {
        if (state instanceof ICorpseRenderState corpse && corpse.lastRites$isCorpse()) cir.setReturnValue(0.0F);
    }

    /** The body reaches well past a lying mannequin's tiny box, so don't cull it by that box. */
    @Inject(method = "getBoundingBoxForCulling(Lnet/minecraft/world/entity/LivingEntity;F)Lnet/minecraft/world/phys/AABB;", at = @At("RETURN"), cancellable = true)
    private void lastRites$corpseCullingBox(LivingEntity entity, float partialTicks, CallbackInfoReturnable<AABB> cir) {
        if (entity instanceof Mannequin && Corpses.isCorpseName(entity.getCustomName())) cir.setReturnValue(cir.getReturnValue().inflate(2.0));
    }
}
