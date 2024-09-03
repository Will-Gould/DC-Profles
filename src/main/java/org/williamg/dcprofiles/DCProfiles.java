package org.williamg.dcprofiles;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.williamg.dcprofiles.command.CommandHandler;
import org.williamg.dcprofiles.database.DatabaseManager;
import org.williamg.dcprofiles.listeners.PlayerJoinListener;

public final class DCProfiles extends JavaPlugin implements CommandExecutor {

    DatabaseManager dbManager;
    CommandHandler commandHandler;

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

    public DatabaseManager getDatabaseManager() {
        return dbManager;
    }
}
