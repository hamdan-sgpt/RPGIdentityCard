package com.rpgidentity;

import com.rpgidentity.command.IdentityAdminCommand;
import com.rpgidentity.command.IdentityCommand;
import com.rpgidentity.config.PluginConfig;
import com.rpgidentity.listener.ChatListener;
import com.rpgidentity.listener.GUIListener;
import com.rpgidentity.listener.ItemInteractListener;
import com.rpgidentity.manager.ChatInputManager;
import com.rpgidentity.manager.IdentityManager;
import com.rpgidentity.manager.VerificationManager;
import org.bukkit.plugin.java.JavaPlugin;

public class RPGIdentityPlugin extends JavaPlugin {

    private PluginConfig pluginConfig;
    private VerificationManager verificationManager;
    private IdentityManager identityManager;
    private ChatInputManager chatInputManager;

    @Override
    public void onEnable() {
        // Inisialisasi Config & Managers
        this.pluginConfig = new PluginConfig(this);
        this.verificationManager = new VerificationManager(this);
        this.identityManager = new IdentityManager(this);
        this.chatInputManager = new ChatInputManager();

        // Save default logo.png and nama.png to plugin data folder if present
        try {
            saveResource("logo.png", false);
        } catch (Exception ignored) {}
        try {
            saveResource("nama.png", false);
        } catch (Exception ignored) {}

        // Registrasi Commands
        IdentityCommand idCmd = new IdentityCommand(this);
        IdentityAdminCommand idAdminCmd = new IdentityAdminCommand(this);

        if (getCommand("identity") != null) {
            getCommand("identity").setExecutor(idCmd);
            getCommand("identity").setTabCompleter(idCmd);
        }
        if (getCommand("idadmin") != null) {
            getCommand("idadmin").setExecutor(idAdminCmd);
            getCommand("idadmin").setTabCompleter(idAdminCmd);
        }

        // Registrasi Listeners
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemInteractListener(this), this);

        getLogger().info("========================================");
        getLogger().info(" RPGIdentityCard v" + getDescription().getVersion() + " berhasil diaktifkan!");
        getLogger().info(" Fitur Ras: Elf, Dwarf, Demon, Human");
        getLogger().info(" System Verifikasi ID: ONLINE");
        getLogger().info("========================================");
    }

    @Override
    public void onDisable() {
        if (identityManager != null) {
            identityManager.saveData();
        }
        if (verificationManager != null) {
            verificationManager.saveRegistry();
        }
        getLogger().info("RPGIdentityCard berhasil dinonaktifkan.");
    }

    public PluginConfig getPluginConfig() { return pluginConfig; }
    public VerificationManager getVerificationManager() { return verificationManager; }
    public IdentityManager getIdentityManager() { return identityManager; }
    public ChatInputManager getChatInputManager() { return chatInputManager; }
}
