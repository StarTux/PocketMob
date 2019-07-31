package com.cavetale.pocketmob;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public enum PocketMob {
    // Animal
    BAT(MobType.ANIMAL),
    CHICKEN(MobType.ANIMAL),
    COW(MobType.ANIMAL),
    FOX(MobType.ANIMAL),
    OCELOT(MobType.ANIMAL),
    PANDA(MobType.ANIMAL),
    PIG(MobType.ANIMAL),
    POLAR_BEAR(MobType.ANIMAL),
    RABBIT(MobType.ANIMAL),
    SHEEP(MobType.ANIMAL),
    TURTLE(MobType.ANIMAL),
    // Monster
    BLAZE(MobType.MONSTER),
    CAVE_SPIDER(MobType.MONSTER),
    CREEPER(MobType.MONSTER),
    DROWNED(MobType.MONSTER),
    ELDER_GUARDIAN(MobType.MONSTER),
    ENDERMAN(MobType.MONSTER),
    ENDERMITE(MobType.MONSTER),
    EVOKER(MobType.MONSTER),
    GHAST(MobType.MONSTER),
    GUARDIAN(MobType.MONSTER),
    HUSK(MobType.MONSTER),
    MAGMA_CUBE(MobType.MONSTER),
    PHANTOM(MobType.MONSTER),
    PILLAGER(MobType.MONSTER),
    RAVAGER(MobType.MONSTER),
    SHULKER(MobType.MONSTER),
    SILVERFISH(MobType.MONSTER),
    SKELETON(MobType.MONSTER),
    SLIME(MobType.MONSTER),
    SPIDER(MobType.MONSTER),
    STRAY(MobType.MONSTER),
    VEX(MobType.MONSTER),
    VINDICATOR(MobType.MONSTER),
    WITCH(MobType.MONSTER),
    WITHER_SKELETON(MobType.MONSTER),
    ZOMBIE(MobType.MONSTER),
    ZOMBIE_VILLAGER(MobType.MONSTER),
    // Pet
    CAT(MobType.PET),
    DONKEY(MobType.PET),
    HORSE(MobType.PET),
    LLAMA(MobType.PET),
    MULE(MobType.PET),
    PARROT(MobType.PET),
    SKELETON_HORSE(MobType.PET),
    TRADER_LLAMA(MobType.PET),
    WOLF(MobType.PET),
    ZOMBIE_HORSE(MobType.PET),
    // Villager
    VILLAGER(MobType.VILLAGER),
    WANDERING_TRADER(MobType.VILLAGER),
    // Water
    COD(MobType.WATER),
    DOLPHIN(MobType.WATER),
    PUFFERFISH(MobType.WATER),
    SALMON(MobType.WATER),
    SQUID(MobType.WATER),
    TROPICAL_FISH(MobType.WATER);

    public final String key;
    public final EntityType entityType;
    public final Material spawnEggType;
    public final MobType mobType;

    PocketMob(@NonNull final MobType mobType) {
        this.key = name().toLowerCase();
        this.entityType = EntityType.valueOf(name());
        this.spawnEggType = Material.valueOf(name() + "_SPAWN_EGG");
        this.mobType = mobType;
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
