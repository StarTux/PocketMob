package com.cavetale.pocketmob;

import com.cavetale.dirty.Dirty;
import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.item.pocketmob.PocketMob;
import com.cavetale.mytems.item.pocketmob.PocketMobTag;
import com.cavetale.mytems.util.Json;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;

/**
 * PocketMob serialization utility class.
 */
public final class PocketMobs {
    protected static final String[] ILLEGAL_MOB_TAGS = {
        "APX",
        "APY",
        "APZ",
        "AttackTick",
        "Bukkit.updateLevel",
        "Dimension",
        "FallDistance",
        "HasRaidGoal",
        "HurtByTimestamp",
        "Leash",
        "Leashed",
        "Motion",
        "OnGround",
        "Paper.Origin",
        "Paper.Origin",
        "Paper.SpawnReason",
        "Passengers",
        "PatrolLeader",
        "PatrolTarget",
        "Patrolling",
        "PortalCooldown",
        "Pos",
        "RaidId",
        "RoarTick",
        "Rotation",
        "SleepingX",
        "SleepingY",
        "SleepingZ",
        "Spigot.ticksLived",
        "StunTick",
        "UUID",
        "UUIDLeast",
        "UUIDMost",
        "Wave",
        "WorldUUIDLeast",
        "WorldUUIDMost",
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

    private static int[] fixUuidTag(Object object) {
        if (!(object instanceof List)) return null;
        List<?> list = (List<?>) object;
        if (list.size() != 4) return null;
        int[] array = new int[4];
        for (int i = 0; i < 4; i += 1) {
            Object o = list.get(i);
            if (!(o instanceof Number)) return null;
            Number number = (Number) o;
            array[i] = number.intValue();
        }
        return array;
    }

    private static void fixEntityTag(Object object) {
        try {
            if (object instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) object;
                if (map.containsKey("UUID")) {
                    int[] array = fixUuidTag(map.get("UUID"));
                    if (array != null) map.put("UUID", array);
                }
                for (Object o : map.values()) fixEntityTag(o);
            } else if (object instanceof List) {
                List<?> list = (List<?>) object;
                for (Object o : list) fixEntityTag(o);
            }
        } catch (RuntimeException re) {
            re.printStackTrace();
        }
    }

    public static Entity item2entity(Location location, ItemStack itemStack, PocketMob pocketMob) {
        PocketMobTag tag = new PocketMobTag();
        tag.load(itemStack, pocketMob);
        Map<String, Object> entityTag = tag.parseMobTag();
        entityTag.put("Pos", Arrays.asList(location.getX(), location.getY(), location.getZ()));
        if (entityTag == null) return null;
        fixEntityTag(entityTag);
        EntityType entityType = pocketMob.getEntityType();
        Entity entity = location.getWorld().spawnEntity(location, entityType, SpawnReason.SPAWNER_EGG, e -> {
                Dirty.setEntityTag(e, entityTag);
            });
        return entity;
    }
}
