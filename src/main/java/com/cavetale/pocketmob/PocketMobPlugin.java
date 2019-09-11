package com.cavetale.pocketmob;

import com.cavetale.dirty.Dirty;
import com.cavetale.worldmarker.ItemMarker;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public final class PocketMobPlugin extends JavaPlugin {
    final EventListener eventListener = new EventListener(this);
    final Metadata metadata = new Metadata(this);
    ConfigurationSection itemsConfig;
    final Random random = new Random(System.nanoTime());
    static final String META_TYPE = "MobCatcher";
    static final String ITEM_PREFIX = "pocketmob:";

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();
        getServer().getPluginManager().registerEvents(eventListener, this);
        getCommand("pocketmob").setExecutor(new PocketMobCommand(this));
    }

    /**
     * Load a config file from the config folder, using the resource
     * that comes with the package jar as default.  Save the resource
     * to disk if it does not already exist.
     */
    ConfigurationSection loadYamlFile(@NonNull final String name) {
        File file = new File(getDataFolder(), name);
        if (!file.exists()) saveResource(name, true);
        YamlConfiguration result = YamlConfiguration
            .loadConfiguration(file);
        YamlConfiguration dfl = YamlConfiguration
            .loadConfiguration(new InputStreamReader(getResource(name)));
        result.setDefaults(dfl);
        return result;
    }

    ConfigurationSection getItemsConfig() {
        if (itemsConfig == null) {
            itemsConfig = loadYamlFile("items.yml");
        }
        return itemsConfig;
    }

    // String Util

    String fmt(@NonNull String txt, Object... args) {
        txt = ChatColor.translateAlternateColorCodes('&', txt);
        if (args.length > 0) txt = String.format(txt, args);
        return txt;
    }

    void act(@NonNull Player player, @NonNull String txt, Object... args) {
        txt = fmt(txt, args);
        player.sendActionBar(txt);
    }

    static String niceEnum(Enum<?> e) {
        return niceEnum(e.name());
    }

    static String niceEnum(String inp) {
        return Stream.of(inp.split("_"))
            .map(s -> s.substring(0, 1) + s.substring(1).toLowerCase())
            .collect(Collectors.joining(" "));
    }

    // Mob Catcher Items

    ItemStack makeItem(@NonNull ConfigurationSection conf) {
        final String id = conf.getString("Id");
        final String tex = conf.getString("Texture");
        ItemStack item = Dirty.makeSkull(id, tex);
        ItemMeta meta = item.getItemMeta();
        final String name = conf.getString("Name");
        final String lore = conf.getString("Lore");
        List<String> lores = Text.wrapMultiline(lore, 40);
        meta.setDisplayName(fmt(name));
        meta.setLore(lores);
        item.setItemMeta(meta);
        return item;
    }

    ItemStack makeMobCatcher(@NonNull MobCatcher mobCatcher) {
        ConfigurationSection conf = getItemsConfig()
            .getConfigurationSection(mobCatcher.configKey);
        ItemStack item = makeItem(conf);
        ItemMarker.setId(item, mobCatcher.customId);
        return item;
    }

    // Eggify

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
                  "Paper.SpawnReason", "HasRaidGoal", "Patrolling",
                  "PatrolLeader", "PatrolTarget", "RaidId", "Wave",
                  "AttackTick", "RoarTick", "StunTick")
            .forEach(t -> entityTag.remove(t));
        Map<String, Object> itemTag = new HashMap<>();
        itemTag.put("EntityTag", entityTag);
        item = Dirty.setItemTag(item, itemTag);
        return item;
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
}
