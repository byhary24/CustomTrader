package ru.customwanderingtrader;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

public final class UpgradeGui implements Listener {
    private static final int BASE_SLOT = 0;
    private static final int BOOK_SLOT = 1;
    private static final int RESULT_SLOT = 2;
    private static final int UPGRADE_COST_LEVELS = 30;

    private final CustomWanderingTraderPlugin plugin;

    public UpgradeGui(CustomWanderingTraderPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        UpgradeHolder holder = new UpgradeHolder();
        Inventory inventory = Bukkit.createInventory(holder, 9, "Bedrock Upgrade");
        holder.setInventory(inventory);
        player.openInventory(inventory);
        updateResult(inventory);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        HumanEntity viewer = event.getView().getPlayer();
        if (!(viewer instanceof Player player)) {
            return;
        }
        if (!plugin.bedrockDetector().isBedrockPlayer(player.getUniqueId())) {
            return;
        }

        AnvilInventory inventory = event.getInventory();
        if (isUnsafeStoredBook(inventory.getItem(0)) || isUnsafeStoredBook(inventory.getItem(1))) {
            event.setResult(null);
            player.sendMessage("§cBedrock-клиенту нельзя применять эти книги в ванильной наковальне. Используйте /upgrade.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof UpgradeHolder)) {
            return;
        }

        Inventory top = event.getView().getTopInventory();
        int rawSlot = event.getRawSlot();

        if (event.isShiftClick() && rawSlot >= top.getSize()) {
            event.setCancelled(true);
            return;
        }

        if (rawSlot == RESULT_SLOT) {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player player) {
                completeUpgrade(player, top);
            }
            return;
        }

        if (rawSlot >= 0 && rawSlot < top.getSize() && rawSlot != BASE_SLOT && rawSlot != BOOK_SLOT) {
            event.setCancelled(true);
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> updateResult(top));
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof UpgradeHolder)) {
            return;
        }

        boolean touchesForbiddenTopSlot = event.getRawSlots().stream()
                .anyMatch(slot -> slot >= 0
                        && slot < event.getView().getTopInventory().getSize()
                        && slot != BASE_SLOT
                        && slot != BOOK_SLOT);

        if (touchesForbiddenTopSlot) {
            event.setCancelled(true);
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> updateResult(event.getView().getTopInventory()));
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof UpgradeHolder)) {
            return;
        }

        ItemStack base = event.getInventory().getItem(BASE_SLOT);
        ItemStack book = event.getInventory().getItem(BOOK_SLOT);
        event.getInventory().setItem(RESULT_SLOT, null);
        event.getInventory().setItem(BASE_SLOT, null);
        event.getInventory().setItem(BOOK_SLOT, null);

        if (event.getPlayer() instanceof Player player) {
            giveBack(player, base);
            giveBack(player, book);
        }
    }

    private void completeUpgrade(Player player, Inventory inventory) {
        UpgradePreview preview = buildPreview(inventory.getItem(BASE_SLOT), inventory.getItem(BOOK_SLOT));
        if (preview == null) {
            updateResult(inventory);
            return;
        }

        if (player.getLevel() < preview.costLevels()) {
            player.sendMessage("§cНужно " + preview.costLevels() + " уровней опыта.");
            return;
        }

        player.giveExpLevels(-preview.costLevels());
        inventory.setItem(BASE_SLOT, null);
        consumeOneBook(inventory);
        inventory.setItem(RESULT_SLOT, null);
        giveBack(player, preview.result());
        updateResult(inventory);
    }

    private void updateResult(Inventory inventory) {
        UpgradePreview preview = buildPreview(inventory.getItem(BASE_SLOT), inventory.getItem(BOOK_SLOT));
        inventory.setItem(RESULT_SLOT, preview == null ? null : preview.result());
    }

    private UpgradePreview buildPreview(ItemStack baseItem, ItemStack bookItem) {
        if (baseItem == null || baseItem.getType().isAir() || baseItem.getAmount() != 1
                || bookItem == null || bookItem.getType() != Material.ENCHANTED_BOOK) {
            return null;
        }
        if (!(bookItem.getItemMeta() instanceof EnchantmentStorageMeta bookMeta) || bookMeta.getStoredEnchants().isEmpty()) {
            return null;
        }

        ItemStack result = baseItem.clone();
        result.setAmount(1);
        ItemMeta resultMeta = result.getItemMeta();
        if (resultMeta == null) {
            return null;
        }

        boolean changed = false;
        for (Map.Entry<Enchantment, Integer> entry : bookMeta.getStoredEnchants().entrySet()) {
            Enchantment enchantment = entry.getKey();
            int level = entry.getValue();

            if (!enchantment.canEnchantItem(result)) {
                continue;
            }
            if (hasConflictingEnchant(resultMeta, enchantment)) {
                continue;
            }

            int currentLevel = resultMeta.getEnchantLevel(enchantment);
            if (level > currentLevel) {
                resultMeta.addEnchant(enchantment, level, true);
                changed = true;
            }
        }

        if (!changed) {
            return null;
        }

        resultMeta.getPersistentDataContainer().set(plugin.upgradeResultKey(), PersistentDataType.INTEGER, UPGRADE_COST_LEVELS);
        result.setItemMeta(resultMeta);
        return new UpgradePreview(result, UPGRADE_COST_LEVELS);
    }

    private boolean hasConflictingEnchant(ItemMeta meta, Enchantment enchantment) {
        for (Enchantment existing : meta.getEnchants().keySet()) {
            if (!existing.equals(enchantment) && existing.conflictsWith(enchantment)) {
                return true;
            }
        }
        return false;
    }

    private boolean isUnsafeStoredBook(ItemStack item) {
        if (item == null || item.getType() != Material.ENCHANTED_BOOK || !(item.getItemMeta() instanceof EnchantmentStorageMeta meta)) {
            return false;
        }

        Byte tagged = meta.getPersistentDataContainer().get(plugin.casinoBookKey(), PersistentDataType.BYTE);
        if (tagged != null && tagged == (byte) 1) {
            return true;
        }

        return meta.getStoredEnchants().entrySet().stream()
                .anyMatch(entry -> entry.getValue() > entry.getKey().getMaxLevel());
    }

    private void consumeOneBook(Inventory inventory) {
        ItemStack book = inventory.getItem(BOOK_SLOT);
        if (book == null) {
            return;
        }
        if (book.getAmount() <= 1) {
            inventory.setItem(BOOK_SLOT, null);
            return;
        }
        book.setAmount(book.getAmount() - 1);
        inventory.setItem(BOOK_SLOT, book);
    }

    private void giveBack(Player player, ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return;
        }

        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
        leftover.values().forEach(left -> player.getWorld().dropItemNaturally(player.getLocation(), left));
    }

    private record UpgradePreview(ItemStack result, int costLevels) {
    }
}
