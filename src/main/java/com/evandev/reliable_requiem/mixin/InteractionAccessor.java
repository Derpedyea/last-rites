package com.evandev.reliable_requiem.mixin;

import net.minecraft.world.entity.Interaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Interaction.class)
public interface InteractionAccessor {
    @Invoker("setWidth")
    void lastRites$setWidth(float width);

    @Invoker("setHeight")
    void lastRites$setHeight(float height);

    @Invoker("setResponse")
    void lastRites$setResponse(boolean response);
}
