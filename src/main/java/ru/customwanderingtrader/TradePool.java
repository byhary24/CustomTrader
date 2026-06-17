package ru.customwanderingtrader;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.OminousBottleMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public final class TradePool {
    private static final int LIMITED_ONCE = 1;
    private static final int LIMITED_THREE = 3;
    private static final int UNLIMITED = 9999;

    private final CustomWanderingTraderPlugin plugin;
    private final List<Supplier<MerchantRecipe>> entries;

    public TradePool(CustomWanderingTraderPlugin plugin) {
        this.plugin = plugin;
        this.entries = buildEntries();
    }

    public List<MerchantRecipe> randomSix() {
        List<Supplier<MerchantRecipe>> shuffled = new ArrayList<>(entries);
        Collections.shuffle(shuffled);

        List<MerchantRecipe> selected = new ArrayList<>(6);
        for (int i = 0; i < Math.min(6, shuffled.size()); i++) {
            selected.add(shuffled.get(i).get());
        }
        return selected;
    }

    private List<Supplier<MerchantRecipe>> buildEntries() {
        List<Supplier<MerchantRecipe>> pool = new ArrayList<>();

        Material[] creeperDiscs = {
                Material.MUSIC_DISC_13,
                Material.MUSIC_DISC_CAT,
                Material.MUSIC_DISC_BLOCKS,
                Material.MUSIC_DISC_CHIRP,
                Material.MUSIC_DISC_FAR,
                Material.MUSIC_DISC_MALL,
                Material.MUSIC_DISC_MELLOHI,
                Material.MUSIC_DISC_STAL,
                Material.MUSIC_DISC_STRAD,
                Material.MUSIC_DISC_WARD,
                Material.MUSIC_DISC_11,
                Material.MUSIC_DISC_WAIT
        };

        for (Material disc : creeperDiscs) {
            pool.add(() -> recipe(item(disc), item(Material.EXPERIENCE_BOTTLE, 8), LIMITED_ONCE));
        }

        pool.add(() -> recipe(item(Material.MUSIC_DISC_OTHERSIDE), item(Material.EMERALD, 32), LIMITED_ONCE));
        pool.add(() -> recipe(item(Material.MUSIC_DISC_TEARS), bundle(Material.BUNDLE,
                item(Material.EXPERIENCE_BOTTLE, 16),
                item(Material.BLAZE_POWDER, 8)), LIMITED_ONCE));
        pool.add(() -> recipe(item(Material.MUSIC_DISC_PIGSTEP), bundle(Material.RED_BUNDLE,
                item(Material.NETHERITE_SCRAP, 3),
                item(Material.GOLD_BLOCK, 8)), LIMITED_ONCE));
        pool.add(() -> recipe(item(Material.MUSIC_DISC_RELIC), bundle(Material.BUNDLE,
                item(Material.DIAMOND, 32),
                item(Material.NETHERITE_SCRAP, 2)), LIMITED_ONCE));
        pool.add(() -> recipe(item(Material.MUSIC_DISC_5), storedBook(Enchantment.EFFICIENCY, 7), LIMITED_ONCE));
        pool.add(() -> recipe(item(Material.MUSIC_DISC_PRECIPICE), ominousBottle(4), LIMITED_ONCE));
        pool.add(() -> recipe(item(Material.MUSIC_DISC_LAVA_CHICKEN), bundle(Material.BUNDLE,
                item(Material.COOKED_CHICKEN, 64)), LIMITED_ONCE));
        pool.add(() -> recipe(item(Material.MUSIC_DISC_CREATOR), storedBook(Enchantment.FORTUNE, 4), LIMITED_ONCE));
        pool.add(() -> recipe(item(Material.MUSIC_DISC_CREATOR_MUSIC_BOX), storedBook(Enchantment.FEATHER_FALLING, 5), LIMITED_ONCE));
        pool.add(() -> recipe(item(Material.PIGLIN_HEAD), bundle(Material.YELLOW_BUNDLE,
                item(Material.GOLD_BLOCK, 52),
                item(Material.NETHERITE_SCRAP, 7)), LIMITED_ONCE));
        pool.add(() -> recipe(item(Material.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE), bundle(Material.BUNDLE,
                item(Material.TOTEM_OF_UNDYING, 2)), LIMITED_ONCE));
        pool.add(() -> recipe(item(Material.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE), item(Material.GOLDEN_APPLE, 16), LIMITED_ONCE));
        pool.add(() -> recipe(item(Material.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE), item(Material.BEACON), LIMITED_ONCE));

        pool.add(() -> recipe(item(Material.ZOMBIE_HEAD), item(Material.IRON_BLOCK, 10), LIMITED_THREE));
        pool.add(() -> recipe(item(Material.SKELETON_SKULL, 3), enchantedBow(), LIMITED_THREE));
        pool.add(() -> recipe(item(Material.WITHER_SKELETON_SKULL), item(Material.DIAMOND, 8), LIMITED_THREE));
        pool.add(() -> recipe(item(Material.CREEPER_HEAD), bundle(Material.GRAY_BUNDLE,
                item(Material.TNT, 16),
                item(Material.FIREWORK_ROCKET, 16),
                item(Material.GUNPOWDER, 32)), LIMITED_THREE));
        pool.add(() -> recipe(item(Material.DRAGON_HEAD), item(Material.DIAMOND, 16), LIMITED_THREE));

        pool.add(() -> recipe(item(Material.DEEPSLATE_COAL_ORE), item(Material.COAL_BLOCK, 16), UNLIMITED));
        pool.add(() -> recipe(item(Material.DEEPSLATE_EMERALD_ORE), item(Material.EMERALD_BLOCK, 10), UNLIMITED));
        pool.add(() -> recipe(item(Material.ECHO_SHARD, 8), item(Material.EXPERIENCE_BOTTLE, 64), UNLIMITED));
        pool.add(() -> recipe(item(Material.RECOVERY_COMPASS), storedBook(Enchantment.SWIFT_SNEAK, 4), UNLIMITED));
        pool.add(() -> recipe(item(Material.HEART_OF_THE_SEA), storedBook(Enchantment.RESPIRATION, 4), UNLIMITED));
        pool.add(() -> recipe(item(Material.NAUTILUS_SHELL, 16), item(Material.TRIDENT), UNLIMITED));
        pool.add(() -> recipe(item(Material.AMETHYST_SHARD, 32), item(Material.EXPERIENCE_BOTTLE, 8), UNLIMITED));
        pool.add(() -> recipe(item(Material.GLISTERING_MELON_SLICE, 16), item(Material.GOLDEN_APPLE), UNLIMITED));
        pool.add(() -> recipe(item(Material.GHAST_TEAR, 2), item(Material.CRYING_OBSIDIAN), UNLIMITED));

        for (Material material : Material.values()) {
            String name = material.name().toUpperCase(Locale.ROOT);
            if (!name.contains("SMITHING_TEMPLATE")) {
                continue;
            }
            if (material == Material.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE || material == Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE) {
                continue;
            }
            pool.add(() -> recipe(item(material), item(Material.DIAMOND, 5), UNLIMITED));
        }

        return List.copyOf(pool);
    }

    private MerchantRecipe recipe(ItemStack ingredient, ItemStack result, int maxUses) {
        MerchantRecipe recipe = new MerchantRecipe(result, maxUses);
        recipe.addIngredient(ingredient);
        recipe.setMaxUses(maxUses);
        recipe.setExperienceReward(false);
        recipe.setVillagerExperience(0);
        recipe.setPriceMultiplier(0.0f);
        return recipe;
    }

    private ItemStack item(Material material) {
        return new ItemStack(material);
    }

    private ItemStack item(Material material, int amount) {
        return new ItemStack(material, amount);
    }

    private ItemStack bundle(Material material, ItemStack... contents) {
        ItemStack bundle = new ItemStack(material);
        bundle.editMeta(BundleMeta.class, meta -> {
            for (ItemStack content : contents) {
                meta.addItem(content);
            }
        });
        return bundle;
    }

    private ItemStack storedBook(Enchantment enchantment, int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        book.editMeta(EnchantmentStorageMeta.class, meta -> {
            meta.addStoredEnchant(enchantment, level, true);
            meta.getPersistentDataContainer().set(plugin.casinoBookKey(), PersistentDataType.BYTE, (byte) 1);
        });
        return book;
    }

    private ItemStack ominousBottle(int amplifier) {
        ItemStack bottle = new ItemStack(Material.OMINOUS_BOTTLE);
        bottle.editMeta(OminousBottleMeta.class, meta -> meta.setAmplifier(amplifier));
        return bottle;
    }

    private ItemStack enchantedBow() {
        ItemStack bow = new ItemStack(Material.BOW);
        bow.editMeta(meta -> {
            meta.addEnchant(Enchantment.FLAME, 1, true);
            meta.addEnchant(Enchantment.INFINITY, 1, true);
            meta.addEnchant(Enchantment.MENDING, 1, true);
        });
        return bow;
    }
}
