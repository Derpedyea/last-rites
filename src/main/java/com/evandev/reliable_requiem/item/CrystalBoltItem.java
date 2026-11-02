package com.evandev.reliable_requiem.item;

import com.evandev.reliable_requiem.compat.ModestMealsCompat;
import com.evandev.reliable_requiem.config.ModConfig;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;

public class CrystalBoltItem extends CapacityCrystalItem {

    public CrystalBoltItem(Properties properties) {
        super(properties);
    }

    @Override
    protected AttributeInstance getAttribute(Player player) {
        return ModestMealsCompat.getStaminaCapacity(player);
    }

    @Override
    protected double getIncrease() {
        return ModConfig.get().crystalBoltStaminaAmount;
    }

    @Override
    protected double getLimit() {
        return ModConfig.get().maxStaminaCapacity;
    }

    @Override
    protected String getLimitReachedMessage() {
        return "message.reliable_requiem.max_stamina_limit_reached";
    }

    @Override
    protected ParticleOptions getParticle() {
        return ParticleTypes.ELECTRIC_SPARK;
    }

    @Override
    protected float getSoundPitch() {
        return 2.0f;
    }
}
