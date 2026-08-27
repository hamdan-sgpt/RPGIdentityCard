package com.rpgidentity.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatInputManager {

    private final Map<UUID, String> awaitingInput = new HashMap<>();

    public void setAwaitingInput(UUID uuid, String field) {
        awaitingInput.put(uuid, field);
    }

    public String getAwaitingInput(UUID uuid) {
        return awaitingInput.get(uuid);
    }

    public boolean isAwaiting(UUID uuid) {
        return awaitingInput.containsKey(uuid);
    }

    public void removeAwaiting(UUID uuid) {
        awaitingInput.remove(uuid);
    }
}
