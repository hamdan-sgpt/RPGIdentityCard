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

        // --- GUI Form Edit/Registrasi (Di-check paling awal) ---
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

                    // Deduct registration cost via console economy command if enabled
                    if (plugin.getPluginConfig().isEconomyEnabled()) {
                        int cost = plugin.getPluginConfig().getRegistrationCost();
                        String takeCmd = plugin.getPluginConfig().getEconomyTakeCommand()
                                .replace("%player%", player.getName())
                                .replace("%cost%", String.valueOf(cost));
                        try {
                            org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), takeCmd);
                        } catch (Exception ignored) {}
                        player.sendMessage(plugin.getPluginConfig().getPaidSuccessMsg());
                    }

                    if (data.getIdNumber() == null) {
                        data.setIdNumber(plugin.getVerificationManager().generateUniqueID());
                    }
                    String sig = plugin.getVerificationManager().generateSignature(data);
                    data.setSignatureHash(sig);
                    data.setRegistered(true);

                    // Register to verification registry database
                    plugin.getVerificationManager().registerID(data.getIdNumber(), sig);
                    plugin.getIdentityManager().saveData();

                    // Export edited PNG card image to disk
                    com.rpgidentity.util.CardImageExporter.generateAndSaveCardPng(plugin, data, player.getName());

                    player.sendMessage(plugin.getPluginConfig().getRegistrationSuccess());
                    player.sendMessage(color(plugin.getPluginConfig().getPrefix() + "&7ID Resmi Kamu: &e" + data.getIdNumber()));
                    player.sendMessage(color(plugin.getPluginConfig().getPrefix() + "&7Status ID: &a&l✅ ASLI (TERVERIFIKASI)"));
                    player.sendMessage(color(plugin.getPluginConfig().getPrefix() + "&7File KTP PNG tersimpan di folder server &bplugins/RPGIdentityCard/cards/" + player.getName() + ".png&7!"));
                    player.sendMessage(color(plugin.getPluginConfig().getPrefix() + "&7Gunakan perintah &b/id &7untuk melihat Kartu Identitas milikmu!"));

                    // Delay item giving slightly so ItemsAdder export & /iazip finish compiling first
                    org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (player.isOnline()) {
                            ItemStack item = CardItemUtil.createCardItem(plugin, data, player);
                            player.getInventory().addItem(item);
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                        }
                    }, 70L);
                }
            }
            return;
        }

        // --- GUI Card Viewer ---
        if (title.equals(IdentityCardGUI.TITLE) || title.contains("VALORIA")) {
            event.setCancelled(true);
            int slot = event.getRawSlot();

            if (slot == 38) { // Ambil Fisik Kartu
                player.closeInventory();
                IdentityData data = plugin.getIdentityManager().getIdentity(player.getUniqueId());
                if (data != null && data.isRegistered()) {
                    com.rpgidentity.util.CardImageExporter.generateAndSaveCardPng(plugin, data, player.getName());
                    ItemStack item = CardItemUtil.createCardItem(plugin, data, player);
                    player.getInventory().addItem(item);
                    player.sendMessage(plugin.getPluginConfig().getGiveCardSuccess(data.getNama()));
                }
            } else if (slot == 40 || slot == 13 || slot == 14 || slot == 15 || slot == 16) { // Edit Data (Khusus Admin)
                if (player.hasPermission("identity.admin")) {
                    player.closeInventory();
                    IdentityFormGUI.open(plugin, player);
                } else {
                    player.sendMessage(plugin.getPluginConfig().getCardLocked());
                }
            } else if (slot == 42) { // Tutup
                player.closeInventory();
            }
            return;
        }
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
