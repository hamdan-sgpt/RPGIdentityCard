package com.rpgidentity.manager;

import com.rpgidentity.RPGIdentityPlugin;
import com.rpgidentity.model.IdentityData;
import com.rpgidentity.model.Race;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IdentityManager {

    private final RPGIdentityPlugin plugin;
    private final Map<UUID, IdentityData> identityMap = new HashMap<>();
    private final File dataFile;
    private FileConfiguration dataConfig;

    public IdentityManager(RPGIdentityPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data/identities.yml");
        loadData();
    }

    public void loadData() {
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Gagal membuat file data/identities.yml: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        identityMap.clear();
        if (dataConfig.contains("identities")) {
            for (String key : dataConfig.getConfigurationSection("identities").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    String path = "identities." + key + ".";
                    IdentityData data = new IdentityData(uuid, dataConfig.getString(path + "nama", "Petualang"));
                    data.setIdNumber(dataConfig.getString(path + "idNumber"));
                    data.setUmur(dataConfig.getInt(path + "umur", 20));
                    data.setProfesi(dataConfig.getString(path + "profesi", "Lumberjack"));
                    data.setRace(Race.fromString(dataConfig.getString(path + "race", "HUMAN")));
                    data.setSignatureHash(dataConfig.getString(path + "signatureHash"));
                    data.setRegistered(dataConfig.getBoolean(path + "registered", false));
                    data.setCustomModelData(dataConfig.getInt(path + "customModelData", 0));

                    identityMap.put(uuid, data);
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    public void saveData() {
        if (dataConfig == null) return;
        for (Map.Entry<UUID, IdentityData> entry : identityMap.entrySet()) {
            String path = "identities." + entry.getKey().toString() + ".";
            IdentityData data = entry.getValue();
            dataConfig.set(path + "idNumber", data.getIdNumber());
            dataConfig.set(path + "nama", data.getNama());
            dataConfig.set(path + "umur", data.getUmur());
            dataConfig.set(path + "profesi", data.getProfesi());
            dataConfig.set(path + "race", data.getRace().name());
            dataConfig.set(path + "signatureHash", data.getSignatureHash());
            dataConfig.set(path + "registered", data.isRegistered());
            dataConfig.set(path + "customModelData", data.getCustomModelData());
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Gagal menyimpan data/identities.yml: " + e.getMessage());
        }
    }

    public IdentityData getIdentity(UUID uuid) {
        return identityMap.get(uuid);
    }

    public IdentityData getOrCreateIdentity(Player player) {
        return identityMap.computeIfAbsent(player.getUniqueId(), uuid -> new IdentityData(uuid, player.getName()));
    }

    public void removeIdentity(UUID uuid) {
        IdentityData old = identityMap.remove(uuid);
        if (old != null && old.getIdNumber() != null) {
            plugin.getVerificationManager().unregisterID(old.getIdNumber());
        }
        if (dataConfig != null) {
            dataConfig.set("identities." + uuid.toString(), null);
            saveData();
        }
    }
}
