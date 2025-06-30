package com.cavetale.pocketmob;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Golem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.WaterMob;

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

    public static MobType mobTypeOf(EntityType entityType) {
        switch (entityType) {

        case ACACIA_BOAT:
        case ACACIA_CHEST_BOAT:
        case BAMBOO_CHEST_RAFT:
        case BAMBOO_RAFT:
        case BIRCH_BOAT:
        case BIRCH_CHEST_BOAT:
        case CHERRY_BOAT:
        case CHERRY_CHEST_BOAT:
        case DARK_OAK_BOAT:
        case DARK_OAK_CHEST_BOAT:
        case JUNGLE_BOAT:
        case JUNGLE_CHEST_BOAT:
        case MANGROVE_BOAT:
        case MANGROVE_CHEST_BOAT:
        case OAK_BOAT:
        case OAK_CHEST_BOAT:
        case PALE_OAK_BOAT:
        case PALE_OAK_CHEST_BOAT:
        case SPRUCE_BOAT:
        case SPRUCE_CHEST_BOAT:

        case AREA_EFFECT_CLOUD:
        case ARMOR_STAND:
        case ARROW:
        case BLOCK_DISPLAY:
        case BREEZE_WIND_CHARGE:
        case CHEST_MINECART:
        case COMMAND_BLOCK_MINECART:
        case DRAGON_FIREBALL:
        case EGG:
        case ENDER_PEARL:
        case END_CRYSTAL:
        case EVOKER_FANGS:
        case EXPERIENCE_BOTTLE:
        case EXPERIENCE_ORB:
        case EYE_OF_ENDER:
        case FALLING_BLOCK:
        case FIREBALL:
        case FIREWORK_ROCKET:
        case FISHING_BOBBER:
        case FURNACE_MINECART:
        case GLOW_ITEM_FRAME:
        case HOPPER_MINECART:
        case INTERACTION:
        case ITEM:
        case ITEM_DISPLAY:
        case ITEM_FRAME:
        case LEASH_KNOT:
        case LIGHTNING_BOLT:
        case LINGERING_POTION:
        case LLAMA_SPIT:
        case MARKER:
        case MINECART:
        case OMINOUS_ITEM_SPAWNER:
        case PAINTING:
        case PLAYER:
        case SHULKER_BULLET:
        case SMALL_FIREBALL:
        case SNOWBALL:
        case SPAWNER_MINECART:
        case SPECTRAL_ARROW:
        case SPLASH_POTION:
        case TEXT_DISPLAY:
        case TNT:
        case TNT_MINECART:
        case TRIDENT:
        case UNKNOWN:
        case WIND_CHARGE:
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

        case ALLAY:
        case ARMADILLO:
        case AXOLOTL:
        case BAT:
        case BEE:
        case CHICKEN:
        case COW:
        case FOX:
        case FROG:
        case GOAT:
        case HAPPY_GHAST:
        case MOOSHROOM:
        case OCELOT:
        case PANDA:
        case PIG:
        case POLAR_BEAR:
        case RABBIT:
        case SHEEP:
        case SNIFFER:
        case STRIDER:
        case TURTLE:

            return MobType.ANIMAL;

        case CAMEL:
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

        case BLAZE:
        case BOGGED:
        case BREEZE:
        case CAVE_SPIDER:
        case CREAKING:
        case CREEPER:
        case DROWNED:
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
        case SNOW_GOLEM:
        case SPIDER:
        case STRAY:
        case VEX:
        case VINDICATOR:
        case WITCH:
        case WITHER_SKELETON:
        case ZOGLIN:
        case ZOMBIE:
        case ZOMBIE_VILLAGER:
        case ZOMBIFIED_PIGLIN:

            return MobType.MONSTER;

        case ELDER_GUARDIAN:
        case ENDER_DRAGON:
        case WARDEN:
        case WITHER:

            return MobType.BOSS;

        default:
            MobType result = guessMobType(entityType);
            System.err.println("Guessed mob type: " + entityType + " => " + result);
            return result;
        }
    }

    protected static MobType guessMobType(EntityType entityType) {
        Class<?> clazz = entityType.getEntityClass();
        if (clazz == null) {
            throw new IllegalArgumentException("No entity class: " + entityType);
        }
        if (!LivingEntity.class.isAssignableFrom(clazz)) {
            return null;
        }
        switch (entityType) {
        case ELDER_GUARDIAN:
        case WARDEN:
            return BOSS;
        default: break;
        }
        if (Boss.class.isAssignableFrom(clazz)) {
            return MobType.BOSS;
        } else if (Monster.class.isAssignableFrom(clazz)) {
            return MobType.MONSTER;
        } else if (Tameable.class.isAssignableFrom(clazz)) {
            return MobType.PET;
        } else if (Fish.class.isAssignableFrom(clazz)) {
            return MobType.FISH;
        } else if (WaterMob.class.isAssignableFrom(clazz)) {
            return MobType.FISH;
        } else if (Enemy.class.isAssignableFrom(clazz)) {
            return MobType.MONSTER;
        } else if (Golem.class.isAssignableFrom(clazz)) {
            return MobType.MONSTER;
        } else if (Animals.class.isAssignableFrom(clazz)) {
            return MobType.ANIMAL;
        } else if (NPC.class.isAssignableFrom(clazz)) {
            return MobType.VILLAGER;
        } else if (Ambient.class.isAssignableFrom(clazz)) {
            return MobType.ANIMAL;
        } else if (Mob.class.isAssignableFrom(clazz)) {
            return MobType.ANIMAL;
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
