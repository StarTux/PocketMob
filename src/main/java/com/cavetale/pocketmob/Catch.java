package com.cavetale.pocketmob;

import java.util.Collection;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;

@RequiredArgsConstructor
final class Catch {
    final PocketMobPlugin plugin;
    double baseChance;
    double accuracy;
    double bonusChance;
    double chanceMultiplier = 1.0;
    double finalChance;
    double roll;
    boolean success;
    boolean correctCatcher;

    void reset() {
        baseChance = 0.0;
        accuracy = 0.0;
        bonusChance = 0.0;
        chanceMultiplier = 1.0;
        finalChance = 0.0;
        roll = 0.0;
        success = false;
        correctCatcher = false;
    }

    private double baseChance(Collection<MobType> mobTypes) {
        for (MobType mobType : mobTypes) {
            if (!mobType.master) continue;
            switch (mobType) {
            case MONSTER: return 0.2;
            case ANIMAL: return 0.35;
            case VILLAGER: return 0.2;
            case ILLAGER: return 0.1;
            }
        }
        return 0.0;
    }

    private void modifyChance(@NonNull Player player,
                              @NonNull LivingEntity entity,
                              @NonNull PocketMob pocketMob,
                              @NonNull MobCatcher mobCatcher) {
        switch (mobCatcher) {
        case MONSTER: {
            final double maxHealth = entity
                .getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            if (maxHealth <= 1.0) {
                // slimes
                bonusChance = 1.0;
                return;
            }
            final double damage = maxHealth - entity.getHealth();
            bonusChance = damage / maxHealth;
            return;
        }
        case ANIMAL: {
            final double maxHealth = entity
                .getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            bonusChance = entity.getHealth() / maxHealth;
            return;
        }
        case VILLAGER: {
            if (!(entity instanceof Villager)) return;
            final Villager villager = (Villager) entity;
            final int level = villager.getVillagerLevel();
            bonusChance = ((double) level / 5.0) * 0.1;
            chanceMultiplier += 1.0;
            return;
        }
        case WATER: {
            bonusChance = 0.0;
            chanceMultiplier += 1.0;
            return;
        }
        case PET:
            if (!(entity instanceof Tameable)) return;
            Tameable tamed = (Tameable) entity;
            bonusChance = player.equals(tamed.getOwner()) ? 1.0 : 0.0;
            return;
        }
    }

    /**
     * Called by EventListener.
     */
    void hit(@NonNull Player player,
             @NonNull LivingEntity entity,
             @NonNull PocketMob pocketMob,
             @NonNull MobCatcher mobCatcher,
             final double intensity) {
        reset();
        baseChance = baseChance(pocketMob.mobTypes);
        accuracy = intensity;
        correctCatcher = pocketMob.mobTypes.contains(mobCatcher.mobType);
        if (correctCatcher) {
            modifyChance(player, entity, pocketMob, mobCatcher);
        }
        finalChance = Math.min(1.0,
                               baseChance * accuracy * chanceMultiplier
                               + bonusChance);
        roll = plugin.random.nextDouble();
        success = roll < finalChance;
    }
}
