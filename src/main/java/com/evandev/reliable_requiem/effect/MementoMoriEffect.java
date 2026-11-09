package com.evandev.reliable_requiem.effect;

import com.evandev.reliable_requiem.config.ModConfig;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

//? if >=1.21 {
import com.evandev.reliable_requiem.Constants;
import net.minecraft.resources.Identifier;
//?}

public class MementoMoriEffect extends MobEffect {

    //? if >=1.21 {
    private static final Identifier MOVEMENT_MOD_ID = Constants.id("memento_mori_movement");
    private static final Identifier DAMAGE_MOD_ID = Constants.id("memento_mori_damage");
    //?} else {
    /*private static final String MOVEMENT_MOD_ID = "4a6f8f0e-1d7b-4c52-9a2e-3b1f6c2d8e01";
    private static final String DAMAGE_MOD_ID = "9c3e2b7a-5f41-4d08-8b6a-7e2d1c4f9a02";
    *///?}

    public MementoMoriEffect() {
        super(MobEffectCategory.HARMFUL, 0x4A4A4A);

        //? if >=1.21 {
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, MOVEMENT_MOD_ID, ModConfig.get().mementoSpeedModifier, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, DAMAGE_MOD_ID, ModConfig.get().mementoDamageModifier, AttributeModifier.Operation.ADD_VALUE);
        //?} else {
        /*this.addAttributeModifier(Attributes.MOVEMENT_SPEED, MOVEMENT_MOD_ID, ModConfig.get().mementoSpeedModifier, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, DAMAGE_MOD_ID, ModConfig.get().mementoDamageModifier, AttributeModifier.Operation.ADDITION);
        *///?}
    }
}
