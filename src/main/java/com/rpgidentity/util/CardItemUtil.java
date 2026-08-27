package com.rpgidentity.util;

import com.rpgidentity.RPGIdentityPlugin;
import com.rpgidentity.config.PluginConfig;
import com.rpgidentity.map.IdentityMapRenderer;
import com.rpgidentity.model.IdentityData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CardItemUtil {

    public static ItemStack createCardItem(RPGIdentityPlugin plugin, IdentityData data, Player targetPlayer) {
        PluginConfig config = plugin.getPluginConfig();
        if (config.useCustomMap()) {
            return createMapCardItem(plugin, data, targetPlayer);
        } else {
            return createPaperCardItem(plugin, data);
        }
    }

    public static ItemStack createCardItem(RPGIdentityPlugin plugin, IdentityData data) {
        return createCardItem(plugin, data, null);
    }

    public static ItemStack createMapCardItem(RPGIdentityPlugin plugin, IdentityData data, Player targetPlayer) {
        PluginConfig config = plugin.getPluginConfig();
        ItemStack item = new ItemStack(Material.FILLED_MAP);
        MapMeta meta = (MapMeta) item.getItemMeta();

        if (meta != null) {
            MapView mapView = Bukkit.createMap(targetPlayer != null ? targetPlayer.getWorld() : Bukkit.getWorlds().get(0));
            mapView.setScale(MapView.Scale.CLOSEST);
            mapView.setUnlimitedTracking(false);

            // Remove default map renderers
            for (MapRenderer r : mapView.getRenderers()) {
                mapView.removeRenderer(r);
            }

            // Add custom KTP map renderer with player's head skin & custom details
            mapView.addRenderer(new IdentityMapRenderer(plugin, data, data.getUuid()));

            meta.setMapView(mapView);
            meta.setDisplayName(color("&b&lKARTU IDENTITAS RPG &8- &f" + data.getNama() + " &8[" + data.getRace().getDisplayName() + "&8]"));

            // Attach Persistent Owner UUID
            NamespacedKey ownerKey = new NamespacedKey(plugin, "owner_uuid");
            meta.getPersistentDataContainer().set(ownerKey, PersistentDataType.STRING, data.getUuid().toString());

            boolean isAuthentic = plugin.getVerificationManager().isAuthentic(data.getIdNumber(), data.getSignatureHash());

            List<String> lore = new ArrayList<>();
            lore.add(color("&8----------------------------------"));
            lore.add(color("&7KERAJAAN &8: &f" + config.getKingdom()));
            lore.add(color("&7KODE ID  &8: &e" + (data.getIdNumber() != null ? data.getIdNumber() : "-")));
            lore.add(color("&7STATUS ID&8: " + (isAuthentic ? "&a&l✅ ASLI (TERVERIFIKASI)" : "&c&l❌ PALSU / PEMALSUAN")));
            lore.add(color("&8----------------------------------"));
            lore.add(color("&7Nama Karakter  &8: &f" + data.getNama()));
            lore.add(color("&7Umur Karakter  &8: &f" + data.getUmur() + " Tahun"));
            lore.add(color("&7Profesi        &8: &f" + data.getProfesi()));
            lore.add(color("&7Ras Karakter   &8: " + data.getRace().getDisplayName()));
            lore.add(color("&7Berlaku Hingga &8: &a" + config.getStatusBerlaku()));
            lore.add(color("&8----------------------------------"));
            lore.add(color("&8SIGNATURE: #" + (data.getSignatureHash() != null ? data.getSignatureHash() : "UNVERIFIED")));
            lore.add(color("&e&oPegang di tangan untuk melihat Gambar KTP!"));
            lore.add(color("&e&oKlik Kanan untuk buka GUI Detail KTP!"));
            lore.add(color("&e&oShift + Klik Kanan ke Pemain untuk tunjukkan Kartu!"));

            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack createPaperCardItem(RPGIdentityPlugin plugin, IdentityData data) {
        PluginConfig config = plugin.getPluginConfig();
        String lowerName = data.getNama() != null ? data.getNama().toLowerCase() : "";
        ItemStack item = null;

        // Check if ItemsAdder custom item exists
        if (Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")) {
            try {
                dev.lone.itemsadder.api.CustomStack stack = dev.lone.itemsadder.api.CustomStack.getInstance("valdora:card_" + lowerName);
                if (stack != null) {
                    item = stack.getItemStack();
                }
            } catch (Exception ignored) {}
        }

        if (item == null) {
            item = new ItemStack(config.getItemMaterial());
        }

        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(color("&b&lKARTU IDENTITAS RPG &8- &f" + data.getNama() + " &8[" + data.getRace().getDisplayName() + "&8]"));
            
            int cmd = data.getCustomModelData() > 0 ? data.getCustomModelData() : data.getRace().getCustomModelData();
            meta.setCustomModelData(cmd);

            // Attach Persistent Owner UUID
            NamespacedKey ownerKey = new NamespacedKey(plugin, "owner_uuid");
            meta.getPersistentDataContainer().set(ownerKey, PersistentDataType.STRING, data.getUuid().toString());

            boolean isAuthentic = plugin.getVerificationManager().isAuthentic(data.getIdNumber(), data.getSignatureHash());

            List<String> lore = new ArrayList<>();
            lore.add(color("&8----------------------------------"));
            lore.add(color("&7KERAJAAN &8: &f" + config.getKingdom()));
            lore.add(color("&7KODE ID  &8: &e" + (data.getIdNumber() != null ? data.getIdNumber() : "-")));
            lore.add(color("&7STATUS ID&8: " + (isAuthentic ? "&a&l✅ ASLI (TERVERIFIKASI)" : "&c&l❌ PALSU / PEMALSUAN")));
            lore.add(color("&8----------------------------------"));
            lore.add(color("&7Nama Karakter  &8: &f" + data.getNama()));
            lore.add(color("&7Umur Karakter  &8: &f" + data.getUmur() + " Tahun"));
            lore.add(color("&7Profesi        &8: &f" + data.getProfesi()));
            lore.add(color("&7Ras Karakter   &8: " + data.getRace().getDisplayName()));
            lore.add(color("&7Berlaku Hingga &8: &a" + config.getStatusBerlaku()));
            lore.add(color("&8----------------------------------"));
            lore.add(color("&8SIGNATURE: #" + (data.getSignatureHash() != null ? data.getSignatureHash() : "UNVERIFIED")));
            lore.add(color("&e&oKlik Kanan untuk melihat Kartu Identitas!"));
            lore.add(color("&e&oShift + Klik Kanan ke Pemain untuk tunjukkan Kartu!"));

            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static UUID getOwnerUuid(RPGIdentityPlugin plugin, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, "owner_uuid");
        if (meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            String uuidStr = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
            try {
                return UUID.fromString(uuidStr);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
