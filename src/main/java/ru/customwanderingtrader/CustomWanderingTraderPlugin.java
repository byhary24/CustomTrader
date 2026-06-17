package ru.customwanderingtrader;

import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class CustomWanderingTraderPlugin extends JavaPlugin {
    private NamespacedKey casinoBookKey;
    private NamespacedKey upgradeResultKey;
    private BedrockDetector bedrockDetector;
    private TradePool tradePool;

    @Override
    public void onEnable() {
        this.casinoBookKey = new NamespacedKey(this, "casino_unsafe_book");
        this.upgradeResultKey = new NamespacedKey(this, "upgrade_result");
        this.bedrockDetector = createBedrockDetector();
        this.tradePool = new TradePool(this);

        TraderListener traderListener = new TraderListener(this, tradePool);
        UpgradeGui upgradeGui = new UpgradeGui(this);

        getServer().getPluginManager().registerEvents(traderListener, this);
        getServer().getPluginManager().registerEvents(upgradeGui, this);

        PluginCommand upgradeCommand = Objects.requireNonNull(getCommand("upgrade"), "upgrade command is missing from plugin.yml");
        upgradeCommand.setExecutor(new UpgradeCommand(this, upgradeGui));

        getLogger().info("CustomWanderingTrader enabled. Floodgate hook: " + (bedrockDetector instanceof FloodgateBedrockDetector));
    }

    public NamespacedKey casinoBookKey() {
        return casinoBookKey;
    }

    public NamespacedKey upgradeResultKey() {
        return upgradeResultKey;
    }

    public BedrockDetector bedrockDetector() {
        return bedrockDetector;
    }

    private BedrockDetector createBedrockDetector() {
        if (getServer().getPluginManager().getPlugin("floodgate") == null) {
            getLogger().warning("Floodgate was not found. /upgrade will be unavailable because no Bedrock players can be detected.");
            return new NoFloodgateBedrockDetector();
        }

        try {
            Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            return new FloodgateBedrockDetector();
        } catch (ClassNotFoundException ex) {
            getLogger().warning("Floodgate plugin is present, but Floodgate API is not available.");
            return new NoFloodgateBedrockDetector();
        }
    }
}
