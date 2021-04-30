package com.cavetale.pocketmob;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Boss;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fish;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Tameable;

public enum MobType {
    FISH(.6),
    ANIMAL(.4),
    PET(.4),
    VILLAGER(.4),
    MONSTER(.25),
    BOSS(.01);

    public final double chance;
    public final Set<EntityType> entityTypes = EnumSet.noneOf(EntityType.class);
    public static final Map<EntityType, MobType> ENTITY_MOB_MAP = new EnumMap<>(EntityType.class);

    MobType(final double chance) {
        this.chance = chance;
    }

    private static MobType mobTypeOf(EntityType entityType) {
        switch (entityType) {
        case ARMOR_STAND:
        case PLAYER:
        case UNKNOWN:
            return null;
        case SQUID:
        case DOLPHIN:
            return MobType.FISH;
        case HOGLIN:
            return MobType.MONSTER;
        default: break;
        }
        Class<?> clazz = entityType.getEntityClass();
        if (clazz == null) {
            throw new IllegalArgumentException("No entity class: " + entityType);
        }
        if (!LivingEntity.class.isAssignableFrom(clazz)) {
            return null;
        }
        if (Boss.class.isAssignableFrom(clazz)) {
            return MobType.BOSS;
        } else if (Monster.class.isAssignableFrom(clazz)) {
            return MobType.MONSTER;
        } else if (Tameable.class.isAssignableFrom(clazz)) {
            return MobType.PET;
        } else if (Fish.class.isAssignableFrom(clazz)) {
            return MobType.FISH;
        } else if (Animals.class.isAssignableFrom(clazz)) {
            return MobType.ANIMAL;
        } else if (NPC.class.isAssignableFrom(clazz)) {
            return MobType.VILLAGER;
        } else if (Ambient.class.isAssignableFrom(clazz)) {
            return MobType.ANIMAL;
        } else if (Mob.class.isAssignableFrom(clazz)) {
            return MobType.MONSTER;
        } else {
            throw new IllegalStateException("No type: " + entityType);
        }
    }

    static {
        for (EntityType entityType : EntityType.values()) {
            MobType mobType = mobTypeOf(entityType);
            if (mobType != null) {
                mobType.entityTypes.add(entityType);
                ENTITY_MOB_MAP.put(entityType, mobType);
            }
        }
    }
}
