package com.evandev.reliable_requiem.mixin;

import com.evandev.reliable_requiem.api.IPlayerKeptItems;
import com.evandev.reliable_requiem.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}

@Mixin(DeathScreen.class)
public abstract class DeathScreenMixin extends Screen {

    protected DeathScreenMixin(Component title) {
        super(title);
    }

    //? if >=26.1 {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void reliableRequiem$onRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        Component deathCoordsText = reliableRequiem$getDeathCoordsText();
        if (deathCoordsText != null) {
            graphics.centeredText(this.font, deathCoordsText, this.width / 2, 115, 0xFFFFFFFF);
        }
    }
    //?} else {
    /*@Inject(method = "render", at = @At("TAIL"))
    private void reliableRequiem$onRender(GuiGraphics graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        Component deathCoordsText = reliableRequiem$getDeathCoordsText();
        if (deathCoordsText != null) {
            graphics.drawCenteredString(this.font, deathCoordsText, this.width / 2, 115, 0xFFFFFFFF);
        }
    }
    *///?}

    @Unique
    private static Component reliableRequiem$getDeathCoordsText() {
        if (!ModConfig.get().enabled || !ModConfig.get().displayDeathCoordinates) return null;

        Player player = Minecraft.getInstance().player;
        if (player == null) return null;

        BlockPos pos = ((IPlayerKeptItems) player).reliableRequiem$getLastDeathPos();
        if (pos == null) pos = player.blockPosition();

        return Component.translatable("text.reliable_requiem.death_coords", pos.getX(), pos.getY(), pos.getZ());
    }
}
