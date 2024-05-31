package com.cavetale.pocketmob;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.junit.Assert;
import org.junit.Test;

public final class PocketMobTest {
    private static final int CUSTOM_MODEL_DATA = 908301;
    private final List<EntityType> types = new ArrayList<>();

    /**
     * This is used to update the Mytems enum every update.
     */
    public void dump() {
        makeTypes();
        System.out.println("\ncom.cavetale.mytems.Mytems\n");
        dumpMytems();
        System.out.println("\ncom.cavetale.mytems.item.pocketmob.PocketMobType\n");
        dumpPocketMobTypes();
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
            Assert.assertTrue(entityType + ": " + mobType + " != " + guess, mobType == guess);
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
        for (EntityType entityType : EntityType.values()) {
            final MobType mobType = MobType.mobTypeOf(entityType);
            if (mobType == null) continue;
            types.add(entityType);
        }
        Collections.sort(types, (a, b) -> a.name().compareTo(b.name()));
    }

    private void dumpMytems() {
        for (EntityType entityType : types) {
            final MobType mobType = MobType.mobTypeOf(entityType);
            if (mobType == null) continue;
            Material material;
            try {
                material = Material.valueOf(entityType.name() + "_SPAWN_EGG");
            } catch (IllegalArgumentException iae) {
                System.out.println("// Irregular");
                material = getIrregularSpawnEgg(entityType);
            }
            System.out.println("POCKET_" + entityType
                               + "("
                               + "PocketMob.class"
                               + ", " + material
                               + ", " + CUSTOM_MODEL_DATA
                               + ", (char) 0"
                               + ", POCKET_MOB"
                               + "),");
        }
    }

    private void dumpPocketMobTypes() {
        for (EntityType entityType : types) {
            final MobType mobType = MobType.mobTypeOf(entityType);
            if (mobType == null) continue;
            System.out.println(entityType
                               + "("
                               + "Mytems." + "POCKET_" + entityType
                               + ", EntityType." + entityType
                               + "),");
        }
    }

    private Material getIrregularSpawnEgg(EntityType type) {
        switch (type) {
        case ILLUSIONER:
            return Material.VINDICATOR_SPAWN_EGG;
        case GIANT:
            return Material.ZOMBIE_SPAWN_EGG;
        default:
            throw new IllegalStateException("No egg: " + type);
        }
    }
}
