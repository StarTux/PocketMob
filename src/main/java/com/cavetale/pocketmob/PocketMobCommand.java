package com.cavetale.pocketmob;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;

@RequiredArgsConstructor
final class PocketMobCommand implements TabExecutor {
    final PocketMobPlugin plugin;

    static class Wrong extends Exception {
        Wrong(final String msg) {
            super(msg);
        }
    }

    enum Perm {
        POCKETMOB,
        ADMIN;

        public final String node;

        Perm() {
            node = "pocketmob" + name().toLowerCase();
        }

        boolean has(Permissible entity) {
            return entity.hasPermission(node);
        }
    }

    enum Instr {
        BALLS(Perm.ADMIN),
        MENU(Perm.POCKETMOB),
        RELOAD(Perm.ADMIN);

        public final String key;
        public final Perm perm;

        Instr(@NonNull final Perm perm) {
            key = name().toLowerCase();
            this.perm = perm;
        }

        static Instr of(@NonNull String arg) {
            for (Instr instr : Instr.values()) {
                if (arg.equals(instr.key)) return instr;
            }
            return null;
        }
    }

    @Override
    public boolean onCommand(final CommandSender sender,
                             final Command command,
                             final String alias,
                             final String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;
        if (args.length == 0) {
            return printCommandHelp(player);
        }
        Instr instr = Instr.of(args[0]);
        if (instr == null) {
            return printCommandHelp(player);
        }
        try {
            boolean ret =
                onCommand(sender, instr,
                          Arrays.copyOfRange(args, 1, args.length));
            if (!ret) {
                return printSingleCommandHelp(player, instr);
            }
        } catch (Wrong wrong) {
            sender.sendMessage(ChatColor.RED + wrong.getMessage());
        }
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command,
                                      String alias, String[] args) {
        if (args.length == 0) return null;
        final String cmd = args[args.length - 1];
        if (args.length == 1) {
            return complete(cmd,
                            Stream.of(Instr.values())
                            .filter(i -> i.perm.has(sender))
                            .map(i -> i.key));
        }
        return Collections.emptyList();
    }

    private List<String> complete(final String arg,
                                  final Stream<String> opt) {
        return opt.filter(o -> o.startsWith(arg))
            .collect(Collectors.toList());
    }

    boolean onCommand(@NonNull CommandSender sender,
                      @NonNull Instr instr,
                      String[] args) throws Wrong {
        switch (instr) {
        case RELOAD: {
            if (args.length != 0) return false;
            plugin.reloadConfig();
            plugin.resourcesConfig = null;
            sender.sendMessage("PocketMob configs reloaded.");
            return true;
        }
        case BALLS: {
            if (args.length != 0) return false;
            Player player = requirePlayer(sender);
            for (MobType mobType : MobType.values()) {
                ItemStack item = plugin.makePocketBall(mobType);
                item.setAmount(16);
                player.getInventory().addItem(item);
                player.sendMessage(item.getAmount() + " " + mobType.displayName
                                   + " catchers given.");
            }
            return true;
        }
        case MENU: {
            if (args.length != 0) return false;
            return true;
        }
        default: return false;
        }
    }

    boolean printCommandHelp(Player player) {
        if (player == null) return false;
        player.sendMessage("");
        player.sendMessage(""
                           + ChatColor.GRAY + "=== "
                           + ChatColor.BLUE + "PocketMob Command Help"
                           + ChatColor.GRAY + " ===");
        for (Instr instr : Instr.values()) {
            if (!instr.perm.has(player)) continue;
            printCommandHelp(player, instr);
        }
        return true;
    }

    boolean printSingleCommandHelp(@NonNull Player player,
                                   @NonNull Instr instr) {
        if (player == null) return false;
        player.sendMessage("");
        player.sendMessage(ChatColor.GRAY + "Usage:");
        printCommandHelp(player, instr);
        return true;
    }

    void printCommandHelp(@NonNull Player player, @NonNull Instr instr) {
        ComponentBuilder cb = new ComponentBuilder("/")
            .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                                  "/pocketmob " + instr.key))
            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                  TextComponent
                                  .fromLegacyText(ChatColor.YELLOW
                                                  + "/pocketmob " + instr.key)))
            .color(ChatColor.YELLOW)
            .append("pocketmob")
            .append(" ")
            .append(instr.key).color(instr.perm == Perm.ADMIN
                                     ? ChatColor.RED : ChatColor.GOLD)
            .append(" - ").color(ChatColor.DARK_GRAY);
        switch (instr) {
        case RELOAD: cb.append("Reload config"); break;
        case BALLS: cb.append("Spawn in some mob catchers"); break;
        case MENU: cb.append("Open the PocketMob menu"); break;
        default: break;
        }
        cb.color(ChatColor.GRAY);
        player.sendMessage(cb.create());
    }

    Player requirePlayer(@NonNull CommandSender sender) throws Wrong {
        if (!(sender instanceof Player)) {
            throw new Wrong("Player expected!");
        }
        return (Player) sender;
    }
}
