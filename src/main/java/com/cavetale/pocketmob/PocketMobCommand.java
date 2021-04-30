package com.cavetale.pocketmob;

import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.MytemsTag;
import com.cavetale.mytems.item.pocketmob.PocketMobTag;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
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

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String alias, final String[] args) {
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    void openMenu(@NonNull Player player) {
        Merchant merchant = plugin.getServer().createMerchant(Component.text("PocketMob Crafting", PocketMobTag.COLOR_BG));
        ArrayList<MerchantRecipe> recipes = new ArrayList<>();
        for (Mytems mytems : MytemsTag.MOB_CATCHER.toList()) {
            ItemStack i1 = null; // TODO
            ItemStack i2 = null; // TODO
            ItemStack result = null; // TODO
            MerchantRecipe recipe = new MerchantRecipe(result, 999);
            recipe.setExperienceReward(false);
            recipe.setIngredients(Arrays.asList(i1, i2));
            recipes.add(recipe);
        }
        merchant.setRecipes(recipes);
        player.openMerchant(merchant, true);
    }
}
