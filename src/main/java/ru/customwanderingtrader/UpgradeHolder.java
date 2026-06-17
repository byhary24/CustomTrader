package ru.customwanderingtrader;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class UpgradeHolder implements InventoryHolder {
    private Inventory inventory;

    void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
