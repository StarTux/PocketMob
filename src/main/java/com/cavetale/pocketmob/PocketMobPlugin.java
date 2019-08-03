package com.cavetale.pocketmob;

import com.cavetale.dirty.Dirty;
import com.cavetale.itemmarker.ItemMarker;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public final class PocketMobPlugin extends JavaPlugin {
    final EventListener eventListener = new EventListener(this);
    final Metadata metadata = new Metadata(this);
    ConfigurationSection resourcesConfig;
    final Random random = new Random(System.nanoTime());
    static final String META_TYPE = "EggType";
    static final String ITEM_TAG = "pocketmob:ball";
    static final String ITEM_MOB = "mob_type";

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();
        getServer().getPluginManager().registerEvents(eventListener, this);
        getCommand("pocketmob").setExecutor(new PocketMobCommand(this));
    }

    ConfigurationSection loadYamlResource(@NonNull final String name) {
        return YamlConfiguration
            .loadConfiguration(new InputStreamReader(getResource(name)));
    }

    ConfigurationSection getResourcesConfig() {
        if (resourcesConfig == null) {
            resourcesConfig = loadYamlResource("resources.yml");
        }
        return resourcesConfig;
    }

    double multiply(PocketMob pocketMob, double chance) {
        double result = chance;
        String k1 = "modifiers." + pocketMob.key;
        if (getConfig().isSet(k1)) {
            result *= getConfig().getDouble(k1);
        }
        // String k2 = "modifiers." + pocketMob.mobType.key;
        // if (!k1.equals(k2) && getConfig().isSet(k2)) {
        //     result *= getConfig().getDouble(k2);
        // }
        //
        String k3 = "maxima." + pocketMob.key;
        if (getConfig().isSet(k3)) {
            result = Math.min(result, getConfig().getDouble(k3));
        }
        // String k4 = "maxima." + pocketMob.mobType.key;
        // if (!k3.equals(k4) && getConfig().isSet(k4)) {
        //     result = Math.min(result, getConfig().getDouble(k4));
        // }
        return Math.min(1.0, result);
    }

    // Eggify

    String fmt(@NonNull String txt, Object... args) {
        txt = ChatColor.translateAlternateColorCodes('&', txt);
        if (args.length > 0) txt = String.format(txt, args);
        return txt;
    }

    void act(@NonNull Player player, @NonNull String txt, Object... args) {
        txt = fmt(txt, args);
        player.sendActionBar(txt);
    }

    ItemStack makeItem(@NonNull ConfigurationSection conf) {
        final String id = conf.getString("Id");
        final String tex = conf.getString("Texture");
        ItemStack item = Dirty.makeSkull(id, tex);
        ItemMeta meta = item.getItemMeta();
        final String name = conf.getString("Name");
        final List<String> lore = conf.getStringList("Lore").stream()
            .map(this::fmt)
            .collect(Collectors.toList());
        meta.setDisplayName(fmt(name));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    ItemStack makePocketBall(@NonNull MobType mobType) {
        ConfigurationSection conf = getResourcesConfig()
            .getConfigurationSection(mobType.displayName + "BallItem");
        ItemStack item = makeItem(conf);
        ItemMarker.setCustomId(item, ITEM_TAG);
        ItemMarker.setMarker(item, ITEM_MOB, mobType.key);
        return item;
    }

    ItemStack eggify(@NonNull PocketMob pocketMob,
                     @NonNull Entity entity) {
        ItemStack item = new ItemStack(pocketMob.spawnEggType);
        Map<String, Object> entityTag = Dirty.getEntityTag(entity);
        Stream.of("Pos", "Rotation", "Motion", "FallDistance", "OnGround",
                  "Dimension", "PortalCooldown", "UUIDMost", "UUIDLeast",
                  "UUID", "Passengers", "HurtByTimestamp", "WorldUUIDLeast",
                  "WorldUUIDMost", "Spigot.ticksLived", "Bukkit.updateLevel",
                  "Leashed", "Leash", "APX", "APY", "APZ", "Brain",
                  "SleepingX", "SleepingY", "SleepingZ", "Paper.Origin",
                  "Paper.SpawnReason")
            .forEach(t -> entityTag.remove(t));
        Map<String, Object> itemTag = new HashMap<>();
        itemTag.put("EntityTag", entityTag);
        item = Dirty.setItemTag(item, itemTag);
        return item;
    }

    static String niceEnum(Enum<?> e) {
        return niceEnum(e.name());
    }

    static String niceEnum(String inp) {
        return Stream.of(inp.split("_"))
            .map(s -> s.substring(0, 1) + s.substring(1).toLowerCase())
            .collect(Collectors.joining(" "));
    }

    Item eggifyAndDrop(Entity entity, PocketMob pocketMob) {
        ItemStack item = eggify(pocketMob, entity);
        if (item == null) return null;
        Item drop = entity.getWorld().dropItem(entity.getLocation(), item);
        entity.remove();
        return drop;
    }

    ItemStack eggifyAndGive(Player player, Entity entity, PocketMob pocketMob) {
        ItemStack item = eggify(pocketMob, entity);
        if (item == null) return null;
        for (ItemStack drop : player.getInventory().addItem(item).values()) {
            entity.getWorld().dropItem(entity.getLocation(), item);
        }
        entity.remove();
        return item;
    }

    /**
     * PocketMob must match entity!
     */
    boolean onPocketBallHit(@NonNull Player player,
                            @NonNull LivingEntity entity,
                            @NonNull PocketMob pocketMob,
                            final double intensity) {
        switch (pocketMob.mobTypes.iterator().next()) {
        case MONSTER: return hitMonster(player, entity, pocketMob, intensity);
        case WATER: return hitWaterMob(player, entity, pocketMob, intensity);
        case VILLAGER: return hitVillager(player, entity, pocketMob, intensity);
        case ANIMAL: return hitAnimal(player, entity, pocketMob, intensity);
        case PET: return hitPet(player, entity, pocketMob, intensity);
        default: return false;
        }
    }

    boolean hitMonster(@NonNull Player player,
                       @NonNull LivingEntity entity,
                       @NonNull PocketMob pocketMob,
                       final double intensity) {
        if (intensity <= 0.99) return false; // Direct hit!
        double health = entity.getHealth();
        double maxHealth = entity
            .getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double chance;
        if (maxHealth <= 1.0) {
            // Small slimes
            chance = 1.0; // configure?
        } else {
            chance = 1.0 - (health / maxHealth);
        }
        chance = multiply(pocketMob, chance);
        double roll = random.nextDouble();
        boolean success = roll < chance;
        if (!success) {
            act(player,
                "&cCapturing %s failed (%d%% chance)",
                niceEnum(entity.getType()),
                (int) (chance * 100.0));
            return false;
        }
        eggifyAndDrop(entity, pocketMob);
        act(player,
            "&aCapturing %s succeeded (%d%% chance)",
            niceEnum(entity.getType()),
            (int) (chance * 100.0));
        return true;
    }

    boolean hitWaterMob(@NonNull Player player,
                        @NonNull LivingEntity entity,
                        @NonNull PocketMob pocketMob,
                        final double intensity) {
        if (intensity < 0.1) return false;
        double chance = intensity;
        chance = multiply(pocketMob, chance);
        double roll = random.nextDouble();
        boolean success = roll < chance;
        if (!success) {
            act(player,
                "&cCapturing %s failed (%d%% chance)",
                niceEnum(entity.getType()),
                (int) (chance * 100.0));
            return false;
        }
        eggifyAndDrop(entity, pocketMob);
        act(player,
            "&9Capturing %s succeeded (%d%% chance)",
            niceEnum(entity.getType()),
            (int) (chance * 100.0));
        return true;
    }

    boolean hitVillager(@NonNull Player player,
                        @NonNull LivingEntity entity,
                        @NonNull PocketMob pocketMob,
                        final double intensity) {
        if (intensity <= 0.99) return false; // Direct hit!
        double chance;
        if (entity instanceof Villager) {
            Villager villager = (Villager) entity;
            if (villager.isSleeping()) return false;
            int level = villager.getVillagerLevel();
            chance = 1.0 - ((double) level / 5.0);
            chance = Math.max(0.05, chance);
        } else {
            chance = 1.0; // configure!
        }
        chance = multiply(pocketMob, chance);
        double roll = random.nextDouble();
        boolean success = roll < chance;
        if (!success) {
            act(player,
                "&cCapturing %s failed (%d%% chance)",
                niceEnum(entity.getType()),
                (int) (chance * 100.0));
            return false;
        }
        eggifyAndDrop(entity, pocketMob);
        act(player,
            "&aCapturing %s succeeded (%d%% chance)",
            niceEnum(entity.getType()),
            (int) (chance * 100.0));
        return true;
    }

    boolean hitAnimal(@NonNull Player player,
                      @NonNull LivingEntity entity,
                      @NonNull PocketMob pocketMob,
                      final double intensity) {
        double health = entity.getHealth();
        double maxHealth = entity
            .getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double chance = health / maxHealth;
        chance = multiply(pocketMob, chance);
        double roll = random.nextDouble();
        boolean success = roll < chance;
        if (!success) {
            act(player,
                "&cCapturing %s failed (%d%% chance)",
                niceEnum(entity.getType()),
                (int) (chance * 100.0));
            return false;
        }
        eggifyAndDrop(entity, pocketMob);
        act(player,
            "&aCapturing %s succeeded (%d%% chance)",
            niceEnum(entity.getType()),
            (int) (chance * 100.0));
        return true;
    }

    boolean hitPet(@NonNull Player player,
                   @NonNull LivingEntity entity,
                   @NonNull PocketMob pocketMob,
                   final double intensity) {
        if (intensity <= 0.99) return false; // Direct hit!
        double chance;
        if (!(entity instanceof Tameable)) {
            // Only ocelots
            chance = 1.0; // configure!
        } else {
            Tameable tameable = (Tameable) entity;
            if (player.equals(tameable.getOwner())) {
                chance = 1.0;
            } else if (tameable.isTamed()) {
                chance = 0.0;
            } else {
                chance = 0.25;
            }
        }
        chance = multiply(pocketMob, chance);
        double roll = random.nextDouble();
        boolean success = roll < chance;
        if (!success) {
            act(player,
                "&cCapturing %s failed (%d%% chance)",
                niceEnum(entity.getType()),
                (int) (chance * 100.0));
            return false;
        }
        eggifyAndGive(player, entity, pocketMob);
        act(player,
            "&aCapturing %s succeeded (%d%% chance)",
            niceEnum(entity.getType()),
            (int) (chance * 100.0));
        return true;
    }
}
