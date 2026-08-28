package com.rpgidentity.config;

import com.rpgidentity.RPGIdentityPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public class PluginConfig {

    private final RPGIdentityPlugin plugin;

    private String kingdom;
    private String city;
    private String statusBerlaku;
    private String secretKey;

    private String prefix;
    private String noIdSelf;
    private String noIdOther;
    private String registrationSuccess;
    private String giveCardSuccess;
    private String showCardSender;
    private String showCardReceiver;
    private String cardLocked;

    private Material itemMaterial;
    private int customModelData;
    private int playerCardCmdStart;
    private boolean useCustomMap;
    private boolean autoIazip;

    private boolean economyEnabled;
    private int registrationCost;
    private String economyTakeCommand;
    private String paidSuccessMsg;
    private String insufficientFundsMsg;

    public PluginConfig(RPGIdentityPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        kingdom = color(config.getString("card.kingdom", "&b&lKERAJAAN VALDORA"));
        city = color(config.getString("card.city", "&7KOTA VALORIA"));
        statusBerlaku = color(config.getString("card.status_berlaku", "&a&lSEUMUR HIDUP"));
        secretKey = config.getString("card.secret_key", "VALDORA-SECRET-SIGNATURE-2026");

        prefix = color(config.getString("messages.prefix", "&8[&bIDENTITY&8] "));
        noIdSelf = color(config.getString("messages.no_id_self", "&cKamu belum mendaftarkan Kartu Identitas! Menampilkan form pendaftaran..."));
        noIdOther = color(config.getString("messages.no_id_other", "&c%player% belum memiliki Kartu Identitas Resmi!"));
        registrationSuccess = color(config.getString("messages.registration_success", "&aKartu Identitas Resmi milikmu telah berhasil terbit!"));
        giveCardSuccess = color(config.getString("messages.give_card_success", "&aBerhasil mengambil Fisik Kartu Identitas milik &e%nama%&a!"));
        showCardSender = color(config.getString("messages.show_card_sender", "&aKamu menunjukkan Kartu Identitas milikmu kepada &e%target%&a."));
        showCardReceiver = color(config.getString("messages.show_card_receiver", "&e%sender% &atelah menunjukkan Kartu Identitas miliknya kepadamu."));
        cardLocked = color(config.getString("messages.card_locked", "&cKartu Identitas kamu sudah terbit dan terkunci! Hanya Admin yang dapat me-edit data ini."));

        String matName = config.getString("item.material", "PAPER");
        try {
            itemMaterial = Material.valueOf(matName.toUpperCase());
        } catch (Exception e) {
            itemMaterial = Material.PAPER;
        }

        customModelData = config.getInt("item.custom_model_data", 900001);
        playerCardCmdStart = config.getInt("item.player_card_cmd_start", 900100);
        useCustomMap = config.getBoolean("item.use_custom_map", false);
        autoIazip = config.getBoolean("item.auto_iazip", true);

        economyEnabled = config.getBoolean("economy.enabled", true);
        registrationCost = config.getInt("economy.cost", 100);
        economyTakeCommand = config.getString("economy.take_command", "economy take %player% %cost%");
        paidSuccessMsg = color(config.getString("economy.paid_success_msg", "&aBiaya penerbitan KTP sebesar &e%cost% Koin &atelah dipotong!"));
        insufficientFundsMsg = color(config.getString("economy.insufficient_funds_msg", "&c&l[❌] Koin kamu tidak cukup! Kamu membutuhkan &e%cost% Koin &cuntuk membuat KTP!"));
    }

    private String color(String s) {
        if (s == null) return "";
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public String getKingdom() { return kingdom; }
    public String getCity() { return city; }
    public String getStatusBerlaku() { return statusBerlaku; }
    public String getSecretKey() { return secretKey; }
    public String getPrefix() { return prefix; }
    public String getNoIdSelf() { return prefix + noIdSelf; }
    public String getNoIdOther(String targetName) { return prefix + noIdOther.replace("%player%", targetName); }
    public String getRegistrationSuccess() { return prefix + registrationSuccess; }
    public String getGiveCardSuccess(String nama) { return prefix + giveCardSuccess.replace("%nama%", nama); }
    public String getShowCardSender(String target) { return prefix + showCardSender.replace("%target%", target); }
    public String getShowCardReceiver(String sender) { return prefix + showCardReceiver.replace("%sender%", sender); }
    public String getCardLocked() { return prefix + cardLocked; }
    public Material getItemMaterial() { return itemMaterial; }
    public int getCustomModelData() { return customModelData; }
    public int getPlayerCardCmdStart() { return playerCardCmdStart; }
    public boolean useCustomMap() { return useCustomMap; }
    public boolean isAutoIazip() { return autoIazip; }

    public boolean isEconomyEnabled() { return economyEnabled; }
    public int getRegistrationCost() { return registrationCost; }
    public String getEconomyTakeCommand() { return economyTakeCommand; }
    public String getPaidSuccessMsg() { return prefix + paidSuccessMsg.replace("%cost%", String.valueOf(registrationCost)); }
    public String getInsufficientFundsMsg() { return prefix + insufficientFundsMsg.replace("%cost%", String.valueOf(registrationCost)); }
}
