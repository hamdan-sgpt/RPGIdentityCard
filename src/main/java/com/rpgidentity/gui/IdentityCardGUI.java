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

    public static final String TITLE = color("&9&lKARTU IDENTITAS RPG - VALORIA");

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

        // PAS FOTO 3x4 (PLAYER SKULL HEAD) - Slot 10 (Supports Offline/Cracked Servers!)
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        if (skullMeta != null) {
            try {
                skullMeta.setOwningPlayer(target);
            } catch (Exception ignored) {}
            skullMeta.setOwner(target.getName());
            skullMeta.setDisplayName(color("&e&lPAS FOTO KARAKTER (3x4)"));
            skullMeta.setLore(Arrays.asList(
                    color("&7Pemilik : &f" + target.getName()),
                    color("&7Ras     : " + data.getRace().getDisplayName()),
                    color("&7Status  : " + (isAuthentic ? "&a&l✅ TERVERIFIKASI ASLI" : "&c&l❌ TIDAK SAH / PALSU"))
            ));
            head.setItemMeta(skullMeta);
        }
        inv.setItem(10, head);

        // DATA ITEMS (Klik untuk Edit)
        inv.setItem(12, createItem(Material.NAME_TAG, color("&b&lKODE ID & VERIFIKASI"),
                color("&e" + data.getIdNumber()),
                color("&7Verifikasi Keaslian: " + (isAuthentic ? "&a&l✅ ASLI (RESMI)" : "&c&l❌ PALSU / DIPALSUSERKAN")),
                color("&8Hash: #" + (data.getSignatureHash() != null ? data.getSignatureHash() : "N/A"))));

        boolean isSelf = viewer.equals(target);
        boolean isAdmin = viewer.hasPermission("identity.admin");
        String editHint = isAdmin ? color("&e&l[ KLIK UNTUK EDIT (ADMIN) ]") : "";

        inv.setItem(13, createItem(Material.PAPER, color("&b&lNAMA KARAKTER"), color("&f" + data.getNama()), "", editHint));
        inv.setItem(14, createItem(Material.CLOCK, color("&b&lUMUR KARAKTER"), color("&f" + data.getUmur() + " Tahun"), "", editHint));
        inv.setItem(15, createItem(Material.NETHER_STAR, color("&b&lRAS KARAKTER"),
                data.getRace().getDisplayName(),
                data.getRace().getDescription(),
                "", editHint));
        inv.setItem(16, createItem(Material.IRON_PICKAXE, color("&b&lPROFESI / PEKERJAAN"), color("&f" + data.getProfesi()), "", editHint));

        inv.setItem(22, createItem(Material.SHIELD, color("&b&lBERLAKU HINGGA"), color("&a" + config.getStatusBerlaku())));

        // ACTION BUTTONS
        if (isSelf) {
            inv.setItem(38, createItem(Material.HOPPER, color("&a&l[ 🖨️ AMBIL FISIK KARTU ]"), color("&7Klik untuk mengambil Kartu Identitas fisik ke inventory.")));
        }
        if (isAdmin) {
            inv.setItem(40, createItem(Material.ANVIL, color("&e&l[ ✏️ EDIT IDENTITAS (ADMIN) ]"), color("&7Klik me-edit data Kartu Identitas ini.")));
        }
        inv.setItem(42, createItem(Material.BARRIER, color("&c&l[ ❌ TUTUP ]"), color("&7Klik untuk menutup tampilan.")));

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
