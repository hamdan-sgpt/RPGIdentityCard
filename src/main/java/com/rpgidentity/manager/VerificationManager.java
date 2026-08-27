package com.rpgidentity.manager;

import com.rpgidentity.RPGIdentityPlugin;
import com.rpgidentity.model.IdentityData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class VerificationManager {

    private final RPGIdentityPlugin plugin;
    private final Map<String, String> registeredIdRegistry = new HashMap<>(); // ID -> SignatureHash
    private final File registryFile;
    private FileConfiguration registryConfig;
    private final Random random = new Random();

    public VerificationManager(RPGIdentityPlugin plugin) {
        this.plugin = plugin;
        this.registryFile = new File(plugin.getDataFolder(), "data/registry.yml");
        loadRegistry();
    }

    public void loadRegistry() {
        if (!registryFile.exists()) {
            registryFile.getParentFile().mkdirs();
            try {
                registryFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Gagal membuat file data/registry.yml: " + e.getMessage());
            }
        }
        registryConfig = YamlConfiguration.loadConfiguration(registryFile);
        registeredIdRegistry.clear();

        if (registryConfig.contains("registry")) {
            for (String id : registryConfig.getConfigurationSection("registry").getKeys(false)) {
                String hash = registryConfig.getString("registry." + id);
                if (hash != null) {
                    registeredIdRegistry.put(id, hash);
                }
            }
        }
    }

    public void saveRegistry() {
        if (registryConfig == null) return;
        for (Map.Entry<String, String> entry : registeredIdRegistry.entrySet()) {
            registryConfig.set("registry." + entry.getKey(), entry.getValue());
        }
        try {
            registryConfig.save(registryFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Gagal menyimpan data/registry.yml: " + e.getMessage());
        }
    }

    public String generateUniqueID() {
        String id;
        do {
            int n1 = 1000 + random.nextInt(9000);
            int n2 = 1000 + random.nextInt(9000);
            int n3 = 1000 + random.nextInt(9000);
            id = String.format("ID-%d-%d-%d", n1, n2, n3);
        } while (registeredIdRegistry.containsKey(id));

        return id;
    }

    public String generateSignature(IdentityData data) {
        String secretKey = plugin.getPluginConfig().getSecretKey();
        String raw = secretKey + ":" + data.getUuid().toString() + ":" + data.getIdNumber() + ":" + data.getNama() + ":" + data.getRace().name();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) { // Take first 12 hex chars
                sb.append(String.format("%02x", hashBytes[i]));
            }
            return sb.toString().toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            return "VALORIA-HASH";
        }
    }

    public void registerID(String idNumber, String signatureHash) {
        registeredIdRegistry.put(idNumber, signatureHash);
        saveRegistry();
    }

    public void unregisterID(String idNumber) {
        if (idNumber != null) {
            registeredIdRegistry.remove(idNumber);
            if (registryConfig != null) {
                registryConfig.set("registry." + idNumber, null);
                saveRegistry();
            }
        }
    }

    public boolean isAuthentic(String idNumber, String signatureHash) {
        if (idNumber == null || signatureHash == null) return false;
        String registeredHash = registeredIdRegistry.get(idNumber);
        return registeredHash != null && registeredHash.equalsIgnoreCase(signatureHash);
    }

    public boolean isRegisteredID(String idNumber) {
        return idNumber != null && registeredIdRegistry.containsKey(idNumber);
    }
}
