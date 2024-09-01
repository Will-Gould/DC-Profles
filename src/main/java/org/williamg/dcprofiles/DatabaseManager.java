package org.williamg.dcprofiles;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

@SuppressWarnings("SqlNoDataSourceInspection")
public class DatabaseManager {

    private final DCProfiles plugin;
    private Connection connection = null;
    private final HikariDataSource dataSource;
    private Integer timeoutTask;
    private final String prefix;

    public DatabaseManager(DCProfiles plugin, String host, String port, String database, String user, String pass, String prefix) {
        this.plugin = plugin;

        String url = "jdbc:postgresql://" + host + ":" + port;

        Properties props = new Properties();
        props.setProperty("dataSourceClassName", "org.postgresql.ds.PGSimpleDataSource");
        props.setProperty("dataSource.user", user);
        props.setProperty("dataSource.password", pass);
        props.setProperty("dataSource.databaseName", database);
        this.prefix = prefix;

        HikariConfig config = new HikariConfig(props);
        config.setJdbcUrl(url);
        this.dataSource = new HikariDataSource(config);

    }

    private void connect() throws SQLException {
        this.connection = this.dataSource.getConnection();
//        try{
//            this.connection = dataSource.getConnection();
//        }catch(SQLException e){
//            this.plugin.getLogger().severe("Error connecting to database");
//            return false;
//        }
//        return true;
    }

    public Connection getConnection() {
        if(connection == null){
            try {
                connect();
            } catch (SQLException e) {
                this.plugin.getLogger().severe("Error connecting to database");
                return null;
            }
        }else{
            //If the connection is open reset the timeout to the next interval after this connection
            cancelTimoutTask();
            createTimoutTask(this);
        }
        return connection;
    }

    private void createTimoutTask(DatabaseManager manager) {
        this.timeoutTask = Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    manager.connection.close();
                    manager.plugin.getLogger().info("Idle connection closed");
                } catch (SQLException e) {
                    manager.plugin.getLogger().warning("Failed to close connection");
                }
                manager.connection = null;
            }
        }, 300*20);
    }

    private void cancelTimoutTask(){
        Bukkit.getScheduler().cancelTask(this.timeoutTask);
    }

    public void initialiseDatabase() throws SQLException {
        if(connection == null){
            connect();
        }

        PreparedStatement profilesStmt = this.connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + prefix + "profiles(" +
                "profile_id serial PRIMARY KEY, " +
                "player_uuid varchar(255), " +
                "name varchar(20), " +
                "ip varchar(39), " +
                "last_online timestamp DEFAULT CURRENT_TIMESTAMP " +
                ");"
        );
        PreparedStatement notesTableStmt = this.connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + prefix + "notes(" +
                "note_id serial PRIMARY KEY, " +
                "player_uuid varchar(255), " +
                "time timestamp DEFAULT CURRENT_TIMESTAMP, " +
                "staff_uuid varchar(255), " +
                "note varchar(255) " +
                ");"
        );
        notesTableStmt.executeUpdate();
        notesTableStmt.close();
        profilesStmt.executeUpdate();
        profilesStmt.close();
    }

}
