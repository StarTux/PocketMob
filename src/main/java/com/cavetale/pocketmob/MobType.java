package com.cavetale.pocketmob;

import lombok.NonNull;

enum MobType {
    /**
     * Monsters can be caught single file with a chance depending on
     * their health.
     */
    MONSTER,
    /**
     * Water mobs can be caught in bulk with a low chance.
     */
    WATER,
    /**
     * Villagers are caught single file, with a mediocre chance.
     */
    VILLAGER,
    /**
     * Animals can be caught in bulk with a high chance.
     */
    ANIMAL,
    /**
     * Owned pets can be caught in bulk, with a guaranteed capture.
     * Unowned pets are caught single file with a slim chance.
     */
    PET;

    public final String key;
    public final String displayName;

    MobType() {
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
