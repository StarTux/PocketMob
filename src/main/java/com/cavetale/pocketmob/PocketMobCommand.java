package com.cavetale.pocketmob;

import com.cavetale.core.command.CommandNode;
import com.cavetale.core.command.CommandWarn;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
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
}
