package com.cavetale.pocketmob;

import com.cavetale.mytems.Mytem;
import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.item.pocketmob.MobCatcher;
import com.cavetale.mytems.item.pocketmob.PocketMob;
import java.util.EnumMap;
import java.util.Map;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

public final class PocketMobPlugin extends JavaPlugin {
    protected final EventListener eventListener = new EventListener(this);
    protected final PocketMobDelegate pocketMobDelegate = new PocketMobDelegate(this);
    protected final MobCatcherDelegate mobCatcherDelegate = new MobCatcherDelegate(this);
    protected static final Map<EntityType, Mytems> ENTITY_MYTEMS_MAP = new EnumMap<>(EntityType.class);

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(eventListener, this);
        getCommand("pocketmob").setExecutor(new PocketMobCommand(this));
        for (Mytems mytems : Mytems.values()) {
            Mytem mytem = mytems.getMytem();
            if (mytem instanceof PocketMob) {
                PocketMob pocketMob = (PocketMob) mytem;
                pocketMob.setDelegate(pocketMobDelegate);
                ENTITY_MYTEMS_MAP.put(pocketMob.getEntityType(), mytems);
            } else if (mytem instanceof MobCatcher) {
                ((MobCatcher) mytem).setDelegate(mobCatcherDelegate);
            }
        }
    }

    @Override
    public void onDisable() {
        for (Mytems mytems : Mytems.values()) {
            Mytem mytem = mytems.getMytem();
            if (mytem instanceof PocketMob) {
                ((PocketMob) mytem).setDelegate(null);
            } else if (mytem instanceof MobCatcher) {
                ((MobCatcher) mytem).setDelegate(null);
            }
        }
    }
}
