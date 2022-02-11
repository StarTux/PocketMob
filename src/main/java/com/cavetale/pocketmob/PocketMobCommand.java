package com.cavetale.pocketmob;

import com.cavetale.core.command.CommandNode;
import com.cavetale.core.command.CommandWarn;
import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.MytemsCategory;
import com.cavetale.mytems.MytemsTag;
import com.cavetale.mytems.item.pocketmob.PocketMobTag;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;

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

    boolean menu(CommandSender sender, String[] args) {
        if (args.length == 0 && sender instanceof Player) {
            openMenu((Player) sender);
            ((Player) sender).sendMessage("Opening menu");
            return true;
        }
        if (args.length != 1) return false;
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) throw new CommandWarn("Player not found: " + args[0]);
        openMenu(target);
        return true;
    }

    void openMenu(@NonNull Player player) {
        Merchant merchant = plugin.getServer().createMerchant(Component.text("PocketMob Crafting", PocketMobTag.COLOR_BG));
        ArrayList<MerchantRecipe> recipes = new ArrayList<>();
        for (Mytems mytems : MytemsTag.of(MytemsCategory.MOB_CATCHERS).getMytems()) {
            List<ItemStack> ingredients = new ArrayList<>(2);
            if (mytems != Mytems.MOB_CATCHER) {
                ingredients.add(Mytems.MOB_CATCHER.createItemStack());
            }
            switch (mytems) {
            case MOB_CATCHER:
                ingredients.add(new ItemStack(Material.MAGMA_CREAM));
                ingredients.add(new ItemStack(Material.GUNPOWDER));
                break;
            case MONSTER_CATCHER:
                ingredients.add(new ItemStack(Material.ROTTEN_FLESH));
                break;
            case PET_CATCHER:
                ingredients.add(new ItemStack(Material.BONE));
                break;
            case ANIMAL_CATCHER:
                ingredients.add(new ItemStack(Material.WHEAT));
                break;
            case VILLAGER_CATCHER:
                ingredients.add(new ItemStack(Material.EMERALD));
                break;
            case FISH_CATCHER:
                ingredients.add(new ItemStack(Material.SLIME_BALL));
                break;
            default: continue;
            }
            ItemStack result = mytems.createItemStack();
            MerchantRecipe recipe = new MerchantRecipe(result, 999);
            recipe.setExperienceReward(false);
            recipe.setIngredients(ingredients);
            recipes.add(recipe);
        }
        merchant.setRecipes(recipes);
        player.openMerchant(merchant, true);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, SoundCategory.MASTER, 0.5f, 1.0f);
    }
}
