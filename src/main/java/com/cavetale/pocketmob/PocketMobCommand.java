package com.cavetale.pocketmob;

import java.util.ArrayList;
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
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.PlayerInventory;
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
        STACK,
        ADMIN;

        public final String node;

        Perm() {
            node = "pocketmob." + name().toLowerCase();
        }

        boolean has(Permissible entity) {
            return entity.hasPermission(node);
        }
    }

    enum Instr {
        MENU(Perm.POCKETMOB),
        STACK(Perm.STACK),
        BALLS(Perm.ADMIN),
        POTIONS(Perm.ADMIN),
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
                item.setAmount(64);
                player.getInventory().addItem(item);
                player.sendMessage(item.getAmount() + " " + mobType.displayName
                                   + " catchers given.");
            }
            return true;
        }
        case POTIONS: {
            if (args.length != 0) return false;
            Player player = requirePlayer(sender);
            for (MobType mobType : MobType.values()) {
                ItemStack item = mobType.getPotionItem();
                item.setAmount(64);
                player.getInventory().addItem(item);
                item = mobType.getIngredientItem();
                item.setAmount(64);
                player.getInventory().addItem(item);
            }
            player.sendMessage("Spawned in some potions and ingredients.");
            return true;
        }
        case MENU: {
            if (args.length != 0) return false;
            Player player = requirePlayer(sender);
            openMenu(player);
            return true;
        }
        case STACK: {
            if (args.length != 0) return false;
            Player player = requirePlayer(sender);
            stackPotions(player);
            player.sendMessage(ChatColor.GREEN
                               + "Your splash potions were stacked.");
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
            .color(instr.perm == Perm.ADMIN
                   ? ChatColor.DARK_RED : ChatColor.YELLOW)
            .append("pocketmob")
            .append(" ")
            .append(instr.key).color(instr.perm == Perm.ADMIN
                                     ? ChatColor.RED : ChatColor.GOLD)
            .append(" - ").color(ChatColor.DARK_GRAY);
        switch (instr) {
        case RELOAD: cb.append("Reload config"); break;
        case BALLS: cb.append("Spawn in some mob catchers"); break;
        case POTIONS: cb.append("Spawn in some potions and ingredients"); break;
        case MENU: cb.append("Open the PocketMob menu"); break;
        case STACK: cb.append("Stack splash potions"); break;
        default: break;
        }
        cb.color(ChatColor.GRAY).italic(true);
        player.sendMessage(cb.create());
    }

    Player requirePlayer(@NonNull CommandSender sender) throws Wrong {
        if (!(sender instanceof Player)) {
            throw new Wrong("Player expected!");
        }
        return (Player) sender;
    }

    void openMenu(@NonNull Player player) {
        Merchant merchant = plugin.getServer()
            .createMerchant(ChatColor.BLUE + "PocketMob Crafting");
        ArrayList<MerchantRecipe> recipes = new ArrayList<>();
        for (MobType mobType : MobType.values()) {
            ItemStack potion = mobType.getPotionItem();
            ItemStack ingredient = mobType.getIngredientItem();
            ItemStack result = plugin.makePocketBall(mobType);
            MerchantRecipe recipe = new MerchantRecipe(result, 999);
            recipe.setExperienceReward(false);
            recipe.setIngredients(Arrays.asList(potion, ingredient));
            recipes.add(recipe);
        }
        merchant.setRecipes(recipes);
        player.openMerchant(merchant, true);
    }

    void stackPotions(@NonNull Player player) {
        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i += 1) {
            ItemStack it = inv.getItem(i);
            if (it == null || it.getType() != Material.SPLASH_POTION) continue;
            final int max = 64;
            int amount = it.getAmount();
            for (int j = i + 1; amount < max && j < inv.getSize(); j += 1) {
                ItemStack ot = inv.getItem(j);
                if (ot == null) continue;
                if (!ot.isSimilar(it)) continue;
                int otAmount = ot.getAmount();
                int add = Math.min(max - amount, otAmount);
                ot.setAmount(otAmount - add);
                amount += add;
            }
            it.setAmount(amount);
        }
    }
}
