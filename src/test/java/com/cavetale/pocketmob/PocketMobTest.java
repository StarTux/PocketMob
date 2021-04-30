package com.cavetale.pocketmob;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.junit.Test;

public final class PocketMobTest {
    @Test
    public void test() throws Exception { }

    void dumpNonLivingEntities() {
        for (EntityType ent : EntityType.values()) {
            Class<?> clazz = ent.getEntityClass();
            if (clazz == null) continue;
            if (!LivingEntity.class.isAssignableFrom(clazz)) continue;
            System.err.println("Not living entity: " + ent);
        }
    }

    void dumpMytems() {
        int customModelData = 908301;
        List<EntityType> types = new ArrayList<>();
        for (EntityType ent : EntityType.values()) {
            switch (ent) {
            case ARMOR_STAND:
            case PLAYER:
            case UNKNOWN:
                continue;
            default:
                break;
            }
            Class<?> clazz = ent.getEntityClass();
            if (clazz == null) continue;
            if (!LivingEntity.class.isAssignableFrom(clazz)) continue;
            types.add(ent);
        }
        Collections.sort(types, (a, b) -> a.name().compareTo(b.name()));
        //System.out.println(types.stream().map(s -> "Mytems.POCKET_" + s).collect(Collectors.joining(", ")));
        for (int i = 0; i < types.size(); i += 1) {
            EntityType type = types.get(i);
            Material material;
            try {
                material = Material.valueOf(type.name() + "_SPAWN_EGG");
            } catch (IllegalArgumentException iae) {
                material = getIrregularSpawnEgg(type);
            }
            System.out.println("POCKET_" + type + "(mytems -> new PocketMob(mytems, EntityType." + type
                               + "), Material." + material + ", " + customModelData + "),");
        }
    }

    Material getIrregularSpawnEgg(EntityType type) {
        switch (type) {
        case ILLUSIONER:
            return Material.VINDICATOR_SPAWN_EGG;
        case GIANT:
            return Material.ZOMBIE_SPAWN_EGG;
        case ENDER_DRAGON:
            return Material.ENDERMAN_SPAWN_EGG;
        case WITHER:
            return Material.WITHER_SKELETON_SPAWN_EGG;
        case MUSHROOM_COW:
            return Material.MOOSHROOM_SPAWN_EGG;
        case SNOWMAN:
            return Material.POLAR_BEAR_SPAWN_EGG;
        case IRON_GOLEM:
            return Material.WOLF_SPAWN_EGG;
        default:
            throw new IllegalStateException("No egg: " + type);
        }
    }

    void printEntityTypes(PrintStream out) throws Exception {
        for (MobType mobType : MobType.values()) {
            String line = "    // " + mobType.name().substring(0, 1)
                + mobType.name().substring(1).toLowerCase();
            out.println(line);
            mobType.entityTypes.stream()
                .map(Enum::name)
                .sorted()
                .forEach(name -> {
                        String ln;
                        ln = "    " + name + "(MobType." + mobType + "),";
                        out.println(ln);
                    });
        }
        out.close();
    }
}
