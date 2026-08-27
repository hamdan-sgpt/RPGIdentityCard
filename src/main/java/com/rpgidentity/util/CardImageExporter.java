package com.rpgidentity.util;

import com.rpgidentity.RPGIdentityPlugin;
import com.rpgidentity.model.IdentityData;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CardImageExporter {

    public static void generateAndSaveCardPng(RPGIdentityPlugin plugin, IdentityData data, String playerName) {
        if (data == null || plugin == null) return;

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // 1. Find mentahan_card.png template
                BufferedImage template = findImageFile(plugin, "mentahan_card.png");

                // 2. Fetch Player Avatar (64x64)
                BufferedImage avatar = null;
                try {
                    URL url = new URL("https://mc-heads.net/avatar/" + data.getUuid().toString() + "/64");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                    conn.setConnectTimeout(3000);
                    conn.setReadTimeout(3000);
                    try (InputStream in = conn.getInputStream()) {
                        avatar = ImageIO.read(in);
                    }
                } catch (Exception ignored) {}

                // 3. Create high resolution image (1920x1130 or native template size)
                int width = template != null ? template.getWidth() : 1280;
                int height = template != null ? template.getHeight() : 750;

                BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = canvas.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                if (template != null) {
                    g.drawImage(template, 0, 0, width, height, null);
                } else {
                    g.setColor(new Color(18, 24, 38));
                    g.fillRect(0, 0, width, height);
                }

                // 4. Draw Avatar into FOTO Frame (Proportional to template dimensions)
                int fx = (int) (width * 0.075);
                int fy = (int) (height * 0.31);
                int fw = (int) (width * 0.335);
                int fh = (int) (height * 0.625);

                if (avatar != null) {
                    g.drawImage(avatar, fx, fy, fw, fh, null);
                }

                // 5. Draw Attributes Text (NAMA, RAS, JOB, ID)
                g.setFont(new Font("SansSerif", Font.BOLD, (int) (height * 0.045)));

                int tx = (int) (width * 0.55);
                int y1 = (int) (height * 0.205);
                int y2 = (int) (height * 0.405);
                int y3 = (int) (height * 0.605);
                int y4 = (int) (height * 0.805);

                String namaChar = data.getNama() != null ? data.getNama() : playerName;
                String rasName = data.getRace() != null ? data.getRace().getRawName() : "Human";
                String jobName = data.getProfesi() != null ? data.getProfesi() : "Lumberjack";
                String idNum = data.getIdNumber() != null ? data.getIdNumber() : "ID-1176-74";

                // NAMA
                g.setColor(Color.WHITE);
                g.drawString(namaChar, tx, y1);

                // RAS
                g.setColor(getRaceColor(rasName));
                g.drawString(rasName, tx, y2);

                // JOB
                g.setColor(Color.WHITE);
                g.drawString(jobName, tx, y3);

                // ID
                g.setColor(getRaceColor(rasName));
                g.drawString(idNum, tx, y4);

                g.dispose();

                // 6. Save PNG file to plugins/RPGIdentityCard/cards/<PlayerName>.png
                File cardsFolder = new File(plugin.getDataFolder(), "cards");
                if (!cardsFolder.exists()) {
                    cardsFolder.mkdirs();
                }

                File outputFile = new File(cardsFolder, playerName + ".png");
                ImageIO.write(canvas, "PNG", outputFile);
                plugin.getLogger().info("TERSIMPAN FILE KTP PNG: " + outputFile.getAbsolutePath());

            } catch (Exception e) {
                plugin.getLogger().warning("Gagal menyimpan file KTP PNG untuk " + playerName + ": " + e.getMessage());
            }
        });
    }

    private static BufferedImage findImageFile(RPGIdentityPlugin plugin, String filename) {
        File[] possibleLocations = new File[]{
                new File(plugin.getDataFolder(), filename),
                new File(filename),
                new File("plugins/RPGIdentityCard/" + filename),
                new File("d:/codingan/A-skript-ktp/" + filename),
                new File("d:/codingan/A-skript-ktp/src/main/resources/" + filename)
        };
        for (File f : possibleLocations) {
            if (f.exists()) {
                try {
                    return ImageIO.read(f);
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    private static Color getRaceColor(String race) {
        if (race == null) return Color.WHITE;
        return switch (race.toUpperCase()) {
            case "HUMAN" -> Color.WHITE;
            case "ELF" -> new Color(46, 204, 113);
            case "DWARF" -> new Color(230, 126, 34);
            case "DEMON" -> new Color(231, 76, 60);
            default -> Color.WHITE;
        };
    }
}
