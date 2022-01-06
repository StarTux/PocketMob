package com.cavetale.pocketmob;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.junit.Test;

public final class PocketMobTest {
    private static final int CUSTOM_MODEL_DATA = 908301;

    @Test
    public void test() throws Exception {
        printEntityTypes(System.out);
        dumpEnum(true);
    }

    void dumpNonLivingEntities() {
        for (EntityType ent : EntityType.values()) {
            Class<?> clazz = ent.getEntityClass();
            if (clazz == null) continue;
            if (!LivingEntity.class.isAssignableFrom(clazz)) continue;
            System.err.println("Not living entity: " + ent);
        }
    }

    void dumpEnum(boolean pocketMobType) {
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
        if (!pocketMobType) {
            for (int i = 0; i < types.size(); i += 1) {
                EntityType type = types.get(i);
                Material material;
                try {
                    material = Material.valueOf(type.name() + "_SPAWN_EGG");
                } catch (IllegalArgumentException iae) {
                    material = getIrregularSpawnEgg(type);
                }
                System.out.println("POCKET_" + type
                                   + "("
                                   + "PocketMob::new"
                                   + ", Material." + material
                                   + ", " + CUSTOM_MODEL_DATA
                                   + ", (char) 0"
                                   + ", Category.POCKET_MOB"
                                   + "),");
            }
        } else {
            for (int i = 0; i < types.size(); i += 1) {
                EntityType type = types.get(i);
                System.out.println(type
                                   + "("
                                   + "Mytems." + "POCKET_" + type
                                   + ", EntityType." + type
                                   + "),");
            }
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

    List<String> sorted(Collection<EntityType> types) {
        List<String> list = new ArrayList<>(types.size());
        for (EntityType type : types) list.add(type.name());
        Collections.sort(list);
        return list;
    }

    void printEntityTypes(PrintStream out) throws Exception {
        for (String entityType : sorted(MobType.NO_MOB_TYPE)) {
            out.println("    case " + entityType + ":");
        }
        out.println("        return null;");
        for (MobType mobType : MobType.values()) {
            for (String entityType : sorted(mobType.entityTypes)) {
                out.println("   case " + entityType + ":");
            }
            out.println("        return MobType." + mobType + ";");
        }
    }
}
