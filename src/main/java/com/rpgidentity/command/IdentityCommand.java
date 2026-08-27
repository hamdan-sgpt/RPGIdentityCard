package com.rpgidentity.command;

import com.rpgidentity.RPGIdentityPlugin;
import com.rpgidentity.gui.IdentityCardGUI;
import com.rpgidentity.gui.IdentityFormGUI;
import com.rpgidentity.model.IdentityData;
import com.rpgidentity.util.CardItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IdentityCommand implements CommandExecutor, TabCompleter {

    private final RPGIdentityPlugin plugin;

    public IdentityCommand(RPGIdentityPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Perintah ini hanya bisa dijalankan oleh pemain.");
            return true;
        }

        if (args.length == 0) {
            IdentityCardGUI.open(plugin, player, player);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "lihat", "view" -> {
                if (args.length >= 2) {
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        player.sendMessage(color(plugin.getPluginConfig().getPrefix() + "&cPemain tidak ditemukan!"));
                        return true;
                    }
                    IdentityCardGUI.open(plugin, player, target);
                } else {
                    IdentityCardGUI.open(plugin, player, player);
                }
            }
            case "verify", "cek", "check" -> {
                Player target = player;
                if (args.length >= 2) {
                    Player found = Bukkit.getPlayer(args[1]);
                    if (found != null) target = found;
                }
                IdentityData data = plugin.getIdentityManager().getIdentity(target.getUniqueId());
                if (data == null || !data.isRegistered()) {
                    player.sendMessage(color(plugin.getPluginConfig().getPrefix() + "&cData Kartu Identitas &e" + target.getName() + " &cbelum terdaftar!"));
                    return true;
                }
                boolean authentic = plugin.getVerificationManager().isAuthentic(data.getIdNumber(), data.getSignatureHash());

                player.sendMessage(color("&8&m=================&r &b&lVERIFIKASI ID &8&m================="));
                player.sendMessage(color("&7Pemilik ID  : &f" + target.getName()));
                player.sendMessage(color("&7Kode ID     : &e" + data.getIdNumber()));
                player.sendMessage(color("&7Ras Karakter: " + data.getRace().getDisplayName()));
                player.sendMessage(color("&7Status ID   : " + (authentic ? "&a&l✅ ASLI (TERVERIFIKASI RESMI)" : "&c&l❌ PALSU / TIDAK SAH")));
                player.sendMessage(color("&8&m============================================"));
            }
            case "ambil", "item", "fisik" -> {
                IdentityData data = plugin.getIdentityManager().getIdentity(player.getUniqueId());
                if (data == null || !data.isRegistered()) {
                    player.sendMessage(plugin.getPluginConfig().getNoIdSelf());
                    IdentityFormGUI.open(plugin, player);
                    return true;
                }
                ItemStack item = CardItemUtil.createCardItem(plugin, data, player);
                player.getInventory().addItem(item);
                player.sendMessage(plugin.getPluginConfig().getGiveCardSuccess(data.getNama()));
            }
            case "buat", "daftar", "edit" -> {
                IdentityData data = plugin.getIdentityManager().getIdentity(player.getUniqueId());
                if (data != null && data.isRegistered() && !player.hasPermission("identity.admin")) {
                    player.sendMessage(plugin.getPluginConfig().getCardLocked());
                    return true;
                }
                IdentityFormGUI.open(plugin, player);
            }
            case "bantuan", "help" -> {
                player.sendMessage(color("&8&m=================&r &b&lIDENTITY MENU &8&m================="));
                player.sendMessage(color("&e/id &7- Buka GUI Kartu Identitas milik sendiri."));
                player.sendMessage(color("&e/id lihat [pemain] &7- Lihat Kartu Identitas pemain lain."));
                player.sendMessage(color("&e/id verify [pemain] &7- Cek keaslian ID (ASLI vs PALSU)."));
                player.sendMessage(color("&e/id ambil &7- Ambil fisik Kartu Identitas di inventory."));
                player.sendMessage(color("&e/id buat &7- Daftar / Edit Formulir Kartu Identitas."));
                player.sendMessage(color("&e/idadmin &7- Perintah administrator."));
                player.sendMessage(color("&8&m============================================"));
            }
            default -> IdentityCardGUI.open(plugin, player, player);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(Arrays.asList("lihat", "verify", "ambil", "buat", "bantuan"), args[0]);
        }
        if (args.length == 2 && ("lihat".equalsIgnoreCase(args[0]) || "view".equalsIgnoreCase(args[0]) || "verify".equalsIgnoreCase(args[0]))) {
            return null; // Return player list
        }
        return new ArrayList<>();
    }

    private List<String> filter(List<String> list, String input) {
        List<String> filtered = new ArrayList<>();
        for (String s : list) {
            if (s.toLowerCase().startsWith(input.toLowerCase())) {
                filtered.add(s);
            }
        }
        return filtered;
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
