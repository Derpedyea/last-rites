package com.evandev.reliable_requiem.mixin;

import net.minecraft.client.model.geom.builders.CubeDefinition;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(CubeListBuilder.class)
public interface CubeListBuilderAccessor {
    @Accessor("cubes")
    List<CubeDefinition> lastRites$cubes();
}
