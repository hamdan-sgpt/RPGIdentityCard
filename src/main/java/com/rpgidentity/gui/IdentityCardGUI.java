package com.rpgidentity.gui;

import com.rpgidentity.RPGIdentityPlugin;
import com.rpgidentity.config.PluginConfig;
import com.rpgidentity.model.IdentityData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;

public class IdentityCardGUI {

    public static final String TITLE = color("&9&lKARTA IDENTITAS RPG - VALORIA");

    public static void open(RPGIdentityPlugin plugin, Player viewer, Player target) {
        IdentityData data = plugin.getIdentityManager().getIdentity(target.getUniqueId());
        PluginConfig config = plugin.getPluginConfig();

        if (data == null || !data.isRegistered()) {
            if (viewer.equals(target)) {
                viewer.sendMessage(config.getNoIdSelf());
                IdentityFormGUI.open(plugin, viewer);
            } else {
                viewer.sendMessage(config.getNoIdOther(target.getName()));
            }
            return;
        }

        boolean isAuthentic = plugin.getVerificationManager().isAuthentic(data.getIdNumber(), data.getSignatureHash());

        Inventory inv = Bukkit.createInventory(null, 45, TITLE);

        ItemStack cyanGlass = createItem(Material.CYAN_STAINED_GLASS_PANE, color("&f"));
        ItemStack blueGlass = createItem(Material.BLUE_STAINED_GLASS_PANE, color("&f"));

        for (int i = 0; i < 45; i++) {
            inv.setItem(i, cyanGlass);
        }
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, blueGlass);
        }

        inv.setItem(4, createItem(Material.BOOK, color("&b&l" + config.getKingdom()),
                color("&7" + config.getCity()),
                color("&8ID: &e" + data.getIdNumber())));

        // PAS FOTO 3x4 (PLAYER SKULL HEAD) - Slot 10
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(target);
            skullMeta.setDisplayName(color("&e&lPAS FOTO KARAKTER (3x4)"));
            skullMeta.setLore(Arrays.asList(
                    color("&7Pemilik : &f" + target.getName()),
                    color("&7Ras     : " + data.getRace().getDisplayName()),
                    color("&7ID Unik : &e" + data.getIdNumber())
            ));
            head.setItemMeta(skullMeta);
        }
        inv.setItem(10, head);

        // DATA ATRIBUT KARTU IDENTITAS
        inv.setItem(13, createItem(Material.PAPER, color("&b&lNAMA KARAKTER"), color("&f" + data.getNama())));
        inv.setItem(14, createItem(Material.CLOCK, color("&b&lUMUR KARAKTER"), color("&f" + data.getUmur() + " Tahun")));
        inv.setItem(15, createItem(Material.IRON_PICKAXE, color("&b&lPROFESI / CLASS"), color("&f" + data.getProfesi())));
        inv.setItem(16, createItem(Material.NETHER_STAR, color("&b&lRAS KARAKTER"), data.getRace().getDisplayName(), data.getRace().getDescription()));

        // STATUS VERIFIKASI KEASLIAN (SLOT 22)
        Material statusMat = isAuthentic ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK;
        String statusTitle = isAuthentic ? color("&a&l✅ STATUS ID: TERVERIFIKASI RESMI (ASLI)") : color("&c&l❌ STATUS ID: PEMALSUAN / PALSU");
        inv.setItem(22, createItem(statusMat, statusTitle,
                color("&7Signature Hash: &8#" + (data.getSignatureHash() != null ? data.getSignatureHash() : "NONE")),
                color("&7Berlaku Hingga : &a" + config.getStatusBerlaku())));

        // TOMBOL-TOMBOL NAVIGASI BOTTOM
        if (viewer.equals(target)) {
            inv.setItem(38, createItem(Material.CHEST, color("&e&l[ 🎴 AMBIL FISIK KARTU ]"), color("&7Klik untuk mengambil item fisik Kartu Identitas.")));
            inv.setItem(40, createItem(Material.ANVIL, color("&b&l[ ✏️ EDIT DATA IDENTITAS ]"), color("&7Klik untuk membuka formulir pendaftaran & edit.")));
        }
        inv.setItem(42, createItem(Material.BARRIER, color("&c&l[ TUTUP MENU ]"), color("&7Klik untuk menutup GUI.")));

        viewer.openInventory(inv);
    }

    private static ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                meta.setLore(Arrays.asList(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
