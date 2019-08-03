package com.cavetale.pocketmob;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

enum MobType {
    // Main Categories
    MONSTER,
    ANIMAL,
    VILLAGER,
    WATER,
    // Specialty
    UNDEAD,
    SPIDER,
    AIR,
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

    PotionData getPotionData() {
        switch (this) {
        case MONSTER:  return new PotionData(PotionType.INSTANT_DAMAGE);
        case WATER:    return new PotionData(PotionType.WATER_BREATHING);
        case VILLAGER: return new PotionData(PotionType.WEAKNESS);
        case ANIMAL:   return new PotionData(PotionType.INSTANT_HEAL);
        case PET:      return new PotionData(PotionType.SLOWNESS);
        default: throw new IllegalArgumentException("" + this);
        }
    }

    ItemStack getPotionItem() {
        ItemStack item = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        meta.setBasePotionData(getPotionData());
        item.setItemMeta(meta);
        return item;
    }

    ItemStack getIngredientItem() {
        switch (this) {
        case MONSTER:  return new ItemStack(Material.ROTTEN_FLESH);
        case WATER:    return new ItemStack(Material.SLIME_BALL);
        case VILLAGER: return new ItemStack(Material.EMERALD);
        case ANIMAL:   return new ItemStack(Material.WHEAT);
        case PET:      return new ItemStack(Material.BONE);
        default: throw new IllegalArgumentException("" + this);
        }
    }
}
