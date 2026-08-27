package com.rpgidentity.model;

import org.bukkit.ChatColor;

public enum Job {
    LUMBERJACK("Lumberjack", "&a🪓 Lumberjack", "&7Penebang kayu & pengolah hutan Kerajaan Valdora."),
    MINER("Miner", "&e⛏️ Miner", "&7Penambang bijih & penggali kekayaan bawah tanah."),
    FARMER("Farmer", "&6🌾 Farmer", "&7Petani pengelola lahan gandum & hasil bumi.");

    private final String rawName;
    private final String displayName;
    private final String description;

    Job(String rawName, String displayName, String description) {
        this.rawName = rawName;
        this.displayName = displayName;
        this.description = description;
    }

    public String getRawName() {
        return rawName;
    }

    public String getDisplayName() {
        return ChatColor.translateAlternateColorCodes('&', displayName);
    }

    public String getDescription() {
        return ChatColor.translateAlternateColorCodes('&', description);
    }

    public Job next() {
        Job[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static Job fromString(String text) {
        if (text == null) return LUMBERJACK;
        for (Job j : values()) {
            if (j.name().equalsIgnoreCase(text) || j.rawName.equalsIgnoreCase(text)) {
                return j;
            }
        }
        return LUMBERJACK;
    }

    public static String sanitizeJob(String input) {
        return fromString(input).getRawName();
    }
}
