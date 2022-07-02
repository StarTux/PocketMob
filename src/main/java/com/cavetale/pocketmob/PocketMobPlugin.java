package com.cavetale.pocketmob;

import com.cavetale.mytems.Mytem;
import com.cavetale.mytems.Mytems;
import com.cavetale.mytems.MytemsCategory;
import com.cavetale.mytems.MytemsTag;
import com.cavetale.mytems.item.pocketmob.MobCatcher;
import com.cavetale.mytems.item.pocketmob.PocketMob;
import com.cavetale.mytems.item.pocketmob.PocketMobTag;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import static net.kyori.adventure.text.Component.text;

public final class PocketMobPlugin extends JavaPlugin {
    @Getter protected static PocketMobPlugin instance;
    protected final EventListener eventListener = new EventListener(this);
    protected final PocketMobDelegate pocketMobDelegate = new PocketMobDelegate(this);
    protected final MobCatcherDelegate mobCatcherDelegate = new MobCatcherDelegate(this);
    protected static final Map<EntityType, Mytems> ENTITY_MYTEMS_MAP = new EnumMap<>(EntityType.class);

    @Override
    public void onEnable() {
        instance = this;
        getServer().getPluginManager().registerEvents(eventListener, this);
        new PocketMobCommand(this).enable();
        for (Mytems mytems : Mytems.values()) {
            Mytem mytem = mytems.getMytem();
            if (mytem instanceof PocketMob) {
                PocketMob pocketMob = (PocketMob) mytem;
                pocketMob.setDelegate(pocketMobDelegate);
                ENTITY_MYTEMS_MAP.put(pocketMob.getEntityType(), mytems);
            } else if (mytem instanceof MobCatcher) {
                ((MobCatcher) mytem).setDelegate(mobCatcherDelegate);
            }
        }
    }

    @Override
    public void onDisable() {
        for (Mytems mytems : Mytems.values()) {
            Mytem mytem = mytems.getMytem();
            if (mytem instanceof PocketMob) {
                ((PocketMob) mytem).setDelegate(null);
            } else if (mytem instanceof MobCatcher) {
                ((MobCatcher) mytem).setDelegate(null);
            }
        }
    }

    public static void openMenu(Player player) {
        Merchant merchant = Bukkit.createMerchant(text("PocketMob Crafting", PocketMobTag.COLOR_BG));
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
