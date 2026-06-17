package ru.customwanderingtrader;

import java.util.UUID;

public final class NoFloodgateBedrockDetector implements BedrockDetector {
    @Override
    public boolean isBedrockPlayer(UUID uuid) {
        return false;
    }
}
