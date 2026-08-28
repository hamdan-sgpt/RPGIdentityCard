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

                if (template == null) {
                    plugin.getLogger().warning("CRITICAL: File mentahan_card.png TIDAK DITEMUKAN! Pastikan mentahan_card.png ada di folder plugins/RPGIdentityCard/ atau src/main/resources/");
                } else {
                    plugin.getLogger().info("SUKSES Memuat Template mentahan_card.png (" + template.getWidth() + "x" + template.getHeight() + ")");
                }

                // 2. Fetch Player Avatar (128x128) - Supports Offline/Cracked Servers via Player Name!
                BufferedImage avatar = fetchPlayerAvatar(playerName, data.getUuid().toString());

                // 3. Native dimensions of mentahan_card.png
                int width = template != null ? template.getWidth() : 1920;
                int height = template != null ? template.getHeight() : 1130;

                BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = canvas.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // 4. Draw mentahan_card.png Background
                if (template != null) {
                    g.drawImage(template, 0, 0, width, height, null);
                } else {
                    g.setColor(new Color(18, 24, 38));
                    g.fillRect(0, 0, width, height);

                    // Emergency Fallback Frame
                    g.setColor(getRaceColor(data.getRace() != null ? data.getRace().getRawName() : "HUMAN"));
                    g.drawRect(20, 20, width - 40, height - 40);
                }

                // 5. Draw Avatar into FOTO Frame
                int fx = (int) (width * 0.051);
                int fy = (int) (height * 0.312);
                int fw = (int) (width * 0.395);
                int fh = (int) (height * 0.592);

                if (avatar != null) {
                    g.drawImage(avatar, fx, fy, fw, fh, null);
                }

                // 6. Draw Attributes Text (NAMA, RAS, JOB, ID)
                g.setFont(new Font("SansSerif", Font.BOLD, (int) (height * 0.052)));

                int tx = (int) (width * 0.555);
                int y1 = (int) (height * 0.205);
                int y2 = (int) (height * 0.408);
                int y3 = (int) (height * 0.612);
                int y4 = (int) (height * 0.815);

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

                // 7. Save PNG file to plugins/RPGIdentityCard/cards/<PlayerName>.png
                File cardsFolder = new File(plugin.getDataFolder(), "cards");
                if (!cardsFolder.exists()) {
                    cardsFolder.mkdirs();
                }

                File outputFile = new File(cardsFolder, playerName + ".png");
                ImageIO.write(canvas, "PNG", outputFile);
                plugin.getLogger().info("TERSIMPAN KTP PNG HASIL EDIT: " + outputFile.getAbsolutePath());

                // Auto-update Resource Pack zip with the newly generated card PNG & model JSON!
                plugin.getResourcePackManager().addPlayerCardToPack(data, playerName);

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
                new File("D:\\codingan\\A-skript-ktp\\" + filename),
                new File("d:/codingan/A-skript-ktp/" + filename),
                new File("D:\\codingan\\A-skript-ktp\\src\\main\\resources\\" + filename),
                new File("d:/codingan/A-skript-ktp/src/main/resources/" + filename),
                new File(System.getProperty("user.dir"), filename),
                new File(System.getProperty("user.dir"), "plugins/RPGIdentityCard/" + filename)
        };
        for (File f : possibleLocations) {
            if (f != null && f.exists()) {
                try {
                    BufferedImage img = ImageIO.read(f);
                    if (img != null) return img;
                } catch (Exception ignored) {}
            }
        }
        try {
            InputStream in = plugin.getResource(filename);
            if (in != null) {
                BufferedImage img = ImageIO.read(in);
                if (img != null) return img;
            }
        } catch (Exception ignored) {}
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

    private static BufferedImage fetchPlayerAvatar(String playerName, String uuidStr) {
        String[] urls = new String[]{
                "https://mc-heads.net/avatar/" + playerName + "/128",
                "https://minotar.net/avatar/" + playerName + "/128",
                "https://mc-heads.net/avatar/" + uuidStr + "/128"
        };
        for (String urlStr : urls) {
            try {
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                conn.setConnectTimeout(2500);
                conn.setReadTimeout(2500);
                try (InputStream in = conn.getInputStream()) {
                    BufferedImage img = ImageIO.read(in);
                    if (img != null && img.getWidth() > 0) {
                        return img;
                    }
                }
            } catch (Exception ignored) {}
        }
        return createFallbackAvatar(128, 128);
    }

    private static BufferedImage createFallbackAvatar(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(50, 60, 80));
        g.fillRect(0, 0, w, h);
        g.setColor(Color.LIGHT_GRAY);
        g.fillOval(w / 4, h / 6, w / 2, h / 2);
        g.fillArc(w / 8, h / 2, (w * 3) / 4, h, 0, 180);
        g.dispose();
        return img;
    }
}
