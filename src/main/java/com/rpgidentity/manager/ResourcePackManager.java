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

                // 3. Create Model JSON with Compact Wide Rectangular Proportions (Aspect Ratio 1.7:1 -> Width 0.85, Height 0.50)
                File targetModel = new File(packDir, "assets/minecraft/models/item/cards/" + lowerName + ".json");
                targetModel.getParentFile().mkdirs();
                try (FileWriter writer = new FileWriter(targetModel)) {
                    writer.write("{\n" +
                            "  \"parent\": \"item/generated\",\n" +
                            "  \"textures\": {\n" +
                            "    \"layer0\": \"item/cards/" + lowerName + "\"\n" +
                            "  },\n" +
                            "  \"display\": {\n" +
                            "    \"thirdperson_righthand\": { \"rotation\": [ 0, 90, -25 ], \"translation\": [ 0, 1.5, 0 ], \"scale\": [ 0.75, 0.441, 0.75 ] },\n" +
                            "    \"thirdperson_lefthand\": { \"rotation\": [ 0, -90, 25 ], \"translation\": [ 0, 1.5, 0 ], \"scale\": [ 0.75, 0.441, 0.75 ] },\n" +
                            "    \"firstperson_righthand\": { \"rotation\": [ 0, 0, 0 ], \"translation\": [ -3.0, 7.5, 0.0 ], \"scale\": [ 0.85, 0.50, 0.85 ] },\n" +
                            "    \"firstperson_lefthand\": { \"rotation\": [ 0, 0, 0 ], \"translation\": [ 3.0, 7.5, 0.0 ], \"scale\": [ 0.85, 0.50, 0.85 ] },\n" +
                            "    \"ground\": { \"rotation\": [ 0, 0, 0 ], \"translation\": [ 0, 2.0, 0 ], \"scale\": [ 0.75, 0.441, 0.75 ] },\n" +
                            "    \"gui\": { \"rotation\": [ 0, 0, 0 ], \"translation\": [ 0, 0, 0 ], \"scale\": [ 1.0, 0.588, 1.0 ] },\n" +
                            "    \"fixed\": { \"rotation\": [ 0, 0, 0 ], \"translation\": [ 0, 0, 0 ], \"scale\": [ 1.0, 0.588, 1.0 ] }\n" +
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

                plugin.getLogger().info("AUTO-UPDATE RESOURCE PACK: Card untuk " + playerName + " berhasil ditambahkan!");

                // 6. ItemsAdder Integration (If ItemsAdder plugin folder exists)
                File itemsAdderDir = new File("plugins/ItemsAdder/contents/valdora");
                if (itemsAdderDir.exists() || Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")) {
                    exportToItemsAdder(cardPng, lowerName, data.getNama());
                    if (data.getNama() != null && !data.getNama().toLowerCase().equalsIgnoreCase(lowerName)) {
                        exportToItemsAdder(cardPng, data.getNama().toLowerCase(), data.getNama());
                    }
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

            // 3. Copy Model JSON with compact wide rectangular display transforms to ItemsAdder models folder
            File iaModel = new File(valdoraDir, "models/item/cards/" + lowerName + ".json");
            iaModel.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(iaModel)) {
                writer.write("{\n" +
                        "  \"parent\": \"item/generated\",\n" +
                        "  \"textures\": {\n" +
                        "    \"layer0\": \"valdora:item/cards/" + lowerName + "\"\n" +
                        "  },\n" +
                        "  \"display\": {\n" +
                        "    \"thirdperson_righthand\": { \"rotation\": [ 0, 90, -25 ], \"translation\": [ 0, 1.5, 0 ], \"scale\": [ 0.75, 0.441, 0.75 ] },\n" +
                        "    \"thirdperson_lefthand\": { \"rotation\": [ 0, -90, 25 ], \"translation\": [ 0, 1.5, 0 ], \"scale\": [ 0.75, 0.441, 0.75 ] },\n" +
                        "    \"firstperson_righthand\": { \"rotation\": [ 0, 0, 0 ], \"translation\": [ -3.0, 7.5, 0.0 ], \"scale\": [ 0.85, 0.50, 0.85 ] },\n" +
                        "    \"firstperson_lefthand\": { \"rotation\": [ 0, 0, 0 ], \"translation\": [ 3.0, 7.5, 0.0 ], \"scale\": [ 0.85, 0.50, 0.85 ] },\n" +
                        "    \"ground\": { \"rotation\": [ 0, 0, 0 ], \"translation\": [ 0, 2.0, 0 ], \"scale\": [ 0.75, 0.441, 0.75 ] },\n" +
                        "    \"gui\": { \"rotation\": [ 0, 0, 0 ], \"translation\": [ 0, 0, 0 ], \"scale\": [ 1.0, 0.588, 1.0 ] },\n" +
                        "    \"fixed\": { \"rotation\": [ 0, 0, 0 ], \"translation\": [ 0, 0, 0 ], \"scale\": [ 1.0, 0.588, 1.0 ] }\n" +
                        "  }\n" +
                        "}");
            }

            // 4. Generate YML Item Config for ItemsAdder linking to model_path
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
                        "      model_path: item/cards/" + lowerName + "\n");
            }

            // 5. Remove any legacy conflicting resourcepack folder if present inside ItemsAdder
            File iaResourcePackDir = new File(valdoraDir, "resourcepack");
            if (iaResourcePackDir.exists()) {
                deleteDirectory(iaResourcePackDir);
            }

            plugin.getLogger().info("ITEMSADDER INTEGRATION: Eksport pack.yml, model_path, YML item 'valdora:card_" + lowerName + "' & tekstur ke ItemsAdder berhasil!");

            // Run /iareload and /iazip if auto_iazip is enabled in config.yml
            if (plugin.getPluginConfig().isAutoIazip() && !iaReloadScheduled) {
                iaReloadScheduled = true;
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    iaReloadScheduled = false;
                    try {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "iareload");
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "iazip");
                        plugin.getLogger().info("ITEMSADDER: /iareload & /iazip berhasil dieksekusi!");
                    } catch (Exception ignored) {}
                }, 40L);
            }

        } catch (Exception e) {
            plugin.getLogger().warning("ItemsAdder Export Warning: " + e.getMessage());
        }
    }

    private void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                deleteDirectory(f);
            } else {
                f.delete();
            }
        }
        dir.delete();
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

        int baseCmd = plugin.getPluginConfig().getCustomModelData();
        int cmdStart = plugin.getPluginConfig().getPlayerCardCmdStart();

        // Static Race Predicates
        json.append("    { \"predicate\": { \"custom_model_data\": ").append(baseCmd).append(" }, \"model\": \"item/paper\" },\n");
        json.append("    { \"predicate\": { \"custom_model_data\": ").append(baseCmd + 1).append(" }, \"model\": \"item/paper\" },\n");
        json.append("    { \"predicate\": { \"custom_model_data\": ").append(baseCmd + 2).append(" }, \"model\": \"item/paper\" },\n");
        json.append("    { \"predicate\": { \"custom_model_data\": ").append(baseCmd + 3).append(" }, \"model\": \"item/paper\" }");

        int assignedCmd = 0;
        File[] modelFiles = cardsDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (modelFiles != null) {
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
