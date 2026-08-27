package com.rpgidentity.command;

import com.rpgidentity.RPGIdentityPlugin;
import com.rpgidentity.model.IdentityData;
import com.rpgidentity.model.Race;
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

public class IdentityAdminCommand implements CommandExecutor, TabCompleter {

    private final RPGIdentityPlugin plugin;

    public IdentityAdminCommand(RPGIdentityPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("identity.admin")) {
            sender.sendMessage(color(plugin.getPluginConfig().getPrefix() + "&cKamu tidak memiliki izin untuk menggunakan perintah ini!"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "reload" -> {
                plugin.getPluginConfig().loadConfig();
                sender.sendMessage(color(plugin.getPluginConfig().getPrefix() + "&aKonfigurasi berhasil di-reload!"));
            }
            case "reset" -> {
                if (args.length < 2) {
                    sender.sendMessage(color(plugin.getPluginConfig().getPrefix() + "&cMasukkan nama player!"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target != null) {
                    plugin.getIdentityManager().removeIdentity(target.getUniqueId());
                    sender.sendMessage(color(plugin.getPluginConfig().getPrefix() + "&aBerhasil mereset data identitas milik &e" + target.getName() + "&a!"));
                } else {
                    sender.sendMessage(color(plugin.getPluginConfig().getPrefix() + "&cPemain tidak ditemukan!"));
                }
            }
            case "give" -> {
                if (args.length < 2) {
                    sender.sendMessage(color(plugin.getPluginConfig().getPrefix() + "&cMasukkan nama player!"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(color(plugin.getPluginConfig().getPrefix() + "&cPemain tidak ditemukan!"));
                    return true;
                }
                IdentityData data = plugin.getIdentityManager().getIdentity(target.getUniqueId());
                if (data == null || !data.isRegistered()) {
                    sender.sendMessage(color(plugin.getPluginConfig().getPrefix() + "&cPlayer belum memiliki Kartu Identitas terdaftar!"));
                    return true;
                }
                ItemStack item = CardItemUtil.createCardItem(plugin, data, target);
                target.getInventory().addItem(item);
                sender.sendMessage(color(plugin.getPluginConfig().getPrefix() + "&aBerhasil memberikan fisik Kartu Identitas ke &e" + target.getName() + "&a."));
            }
            case "setras" -> {
                if (args.length < 3) {
                    sender.sendMessage(color(plugin.getPluginConfig().getPrefix() + "&cFormat: /idadmin setras <player> <Elf|Dwarf|Demon|Human>"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(color(plugin.getPluginConfig().getPrefix() + "&cPemain tidak ditemukan!"));
                    return true;
                }
                IdentityData data = plugin.getIdentityManager().getOrCreateIdentity(target);
                Race race = Race.fromString(args[2]);
                data.setRace(race);
                if (data.getIdNumber() != null) {
                    data.setSignatureHash(plugin.getVerificationManager().generateSignature(data));
                    plugin.getVerificationManager().registerID(data.getIdNumber(), data.getSignatureHash());
                }
                plugin.getIdentityManager().saveData();
                sender.sendMessage(color(plugin.getPluginConfig().getPrefix() + "&aBerhasil mengubah Ras " + target.getName() + " menjadi " + race.getDisplayName() + "&a."));
            }
            case "set" -> {
                if (args.length < 4) {
                    sender.sendMessage(color(plugin.getPluginConfig().getPrefix() + "&cFormat: /idadmin set <player> <nama|umur|profesi> <nilai>"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(color(plugin.getPluginConfig().getPrefix() + "&cPemain tidak ditemukan!"));
                    return true;
                }
                IdentityData data = plugin.getIdentityManager().getOrCreateIdentity(target);
                String field = args[2].toLowerCase();
                String value = String.join(" ", Arrays.copyOfRange(args, 3, args.length));

                switch (field) {
                    case "nama" -> data.setNama(value);
                    case "umur" -> {
                        try {
                            data.setUmur(Integer.parseInt(value));
                        } catch (NumberFormatException e) {
                            sender.sendMessage(color(plugin.getPluginConfig().getPrefix() + "&cUmur harus berupa angka!"));
                            return true;
                        }
                    }
                    case "profesi" -> data.setProfesi(com.rpgidentity.model.Job.sanitizeJob(value));
                    default -> {
                        sender.sendMessage(color(plugin.getPluginConfig().getPrefix() + "&cField tidak valid! Gunakan: nama, umur, atau profesi."));
                        return true;
                    }
                }

                if (data.getIdNumber() != null) {
                    data.setSignatureHash(plugin.getVerificationManager().generateSignature(data));
                    plugin.getVerificationManager().registerID(data.getIdNumber(), data.getSignatureHash());
                }
                plugin.getIdentityManager().saveData();
                sender.sendMessage(color(plugin.getPluginConfig().getPrefix() + "&aBerhasil mengubah " + field + " " + target.getName() + " menjadi: &e" + value));
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(color("&8&m=================&r &b&lIDENTITY ADMIN &8&m================="));
        sender.sendMessage(color("&e/idadmin reload &7- Reload konfigurasi plugin."));
        sender.sendMessage(color("&e/idadmin reset <player> &7- Reset data ID player."));
        sender.sendMessage(color("&e/idadmin give <player> &7- Berikan item fisik KTP ke player."));
        sender.sendMessage(color("&e/idadmin setras <player> <Elf/Dwarf/Demon/Human> &7- Set Ras player."));
        sender.sendMessage(color("&e/idadmin set <player> <nama/umur/profesi> <nilai> &7- Set atribut player."));
        sender.sendMessage(color("&8&m============================================"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("identity.admin")) return new ArrayList<>();

        if (args.length == 1) {
            return filter(Arrays.asList("reload", "reset", "give", "setras", "set"), args[0]);
        }
        if (args.length == 2 && ("reset".equalsIgnoreCase(args[0]) || "give".equalsIgnoreCase(args[0]) || "setras".equalsIgnoreCase(args[0]) || "set".equalsIgnoreCase(args[0]))) {
            return null; // Return player list
        }
        if (args.length == 3 && "setras".equalsIgnoreCase(args[0])) {
            return filter(Arrays.asList("Human", "Elf", "Dwarf", "Demon"), args[2]);
        }
        if (args.length == 3 && "set".equalsIgnoreCase(args[0])) {
            return filter(Arrays.asList("nama", "umur", "profesi"), args[2]);
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
