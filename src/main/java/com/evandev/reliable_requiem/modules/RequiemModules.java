package com.evandev.reliable_requiem.modules;

import com.evandev.reliable_requiem.corpse.Corpses;

import com.evandev.reliable_requiem.Constants;
import com.evandev.reliable_requiem.api.IPlayerKeptItems;
import com.evandev.reliable_requiem.compat.ModestMealsCompat;
import com.evandev.reliable_requiem.config.ModConfig;
import com.evandev.reliable_requiem.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Random;

//? if >=26.3 {
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.item.ItemEntity;
//?}
//? if >=26.1 {
import net.minecraft.world.level.gamerules.GameRules;
//?} else {
/*import net.minecraft.world.level.GameRules;
*///?}
//? if <1.21 {
/*import net.minecraft.world.item.enchantment.EnchantmentHelper;
*///?}

public class RequiemModules {

    public static final TagKey<Enchantment> SOULBOUND_TAG = TagKey.create(Registries.ENCHANTMENT, Constants.id("c", "soulbound"));
    public static final TagKey<Item> RETAINED_ON_DEATH_TAG = TagKey.create(Registries.ITEM, Constants.id("retained_on_death"));
    private static final Random RAND = new Random();

    public static void onPlayerClone(ServerPlayer original, ServerPlayer newPlayer, boolean wasDeath) {
        if (!wasDeath || !ModConfig.get().enabled) return;
        ModConfig config = ModConfig.get();

        // Hunger Module
        FoodData oldFood = original.getFoodData();
        FoodData newFood = newPlayer.getFoodData();
        int foodLevel = config.keepFood ? oldFood.getFoodLevel() : 20;
        newFood.setFoodLevel(Math.max(config.minFood, Math.min(config.maxFood, foodLevel)));
        if (config.keepSaturation) newFood.setSaturation(oldFood.getSaturationLevel());

        // Effects Module
        if (config.keepPositiveEffects || config.keepNegativeEffects) {
            for (MobEffectInstance effectInstance : original.getActiveEffects()) {
                //? if >=1.21 {
                boolean isHarmful = effectInstance.getEffect().value().getCategory() == MobEffectCategory.HARMFUL;
                //?} else {
                /*boolean isHarmful = effectInstance.getEffect().getCategory() == MobEffectCategory.HARMFUL;
                *///?}
                if ((isHarmful && config.keepNegativeEffects) || (!isHarmful && config.keepPositiveEffects)) {
                    if (ModEffects.isMementoMori(effectInstance)) continue;
                    newPlayer.addEffect(new MobEffectInstance(effectInstance));
                }
            }
        }

        // Experience Module
        if (config.lostXpPercent < 1.0) {
            int totalOriginalPoints = getExperiencePoints(original);
            int keptPoints = totalOriginalPoints - (int) (totalOriginalPoints * config.lostXpPercent);
            newPlayer.setExperienceLevels(0);
            newPlayer.setExperiencePoints(0);
            newPlayer.giveExperiencePoints(keptPoints);
        }

        // Memento Mori
        if (config.applyMementoMori) {
            newPlayer.addEffect(ModEffects.mementoMori(config.mementoDuration));
        }

        // Health Module
        AttributeInstance oldMaxHealthAttr = original.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance newMaxHealthAttr = newPlayer.getAttribute(Attributes.MAX_HEALTH);
        if (oldMaxHealthAttr != null && newMaxHealthAttr != null) {
            double currentMax = oldMaxHealthAttr.getBaseValue();
            if (config.enableHealthReductionOnDeath) {
                currentMax = Math.max(config.minMaxHealth, Math.min(config.maxMaxHealth, currentMax - config.healthLossPerDeath));
            }
            newMaxHealthAttr.setBaseValue(currentMax);
            Services.PLATFORM.updateHeartCrystalsHealth(newPlayer, currentMax);
        }

        // Stamina Module (Modest Meals)
        if (ModestMealsCompat.isLoaded()) {
            AttributeInstance oldStaminaAttr = ModestMealsCompat.getStaminaCapacity(original);
            AttributeInstance newStaminaAttr = ModestMealsCompat.getStaminaCapacity(newPlayer);
            if (oldStaminaAttr != null && newStaminaAttr != null) {
                double capacity = oldStaminaAttr.getBaseValue();
                if (config.enableStaminaReductionOnDeath) {
                    capacity = Math.max(config.minStaminaCapacity, Math.min(config.maxStaminaCapacity, capacity - config.staminaLossPerDeath));
                }
                newStaminaAttr.setBaseValue(capacity);
            }
        }

        double maxHealth = newPlayer.getMaxHealth();
        double targetHealth = config.scaleRespawnHealth ? (maxHealth * config.respawnHealthPercent) : maxHealth;
        targetHealth = Math.min(targetHealth, config.maxRespawnHealth);
        newPlayer.setHealth((float) Math.max(1.0, targetHealth));

        // Randomized Spawn Module
        if (config.respawnRandomRadius > 0 && newPlayer.level() instanceof ServerLevel serverLevel) {
            BlockPos currentPos = newPlayer.blockPosition();
            double offsetX = (RAND.nextDouble() * 2 - 1) * config.respawnRandomRadius;
            double offsetZ = (RAND.nextDouble() * 2 - 1) * config.respawnRandomRadius;

            int newX = currentPos.getX() + (int) offsetX;
            int newZ = currentPos.getZ() + (int) offsetZ;

            int newY = serverLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, newX, newZ);
            newPlayer.teleportTo(newX + 0.5, newY + 0.1, newZ + 0.5);
        }
    }

    public static boolean onSetSpawn() {
        return ModConfig.get().enabled && ModConfig.get().restrictRespawning;
    }

    public static void onPlayerRespawn(ServerPlayer player) {
        for (MobEffectInstance effectInstance : player.getActiveEffects()) {
            //? if >=1.21 {
            player.connection.send(new ClientboundUpdateMobEffectPacket(player.getId(), effectInstance, true));
            //?} else {
            /*player.connection.send(new ClientboundUpdateMobEffectPacket(player.getId(), effectInstance));
            *///?}
        }

        ModConfig config = ModConfig.get();
        if (config.enabled && config.scaleRespawnHealth) {
            AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealthAttr != null) {
                double maxHealth = maxHealthAttr.getValue();
                double targetHealth = Math.min(maxHealth * config.respawnHealthPercent, config.maxRespawnHealth);
                player.setHealth((float) Math.max(1.0, targetHealth));
            }
        }
    }

    public static boolean processItemOnDeath(ItemStack stack, ServerPlayer player, int slotIndex) {
        if (!ModConfig.get().enabled || stack.isEmpty()) return true;
        ModConfig config = ModConfig.get();

        String currentDimension = player.level().dimension().identifier().toString();
        if (config.bypassKeepInventoryDimensions.contains(currentDimension)) {
            return true;
        }

        String lastDamageSource = ((IPlayerKeptItems) player).reliableRequiem$getLastDamageSource();
        if (lastDamageSource != null && config.bypassKeepInventoryDamageSources.contains(lastDamageSource)) {
            return true;
        }

        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        if (config.destroyedItemOverrides.contains(itemId)) {
            stack.shrink(stack.getCount());
            return false;
        }
        if (config.droppedItemOverrides.contains(itemId)) {
            return true;
        }
        boolean forceKeep = config.keptItemOverrides.contains(itemId);

        boolean hasSoulbound = hasSoulbound(stack);

        double keepChance = getKeepChance(slotIndex, config);

        boolean hasRetainedTag = stack.is(RETAINED_ON_DEATH_TAG);

        if (forceKeep || hasSoulbound || hasRetainedTag || (keepChance > 0 && RAND.nextDouble() < keepChance)) {
            if (stack.isDamageableItem() && config.keepDurabilityLoss > 0) {
                int maxDamage = stack.getMaxDamage();
                int minRemaining = (int) Math.ceil(maxDamage * config.minKeptDurability);
                if (!config.allowDurabilityBreak) {
                    minRemaining = Math.max(1, minRemaining);
                }
                int remaining = maxDamage - stack.getDamageValue();
                int damageAmount = Math.min((int) (maxDamage * config.keepDurabilityLoss), Math.max(0, remaining - minRemaining));

                if (damageAmount > 0) {
                    //? if >=26.1 {
                    stack.hurtAndBreak(damageAmount, player.level(), player, (brokenItem) -> {
                    });
                    //?} else if >=1.21 {
                    /*stack.hurtAndBreak(damageAmount, player.serverLevel(), player, (brokenItem) -> {
                    });
                    *///?} else {
                    /*stack.hurtAndBreak(damageAmount, player, (brokenPlayer) -> {
                    });
                    *///?}
                }
            }
            return false;
        }

        if (config.defaultDestroyChance > 0 && RAND.nextDouble() < config.defaultDestroyChance) {
            stack.shrink(stack.getCount());
            return false;
        }

        return true;
    }

    private static boolean hasSoulbound(ItemStack stack) {
        //? if >=1.21 {
        for (var holder : stack.getEnchantments().keySet()) {
            if (holder.is(SOULBOUND_TAG)) {
                return true;
            }
        }
        //?} else {
        /*for (Enchantment enchantment : EnchantmentHelper.getEnchantments(stack).keySet()) {
            if (BuiltInRegistries.ENCHANTMENT.wrapAsHolder(enchantment).is(SOULBOUND_TAG)) {
                return true;
            }
        }
        *///?}
        return false;
    }

    private static double getKeepChance(int slotIndex, ModConfig config) {
        double keepChance = 0.0;
        if (slotIndex >= 0 && slotIndex <= 8) {
            keepChance = config.keepHotbarChance;
        } else if (slotIndex >= 9 && slotIndex <= 35) {
            keepChance = config.keepMainInventoryChance;
        } else if (slotIndex >= 36 && slotIndex <= 39) {
            keepChance = config.keepArmorChance;
        } else if (slotIndex == 40) {
            keepChance = config.keepOffhandChance;
        } else if (slotIndex == -1) {
            keepChance = config.keepAccessoriesChance;
        }
        return keepChance;
    }

    public static boolean isKeepInventory(ServerPlayer player) {
        //? if >=26.1 {
        return player.level().getGameRules().get(GameRules.KEEP_INVENTORY);
        //?} else {
        /*return player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
        *///?}
    }

    public static void dropDeathItem(Player player, ItemStack stack) {
        dropDeathItem(player, stack, null);
    }

    /**
     * Drops an item on death, or puts it in the player's corpse when corpses are on. {@code home} is the slot it came
     * from ({@code inv:<index>} or an accessory slot key), used to put it back there later.
     */
    public static void dropDeathItem(Player player, ItemStack stack, String home) {
        if (Corpses.collect(player, home, stack)) return;
        //? if >=26.3 {
        ItemEntity itemEntity = player.createItemStackToDrop(stack, true, false);
        if (itemEntity != null) {
            player.level().addFreshEntity(itemEntity);
        }
        //?} else {
        /*player.drop(stack, true, false);
        *///?}
    }

    public static void returnItem(Player player, ItemStack stack) {
        //? if >=26.3 {
        player.getInventory().placeItemBackInInventory(stack, Prediction.SERVER_ONLY);
        //?} else {
        /*player.getInventory().placeItemBackInInventory(stack);
        *///?}
    }

    public static int calculateDroppedExperience(Player player, int originalDrop) {
        if (!ModConfig.get().enabled) return originalDrop;
        ModConfig config = ModConfig.get();
        int dropped = player.experienceLevel * config.droppedXpPerLevel;
        return Math.min(dropped, config.maxDroppedXp);
    }

    private static int getExperiencePoints(Player player) {
        return Math.round(player.experienceProgress * player.getXpNeededForNextLevel())
                + getExperiencePointsFromLevel(player.experienceLevel);
    }

    private static int getExperiencePointsFromLevel(int levels) {
        if (levels <= 16) return levels * levels + 6 * levels;
        if (levels <= 31) return (int) (levels * levels * 2.5 - levels * 40.5 + 360);
        return (int) (levels * levels * 4.5 - levels * 162.5 + 2220);
    }
}
