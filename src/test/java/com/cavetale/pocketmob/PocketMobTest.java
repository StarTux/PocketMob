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
    private final List<EntityType> types = new ArrayList<>();

    public void dump() throws Exception {
        printEntityTypes(System.out);
        makeTypes();
        // System.out.print("\n// Non Living\n\n");
        // dumpNonLivingEntities();
        System.out.print("\n// Types\n\n");
        dumpTypes();
        System.out.print("\n// Mytems\n\n");
        dumpMytems();
    }

    @Test
    public void test() throws Exception {
        for (EntityType entityType : EntityType.values()) {
            if (entityType.getEntityClass() == null) continue;
            final MobType mobType = MobType.mobTypeOf(entityType);
            final MobType guess;
            try {
                guess = MobType.guessMobType(entityType);
            } catch (IllegalStateException ise) {
                continue;
            }
            assert mobType == guess : entityType + ": " + mobType + " != " + guess;
        }
    }

    private void dumpNonLivingEntities() {
        for (EntityType ent : EntityType.values()) {
            Class<?> clazz = ent.getEntityClass();
            if (clazz == null) continue;
            if (LivingEntity.class.isAssignableFrom(clazz)) continue;
            System.err.println("Not living entity: " + ent);
        }
    }

    private void printEntityTypes(PrintStream out) throws Exception {
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

    private List<String> sorted(Collection<EntityType> typeCollection) {
        List<String> list = new ArrayList<>(typeCollection.size());
        for (EntityType type : typeCollection) list.add(type.name());
        Collections.sort(list);
        return list;
    }

    private void makeTypes() {
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
    }

    private void dumpTypes() {
        for (int i = 0; i < types.size(); i += 1) {
            EntityType type = types.get(i);
            Material material;
            try {
                material = Material.valueOf(type.name() + "_SPAWN_EGG");
            } catch (IllegalArgumentException iae) {
                System.out.println("// Irregular");
                material = getIrregularSpawnEgg(type);
            }
            System.out.println("POCKET_" + type
                               + "("
                               + "PocketMob::new"
                               + ", Material." + material
                               + ", " + CUSTOM_MODEL_DATA
                               + ", (char) 0"
                               + ", POCKET_MOB"
                               + "),");
        }
    }

    private void dumpMytems() {
        for (int i = 0; i < types.size(); i += 1) {
            EntityType type = types.get(i);
            System.out.println(type
                               + "("
                               + "Mytems." + "POCKET_" + type
                               + ", EntityType." + type
                               + "),");
        }
    }

    private Material getIrregularSpawnEgg(EntityType type) {
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
}
