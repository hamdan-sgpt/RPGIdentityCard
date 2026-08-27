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
    private BufferedImage cachedLogo = null;
    private BufferedImage cachedNama = null;

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

        // Fetch player head skin avatar & logo/nama images asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // Load logo.png
            cachedLogo = findImageFile("logo.png");
            // Load nama.png
            cachedNama = findImageFile("nama.png");

            // Fetch avatar from mc-heads
            try {
                URL url = new URL("https://mc-heads.net/avatar/" + playerUuid.toString() + "/32");
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
        return null;
    }

    private BufferedImage createDefaultAvatar() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(30, 38, 56));
        g.fillRect(0, 0, 32, 32);
        g.setColor(new Color(229, 192, 123));
        g.drawRect(0, 0, 31, 31);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 8));
        g.drawString("FOTO", 4, 18);
        g.dispose();
        return img;
    }

    private void drawToCanvas(MapCanvas canvas) {
        BufferedImage img = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();

        // Pixel-sharp rendering hints
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);

        Color bg = new Color(18, 24, 38);
        Color raceColor = getRaceGoldColor(data != null && data.getRace() != null ? data.getRace().getRawName() : "HUMAN");
        Color blueAccent = new Color(97, 175, 239);
        Color greenStatus = new Color(152, 195, 121);

        // 1. Dark Navy Background
        g.setColor(bg);
        g.fillRect(0, 0, 128, 128);

        // 2. Double Border
        g.setColor(raceColor);
        g.drawRect(2, 2, 123, 123);
        g.setColor(blueAccent);
        g.drawRect(4, 4, 119, 119);

        // 3. Header: logo.png (6, 5) & nama.png (28, 5)
        if (cachedLogo != null) {
            g.drawImage(cachedLogo, 6, 5, 20, 20, null);
        }

        if (cachedNama != null) {
            g.drawImage(cachedNama, 28, 5, 90, 20, null);
        } else {
            int textX = (cachedLogo != null) ? 28 : 14;
            g.setFont(new Font("Monospaced", Font.BOLD, 9));
            g.setColor(raceColor);
            g.drawString("KERAJAAN VALORIA", textX, 14);

            g.setFont(new Font("Monospaced", Font.BOLD, 7));
            g.setColor(new Color(171, 178, 191));
            g.drawString("KARTU IDENTITAS RESMI", textX, 23);
        }

        // Header Line Separator
        g.setColor(raceColor);
        g.drawLine(6, 27, 121, 27);

        // 4. Pas Foto Skin Player (32x32) at (7, 31)
        if (cachedAvatar != null) {
            g.drawImage(cachedAvatar, 7, 31, 32, 32, null);
        } else {
            g.drawImage(createDefaultAvatar(), 7, 31, 32, 32, null);
        }
        g.setColor(raceColor);
        g.drawRect(6, 30, 34, 34);

        // 5. Metadata Data Attributes (x = 43) - NO TRUNCATION!
        g.setFont(new Font("Monospaced", Font.BOLD, 7));

        String idNum = (data != null && data.getIdNumber() != null) ? data.getIdNumber() : "ID-1176-74";
        String namaChar = (data != null && data.getNama() != null) ? data.getNama() : "Player";
        String rasName = (data != null && data.getRace() != null) ? data.getRace().getRawName() : "Human";
        String jobName = (data != null && data.getProfesi() != null) ? data.getProfesi() : "Lumberjack";
        boolean isAuthentic = plugin.getVerificationManager().isAuthentic(idNum, data != null ? data.getSignatureHash() : "");

        g.setColor(raceColor);
        g.drawString("ID  : " + idNum, 43, 37);

        g.setColor(isAuthentic ? greenStatus : Color.RED);
        g.drawString("VER : " + (isAuthentic ? "ASLI (RESMI)" : "PALSU"), 43, 46);

        g.setColor(Color.WHITE);
        g.drawString("NAMA: " + namaChar, 43, 55);

        g.setColor(raceColor);
        g.drawString("RAS : " + rasName, 43, 64);

        g.setColor(Color.WHITE);
        g.drawString("JOB : " + jobName, 43, 73);

        // Footer Line Separator
        g.setColor(raceColor);
        g.drawLine(6, 78, 121, 78);

        // 6. Footer Info
        g.setFont(new Font("Monospaced", Font.BOLD, 7));
        g.setColor(new Color(171, 178, 191));
        g.drawString("UMUR: " + (data != null ? data.getUmur() : 20) + " THN | VALORIA", 10, 88);

        g.setColor(raceColor);
        g.drawString("OFFICIAL IDENTITY CARD", 10, 98);

        g.setColor(greenStatus);
        g.drawString("VERIFIED BY ROYAL SYSTEM", 10, 108);

        g.dispose();

        // Render entire complete image to MapCanvas atomically
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
