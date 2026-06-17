package ru.customwanderingtrader;

import org.geysermc.floodgate.api.FloodgateApi;

import java.util.UUID;

public final class FloodgateBedrockDetector implements BedrockDetector {
    @Override
    public boolean isBedrockPlayer(UUID uuid) {
        return FloodgateApi.getInstance().isFloodgatePlayer(uuid);
    }
}
