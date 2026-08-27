package com.rpgidentity.listener;

import com.rpgidentity.RPGIdentityPlugin;
import com.rpgidentity.gui.IdentityFormGUI;
import com.rpgidentity.model.IdentityData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

public class ChatListener implements Listener {

    private final RPGIdentityPlugin plugin;

    public ChatListener(RPGIdentityPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (plugin.getChatInputManager().isAwaiting(uuid)) {
            event.setCancelled(true);
            String input = event.getMessage().trim();
            String field = plugin.getChatInputManager().getAwaitingInput(uuid);
            plugin.getChatInputManager().removeAwaiting(uuid);

            if ("batal".equalsIgnoreCase(input) || "cancel".equalsIgnoreCase(input)) {
                player.sendMessage(color(plugin.getPluginConfig().getPrefix() + "&cPengisian dibatalkan."));
            } else {
                IdentityData data = plugin.getIdentityManager().getOrCreateIdentity(player);
                switch (field) {
                    case "nama" -> {
                        data.setNama(input);
                        player.sendMessage(color(plugin.getPluginConfig().getPrefix() + "&aNama Karakter berhasil disimpan: &e" + input));
                    }
                    case "umur" -> {
                        try {
                            int age = Integer.parseInt(input);
                            data.setUmur(age);
                            player.sendMessage(color(plugin.getPluginConfig().getPrefix() + "&aUmur Karakter berhasil disimpan: &e" + age + " Tahun"));
                        } catch (NumberFormatException e) {
                            player.sendMessage(color(plugin.getPluginConfig().getPrefix() + "&cUmur harus berupa angka! Mengubah ke default 20."));
                            data.setUmur(20);
                        }
                    }
                    case "profesi" -> {
                        String validJob = com.rpgidentity.model.Job.sanitizeJob(input);
                        data.setProfesi(validJob);
                        player.sendMessage(color(plugin.getPluginConfig().getPrefix() + "&aProfesi Karakter berhasil disimpan: &e" + validJob));
                    }
                }
            }

            Bukkit.getScheduler().runTask(plugin, () -> IdentityFormGUI.open(plugin, player));
        }
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
