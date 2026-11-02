package com.evandev.reliable_requiem.item;

import com.evandev.reliable_requiem.config.ModConfig;
import com.evandev.reliable_requiem.platform.Services;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class CrystalHeartItem extends CapacityCrystalItem {

    public CrystalHeartItem(Properties properties) {
        super(properties);
    }

    @Override
    protected AttributeInstance getAttribute(Player player) {
        return player.getAttribute(Attributes.MAX_HEALTH);
    }

    @Override
    protected double getIncrease() {
        return ModConfig.get().crystalHeartHealthAmount;
    }

    @Override
    protected double getLimit() {
        return ModConfig.get().maxMaxHealth;
    }

    @Override
    protected String getLimitReachedMessage() {
        return "message.reliable_requiem.max_health_limit_reached";
    }

    @Override
    protected ParticleOptions getParticle() {
        return ParticleTypes.HEART;
    }

    @Override
    protected float getSoundPitch() {
        return 2.6f;
    }

    @Override
    protected void onIncreased(Player player, double newValue, double increase) {
        Services.PLATFORM.updateHeartCrystalsHealth(player, newValue);
        player.heal((float) increase);
    }
}
