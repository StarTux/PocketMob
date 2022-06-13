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

    public static final Map<EntityType, MobType> ENTITY_MOB_MAP = new EnumMap<>(EntityType.class);
    public static final Set<EntityType> NO_MOB_TYPE = EnumSet.noneOf(EntityType.class);
    public final double chance;
    public final Set<EntityType> entityTypes = EnumSet.noneOf(EntityType.class);

    MobType(final double chance) {
        this.chance = chance;
    }

    private static MobType mobTypeOf(EntityType entityType) {
        switch (entityType) {
        case AREA_EFFECT_CLOUD:
        case ARMOR_STAND:
        case ARROW:
        case BOAT:
        case CHEST_BOAT:
        case DRAGON_FIREBALL:
        case DROPPED_ITEM:
        case EGG:
        case ENDER_CRYSTAL:
        case ENDER_PEARL:
        case ENDER_SIGNAL:
        case EVOKER_FANGS:
        case EXPERIENCE_ORB:
        case FALLING_BLOCK:
        case FIREBALL:
        case FIREWORK:
        case FISHING_HOOK:
        case GLOW_ITEM_FRAME:
        case ITEM_FRAME:
        case LEASH_HITCH:
        case LIGHTNING:
        case LLAMA_SPIT:
        case MARKER:
        case MINECART:
        case MINECART_CHEST:
        case MINECART_COMMAND:
        case MINECART_FURNACE:
        case MINECART_HOPPER:
        case MINECART_MOB_SPAWNER:
        case MINECART_TNT:
        case PAINTING:
        case PLAYER:
        case PRIMED_TNT:
        case SHULKER_BULLET:
        case SMALL_FIREBALL:
        case SNOWBALL:
        case SPECTRAL_ARROW:
        case SPLASH_POTION:
        case THROWN_EXP_BOTTLE:
        case TRIDENT:
        case UNKNOWN:
        case WITHER_SKULL:
            return null;
        case COD:
        case DOLPHIN:
        case GLOW_SQUID:
        case PUFFERFISH:
        case SALMON:
        case SQUID:
        case TADPOLE:
        case TROPICAL_FISH:
            return MobType.FISH;
        case AXOLOTL:
        case BAT:
        case BEE:
        case CHICKEN:
        case COW:
        case FOX:
        case FROG:
        case GOAT:
        case MUSHROOM_COW:
        case OCELOT:
        case PANDA:
        case PIG:
        case POLAR_BEAR:
        case RABBIT:
        case SHEEP:
        case STRIDER:
        case TURTLE:
            return MobType.ANIMAL;
        case CAT:
        case DONKEY:
        case HORSE:
        case LLAMA:
        case MULE:
        case PARROT:
        case SKELETON_HORSE:
        case TRADER_LLAMA:
        case WOLF:
        case ZOMBIE_HORSE:
            return MobType.PET;
        case VILLAGER:
        case WANDERING_TRADER:
            return MobType.VILLAGER;
        case ALLAY:
        case BLAZE:
        case CAVE_SPIDER:
        case CREEPER:
        case DROWNED:
        case ELDER_GUARDIAN:
        case ENDERMAN:
        case ENDERMITE:
        case EVOKER:
        case GHAST:
        case GIANT:
        case GUARDIAN:
        case HOGLIN:
        case HUSK:
        case ILLUSIONER:
        case IRON_GOLEM:
        case MAGMA_CUBE:
        case PHANTOM:
        case PIGLIN:
        case PIGLIN_BRUTE:
        case PILLAGER:
        case RAVAGER:
        case SHULKER:
        case SILVERFISH:
        case SKELETON:
        case SLIME:
        case SNOWMAN:
        case SPIDER:
        case STRAY:
        case VEX:
        case VINDICATOR:
        case WARDEN:
        case WITCH:
        case WITHER_SKELETON:
        case ZOGLIN:
        case ZOMBIE:
        case ZOMBIE_VILLAGER:
        case ZOMBIFIED_PIGLIN:
            return MobType.MONSTER;
        case ENDER_DRAGON:
        case WITHER:
            return MobType.BOSS;
        default:
            MobType result = guessMobType(entityType);
            System.err.println("Guessed mob type: " + entityType + " => " + result);
            return result;
        }
    }

    private static MobType guessMobType(EntityType entityType) {
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
            } else {
                NO_MOB_TYPE.add(entityType);
            }
        }
    }
}
