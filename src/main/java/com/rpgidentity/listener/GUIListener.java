package com.rpgidentity.listener;

import com.rpgidentity.RPGIdentityPlugin;
import com.rpgidentity.gui.IdentityCardGUI;
import com.rpgidentity.gui.IdentityFormGUI;
import com.rpgidentity.model.IdentityData;
import com.rpgidentity.util.CardItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    private final RPGIdentityPlugin plugin;

    public GUIListener(RPGIdentityPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();

        // --- GUI Card Viewer ---
        if (title.equals(IdentityCardGUI.TITLE)) {
            event.setCancelled(true);
            int slot = event.getRawSlot();

            if (slot == 38) { // Ambil Fisik Kartu
                player.closeInventory();
                IdentityData data = plugin.getIdentityManager().getIdentity(player.getUniqueId());
                if (data != null && data.isRegistered()) {
                    ItemStack item = CardItemUtil.createCardItem(plugin, data, player);
                    player.getInventory().addItem(item);
                    player.sendMessage(plugin.getPluginConfig().getGiveCardSuccess(data.getNama()));
                }
            } else if (slot == 40) { // Edit Data
                player.closeInventory();
                IdentityFormGUI.open(plugin, player);
            } else if (slot == 42) { // Tutup
                player.closeInventory();
            }
            return;
        }

        // --- GUI Form Edit/Registrasi ---
        if (title.equals(IdentityFormGUI.TITLE)) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            IdentityData data = plugin.getIdentityManager().getOrCreateIdentity(player);

            switch (slot) {
                case 10 -> { // Nama Karakter
                    player.closeInventory();
                    plugin.getChatInputManager().setAwaitingInput(player.getUniqueId(), "nama");
                    player.sendMessage(color(plugin.getPluginConfig().getPrefix() + "&eSilakan ketik &bNAMA KARAKTER &ekamu di chat (Ketik 'batal' untuk membatalkan):"));
                }
                case 11 -> { // Umur Karakter
                    player.closeInventory();
                    plugin.getChatInputManager().setAwaitingInput(player.getUniqueId(), "umur");
                    player.sendMessage(color(plugin.getPluginConfig().getPrefix() + "&eSilakan ketik &bUMUR KARAKTER &e(Angka, contoh: 25) di chat:"));
                }
                case 12 -> { // Profesi / Pekerjaan (Cycle: Lumberjack -> Miner -> Farmer)
                    com.rpgidentity.model.Job current = com.rpgidentity.model.Job.fromString(data.getProfesi());
                    data.setProfesi(current.next().getRawName());
                    IdentityFormGUI.open(plugin, player);
                }
                case 14 -> { // Ras Karakter (Cycle: Human -> Elf -> Dwarf -> Demon)
                    data.setRace(data.getRace().next());
                    IdentityFormGUI.open(plugin, player);
                }
                case 22 -> { // Simpan & Terbitkan Kartu ID
                    player.closeInventory();
                    if (data.getIdNumber() == null) {
                        data.setIdNumber(plugin.getVerificationManager().generateUniqueID());
                    }
                    String sig = plugin.getVerificationManager().generateSignature(data);
                    data.setSignatureHash(sig);
                    data.setRegistered(true);

                    // Register to verification registry database
                    plugin.getVerificationManager().registerID(data.getIdNumber(), sig);
                    plugin.getIdentityManager().saveData();

                    player.sendMessage(plugin.getPluginConfig().getRegistrationSuccess());
                    player.sendMessage(color(plugin.getPluginConfig().getPrefix() + "&7ID Resmi Kamu: &e" + data.getIdNumber()));
                    player.sendMessage(color(plugin.getPluginConfig().getPrefix() + "&7Status ID: &a&l✅ ASLI (TERVERIFIKASI)"));
                    player.sendMessage(color(plugin.getPluginConfig().getPrefix() + "&7Gunakan perintah &b/id &7untuk melihat Kartu Identitas milikmu!"));

                    ItemStack item = CardItemUtil.createCardItem(plugin, data, player);
                    player.getInventory().addItem(item);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                }
            }
        }
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
