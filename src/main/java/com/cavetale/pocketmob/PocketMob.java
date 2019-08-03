package com.cavetale.pocketmob;

import java.util.EnumSet;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public enum PocketMob {
    // Animals (Non-Fish)
    BAT(MobType.ANIMAL),
    CHICKEN(MobType.ANIMAL),
    COW(MobType.ANIMAL),
    FOX(MobType.ANIMAL),
    MOOSHROOM(EntityType.MUSHROOM_COW, MobType.ANIMAL),
    PANDA(MobType.ANIMAL),
    PIG(MobType.ANIMAL),
    POLAR_BEAR(MobType.ANIMAL),
    RABBIT(MobType.ANIMAL),
    SHEEP(MobType.ANIMAL),
    // Undead Monster
    DROWNED(MobType.MONSTER, MobType.UNDEAD),
    HUSK(MobType.MONSTER, MobType.UNDEAD),
    PHANTOM(MobType.MONSTER, MobType.UNDEAD, MobType.AIR),
    SKELETON(MobType.MONSTER, MobType.UNDEAD),
    STRAY(MobType.MONSTER, MobType.UNDEAD),
    WITHER_SKELETON(MobType.MONSTER, MobType.UNDEAD, MobType.NETHER),
    ZOMBIE(MobType.MONSTER, MobType.UNDEAD),
    ZOMBIE_PIGMAN(EntityType.PIG_ZOMBIE, MobType.MONSTER, MobType.UNDEAD,
                  MobType.NETHER),
    ZOMBIE_VILLAGER(MobType.MONSTER, MobType.UNDEAD, MobType.VILLAGER),
    // Spider Monster
    CAVE_SPIDER(MobType.MONSTER, MobType.SPIDER),
    ENDERMITE(MobType.MONSTER, MobType.SPIDER),
    SILVERFISH(MobType.MONSTER, MobType.SPIDER),
    SPIDER(MobType.MONSTER, MobType.SPIDER),
    // Water Monster
    ELDER_GUARDIAN(MobType.MONSTER, MobType.WATER),
    GUARDIAN(MobType.MONSTER, MobType.WATER),
    // Monster
    BLAZE(MobType.MONSTER, MobType.NETHER),
    CREEPER(MobType.MONSTER),
    ENDERMAN(MobType.MONSTER),
    GHAST(MobType.MONSTER),
    MAGMA_CUBE(MobType.MONSTER, MobType.NETHER),
    SHULKER(MobType.MONSTER),
    SLIME(MobType.MONSTER),
    // Illager
    EVOKER(MobType.ILLAGER),
    PILLAGER(MobType.ILLAGER),
    RAVAGER(MobType.ILLAGER),
    VEX(MobType.ILLAGER, MobType.AIR),
    VINDICATOR(MobType.ILLAGER),
    WITCH(MobType.ILLAGER),
    // Undead Pet
    ZOMBIE_HORSE(MobType.PET, MobType.UNDEAD),
    SKELETON_HORSE(MobType.PET, MobType.UNDEAD),
    // Animal Pet
    CAT(MobType.PET, MobType.ANIMAL),
    DONKEY(MobType.PET, MobType.ANIMAL),
    HORSE(MobType.PET, MobType.ANIMAL),
    LLAMA(MobType.PET, MobType.ANIMAL),
    MULE(MobType.PET, MobType.ANIMAL),
    OCELOT(MobType.PET, MobType.ANIMAL),
    PARROT(MobType.PET, MobType.ANIMAL, MobType.AIR),
    TRADER_LLAMA(MobType.PET, MobType.ANIMAL),
    WOLF(MobType.PET, MobType.ANIMAL),
    // Villager
    VILLAGER(MobType.VILLAGER),
    WANDERING_TRADER(MobType.VILLAGER),
    // Water
    COD(MobType.WATER),
    DOLPHIN(MobType.WATER),
    PUFFERFISH(MobType.WATER),
    SALMON(MobType.WATER),
    SQUID(MobType.WATER),
    TROPICAL_FISH(MobType.WATER),
    TURTLE(MobType.WATER);

    public final String key;
    public final EntityType entityType;
    public final Material spawnEggType;
    public final EnumSet<MobType> mobTypes;

    PocketMob(@NonNull final EntityType entityType,
              @NonNull final MobType mobType,
              final MobType... mobTypes) {
        this.key = name().toLowerCase();
        this.entityType = entityType;
        this.spawnEggType = Material.valueOf(name() + "_SPAWN_EGG");
        this.mobTypes = EnumSet.of(mobType, mobTypes);
    }

    PocketMob(@NonNull final MobType mobType,
              final MobType... mobTypes) {
        this.key = name().toLowerCase();
        this.entityType = EntityType.valueOf(name());
        this.spawnEggType = Material.valueOf(name() + "_SPAWN_EGG");
        this.mobTypes = EnumSet.of(mobType, mobTypes);
    }

    public static PocketMob of(@NonNull EntityType entityType) {
        for (PocketMob mob : PocketMob.values()) {
            if (mob.entityType == entityType) {
                return mob;
            }
        }
        return null;
    }
}
