package com.cavetale.pocketmob;

import com.cavetale.mytems.item.pocketmob.PocketMob;
import com.cavetale.worldmarker.entity.EntityMarker;
import lombok.RequiredArgsConstructor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

@RequiredArgsConstructor
public final class PocketMobDelegate implements PocketMob.Delegate {
    protected final PocketMobPlugin plugin;

    @Override
    public void onPlayerRightClick(PocketMob pocketMob, PlayerInteractEvent event, Player player, ItemStack item) {
        if (!player.hasPermission("pocketmob.catch")) return;
        Vector velocity = player.getLocation().getDirection().normalize();
        Egg projectile = player.launchProjectile(Egg.class, velocity);
        if (projectile == null) return;
        projectile.setPersistent(true);
        ItemStack thrownItem = item.clone();
        thrownItem.setAmount(1);
        if (player.getGameMode() != GameMode.CREATIVE) {
            item.subtract(1);
        }
        projectile.setItem(thrownItem);
        EntityMarker.setId(projectile, pocketMob.getKey().id);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EGG_THROW, SoundCategory.PLAYERS, 1.0f, 1.0f);
    }
}
