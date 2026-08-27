package com.rpgidentity.map;

import com.rpgidentity.RPGIdentityPlugin;
import com.rpgidentity.model.IdentityData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class IdentityMapRenderer extends MapRenderer {

    private final RPGIdentityPlugin plugin;
    private final IdentityData data;
    private final UUID playerUuid;
    private boolean rendered = false;
    private BufferedImage cachedAvatar = null;
    private BufferedImage cachedTemplate = null;

    public IdentityMapRenderer(RPGIdentityPlugin plugin, IdentityData data, UUID playerUuid) {
        super(true);
        this.plugin = plugin;
        this.data = data;
        this.playerUuid = playerUuid;
    }

    @Override
    public void render(MapView view, MapCanvas canvas, Player player) {
        if (rendered) return;
        rendered = true;

        // Fetch player head skin avatar & template image asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // Load mentahan_card.png template
            cachedTemplate = findImageFile("mentahan_card.png");

            // Fetch avatar from mc-heads
            try {
                URL url = new URL("https://mc-heads.net/avatar/" + playerUuid.toString() + "/64");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);
                try (InputStream in = conn.getInputStream()) {
                    cachedAvatar = ImageIO.read(in);
                }
            } catch (Exception e) {
                cachedAvatar = createDefaultAvatar();
            }

            // Draw to MapCanvas on main server thread
            Bukkit.getScheduler().runTask(plugin, () -> drawToCanvas(canvas));
        });
    }

    private BufferedImage findImageFile(String filename) {
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
        try {
            InputStream in = plugin.getResource(filename);
            if (in != null) {
                return ImageIO.read(in);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private BufferedImage createDefaultAvatar() {
        BufferedImage img = new BufferedImage(40, 60, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(30, 38, 56));
        g.fillRect(0, 0, 40, 60);
        g.setColor(new Color(229, 192, 123));
        g.drawRect(0, 0, 39, 59);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 8));
        g.drawString("FOTO", 8, 32);
        g.dispose();
        return img;
    }

    private void drawToCanvas(MapCanvas canvas) {
        BufferedImage img = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();

        // High quality pixel-sharp rendering hints
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);

        Color bg = new Color(18, 24, 38);
        Color raceColor = getRaceGoldColor(data != null && data.getRace() != null ? data.getRace().getRawName() : "HUMAN");

        // 1. Draw Template mentahan_card.png or default dark navy background
        if (cachedTemplate != null) {
            g.drawImage(cachedTemplate, 0, 0, 128, 128, null);
        } else {
            g.setColor(bg);
            g.fillRect(0, 0, 128, 128);

            g.setColor(raceColor);
            g.drawRect(2, 2, 123, 123);
            g.setColor(new Color(97, 175, 239));
            g.drawRect(4, 4, 119, 119);

            g.setFont(new Font("Monospaced", Font.BOLD, 8));
            g.setColor(raceColor);
            g.drawString("VALDORA UNIVERSE", 10, 15);
            g.drawLine(6, 22, 121, 22);
        }

        // 2. Pas Foto Skin Player (Placed inside the futuristic FOTO frame at x=10, y=40, width=42, height=70)
        int photoX = 9;
        int photoY = 40;
        int photoWidth = 43;
        int photoHeight = 70;

        if (cachedAvatar != null) {
            g.drawImage(cachedAvatar, photoX, photoY, photoWidth, photoHeight, null);
        } else {
            g.drawImage(createDefaultAvatar(), photoX, photoY, photoWidth, photoHeight, null);
        }

        // 3. Render Custom Player Attributes Directly onto Template Slots
        g.setFont(new Font("Monospaced", Font.BOLD, 7));

        String namaChar = (data != null && data.getNama() != null) ? data.getNama() : "Player";
        if (namaChar.length() > 10) namaChar = namaChar.substring(0, 10);

        String rasName = (data != null && data.getRace() != null) ? data.getRace().getRawName() : "Human";

        String jobName = (data != null && data.getProfesi() != null) ? data.getProfesi() : "Lumberjack";
        if (jobName.length() > 10) jobName = jobName.substring(0, 10);

        String idNum = (data != null && data.getIdNumber() != null) ? data.getIdNumber() : "ID-1176-74";
        if (idNum.length() > 11) idNum = idNum.substring(0, 11);

        // NAMA Value (x = 70, y = 26)
        g.setColor(Color.WHITE);
        g.drawString(namaChar, 70, 26);

        // RAS Value (x = 70, y = 51)
        g.setColor(raceColor);
        g.drawString(rasName, 70, 51);

        // JOB Value (x = 70, y = 76)
        g.setColor(Color.WHITE);
        g.drawString(jobName, 70, 76);

        // ID Value (x = 70, y = 101)
        g.setColor(raceColor);
        g.drawString(idNum, 70, 101);

        g.dispose();

        // Render entire complete image onto MapCanvas
        canvas.drawImage(0, 0, img);
    }

    private Color getRaceGoldColor(String race) {
        if (race == null) return Color.WHITE;
        return switch (race.toUpperCase()) {
            case "HUMAN" -> Color.WHITE;               // Putih
            case "ELF" -> new Color(46, 204, 113);     // Hijau
            case "DWARF" -> new Color(230, 126, 34);   // Oren
            case "DEMON" -> new Color(231, 76, 60);    // Merah
            default -> Color.WHITE;
        };
    }
}
