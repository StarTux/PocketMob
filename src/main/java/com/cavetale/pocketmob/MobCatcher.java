package com.cavetale.pocketmob;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

enum MobCatcher {
    MONSTER(MobType.MONSTER),
    ANIMAL(MobType.ANIMAL),
    VILLAGER(MobType.VILLAGER),
    WATER(MobType.WATER),
    PET(MobType.PET);

    public final String key;
    public final String configKey;
    public final String customId; // For ItemMarker
    public final String displayName;
    public final MobType mobType;

    MobCatcher(@NonNull final MobType mobType) {
        key = name().toLowerCase();
        configKey = key + "_catcher";
        customId = PocketMobPlugin.ITEM_PREFIX + key + "_catcher";
        displayName = name().substring(0, 1)
            + name().substring(1).toLowerCase();
        this.mobType = mobType;
    }

    static MobCatcher of(@NonNull String name) {
        for (MobCatcher mobCatcher : MobCatcher.values()) {
            if (name.equals(mobCatcher.key)) return mobCatcher;
            if (name.equals(mobCatcher.customId)) return mobCatcher;
            if (name.equals(mobCatcher.displayName)) return mobCatcher;
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
