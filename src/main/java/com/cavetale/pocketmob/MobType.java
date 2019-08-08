package com.cavetale.pocketmob;

import lombok.NonNull;

enum MobType {
    // Main Categories
    MONSTER(true),
    ANIMAL(true),
    VILLAGER(true),
    ILLAGER(true),
    // Specialty
    AIR,
    NETHER,
    PET,
    SPIDER,
    UNDEAD,
    WATER;

    public final boolean master;
    public final String key;
    public final String displayName;

    MobType() {
        this(false);
    }

    MobType(boolean master) {
        this.master = master;
        key = name().toLowerCase();
        displayName = name().substring(0, 1)
            + name().substring(1).toLowerCase();
    }

    static MobType of(@NonNull String name) {
        for (MobType mobType : MobType.values()) {
            if (mobType.key.equals(name)) return mobType;
            if (mobType.displayName.equals(name)) return mobType;
        }
        return null;
    }
}
