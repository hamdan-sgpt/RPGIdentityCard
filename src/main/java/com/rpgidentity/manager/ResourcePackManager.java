package com.rpgidentity.manager;

import com.rpgidentity.RPGIdentityPlugin;
import com.rpgidentity.model.IdentityData;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ResourcePackManager {

    private final RPGIdentityPlugin plugin;
    private final File packDir;
    private final File zipFile;
    private boolean iaReloadScheduled = false;

    public ResourcePackManager(RPGIdentityPlugin plugin) {
        this.plugin = plugin;
        this.packDir = new File(plugin.getDataFolder(), "resourcepack");
        this.zipFile = new File(plugin.getDataFolder(), "resourcepack.zip");
        initFolderStructure();
    }

    private void initFolderStructure() {
        File modelsDir = new File(packDir, "assets/minecraft/models/item");
        File texturesDir = new File(packDir, "assets/minecraft/textures/item/cards");
        modelsDir.mkdirs();
        texturesDir.mkdirs();

        // Write pack.mcmeta if missing
        File mcmeta = new File(packDir, "pack.mcmeta");
        if (!mcmeta.exists()) {
            try (FileWriter writer = new FileWriter(mcmeta)) {
                writer.write("{\n" +
                        "  \"pack\": {\n" +
                        "    \"pack_format\": 15,\n" +
                        "    \"description\": \"Valdora Universe RPG Identity Cards Resource Pack\"\n" +
                        "  }\n" +
                        "}");
            } catch (Exception ignored) {}
        }
    }

    public void addPlayerCardToPack(IdentityData data, String playerName) {
        if (data == null || playerName == null) return;

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // 1. Source PNG file
                File cardPng = new File(plugin.getDataFolder(), "cards/" + playerName + ".png");
                if (!cardPng.exists()) return;

                String lowerName = playerName.toLowerCase();

                // 2. Copy PNG to standard resource pack textures
                File targetTexture = new File(packDir, "assets/minecraft/textures/item/cards/" + lowerName + ".png");
                targetTexture.getParentFile().mkdirs();
                Files.copy(cardPng.toPath(), targetTexture.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // 3. Create Model JSON
                File targetModel = new File(packDir, "assets/minecraft/models/item/cards/" + lowerName + ".json");
                targetModel.getParentFile().mkdirs();
                try (FileWriter writer = new FileWriter(targetModel)) {
                    writer.write("{\n" +
                            "  \"parent\": \"item/generated\",\n" +
                            "  \"textures\": {\n" +
                            "    \"layer0\": \"item/cards/" + lowerName + "\"\n" +
                            "  }\n" +
                            "}");
                }

                // 4. Update paper.json override predicates
                int assignedCmd = updatePaperJsonOverrides(lowerName);
                if (assignedCmd > 0) {
                    data.setCustomModelData(assignedCmd);
                    plugin.getIdentityManager().saveData();
                }

                // 5. Zip resourcepack folder
                zipPackFolder();

                plugin.getLogger().info("AUTO-UPDATE RESOURCE PACK: Card untuk " + playerName + " (CustomModelData: " + assignedCmd + ") berhasil ditambahkan!");

                // 6. ItemsAdder Integration (If ItemsAdder plugin folder exists)
                File itemsAdderDir = new File("plugins/ItemsAdder/contents/valdora");
                if (itemsAdderDir.exists() || Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")) {
                    exportToItemsAdder(cardPng, lowerName, data.getNama());
                }

            } catch (Exception e) {
                plugin.getLogger().warning("Gagal auto-update resource pack untuk " + playerName + ": " + e.getMessage());
            }
        });
    }

    private void exportToItemsAdder(File cardPng, String lowerName, String displayName) {
        try {
            File valdoraDir = new File("plugins/ItemsAdder/contents/valdora");
            valdoraDir.mkdirs();

            // 1. Write pack.yml if missing (Required for ItemsAdder to recognize namespace)
            File packYml = new File(valdoraDir, "pack.yml");
            if (!packYml.exists()) {
                try (FileWriter writer = new FileWriter(packYml)) {
                    writer.write("name: Valdora KTP Cards\n" +
                            "author: RPGIdentityCard\n" +
                            "version: 1.0.0\n");
                }
            }

            // 2. Copy texture to ItemsAdder textures folder
            File iaTexture = new File(valdoraDir, "textures/item/cards/" + lowerName + ".png");
            iaTexture.getParentFile().mkdirs();
            Files.copy(cardPng.toPath(), iaTexture.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // 3. Generate YML Item Config for ItemsAdder
            File iaConfig = new File(valdoraDir, "configs/cards/" + lowerName + ".yml");
            iaConfig.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(iaConfig)) {
                writer.write("info:\n" +
                        "  namespace: valdora\n" +
                        "items:\n" +
                        "  card_" + lowerName + ":\n" +
                        "    display_name: \"&b&lKARTU IDENTITAS RPG &8- &f" + (displayName != null ? displayName : lowerName) + "\"\n" +
                        "    resource:\n" +
                        "      material: PAPER\n" +
                        "      generate: true\n" +
                        "      textures:\n" +
                        "        - item/cards/" + lowerName + "\n");
            }

            // 4. Merge entire resourcepack folder into ItemsAdder's resourcepack subfolder
            File iaResourcePackDir = new File(valdoraDir, "resourcepack");
            copyDirectory(packDir, iaResourcePackDir);

            plugin.getLogger().info("ITEMSADDER INTEGRATION: Eksport pack.yml, YML item 'valdora:card_" + lowerName + "' & tekstur ke ItemsAdder berhasil!");

            // Run /iazip only if auto_iazip is enabled in config.yml
            if (plugin.getPluginConfig().isAutoIazip() && !iaReloadScheduled) {
                iaReloadScheduled = true;
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    iaReloadScheduled = false;
                    try {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "iazip");
                        plugin.getLogger().info("ITEMSADDER: /iazip berhasil dieksekusi!");
                    } catch (Exception ignored) {}
                }, 60L);
            }

        } catch (Exception e) {
            plugin.getLogger().warning("ItemsAdder Export Warning: " + e.getMessage());
        }
    }

    private void copyDirectory(File sourceDir, File targetDir) throws Exception {
        if (!sourceDir.exists()) return;
        if (!targetDir.exists()) targetDir.mkdirs();
        File[] files = sourceDir.listFiles();
        if (files == null) return;
        for (File file : files) {
            File targetFile = new File(targetDir, file.getName());
            if (file.isDirectory()) {
                copyDirectory(file, targetFile);
            } else {
                Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private int updatePaperJsonOverrides(String targetLowerName) {
        File paperJson = new File(packDir, "assets/minecraft/models/item/paper.json");
        File cardsDir = new File(packDir, "assets/minecraft/models/item/cards");

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"parent\": \"item/generated\",\n");
        json.append("  \"textures\": {\n");
        json.append("    \"layer0\": \"item/paper\"\n");
        json.append("  },\n");
        json.append("  \"overrides\": [\n");

        // Static Race Predicates
        json.append("    { \"predicate\": { \"custom_model_data\": 20001 }, \"model\": \"item/paper\" },\n");
        json.append("    { \"predicate\": { \"custom_model_data\": 20002 }, \"model\": \"item/paper\" },\n");
        json.append("    { \"predicate\": { \"custom_model_data\": 20003 }, \"model\": \"item/paper\" },\n");
        json.append("    { \"predicate\": { \"custom_model_data\": 20004 }, \"model\": \"item/paper\" }");

        int assignedCmd = 0;
        File[] modelFiles = cardsDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (modelFiles != null) {
            int cmdStart = 20100;
            for (File f : modelFiles) {
                cmdStart++;
                String nameNoExt = f.getName().replace(".json", "");
                if (targetLowerName != null && targetLowerName.equalsIgnoreCase(nameNoExt)) {
                    assignedCmd = cmdStart;
                }
                json.append(",\n    { \"predicate\": { \"custom_model_data\": ").append(cmdStart).append(" }, \"model\": \"item/cards/").append(nameNoExt).append("\" }");
            }
        }

        json.append("\n  ]\n");
        json.append("}\n");

        try (FileWriter writer = new FileWriter(paperJson)) {
            writer.write(json.toString());
        } catch (Exception ignored) {}

        return assignedCmd;
    }

    private void zipPackFolder() {
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            zipDir(packDir, packDir, zos);
        } catch (Exception ignored) {}
    }

    private void zipDir(File rootDir, File sourceDir, ZipOutputStream zos) throws Exception {
        File[] files = sourceDir.listFiles();
        if (files == null) return;
        byte[] buffer = new byte[1024];
        for (File file : files) {
            if (file.isDirectory()) {
                zipDir(rootDir, file, zos);
            } else {
                String relativePath = rootDir.toPath().relativize(file.toPath()).toString().replace("\\", "/");
                zos.putNextEntry(new ZipEntry(relativePath));
                try (FileInputStream fis = new FileInputStream(file)) {
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                }
                zos.closeEntry();
            }
        }
    }
}
