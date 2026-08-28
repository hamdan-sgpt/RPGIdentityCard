package com.rpgidentity.map;

import com.rpgidentity.RPGIdentityPlugin;
import com.rpgidentity.model.IdentityData;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.UUID;

public class IdentityMapRenderer extends MapRenderer {

    private final RPGIdentityPlugin plugin;
    private final IdentityData data;
    private final UUID targetUuid;
    private boolean rendered = false;

    public IdentityMapRenderer(RPGIdentityPlugin plugin, IdentityData data, UUID targetUuid) {
        super(true);
        this.plugin = plugin;
        this.data = data;
        this.targetUuid = targetUuid;
    }

    @Override
    public void render(MapView map, MapCanvas canvas, Player player) {
        if (rendered) return;
        rendered = true;

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                BufferedImage cardImage = renderCardImage();
                if (cardImage != null) {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        canvas.drawImage(0, 0, cardImage);
                    });
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Gagal menggambar KTP di Peta: " + e.getMessage());
            }
        });
    }

    private BufferedImage renderCardImage() {
        try {
            // 1. Load mentahan_card.png template
            File templateFile = new File(plugin.getDataFolder(), "mentahan_card.png");
            BufferedImage template = null;
            if (templateFile.exists()) {
                template = ImageIO.read(templateFile);
            }
            if (template == null) {
                File rootTemplate = new File("mentahan_card.png");
                if (rootTemplate.exists()) {
                    template = ImageIO.read(rootTemplate);
                }
            }

            // Create 128x128 canvas for Minecraft Map
            BufferedImage mapImg = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = mapImg.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (template != null) {
                g.drawImage(template, 0, 0, 128, 128, null);
            } else {
                g.setColor(new Color(15, 23, 42));
                g.fillRect(0, 0, 128, 128);
            }

            // 2. Fetch Player Avatar Head
            BufferedImage avatar = null;
            try {
                URL skinUrl = new URL("https://mc-heads.net/avatar/" + targetUuid.toString() + "/64");
                avatar = ImageIO.read(skinUrl);
            } catch (Exception ignored) {}

            // Draw Avatar inside FOTO frame box (x=6, y=40, w=50, h=75)
            if (avatar != null) {
                g.drawImage(avatar, 6, 40, 50, 75, null);
            } else {
                g.setColor(new Color(30, 41, 59));
                g.fillRect(6, 40, 50, 75);
                g.setColor(Color.LIGHT_GRAY);
                g.setFont(new Font("SansSerif", Font.BOLD, 7));
                g.drawString("FOTO", 18, 80);
            }

            // 3. Draw Text Data (Nama, Ras, Job, ID)
            String charName = data.getNama() != null ? data.getNama() : "Pemain";
            String raceName = data.getRace() != null ? data.getRace().getDisplayName() : "Human";
            String jobName = data.getProfesi() != null ? data.getProfesi() : "Lumberjack";
            String idNum = data.getIdNumber() != null ? data.getIdNumber() : "ID-000-000";
            if (idNum.length() > 10) {
                idNum = idNum.substring(0, 10);
            }

            Color raceColor = Color.CYAN;
            if (data.getRace() != null) {
                switch (data.getRace()) {
                    /* ELF */ case ELF -> raceColor = new Color(34, 197, 94);
                    /* DWARF */ case DWARF -> raceColor = new Color(249, 115, 22);
                    /* DEMON */ case DEMON -> raceColor = new Color(239, 68, 68);
                    /* HUMAN */ default -> raceColor = new Color(59, 130, 246);
                }
            }

            g.setFont(new Font("SansSerif", Font.BOLD, 7));

            // Nama Karakter (x = 70, y = 32)
            g.setColor(Color.WHITE);
            g.drawString(charName, 70, 32);

            // Ras Karakter (x = 70, y = 54)
            g.setColor(raceColor);
            g.drawString(raceName, 70, 54);

            // Job Karakter (x = 70, y = 76)
            g.setColor(Color.WHITE);
            g.drawString(jobName, 70, 76);

            // ID Value (x = 65, y = 100)
            g.setFont(new Font("Monospaced", Font.BOLD, 6));
            g.setColor(raceColor);
            g.drawString(idNum, 65, 100);

            g.dispose();

            return mapImg;
        } catch (Exception e) {
            plugin.getLogger().warning("Error rendering map card image: " + e.getMessage());
            return null;
        }
    }
}
