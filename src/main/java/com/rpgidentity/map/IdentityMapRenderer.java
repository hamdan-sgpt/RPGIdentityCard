package com.rpgidentity.map;

import com.rpgidentity.RPGIdentityPlugin;
import com.rpgidentity.model.IdentityData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MinecraftFont;
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
            // Load logo.png & nama.png
            try {
                File logoFile = new File(plugin.getDataFolder(), "logo.png");
                if (!logoFile.exists()) logoFile = new File("logo.png");
                if (logoFile.exists()) cachedLogo = ImageIO.read(logoFile);
            } catch (Exception ignored) {}

            try {
                File namaFile = new File(plugin.getDataFolder(), "nama.png");
                if (!namaFile.exists()) namaFile = new File("nama.png");
                if (namaFile.exists()) cachedNama = ImageIO.read(namaFile);
            } catch (Exception ignored) {}

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
        // 1. Draw Background, Borders, Logo, Banner Name & Avatar Photo Box on BufferedImage
        BufferedImage img = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();

        Color bg = new Color(18, 24, 38);
        Color raceColor = getRaceGoldColor(data.getRace().getRawName());
        Color blueAccent = new Color(97, 175, 239);

        // Background
        g.setColor(bg);
        g.fillRect(0, 0, 128, 128);

        // Crisp Double Border
        g.setColor(raceColor);
        g.drawRect(2, 2, 123, 123);
        g.setColor(blueAccent);
        g.drawRect(4, 4, 119, 119);

        // Draw logo.png on top left (6, 5) size 20x20
        if (cachedLogo != null) {
            g.drawImage(cachedLogo, 6, 5, 20, 20, null);
        }

        // Draw nama.png header banner (28, 5) size 90x20
        if (cachedNama != null) {
            g.drawImage(cachedNama, 28, 5, 90, 20, null);
        }

        // Header Line Separator
        g.setColor(raceColor);
        g.drawLine(6, 27, 121, 27);

        // Pas Foto Skin Player (32x32) at (7, 31)
        if (cachedAvatar != null) {
            g.drawImage(cachedAvatar, 7, 31, 32, 32, null);
        } else {
            g.drawImage(createDefaultAvatar(), 7, 31, 32, 32, null);
        }
        g.setColor(raceColor);
        g.drawRect(6, 30, 34, 34);

        // Footer Line Separator
        g.setColor(raceColor);
        g.drawLine(6, 78, 121, 78);

        g.dispose();

        // Render base graphics to MapCanvas
        canvas.drawImage(0, 0, img);

        // 2. Render Header & Metadata Text using Bukkit's Native MinecraftFont
        String c = getRaceColorCode(data.getRace().getRawName());
        boolean isAuthentic = plugin.getVerificationManager().isAuthentic(data.getIdNumber(), data.getSignatureHash());

        // Header Text (If nama.png is not loaded)
        if (cachedNama == null) {
            int textX = (cachedLogo != null) ? 28 : 16;
            canvas.drawText(textX, 8, MinecraftFont.Font, c + "§lKERAJAAN VALORIA");
            canvas.drawText(textX, 18, MinecraftFont.Font, "§7KARTU IDENTITAS RESMI");
        }

        // Metadata Data Attributes (x = 43) - NO UNNECESSARY TRUNCATION!
        canvas.drawText(43, 31, MinecraftFont.Font, c + "ID  : §f" + truncate(data.getIdNumber(), 12));
        canvas.drawText(43, 40, MinecraftFont.Font, "§7VER : " + (isAuthentic ? "§aASLI" : "§cPALSU"));
        canvas.drawText(43, 49, MinecraftFont.Font, "§7NAMA: §f" + truncate(data.getNama(), 12));
        canvas.drawText(43, 58, MinecraftFont.Font, "§7RAS : " + c + truncate(data.getRace().getRawName(), 12));
        canvas.drawText(43, 67, MinecraftFont.Font, "§7JOB : §f" + truncate(data.getProfesi(), 12));

        // Footer Text
        canvas.drawText(10, 83, MinecraftFont.Font, "§7UMUR: §f" + data.getUmur() + " THN §8| §7VALORIA");
        canvas.drawText(10, 94, MinecraftFont.Font, c + "OFFICIAL IDENTITY CARD");
        canvas.drawText(10, 105, MinecraftFont.Font, "§aVERIFIED BY ROYAL SYSTEM");
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

    private String getRaceColorCode(String race) {
        if (race == null) return "§f";
        return switch (race.toUpperCase()) {
            case "HUMAN" -> "§f";
            case "ELF" -> "§a";
            case "DWARF" -> "§6";
            case "DEMON" -> "§c";
            default -> "§f";
        };
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen);
    }
}
