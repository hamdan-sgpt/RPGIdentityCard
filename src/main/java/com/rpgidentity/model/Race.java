package com.rpgidentity.model;

import org.bukkit.ChatColor;

public enum Race {
    HUMAN("Human", "&f👤 Human", "&fBangsa Manusia biasa yang serbabisa & adaptif.", 900001),
    ELF("Elf", "&a🧝 Elf", "&aKaum Bangsa Elf yang lincah, anggun, & berumur panjang.", 900002),
    DWARF("Dwarf", "&6⛏️ Dwarf", "&6Penempa tangguh dari pegunungan batu bawah tanah.", 900003),
    DEMON("Demon", "&c👿 Demon", "&cBangsa Iblis bertanduk dengan kekuatan magis kegelapan.", 900004);

    private final String rawName;
    private final String displayName;
    private final String description;
    private final int customModelData;

    Race(String rawName, String displayName, String description, int customModelData) {
        this.rawName = rawName;
        this.displayName = displayName;
        this.description = description;
        this.customModelData = customModelData;
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

    public int getCustomModelData() {
        return customModelData;
    }

    public Race next() {
        Race[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static Race fromString(String text) {
        if (text == null) return HUMAN;
        for (Race race : values()) {
            if (race.name().equalsIgnoreCase(text) || race.rawName.equalsIgnoreCase(text)) {
                return race;
            }
        }
        return HUMAN;
    }
}
