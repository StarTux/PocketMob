package com.cavetale.pocketmob;

import com.cavetale.core.command.CommandNode;
import com.cavetale.core.command.CommandWarn;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
final class PocketMobCommand implements TabExecutor {
    protected final PocketMobPlugin plugin;
    protected CommandNode rootNode;

    protected void enable() {
        rootNode = new CommandNode("pocketmob");
        rootNode.addChild("menu").arguments("[player]")
            .description("Open the PocketMob shop menu")
            .senderCaller(this::menu);
        rootNode.addChild("dump").denyTabCompletion()
            .description("Dump Pocket Mob types enum")
            .senderCaller(this::dump);
        plugin.getCommand("pocketmob").setExecutor(this);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String alias, final String[] args) {
        return rootNode.call(sender, command, alias, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return rootNode.complete(sender, command, alias, args);
    }

    private boolean menu(CommandSender sender, String[] args) {
        if (args.length == 0 && sender instanceof Player) {
            plugin.openMenu((Player) sender);
            ((Player) sender).sendMessage("Opening menu");
            return true;
        }
        if (args.length != 1) return false;
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) throw new CommandWarn("Player not found: " + args[0]);
        plugin.openMenu(target);
        return true;
    }

    private void dump(CommandSender sender) {
        for (EntityType entityType : EntityType.values()) {
            final MobType mobType = MobType.mobTypeOf(entityType);
            if (mobType == null) continue;
            final Color layer0 = Bukkit.getUnsafe().getSpawnEggLayerColor(entityType, 0);
            final Color layer1 = Bukkit.getUnsafe().getSpawnEggLayerColor(entityType, 1);
            System.out.println(entityType
                               + "("
                               + "Mytems." + "POCKET_" + entityType
                               + ", EntityType." + entityType
                               + ", 0x" + Integer.toHexString(layer0.asARGB())
                               + ", 0x" + Integer.toHexString(layer1.asARGB())
                               + "),");
        }
    }
}
