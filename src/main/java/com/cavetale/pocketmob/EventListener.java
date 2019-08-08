package com.cavetale.pocketmob;

import com.cavetale.itemmarker.CustomItemUseEvent;
import com.cavetale.itemmarker.ItemMarker;
import java.util.Objects;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * We listen for events pertaining to the PocketBall. Mob egg spawning
 * will be handled by vanilla. We do however protect mob spawners from
 * being modified.
 */
final class EventListener implements Listener {
    final PocketMobPlugin plugin;

    EventListener(@NonNull final PocketMobPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    void onPotionSplash(final PotionSplashEvent event) {
        if (!plugin.metadata.has(event.getPotion(), plugin.META_TYPE)) return;
        if (!(event.getPotion().getShooter() instanceof Player)) return;
        final Player player = (Player) event.getPotion().getShooter();
        String typeName = plugin
            .metadata.get(event.getPotion(), plugin.META_TYPE, String.class)
            .orElse(null);
        if (typeName == null) return;
        event.setCancelled(true);
        final MobCatcher mobCatcher = MobCatcher.of(typeName);
        if (mobCatcher == null) return;
        Catch caught = 
            event.getAffectedEntities().stream()
            .map(e -> onMobCatcherHit(player, e, mobCatcher, event))
            .filter(Objects::nonNull)
            .max((a, b) -> Double.compare(a.accuracy, b.accuracy))
            .orElse(null);
        event.getPotion().remove();
        if (caught == null) return;
        // Msg
    }

    private Catch onMobCatcherHit(@NonNull Player player,
                                  @NonNull LivingEntity entity,
                                  @NonNull MobCatcher mobCatcher,
                                  @NonNull PotionSplashEvent event) {
        if (!entity.isValid()) return null;
        final PocketMob pocketMob = PocketMob.of(entity.getType());
        if (pocketMob == null) return null;
        final double intensity = event.getIntensity(entity);
        if (intensity < 0.01) return null;
        Catch caught = new Catch(plugin);
        caught.hit(player, entity, pocketMob, mobCatcher, intensity);
        if (!caught.success) return null;
        if (null == plugin.eggifyAndDrop(entity, pocketMob)) return null;
        entity.remove();
        return caught;
    }

    @EventHandler(ignoreCancelled = true)
    void onCustomItemUse(CustomItemUseEvent event) {
        if (!event.getCustomId().startsWith(plugin.ITEM_PREFIX)) return;
        event.setCancelled(true);
        MobCatcher mobCatcher = MobCatcher.of(event.getCustomId());
        if (mobCatcher == null) return;
        Player player = event.getPlayer();
        if (player.isSneaking()) return;
        ItemStack item = event.getItem();
        item.setAmount(item.getAmount() - 1);
        double speed = plugin.getConfig().getDouble("throwSpeed");
        Vector dir = player.getLocation().getDirection()
            .normalize().multiply(speed);
        ThrownPotion potion = player.launchProjectile(ThrownPotion.class, dir);
        plugin.metadata.set(potion, plugin.META_TYPE, mobCatcher.key);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Action
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        // Player
        Player player = event.getPlayer();
        if (player.isOp()) return;
        if (!event.hasBlock()) return;
        if (!event.hasItem()) return;
        // Block
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (block.getType() != Material.SPAWNER) return;
        // Item
        ItemStack item = event.getItem();
        if (item == null) return;
        if (!item.getType().name().endsWith("_SPAWN_EGG")) return;
        // Cancel
        event.setCancelled(true);
    }
}
