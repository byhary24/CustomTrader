package ru.customwanderingtrader;

import org.bukkit.entity.Player;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class TraderListener implements Listener {
    private final CustomWanderingTraderPlugin plugin;
    private final TradePool tradePool;
    private final Set<UUID> bedrockPlayersWarnedAboutUnsafeBooks = new HashSet<>();

    public TraderListener(CustomWanderingTraderPlugin plugin, TradePool tradePool) {
        this.plugin = plugin;
        this.tradePool = tradePool;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWanderingTraderSpawn(CreatureSpawnEvent event) {
        if (!(event.getEntity() instanceof WanderingTrader trader)) {
            return;
        }

        plugin.getServer().getScheduler().runTask(plugin, () -> trader.setRecipes(tradePool.randomSix()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVillagerAcquireTrade(VillagerAcquireTradeEvent event) {
        if (!(event.getEntity() instanceof WanderingTrader trader)) {
            return;
        }

        plugin.getServer().getScheduler().runTask(plugin, () -> trader.setRecipes(tradePool.randomSix()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMerchantOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        if (event.getView().getType() != InventoryType.MERCHANT) {
            return;
        }

        Inventory top = event.getView().getTopInventory();
        if (!(top instanceof MerchantInventory merchantInventory)) {
            return;
        }
        if (!(merchantInventory.getMerchant() instanceof WanderingTrader trader)) {
            return;
        }
        if (!plugin.bedrockDetector().isBedrockPlayer(player.getUniqueId())) {
            return;
        }

        boolean hasUnsafeBook = trader.getRecipes().stream()
                .map(recipe -> recipe.getResult())
                .anyMatch(this::isUnsafeCasinoBook);

        if (hasUnsafeBook && bedrockPlayersWarnedAboutUnsafeBooks.add(player.getUniqueId())) {
            player.sendMessage("§eУ этого торговца есть нестандартные книги. Для Bedrock используйте §a/upgrade§e, а не ванильную наковальню.");
        }
    }

    private boolean isUnsafeCasinoBook(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        if (!(item.getItemMeta() instanceof EnchantmentStorageMeta meta)) {
            return false;
        }
        Byte tagged = meta.getPersistentDataContainer().get(plugin.casinoBookKey(), PersistentDataType.BYTE);
        if (tagged != null && tagged == (byte) 1) {
            return true;
        }
        return meta.getStoredEnchants().entrySet().stream()
                .anyMatch(entry -> entry.getValue() > entry.getKey().getMaxLevel());
    }
}
