package ru.customwanderingtrader;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class UpgradeCommand implements CommandExecutor {
    private final CustomWanderingTraderPlugin plugin;
    private final UpgradeGui upgradeGui;

    public UpgradeCommand(CustomWanderingTraderPlugin plugin, UpgradeGui upgradeGui) {
        this.plugin = plugin;
        this.upgradeGui = upgradeGui;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!plugin.bedrockDetector().isBedrockPlayer(player.getUniqueId())) {
            player.sendMessage("§c/upgrade доступен только Bedrock-игрокам через Floodgate.");
            return true;
        }

        upgradeGui.open(player);
        return true;
    }
}
