package com.evandev.reliable_requiem.item;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

//? if >=26.1 {
import net.minecraft.world.InteractionResult;
//?} else {
/*import net.minecraft.world.InteractionResultHolder;
*///?}

public abstract class CapacityCrystalItem extends Item {

    protected CapacityCrystalItem(Properties properties) {
        super(properties);
    }

    @Nullable
    protected abstract AttributeInstance getAttribute(Player player);

    protected abstract double getIncrease();

    protected abstract double getLimit();

    protected abstract String getLimitReachedMessage();

    protected abstract ParticleOptions getParticle();

    protected abstract float getSoundPitch();

    protected void onIncreased(Player player, double newValue, double increase) {
    }

    @Override
    //? if >=26.1 {
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
    //?} else {
    /*public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    *///?}
        ItemStack itemStack = player.getItemInHand(hand);

        AttributeInstance attribute = getAttribute(player);
        if (attribute == null) {
            //? if >=26.1 {
            return InteractionResult.PASS;
            //?} else {
            /*return InteractionResultHolder.pass(itemStack);
            *///?}
        }

        double current = attribute.getBaseValue();
        double limit = getLimit();
        if (current >= limit) {
            if (!level.isClientSide()) {
                Component message = Component.translatable(getLimitReachedMessage());
                //? if >=26.1 {
                player.sendOverlayMessage(message);
                //?} else {
                /*player.displayClientMessage(message, true);
                *///?}
            }
            //? if >=26.1 {
            return InteractionResult.FAIL;
            //?} else {
            /*return InteractionResultHolder.fail(itemStack);
            *///?}
        }

        if (!level.isClientSide()) {
            double increase = getIncrease();
            double newValue = Math.min(limit, current + increase);
            attribute.setBaseValue(newValue);
            onIncreased(player, newValue, increase);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.8f, getSoundPitch());

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(getParticle(), player.getX(), player.getY() + 1.0, player.getZ(),
                        12, 0.4, 0.5, 0.4, 0.1);
            }

            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
        }

        //? if >=26.1 {
        return InteractionResult.SUCCESS;
        //?} else {
        /*return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
        *///?}
    }
}
