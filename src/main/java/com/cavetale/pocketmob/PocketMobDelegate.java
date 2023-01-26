package com.cavetale.pocketmob;

import com.cavetale.mytems.item.pocketmob.PocketMob;
import com.cavetale.worldmarker.entity.EntityMarker;
import io.papermc.paper.event.block.BlockPreDispenseEvent;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

@RequiredArgsConstructor
public final class PocketMobDelegate implements PocketMob.Delegate {
    protected final PocketMobPlugin plugin;

    @Override
    public void onPlayerRightClick(PocketMob pocketMob, PlayerInteractEvent event, Player player, ItemStack item) {
        if (player.getGameMode() == GameMode.SPECTATOR) return;
        if (!player.hasPermission("pocketmob.catch")) return;
        Vector velocity = player.getLocation().getDirection().normalize();
        Egg projectile = player.launchProjectile(Egg.class, velocity);
        if (projectile == null || projectile.isDead()) return;
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

    @Override
    public void onBlockPreDispense(PocketMob pocketMob, BlockPreDispenseEvent event) {
        BlockData blockData = event.getBlock().getBlockData();
        if (!(blockData instanceof Directional)) return;
        Directional directional = (Directional) blockData;
        ItemStack itemStack = event.getItemStack();
        ItemStack dispensed = itemStack.clone();
        dispensed.setAmount(1);
        BlockFace direction = directional.getFacing();
        Block dropBlock = event.getBlock().getRelative(direction);
        if (!dropBlock.isPassable()) return;
        Location dropAt = dropBlock.getLocation().add(0.5, 0.5, 0.5)
            .subtract(direction.getDirection().multiply(0.49));
        Vector velocity = direction.getDirection().multiply(0.5);
        BlockDispenseEvent event2 = new BlockDispenseEvent(event.getBlock(), dispensed, velocity);
        Bukkit.getPluginManager().callEvent(event2);
        if (event2.isCancelled()) return;
        Egg projectile = dropAt.getWorld().spawn(dropAt, Egg.class, e -> {
                e.setVelocity(velocity);
                e.setItem(dispensed);
            });
        if (projectile == null) return;
        EntityMarker.setId(projectile, pocketMob.getKey().id);
        itemStack.subtract(1);
        dropAt.getWorld().playSound(dropAt, Sound.BLOCK_DISPENSER_DISPENSE, SoundCategory.BLOCKS, 0.5f, 1.0f);
        dropAt.getWorld().spawnParticle(Particle.SMOKE_NORMAL, dropAt, 4, 0, 0, 0, 0.075);
    }
}
