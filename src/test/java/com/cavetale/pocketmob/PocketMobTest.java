package com.cavetale.pocketmob;

import java.io.File;
import java.io.PrintStream;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Boss;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.WaterMob;
import org.junit.Assert;
import org.junit.Test;

public final class PocketMobTest {
    @Test
    public void test() throws Exception {
        grabEntityTypes();
    }

    void grabEntityTypes() throws Exception {
        File file = new File("target/mobtypes.out");
        PrintStream out = new PrintStream(file);
        Map<MobType, Set<EntityType>> map = new EnumMap<>(MobType.class);
        for (MobType mobType : MobType.values()) {
            map.put(mobType, EnumSet.noneOf(EntityType.class));
        }
        for (EntityType ent : EntityType.values()) {
            Material mat;
            try {
                mat = Material.valueOf(ent.name() + "_SPAWN_EGG");
            } catch (IllegalArgumentException iae) {
                continue;
            }
            MobType mobType;
            Class<?> clazz = ent.getEntityClass();
            if (Boss.class.isAssignableFrom(clazz)) {
                continue;
                //mobType = MobType.BOSS;
            } else if (Monster.class.isAssignableFrom(clazz)) {
                mobType = MobType.MONSTER;
            } else if (Tameable.class.isAssignableFrom(clazz)) {
                mobType = MobType.PET;
            } else if (Animals.class.isAssignableFrom(clazz)) {
                mobType = MobType.ANIMAL;
            } else if (WaterMob.class.isAssignableFrom(clazz)) {
                mobType = MobType.WATER;
            } else if (NPC.class.isAssignableFrom(clazz)) {
                mobType = MobType.VILLAGER;
            } else if (Ambient.class.isAssignableFrom(clazz)) {
                // Bats
                mobType = MobType.ANIMAL;
            } else if (Mob.class.isAssignableFrom(clazz)) {
                mobType = MobType.MONSTER;
            } else {
                System.err.println("No type: " + ent);
                mobType = null;
            }
            Assert.assertNotNull(mobType);
            Set<EntityType> types = map.get(mobType);
            types.add(ent);
        }
        for (MobType mobType : MobType.values()) {
            String line = "    // " + mobType.name().substring(0, 1)
                + mobType.name().substring(1).toLowerCase();
            System.out.println(line);
            out.println(line);
            map.get(mobType).stream()
                .map(Enum::name)
                .sorted()
                .forEach(name -> {
                        String ln;
                        ln = "    " + name + "(MobType." + mobType + "),";
                        System.out.println(ln);
                        out.println(ln);
                    });
        }
        out.close();
    }
}
