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

        // Disable anti-aliasing for sharp, pixel-perfect HD text rendering on 128x128 map
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);

        // Theme colors per race
        Color bg = new Color(18, 24, 38);
        Color gold = getRaceGoldColor(data.getRace().getRawName());
        Color blueAccent = new Color(97, 175, 239);
        Color greenStatus = new Color(152, 195, 121);
        Color textWhite = Color.WHITE;
        Color textGray = new Color(180, 190, 205);

        // 1. Dark Navy Background
        g.setColor(bg);
        g.fillRect(0, 0, 128, 128);

        // 2. Crisp Double Border
        g.setColor(gold);
        g.drawRect(2, 2, 123, 123);
        g.setColor(blueAccent);
        g.drawRect(4, 4, 119, 119);

        // 3. Header
        g.setFont(new Font("Dialog", Font.BOLD, 9));
        g.setColor(gold);
        g.drawString("KERAJAAN VALORIA", 14, 14);

        g.setFont(new Font("Dialog", Font.BOLD, 7));
        g.setColor(textGray);
        g.drawString("KARTU IDENTITAS RESMI", 16, 23);
        g.setColor(gold);
        g.drawLine(6, 26, 121, 26);

        // 4. Pas Foto Skin Player (32x32) at (7, 30)
        if (cachedAvatar != null) {
            g.drawImage(cachedAvatar, 7, 30, 32, 32, null);
        } else {
            g.drawImage(createDefaultAvatar(), 7, 30, 32, 32, null);
        }
        g.setColor(gold);
        g.drawRect(6, 29, 34, 34);

        // 5. Data Atribut Kartu KTP (Sharp Monospace Pixel Font)
        g.setFont(new Font("Monospaced", Font.BOLD, 8));

        g.setColor(gold);
        g.drawString("ID  :" + truncate(data.getIdNumber(), 9), 43, 36);

        boolean isAuthentic = plugin.getVerificationManager().isAuthentic(data.getIdNumber(), data.getSignatureHash());
        g.setColor(isAuthentic ? greenStatus : Color.RED);
        g.drawString("VER :" + (isAuthentic ? "ASLI(RESMI)" : "PALSU"), 43, 46);

        g.setColor(textWhite);
        g.drawString("NAMA:" + truncate(data.getNama(), 9), 43, 56);

        g.setColor(greenStatus);
        g.drawString("RAS :" + truncate(data.getRace().getRawName(), 9), 43, 66);

        g.setColor(textWhite);
        g.drawString("JOB :" + truncate(data.getProfesi(), 9), 43, 76);

        // 6. Pembatas Line
        g.setColor(gold);
        g.drawLine(6, 80, 121, 80);

        // 7. Footer Info
        g.setFont(new Font("Dialog", Font.BOLD, 7));
        g.setColor(textGray);
        g.drawString("UMUR: " + data.getUmur() + " THN | VALORIA", 10, 90);

        g.setColor(gold);
        g.drawString("OFFICIAL IDENTITY CARD", 10, 100);

        g.setColor(greenStatus);
        g.drawString("VERIFIED BY ROYAL SYSTEM", 10, 110);

        g.dispose();

        // Render directly to MapCanvas
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

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen);
    }
}
