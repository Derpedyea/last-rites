package com.evandev.reliable_requiem.mixin;

import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.item.component.ResolvableProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Mannequin.class)
public interface MannequinAccessor {
    @Invoker("setProfile")
    void lastRites$setProfile(ResolvableProfile profile);

    @Invoker("setImmovable")
    void lastRites$setImmovable(boolean immovable);

    @Invoker("setHideDescription")
    void lastRites$setHideDescription(boolean hide);
}
