package com.cavetale.pocketmob;

import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.MytemsTag;
import com.cavetale.mytems.item.pocketmob.PocketMob;
import com.winthier.generic_events.GenericEvents;
import java.util.Objects;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.ThrowableProjectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/**
 * We listen for events pertaining to the PocketBall. Mob egg spawning
 * will be handled by vanilla. We do however protect mob spawners from
 * being modified.
 */
@RequiredArgsConstructor
public final class EventListener implements Listener {
    protected final PocketMobPlugin plugin;
    private Random random = new Random();

    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGH)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof ThrowableProjectile)) return;
        ThrowableProjectile projectile = (ThrowableProjectile) event.getEntity();
        ItemStack thrownItem = projectile.getItem();
        Mytems mytems = Mytems.forItem(thrownItem);
        Player player = projectile.getShooter() instanceof Player
            ? (Player) projectile.getShooter()
            : null;
        if (mytems == null) return;
        if (MytemsTag.MOB_CATCHER.isTagged(mytems)) {
            event.setCancelled(true);
            projectile.remove();
            switch (mytems) {
            case ANIMAL_CATCHER: {
                double range = 2.0;
                int amount = (int) (range * range * range * 8.0 * 4.0);
                projectile.getWorld().spawnParticle(Particle.SPELL_MOB, projectile.getLocation(), amount, range, range, range, 1);
                for (Entity nearby : projectile.getNearbyEntities(range, range, range)) {
                    CatchResult catchResult = animalCatcher(projectile, nearby, player);
                    catchEffect(projectile.getLocation(), catchResult, null);
                }
                return;
            }
            case PET_CATCHER: {
                double range = 1.5;
                int amount = (int) (range * range * range * 8.0 * 4.0);
                projectile.getWorld().spawnParticle(Particle.SPELL_MOB, projectile.getLocation(), amount, range, range, range, 1);
                for (Entity nearby : projectile.getNearbyEntities(range, range, range)) {
                    CatchResult catchResult = petCatcher(projectile, nearby, player);
                    catchEffect(projectile.getLocation(), catchResult, null);
                }
                return;
            }
            case FISH_CATCHER: {
                double range = 3;
                int amount = (int) (range * range * range * 8.0 * 4.0);
                projectile.getWorld().spawnParticle(Particle.SPELL_MOB, projectile.getLocation(), amount, range, range, range, 1);
                for (Entity nearby : projectile.getNearbyEntities(range, range, range)) {
                    CatchResult catchResult = fishCatcher(projectile, nearby, player);
                    catchEffect(projectile.getLocation(), catchResult, null);
                }
                return;
            }
            case VILLAGER_CATCHER: {
                CatchResult catchResult = villagerCatcher(projectile, event.getHitEntity(), player);
                catchEffect(projectile.getLocation(), catchResult, mytems);
                return;
            }
            case MONSTER_CATCHER: {
                CatchResult catchResult = monsterCatcher(projectile, event.getHitEntity(), player);
                catchEffect(projectile.getLocation(), catchResult, mytems);
                return;
            }
            case MOB_CATCHER: default: {
                CatchResult catchResult = mobCatcher(projectile, event.getHitEntity(), player);
                catchEffect(projectile.getLocation(), catchResult, mytems);
                return;
            }
            }
        } else if (MytemsTag.POCKET_MOB.isTagged(mytems)) {
            event.setCancelled(true);
            projectile.remove();
            PocketMob pocketMob = (PocketMob) mytems.getMytem();
            final Entity entity;
            switch (pocketMob.getEntityType()) {
            case ENDER_DRAGON: case WITHER:
                entity = null;
                break;
            default:
                entity = PocketMobs.item2entity(projectile.getLocation(), thrownItem, pocketMob);
            }
            if (entity == null) {
                projectile.getWorld().dropItem(projectile.getLocation(), thrownItem);
            }
            if (player != null && entity instanceof Tameable) {
                Tameable tameable = (Tameable) entity;
                tameable.setOwner(player);
            }
        }
    }

    void catchEffect(Location location, CatchResult catchResult, Mytems mytems) {
        switch (catchResult) {
        case MISS: case UNCATCHABLE: case DENIED:
            if (mytems == null) return;
            location.getWorld().dropItem(location, mytems.createItemStack());
            location.getWorld().playSound(location, Sound.ENTITY_ITEM_PICKUP, SoundCategory.MASTER, 0.5f, 0.5f);
            break;
        case BAD_LUCK:
            location.getWorld().playSound(location, Sound.BLOCK_FIRE_EXTINGUISH, SoundCategory.MASTER, 0.5f, 1.5f);
            location.getWorld().spawnParticle(Particle.SMOKE_NORMAL, location, 16, 0.5, 0.5, 0.5, 0.0);
            break;
        case SUCCESS:
            location.getWorld().playSound(location, Sound.BLOCK_ANVIL_LAND, SoundCategory.MASTER, 0.5f, 1.6f);
            location.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, location, 16, 0.5, 0.5, 0.5, 0.0);
        default: break;
        }
    }

    boolean runPlayerChecks(Player player, Entity entity) {
        if (entity instanceof Tameable) {
            Tameable tameable = (Tameable) entity;
            if (tameable.isTamed()) {
                return Objects.equals(player.getUniqueId(), tameable.getOwnerUniqueId());
            }
        }
        if (!GenericEvents.playerCanDamageEntity(player, entity)) {
            return false;
        }
        return true;
    }

    boolean eggify(LivingEntity entity) {
        Mytems mytems = PocketMobPlugin.ENTITY_MYTEMS_MAP.get(entity.getType());
        if (mytems == null) return false;
        if (entity instanceof InventoryHolder) {
            for (ItemStack itemStack : ((InventoryHolder) entity).getInventory()) {
                if (itemStack == null || itemStack.getType() == Material.AIR) continue;
                entity.getWorld().dropItem(entity.getLocation(), itemStack.clone());
            }
            ((InventoryHolder) entity).getInventory().clear();
        }
        EntityEquipment equipment = entity.getEquipment();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (equipment.getDropChance(slot) == 0) continue;
            ItemStack itemStack = equipment.getItem(slot);
            if (itemStack == null || itemStack.getType() == Material.AIR) continue;
            if (random.nextDouble() > equipment.getDropChance(slot)) continue;
            entity.getWorld().dropItem(entity.getLocation(), itemStack.clone());
        }
        equipment.clear();
        ItemStack itemStack = PocketMobs.entity2item(entity, mytems);
        if (itemStack == null) return false;
        Item item = entity.getWorld().dropItem(entity.getLocation(), itemStack);
        if (item == null) return false;
        entity.remove();
        return true;
    }

    /**
     * Direct hit with MobCatcher.
     * @return true if entity was eggified, false otherwise.
     */
    CatchResult mobCatcher(ThrowableProjectile projectile, Entity hitEntity, Player player) {
        if (hitEntity == null) return CatchResult.MISS;
        if (!(hitEntity instanceof LivingEntity)) return CatchResult.UNCATCHABLE;
        LivingEntity living = (LivingEntity) hitEntity;
        MobType mobType = MobType.ENTITY_MOB_MAP.get(living.getType());
        if (mobType == null) return CatchResult.UNCATCHABLE;
        if (player == null || player.getGameMode() != GameMode.CREATIVE) {
            if (random.nextDouble() > mobType.chance) return CatchResult.BAD_LUCK;
        }
        if (player != null && !runPlayerChecks(player, living)) return CatchResult.UNCATCHABLE;
        if (!eggify(living)) return CatchResult.DENIED;
        return CatchResult.SUCCESS;
    }

    CatchResult monsterCatcher(ThrowableProjectile projectile, Entity hitEntity, Player player) {
        if (hitEntity == null) return CatchResult.MISS;
        if (!(hitEntity instanceof LivingEntity)) return CatchResult.UNCATCHABLE;
        LivingEntity living = (LivingEntity) hitEntity;
        MobType mobType = MobType.ENTITY_MOB_MAP.get(living.getType());
        if (mobType != MobType.MONSTER && mobType != MobType.BOSS) return CatchResult.UNCATCHABLE;
        if (player != null && !runPlayerChecks(player, living)) return CatchResult.UNCATCHABLE;
        double maxHealth = living.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double health = living.getHealth();
        double chance = Math.min(0.95, mobType.chance + (1.0 - (health / maxHealth)));
        if (mobType == MobType.BOSS) chance *= 0.1;
        if (random.nextDouble() > chance) return CatchResult.BAD_LUCK;
        if (!eggify(living)) return CatchResult.DENIED;
        return CatchResult.SUCCESS;
    }

    CatchResult villagerCatcher(ThrowableProjectile projectile, Entity hitEntity, Player player) {
        if (hitEntity == null) return CatchResult.MISS;
        if (!(hitEntity instanceof LivingEntity)) return CatchResult.UNCATCHABLE;
        LivingEntity living = (LivingEntity) hitEntity;
        MobType mobType = MobType.ENTITY_MOB_MAP.get(living.getType());
        if (mobType != MobType.VILLAGER) return CatchResult.UNCATCHABLE;
        if (player != null && !runPlayerChecks(player, living)) return CatchResult.UNCATCHABLE;
        if (!eggify(living)) return CatchResult.DENIED;
        return CatchResult.SUCCESS;
    }

    CatchResult animalCatcher(ThrowableProjectile projectile, Entity hitEntity, Player player) {
        if (hitEntity == null) return CatchResult.MISS;
        if (!(hitEntity instanceof LivingEntity)) return CatchResult.UNCATCHABLE;
        LivingEntity living = (LivingEntity) hitEntity;
        MobType mobType = MobType.ENTITY_MOB_MAP.get(living.getType());
        if (mobType != MobType.ANIMAL && mobType != MobType.PET) {
            return CatchResult.UNCATCHABLE;
        }
        if (player != null && !runPlayerChecks(player, living)) return CatchResult.UNCATCHABLE;
        double chance = 0.8;
        if (random.nextDouble() > chance) return CatchResult.BAD_LUCK;
        if (!eggify(living)) return CatchResult.DENIED;
        return CatchResult.SUCCESS;
    }

    CatchResult petCatcher(ThrowableProjectile projectile, Entity hitEntity, Player player) {
        if (hitEntity == null) return CatchResult.MISS;
        if (!(hitEntity instanceof Tameable)) return CatchResult.UNCATCHABLE;
        Tameable living = (Tameable) hitEntity;
        MobType mobType = MobType.ENTITY_MOB_MAP.get(living.getType());
        if (mobType == null) return CatchResult.UNCATCHABLE;
        if (player == null || !living.isTamed() || !Objects.equals(player.getUniqueId(), living.getOwnerUniqueId())) {
            return CatchResult.UNCATCHABLE;
        }
        if (!eggify(living)) return CatchResult.DENIED;
        return CatchResult.SUCCESS;
    }

    CatchResult fishCatcher(ThrowableProjectile projectile, Entity hitEntity, Player player) {
        if (hitEntity == null) return CatchResult.MISS;
        if (!(hitEntity instanceof LivingEntity)) return CatchResult.UNCATCHABLE;
        LivingEntity living = (LivingEntity) hitEntity;
        MobType mobType = MobType.ENTITY_MOB_MAP.get(living.getType());
        if (mobType != MobType.FISH) return CatchResult.UNCATCHABLE;
        if (player != null && !runPlayerChecks(player, living)) return CatchResult.UNCATCHABLE;
        if (!eggify(living)) return CatchResult.DENIED;
        return CatchResult.SUCCESS;
    }
}
