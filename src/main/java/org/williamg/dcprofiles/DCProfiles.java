package org.williamg.dcprofiles;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.williamg.dcprofiles.command.CommandHandler;
import org.williamg.dcprofiles.database.DatabaseManager;
import org.williamg.dcprofiles.listeners.PlayerJoinListener;

public final class DCProfiles extends JavaPlugin implements CommandExecutor {

    private DatabaseManager dbManager;
    private CommandHandler commandHandler;
    private Chat chat = null;
    private Permission perms = null;

    @Override
    public void onEnable() {

        //Save default config and get config sql params
        saveDefaultConfig();
        FileConfiguration config = this.getConfig();
        String sqlHost = config.getString("sql.host");
        String sqlPort = config.getString("sql.port");
        String sqlDatabase = config.getString("sql.database");
        String sqlUser = config.getString("sql.username");
        String sqlPassword = config.getString("sql.password");
        String prefix = config.getString("sql.prefix");

        //Try and load vault permissions and chat APIs
        loadVaultAPIs();

        //Create database manager and connection and initialise database
        dbManager = new DatabaseManager(this, sqlHost, sqlPort, sqlDatabase, sqlUser, sqlPassword, prefix);
        dbManager.initialiseDatabase();

        //Create command handler
        commandHandler = new CommandHandler(this);

        //Register listeners
        this.getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Disabled");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        this.commandHandler.handleCommand(sender, label, args);
        return true;
    }

    private boolean setupChat(){
        RegisteredServiceProvider<Chat> rsp = this.getServer().getServicesManager().getRegistration(Chat.class);
        if(rsp != null){
            chat = rsp.getProvider();
            return true;
        }
        return false;
    }

    public boolean setupPermissions(){
        RegisteredServiceProvider<Permission> rsp = this.getServer().getServicesManager().getRegistration(Permission.class);
        if(rsp != null){
            perms = rsp.getProvider();
            return true;
        }
        return false;
    }

    public DatabaseManager getDatabaseManager() {
        return dbManager;
    }

    private void loadVaultAPIs(){
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            getLogger().info("Vault is enabled");
            if(setupChat()){
                getLogger().info("Successfully loaded chat plugin: " + chat.getName());
            }else{
                getLogger().info("Failed to load chat plugin");
            }
            if(setupPermissions()){
                getLogger().info("Successfully loaded permission plugin: " + perms.getName());
            }else{
                getLogger().info("Failed to load permission plugin");
            }
        }else{
            getLogger().info("Vault is not enabled could not hook into chat or permission API");
        }
    }

    public Chat getChat() {
        return chat;
    }

    public Permission getPermissions() {
        return perms;
    }
}
