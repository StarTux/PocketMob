package com.cavetale.pocketmob;

import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.item.pocketmob.PocketMob;
import com.cavetale.mytems.item.pocketmob.PocketMobTag;
import javax.annotation.Nullable;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import static org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.SPAWNER_EGG;

/**
 * PocketMob serialization utility class.
 */
public final class PocketMobs {
    public static PocketMobTag entity2tag(@NonNull Entity entity) {
        PocketMobTag tag = new PocketMobTag();
        tag.serializeMob(entity);
        return tag;
    }

    public static ItemStack entity2item(@NonNull Entity entity, @NonNull Mytems mytems) {
        ItemStack itemStack = mytems.createItemStack();
        PocketMobTag tag = entity2tag(entity);
        tag.store(itemStack, (PocketMob) mytems.getMytem());
        return itemStack;
    }

    public static Entity item2entity(Location location, ItemStack itemStack, PocketMob pocketMob, @Nullable Player player) {
        PocketMobTag tag = new PocketMobTag();
        tag.load(itemStack, pocketMob);
        if (tag.getMob() == null) {
            return location.getWorld().spawnEntity(location, pocketMob.getEntityType(), SPAWNER_EGG);
        }
        Entity entity = tag.deserializeMob(location.getWorld());
        if (entity == null) return null;
        if (entity instanceof Villager villager && villager.isSleeping()) {
            villager.wakeup();
        }
        if (player != null && entity instanceof Tameable tameable && tameable.isTamed()) {
            tameable.setOwner(player);
        }
        return entity.spawnAt(location, SPAWNER_EGG)
            ? entity
            : null;
    }

    private PocketMobs() { }
}
