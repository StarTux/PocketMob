package com.cavetale.pocketmob;

import com.cavetale.itemmarker.CustomItemUseEvent;
import com.cavetale.itemmarker.ItemMarker;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
final class EventListener implements Listener {
    final PocketMobPlugin plugin;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    void onPotionSplash(final PotionSplashEvent event) {
        if (!plugin.metadata.has(event.getPotion(), plugin.META_TYPE)) return;
        if (!(event.getPotion().getShooter() instanceof Player)) return;
        final Player player = (Player) event.getPotion().getShooter();
        String mobName = plugin
            .metadata.get(event.getPotion(), plugin.META_TYPE, String.class)
            .orElse(null);
        if (mobName == null) return;
        MobType mobType = MobType.of(mobName);
        if (mobType == null) return;
        event.setCancelled(true);
        for (LivingEntity entity : event.getAffectedEntities()) {
            if (!entity.isValid()) continue;
            PocketMob pocketMob = PocketMob.of(entity.getType());
            if (pocketMob == null) continue;
            double intensity = event.getIntensity(entity);
            plugin.onPocketBallHit(player, entity, pocketMob, intensity);
        }
        event.getPotion().remove();
    }

    @EventHandler(ignoreCancelled = true)
    void onCustomItemUse(CustomItemUseEvent event) {
        if (!event.getCustomId().equals(plugin.ITEM_TAG)) return;
        event.setCancelled(true);
        Player player = event.getPlayer();
        if (player.isSneaking()) return;
        ItemStack item = event.getItem();
        String mobName = ItemMarker
            .getMarker(item, plugin.ITEM_MOB, String.class)
            .orElse(null);
        if (mobName == null) return;
        MobType mobType = MobType.of(mobName);
        if (mobType == null) return;
        item.setAmount(item.getAmount() - 1);
        double speed = plugin.getConfig().getDouble("throwSpeed");
        Vector dir = player.getLocation().getDirection()
            .normalize().multiply(speed);
        ThrownPotion potion = player.launchProjectile(ThrownPotion.class, dir);
        plugin.metadata.set(potion, plugin.META_TYPE, mobType.key);
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
