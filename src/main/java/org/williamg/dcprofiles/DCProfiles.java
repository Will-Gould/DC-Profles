package org.williamg.dcprofiles;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class DCProfiles extends JavaPlugin {

    DatabaseManager dbManager;

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

        //Create database manager and test connection
        dbManager = new DatabaseManager(this, sqlHost, sqlPort, sqlDatabase, sqlUser, sqlPassword, prefix);
        try{
            dbManager.getConnection().isValid(5);
            this.getLogger().info("Successfully connected to database");
        }catch (Exception e){
            this.getLogger().severe("Failed to connect to database disabling plugin...");
            onDisable();
        }

        //Initialise database
        initialiseDatabase();

    }

    @Override
    public void onDisable() {
        
        this.getServer().getPluginManager().disablePlugin(this);
    }

    private void initialiseDatabase() {
        try{
            dbManager.initialiseDatabase();
            this.getLogger().info("Successfully initialised database");
        } catch (Exception e) {
            this.getLogger().severe("Failed to initialise database disabling plugin...");
            onDisable();
        }
    }
}
