package com.rpgidentity.gui;

import com.rpgidentity.RPGIdentityPlugin;
import com.rpgidentity.model.IdentityData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class IdentityFormGUI {

    public static final String TITLE = color("&9&lFORMULIR KARTU IDENTITAS RPG");

    public static void open(RPGIdentityPlugin plugin, Player player) {
        IdentityData data = plugin.getIdentityManager().getOrCreateIdentity(player);

        if (data.isRegistered() && !player.hasPermission("identity.admin")) {
            player.sendMessage(plugin.getPluginConfig().getCardLocked());
            IdentityCardGUI.open(plugin, player, player);
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27, TITLE);
        ItemStack glass = createItem(Material.GRAY_STAINED_GLASS_PANE, color("&f"));
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, glass);
        }

        inv.setItem(10, createItem(Material.PAPER, color("&b&lNAMA KARAKTER"),
                color("&7Saat ini: &f" + data.getNama()), "", color("&e&l[ KLIK UNTUK UBAH ]")));

        inv.setItem(11, createItem(Material.CLOCK, color("&b&lUMUR KARAKTER"),
                color("&7Saat ini: &f" + data.getUmur() + " Tahun"), "", color("&e&l[ KLIK UNTUK UBAH ]")));

        com.rpgidentity.model.Job currentJob = com.rpgidentity.model.Job.fromString(data.getProfesi());
        inv.setItem(12, createItem(Material.IRON_PICKAXE, color("&b&lPROFESI / PEKERJAAN"),
                color("&7Saat ini: " + currentJob.getDisplayName()),
                currentJob.getDescription(),
                "",
                color("&e&l[ KLIK UNTUK GANTI PEKERJAAN ]"),
                color("&7Pilihan Pekerjaan: &fLumberjack, Miner, Farmer")));


        // RAS SELECTION (Click-to-Cycle: Human -> Elf -> Dwarf -> Demon)
        inv.setItem(14, createItem(Material.NETHER_STAR, color("&b&lRAS KARAKTER"),
                color("&7Saat ini: " + data.getRace().getDisplayName()),
                data.getRace().getDescription(),
                "",
                color("&e&l[ KLIK UNTUK GANTI RAS ]"),
                color("&7Pilihan Ras: &fHuman, Elf, Dwarf, Demon")));

        inv.setItem(22, createItem(Material.EMERALD_BLOCK, color("&a&l[ ✅ SIMPAN & TERBITKAN ID ]"),
                color("&7Klik di sini jika semua data karakter sudah sesuai!")));

        player.openInventory(inv);
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
