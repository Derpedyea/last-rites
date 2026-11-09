package com.evandev.reliable_requiem.compat;

import com.evandev.reliable_requiem.Constants;
import com.evandev.reliable_requiem.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class ModestMealsCompat {

    public static final String MOD_ID = "modest_meals";
    private static final Identifier STAMINA_CAPACITY = Constants.id(MOD_ID, "stamina_capacity");

    public static boolean isLoaded() {
        return Services.PLATFORM.isModLoaded(MOD_ID);
    }

    @Nullable
    public static AttributeInstance getStaminaCapacity(Player player) {
        //? if >=26.1 {
        return BuiltInRegistries.ATTRIBUTE.get(STAMINA_CAPACITY).map(player::getAttribute).orElse(null);
        //?} else if >=1.21 {
        /*return BuiltInRegistries.ATTRIBUTE.getHolder(STAMINA_CAPACITY).map(player::getAttribute).orElse(null);
        *///?} else {
        /*var attribute = BuiltInRegistries.ATTRIBUTE.get(STAMINA_CAPACITY);
        return attribute == null ? null : player.getAttribute(attribute);
        *///?}
    }
}
