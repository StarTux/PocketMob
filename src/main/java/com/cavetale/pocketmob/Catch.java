package com.cavetale.pocketmob;

import java.util.Collection;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
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
    double finalChance;
    double roll;
    boolean success;
    boolean correctCatcher;
    String bonusName = "";
    String entityName = "";

    private double baseChance(Collection<MobType> mobTypes) {
        for (MobType mobType : mobTypes) {
            if (!mobType.master) continue;
            switch (mobType) {
            case MONSTER: return 0.1;
            case ANIMAL: return 0.25;
            case VILLAGER: return 0.15;
            case ILLAGER: return 0.05;
            default: continue;
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
            if (accuracy < 1.0) {
                bonusName = "MISS";
                bonusChance = 0.0;
                return;
            }
            bonusName = "DMG";
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
            bonusName = "LIFE";
            final double maxHealth = entity
                .getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            bonusChance = entity.getHealth() / maxHealth;
            return;
        }
        case VILLAGER: {
            bonusName = "LVL";
            if (!(entity instanceof Villager)) return;
            final Villager villager = (Villager) entity;
            final int level = villager.getVillagerLevel();
            bonusChance = ((double) level / 5.0) * 0.1;
            return;
        }
        case WATER: {
            if (accuracy < 1.0) {
                bonusName = "MISS";
                bonusChance = 0.0;
                return;
            }
            bonusName = "WATER";
            bonusChance = 0.5;
            return;
        }
        case PET:
            if (!(entity instanceof Tameable)) return;
            Tameable tamed = (Tameable) entity;
            if (!tamed.isTamed()) {
                bonusName = "UNTAMED";
                bonusChance = 0.5;
            } else {
                bonusName = "TAMED";
                if (player.equals(tamed.getOwner())) {
                    bonusChance = 1.0;
                } else {
                    baseChance = 0.0;
                    bonusChance = 0.0;
                }
            }
            return;
        default: return;
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
        entityName = PocketMobPlugin.niceEnum(entity.getType().name());
        baseChance = baseChance(pocketMob.mobTypes);
        accuracy = intensity;
        correctCatcher = pocketMob.mobTypes.contains(mobCatcher.mobType);
        if (correctCatcher) {
            modifyChance(player, entity, pocketMob, mobCatcher);
        }
        finalChance = Math.max(0.0,
                               Math.min(1.0,
                                        baseChance * accuracy
                                        + bonusChance));
        roll = plugin.random.nextDouble();
        success = roll < finalChance;
    }

    private static String fp(double p) {
        if (p >= 1.0) return ChatColor.BOLD + "100";
        return "" + ((int) (p * 100.0));
    }

    String message() {
        final ChatColor c = success ? ChatColor.GREEN : ChatColor.RED;
        final ChatColor d = success ? ChatColor.YELLOW : ChatColor.DARK_RED;
        final String res = success ? "HIT" : "FAIL";
        final ChatColor x = ChatColor.GRAY;
        if (!correctCatcher) {
            return d + entityName
                + " " + c + fp(finalChance) + x + "%"
                + d + " (Wrong Catcher)"
                + x + " => " + d + res;
        }
        return d + entityName + " "
            + c + fp(baseChance * accuracy) + x + "%"
            // + x + " x "
            // + c + fp(accuracy) + x + "%"
            // + d + "ACC"
            + x + " + "
            + c + fp(bonusChance) + x + "%"
            + d + bonusName
            + x + " => "
            + c + fp(finalChance) + x + "%"
            + d + res;
    }
}
