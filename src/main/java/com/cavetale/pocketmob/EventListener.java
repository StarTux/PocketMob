package com.cavetale.pocketmob;

import com.cavetale.core.event.block.PlayerBlockAbilityQuery;
import com.cavetale.core.event.entity.PlayerEntityAbilityQuery;
import com.cavetale.core.event.entity.PluginEntityEvent;
import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.MytemsTag;
import com.cavetale.mytems.item.pocketmob.PocketMob;
import com.cavetale.worldmarker.entity.EntityMarker;
import com.cavetale.worldmarker.item.ItemMarker;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.ComplexEntityPart;
import org.bukkit.entity.EnderDragon;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

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
                    catchEffect(projectile.getLocation(), catchResult, null, player);
                }
                return;
            }
            case PET_CATCHER: {
                double range = 1.5;
                int amount = (int) (range * range * range * 8.0 * 4.0);
                projectile.getWorld().spawnParticle(Particle.SPELL_MOB, projectile.getLocation(), amount, range, range, range, 1);
                for (Entity nearby : projectile.getNearbyEntities(range, range, range)) {
                    CatchResult catchResult = petCatcher(projectile, nearby, player);
                    catchEffect(projectile.getLocation(), catchResult, null, player);
                }
                return;
            }
            case FISH_CATCHER: {
                double range = 3;
                int amount = (int) (range * range * range * 8.0 * 4.0);
                projectile.getWorld().spawnParticle(Particle.SPELL_MOB, projectile.getLocation(), amount, range, range, range, 1);
                for (Entity nearby : projectile.getNearbyEntities(range, range, range)) {
                    CatchResult catchResult = fishCatcher(projectile, nearby, player);
                    catchEffect(projectile.getLocation(), catchResult, null, player);
                }
                return;
            }
            case VILLAGER_CATCHER: {
                CatchResult catchResult = villagerCatcher(projectile, event.getHitEntity(), player);
                catchEffect(projectile.getLocation(), catchResult, mytems, player);
                return;
            }
            case MONSTER_CATCHER: {
                CatchResult catchResult = monsterCatcher(projectile, event.getHitEntity(), player);
                catchEffect(projectile.getLocation(), catchResult, mytems, player);
                return;
            }
            case MOB_CATCHER: default: {
                CatchResult catchResult = mobCatcher(projectile, event.getHitEntity(), player);
                catchEffect(projectile.getLocation(), catchResult, mytems, player);
                return;
            }
            }
        } else if (MytemsTag.POCKET_MOB.isTagged(mytems)) {
            event.setCancelled(true);
            projectile.remove();
            PocketMob pocketMob = (PocketMob) mytems.getMytem();
            final Entity entity;
            Location location = projectile.getLocation();
            if (player != null && !PlayerBlockAbilityQuery.Action.SPAWN_MOB.query(player, location.getBlock())) {
                entity = null;
            } else {
                switch (pocketMob.getEntityType()) {
                case ENDER_DRAGON: case WITHER:
                    entity = null;
                    break;
                default:
                    entity = PocketMobs.item2entity(location, thrownItem, pocketMob, player);
                }
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

    private void catchEffect(Location location, CatchResult catchResult, Mytems mytems, Player player) {
        switch (catchResult) {
        case MISS: case UNCATCHABLE: case DENIED:
            if (mytems == null) return;
            if (player == null || player.getGameMode() != GameMode.CREATIVE) {
                location.getWorld().dropItem(location, mytems.createItemStack());
            }
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

    private boolean runPlayerChecks(@NonNull final Player player, @NonNull final Entity entity) {
        if (!PlayerEntityAbilityQuery.Action.CATCH.query(player, entity)) {
            return false;
        }
        if (entity instanceof Tameable) {
            Tameable tameable = (Tameable) entity;
            if (tameable.isTamed()) {
                UUID owner = tameable.getOwnerUniqueId();
                if (owner != null) {
                    return Objects.equals(player.getUniqueId(), owner);
                }
            }
        }
        return true;
    }

    private boolean runEntityChecks(Entity entity) {
        if (EntityMarker.hasId(entity)) return false;
        if (!entity.getPassengers().isEmpty()) return false;
        if (entity instanceof EnderDragon) {
            if (((EnderDragon) entity).getPhase() == EnderDragon.Phase.DYING) return false;
        }
        return true;
    }

    /**
     * A simple item may be kept in the Pocket Mob's inventory. It's
     * either:
     * - A new item with the same material
     * - The above plus identical enchants and damage value
     * Anything more complex shall be removed prior to eggification.
     */
    static boolean isSimpleItem(ItemStack itemStack) {
        if (ItemMarker.hasId(itemStack)) return false;
        ItemStack copy = new ItemStack(itemStack.getType());
        if (copy.isSimilar(itemStack)) return true;
        if (!itemStack.hasItemMeta()) return false;
        copy.addEnchantments(itemStack.getEnchantments());
        ItemMeta meta = itemStack.getItemMeta();
        if (meta instanceof Damageable) {
            Damageable damageable = (Damageable) meta;
            if (damageable.hasDamage()) {
                Damageable copyDamageable = (Damageable) copy.getItemMeta();
                copyDamageable.setDamage(damageable.getDamage());
                copy.setItemMeta((ItemMeta) copyDamageable);
            }
        }
        return copy.isSimilar(itemStack);
    }

    private boolean eggify(final LivingEntity entity) {
        if (!PluginEntityEvent.Action.EGGIFY.call(plugin, entity)) {
            return false;
        }
        Mytems mytems = PocketMobPlugin.ENTITY_MYTEMS_MAP.get(entity.getType());
        if (mytems == null) return false;
        if (entity instanceof InventoryHolder) {
            InventoryHolder holder = (InventoryHolder) entity;
            Inventory inventory = holder.getInventory();
            for (int i = 0; i < inventory.getSize(); i += 1) {
                ItemStack itemStack = inventory.getItem(i);
                if (itemStack == null || itemStack.getType() == Material.AIR) continue;
                if (isSimpleItem(itemStack)) continue;
                entity.getWorld().dropItem(entity.getLocation(), itemStack.clone());
                inventory.setItem(i, null);
            }
        }
        EntityEquipment equipment = entity.getEquipment();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack itemStack = equipment.getItem(slot);
            if (itemStack == null || itemStack.getType() == Material.AIR) continue;
            if (isSimpleItem(itemStack)) continue;
            double dropChance = equipment.getDropChance(slot);
            if (dropChance > 0 && random.nextDouble() < dropChance) {
                entity.getWorld().dropItem(entity.getLocation(), itemStack.clone());
            }
            equipment.setItem(slot, null);
        }
        ItemStack itemStack = PocketMobs.entity2item(entity, mytems);
        if (itemStack == null) return false;
        Item item = entity.getWorld().dropItem(entity.getLocation(), itemStack);
        if (item == null) return false;
        if (entity.isLeashed()) {
            entity.setLeashHolder(null);
        }
        if (entity instanceof EnderDragon) {
            EnderDragon enderDragon = (EnderDragon) entity;
            DragonBattle dragonBattle = enderDragon.getDragonBattle();
            if (dragonBattle != null) {
                enderDragon.setHealth(0);
                return true;
            }
        }
        entity.remove();
        return true;
    }

    /**
     * Direct hit with MobCatcher.
     * @return true if entity was eggified, false otherwise.
     */
    CatchResult mobCatcher(ThrowableProjectile projectile, Entity hitEntity, Player player) {
        if (hitEntity instanceof ComplexEntityPart) hitEntity = ((ComplexEntityPart) hitEntity).getParent();
        if (hitEntity == null || !hitEntity.isValid()) return CatchResult.MISS;
        if (!(hitEntity instanceof LivingEntity)) return CatchResult.UNCATCHABLE;
        LivingEntity living = (LivingEntity) hitEntity;
        if (player != null && player.getGameMode() == GameMode.CREATIVE) {
            return eggify(living) ? CatchResult.SUCCESS : CatchResult.UNCATCHABLE;
        }
        if (!runEntityChecks(living)) return CatchResult.UNCATCHABLE;
        MobType mobType = MobType.ENTITY_MOB_MAP.get(living.getType());
        if (mobType == null || mobType == MobType.BOSS) return CatchResult.UNCATCHABLE;
        if (random.nextDouble() > mobType.chance) return CatchResult.BAD_LUCK;
        if (player != null && !runPlayerChecks(player, living)) return CatchResult.UNCATCHABLE;
        if (!eggify(living)) return CatchResult.DENIED;
        return CatchResult.SUCCESS;
    }

    CatchResult monsterCatcher(ThrowableProjectile projectile, Entity hitEntity, Player player) {
        if (hitEntity instanceof ComplexEntityPart) hitEntity = ((ComplexEntityPart) hitEntity).getParent();
        if (hitEntity == null || !hitEntity.isValid()) return CatchResult.MISS;
        if (!(hitEntity instanceof LivingEntity)) return CatchResult.UNCATCHABLE;
        LivingEntity living = (LivingEntity) hitEntity;
        if (player != null && player.getGameMode() == GameMode.CREATIVE) {
            return eggify(living) ? CatchResult.SUCCESS : CatchResult.UNCATCHABLE;
        }
        if (!runEntityChecks(living)) return CatchResult.UNCATCHABLE;
        MobType mobType = MobType.ENTITY_MOB_MAP.get(living.getType());
        if (mobType != MobType.MONSTER && mobType != MobType.BOSS) return CatchResult.UNCATCHABLE;
        if (player != null && !runPlayerChecks(player, living)) return CatchResult.UNCATCHABLE;
        double maxHealth = living.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double health = living.getHealth();
        double chance = Math.min(0.95, mobType.chance + (1.0 - (health / maxHealth)));
        if (mobType == MobType.BOSS) chance *= 0.025;
        if (random.nextDouble() > chance) return CatchResult.BAD_LUCK;
        if (!eggify(living)) return CatchResult.DENIED;
        return CatchResult.SUCCESS;
    }

    CatchResult villagerCatcher(ThrowableProjectile projectile, Entity hitEntity, Player player) {
        if (hitEntity instanceof ComplexEntityPart) hitEntity = ((ComplexEntityPart) hitEntity).getParent();
        if (hitEntity == null || !hitEntity.isValid()) return CatchResult.MISS;
        if (!(hitEntity instanceof LivingEntity)) return CatchResult.UNCATCHABLE;
        LivingEntity living = (LivingEntity) hitEntity;
        if (player != null && player.getGameMode() == GameMode.CREATIVE) {
            return eggify(living) ? CatchResult.SUCCESS : CatchResult.UNCATCHABLE;
        }
        if (!runEntityChecks(living)) return CatchResult.UNCATCHABLE;
        MobType mobType = MobType.ENTITY_MOB_MAP.get(living.getType());
        if (mobType != MobType.VILLAGER) return CatchResult.UNCATCHABLE;
        if (player != null && !runPlayerChecks(player, living)) return CatchResult.UNCATCHABLE;
        if (!eggify(living)) return CatchResult.DENIED;
        return CatchResult.SUCCESS;
    }

    CatchResult animalCatcher(ThrowableProjectile projectile, Entity hitEntity, Player player) {
        if (hitEntity instanceof ComplexEntityPart) hitEntity = ((ComplexEntityPart) hitEntity).getParent();
        if (hitEntity == null || !hitEntity.isValid()) return CatchResult.MISS;
        if (!(hitEntity instanceof LivingEntity)) return CatchResult.UNCATCHABLE;
        LivingEntity living = (LivingEntity) hitEntity;
        if (player != null && player.getGameMode() == GameMode.CREATIVE) {
            return eggify(living) ? CatchResult.SUCCESS : CatchResult.UNCATCHABLE;
        }
        if (!runEntityChecks(living)) return CatchResult.UNCATCHABLE;
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
        if (hitEntity instanceof ComplexEntityPart) hitEntity = ((ComplexEntityPart) hitEntity).getParent();
        if (hitEntity == null || !hitEntity.isValid()) return CatchResult.MISS;
        if (!(hitEntity instanceof Tameable)) return CatchResult.UNCATCHABLE;
        Tameable living = (Tameable) hitEntity;
        MobType mobType = MobType.ENTITY_MOB_MAP.get(living.getType());
        if (mobType == null) return CatchResult.UNCATCHABLE;
        if (player == null || !living.isTamed()) return CatchResult.UNCATCHABLE;
        UUID owner = living.getOwnerUniqueId();
        if (owner != null && !Objects.equals(player.getUniqueId(), owner)) {
            return CatchResult.UNCATCHABLE;
        }
        if (!eggify(living)) return CatchResult.DENIED;
        return CatchResult.SUCCESS;
    }

    CatchResult fishCatcher(ThrowableProjectile projectile, Entity hitEntity, Player player) {
        if (hitEntity == null || !hitEntity.isValid()) return CatchResult.MISS;
        if (!(hitEntity instanceof LivingEntity)) return CatchResult.UNCATCHABLE;
        LivingEntity living = (LivingEntity) hitEntity;
        if (player != null && player.getGameMode() == GameMode.CREATIVE) {
            return eggify(living) ? CatchResult.SUCCESS : CatchResult.UNCATCHABLE;
        }
        if (!runEntityChecks(living)) return CatchResult.UNCATCHABLE;
        MobType mobType = MobType.ENTITY_MOB_MAP.get(living.getType());
        if (mobType != MobType.FISH) return CatchResult.UNCATCHABLE;
        if (player != null && !runPlayerChecks(player, living)) return CatchResult.UNCATCHABLE;
        if (!eggify(living)) return CatchResult.DENIED;
        return CatchResult.SUCCESS;
    }
}
