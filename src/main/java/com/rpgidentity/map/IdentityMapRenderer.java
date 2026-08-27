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

        // Fetch player head skin avatar asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
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

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Theme colors per race
        Color bg = new Color(18, 24, 38);
        Color gold = getRaceGoldColor(data.getRace().getRawName());
        Color blueAccent = new Color(97, 175, 239);
        Color greenStatus = new Color(152, 195, 121);
        Color textWhite = Color.WHITE;
        Color textGray = new Color(171, 178, 191);

        // 1. Background
        g.setColor(bg);
        g.fillRect(0, 0, 128, 128);

        // 2. Borders
        g.setColor(gold);
        g.drawRect(2, 2, 123, 123);
        g.setColor(blueAccent);
        g.drawRect(4, 4, 119, 119);

        // 3. Header
        g.setFont(new Font("SansSerif", Font.BOLD, 7));
        g.setColor(gold);
        g.drawString("KERAJAAN VALORIA", 22, 13);
        g.setFont(new Font("SansSerif", Font.PLAIN, 6));
        g.setColor(textGray);
        g.drawString("KARTU IDENTITAS RPG RESMI", 16, 21);
        g.setColor(gold);
        g.drawLine(7, 24, 120, 24);

        // 4. Pas Foto Skin Player (32x32) at (8, 28)
        if (cachedAvatar != null) {
            g.drawImage(cachedAvatar, 8, 28, 32, 32, null);
        } else {
            g.drawImage(createDefaultAvatar(), 8, 28, 32, 32, null);
        }
        g.setColor(gold);
        g.drawRect(7, 27, 34, 34);

        // 5. Data Atribut Kartu KTP
        g.setFont(new Font("Monospaced", Font.BOLD, 6));

        g.setColor(gold);
        g.drawString("ID   : " + truncate(data.getIdNumber(), 10), 45, 33);

        boolean isAuthentic = plugin.getVerificationManager().isAuthentic(data.getIdNumber(), data.getSignatureHash());
        g.setColor(isAuthentic ? greenStatus : Color.RED);
        g.drawString("VERIF: " + (isAuthentic ? "ASLI (RESMI)" : "PALSU"), 45, 41);

        g.setColor(textWhite);
        g.drawString("NAMA : " + truncate(data.getNama(), 10), 45, 49);

        g.setColor(greenStatus);
        g.drawString("RAS  : " + truncate(data.getRace().getRawName(), 10), 45, 57);

        g.setColor(textWhite);
        g.drawString("CLASS: " + truncate(data.getProfesi(), 10), 45, 65);

        // 6. Pembatas Line
        g.setColor(gold);
        g.drawLine(7, 68, 120, 68);

        // 7. Footer Info
        g.setFont(new Font("SansSerif", Font.PLAIN, 5));
        g.setColor(textGray);
        g.drawString("UMUR: " + data.getUmur() + " TAHUN | VALORIA REGISTRY", 10, 75);

        g.setFont(new Font("SansSerif", Font.BOLD, 5));
        g.setColor(gold);
        g.drawString("OFFICIAL ROYAL IDENTITY CARD", 18, 83);

        g.setColor(greenStatus);
        g.drawString("VERIFIED BY ROYAL SYSTEM #" + truncate(data.getSignatureHash(), 6), 10, 91);

        g.dispose();

        // Render directly to MapCanvas
        canvas.drawImage(0, 0, img);
    }

    private Color getRaceGoldColor(String race) {
        if (race == null) return new Color(229, 192, 123);
        return switch (race.toUpperCase()) {
            case "ELF" -> new Color(46, 204, 113);
            case "DWARF" -> new Color(241, 196, 15);
            case "DEMON" -> new Color(231, 76, 60);
            default -> new Color(229, 192, 123);
        };
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen);
    }
}
