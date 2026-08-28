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

    public PluginConfig(RPGIdentityPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        this.kingdom = colorize(config.getString("header.kingdom", "KERAJAAN VALORIA - KARTU IDENTITAS RPG"));
        this.city = colorize(config.getString("header.city", "KOTA ATLANTA CENTRAL"));
        this.statusBerlaku = colorize(config.getString("header.status_berlaku", "SEUMUR HIDUP"));
        this.secretKey = config.getString("security.secret_key", "ValoriaSecretKey2026");

        this.prefix = colorize(config.getString("messages.prefix", "&8[&b&lIDENTITY&8] &r"));
        this.noIdSelf = colorize(config.getString("messages.no_id_self", "&cKamu belum mendaftar Kartu Identitas! Membuka formulir..."));
        this.noIdOther = colorize(config.getString("messages.no_id_other", "&cPemain &e%player% &cbelum mendaftar Kartu Identitas."));
        this.registrationSuccess = colorize(config.getString("messages.registration_success", "&a&lSELAMAT! Kartu Identitas kamu berhasil diterbitkan secara resmi."));
        this.giveCardSuccess = colorize(config.getString("messages.give_card_success", "&aBerhasil mengambil Fisik Kartu Identitas milik &e%nama%&a!"));
        this.showCardSender = colorize(config.getString("messages.show_card_sender", "&aKamu menunjukkan Kartu Identitas milikmu kepada &e%target%&a!"));
        this.showCardReceiver = colorize(config.getString("messages.show_card_receiver", "&e%sender% &amenunjukkan Kartu Identitas miliknya kepadamu:"));
        this.cardLocked = colorize(config.getString("messages.card_locked", "&cKartu Identitas kamu sudah diterbitkan secara resmi dan dikunci! Hubungi Admin jika ingin mengubah data."));

        String matStr = config.getString("item.material", "PAPER");
        Material mat = Material.matchMaterial(matStr);
        this.itemMaterial = mat != null ? mat : Material.PAPER;
        this.customModelData = config.getInt("item.custom_model_data", 900001);
        this.playerCardCmdStart = config.getInt("item.player_card_cmd_start", 900100);
        this.useCustomMap = config.getBoolean("item.use_custom_map", false);
        this.autoIazip = config.getBoolean("item.auto_iazip", true);

        this.economyEnabled = config.getBoolean("economy.enabled", true);
        this.registrationCost = config.getInt("economy.cost", 100);
        this.economyTakeCommand = config.getString("economy.take_command", "economy take %player% %cost%");
        this.paidSuccessMsg = colorize(config.getString("economy.paid_success_msg", "&aBiaya penerbitan KTP sebesar &e%cost% Koin &atelah dipotong!"));
    }

    public String colorize(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
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
}
