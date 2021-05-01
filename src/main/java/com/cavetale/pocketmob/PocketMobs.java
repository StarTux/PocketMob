package com.cavetale.pocketmob;

import com.cavetale.dirty.Dirty;
import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.item.pocketmob.PocketMob;
import com.cavetale.mytems.item.pocketmob.PocketMobTag;
import com.cavetale.mytems.util.Json;
import java.util.Arrays;
import java.util.Map;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;

/**
 * PocketMob utility class.
 */
public final class PocketMobs {
    protected static final String[] ILLEGAL_MOB_TAGS = {
        "APX", "APY", "APZ", "AttackTick", "Brain", "Brain",
        "Bukkit.updateLevel", "Dimension", "FallDistance",
        "HasRaidGoal", "HurtByTimestamp", "Leash", "Leashed",
        "Motion", "OnGround", "Paper.Origin", "Paper.Origin",
        "Paper.SpawnReason", "Passengers", "PatrolLeader",
        "PatrolTarget", "Patrolling", "PortalCooldown", "Pos",
        "RaidId", "RoarTick", "Rotation", "SleepingX", "SleepingY",
        "SleepingZ", "Spigot.ticksLived", "StunTick", "UUID",
        "UUIDLeast", "UUIDMost", "Wave", "WorldUUIDLeast",
        "WorldUUIDMost"
    };

    private PocketMobs() { }

    public static PocketMobTag entity2tag(@NonNull Entity entity) {
        Map<String, Object> entityTag = Dirty.getEntityTag(entity);
        for (String illegalMobTag : ILLEGAL_MOB_TAGS) {
            entityTag.remove(illegalMobTag);
        }
        PocketMobTag tag = new PocketMobTag();
        tag.setMobTag(Json.serialize(entityTag));
        tag.setDisplayName(entity.customName());
        return tag;
    }

    public static ItemStack entity2item(@NonNull Entity entity, @NonNull Mytems mytems) {
        ItemStack itemStack = mytems.createItemStack();
        PocketMobTag tag = entity2tag(entity);
        tag.store(itemStack, (PocketMob) mytems.getMytem());
        return itemStack;
    }

    public static Entity item2entity(Location location, ItemStack itemStack, PocketMob pocketMob) {
        PocketMobTag tag = new PocketMobTag();
        tag.load(itemStack, pocketMob);
        Map<String, Object> entityTag = tag.parseMobTag();
        entityTag.put("Pos", Arrays.asList(location.getX(), location.getY(), location.getZ()));
        if (entityTag == null) return null;
        EntityType entityType = pocketMob.getEntityType();
        Entity entity = location.getWorld().spawnEntity(location, entityType, SpawnReason.SPAWNER_EGG, e -> {
                Dirty.setEntityTag(e, entityTag);
            });
        return entity;
    }
}
