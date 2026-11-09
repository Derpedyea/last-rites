package com.evandev.reliable_requiem.mixin;

import com.evandev.reliable_requiem.api.ICorpseRenderState;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.ARGB;
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
}
