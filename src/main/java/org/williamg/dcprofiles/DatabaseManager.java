package org.williamg.dcprofiles;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariProxyPreparedStatement;
import com.zaxxer.hikari.pool.HikariProxyResultSet;
import org.bukkit.Bukkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

@SuppressWarnings("SqlNoDataSourceInspection")
public class DatabaseManager {

    private final DCProfiles plugin;
    private Connection connection = null;
    private final HikariDataSource dataSource;
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

        //Connect to database disable plugin if unable to connect
        try{
            createConnection();
            this.plugin.getLogger().info("Successfully connected to database ");
        } catch (SQLException e) {
            this.plugin.getLogger().severe("Failed to connect to database disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this.plugin);
        }
    }

    public Connection getConnection() {
        if(validConnection()){
           return connection;
        }

        //Attempt to connect to database if unsuccessful disable plugin
        try{
            this.plugin.getLogger().info("Attempting to re-connect to database...");
            createConnection();
            return this.connection;
        }catch (SQLException e) {
            this.plugin.getLogger().severe("Failed to connect to database disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this.plugin);
        }
        return null;
    }

    public List<Profile> getProfilesByName(String n){
        ArrayList<Profile> users = new ArrayList<>();
        Connection c = getConnection();

        try{
            PreparedStatement nameQuery = c.prepareStatement("SELECT * FROM " + prefix + "profiles WHERE name=?");
            nameQuery.setString(1, n);
            ResultSet resultSet = nameQuery.executeQuery();
            while(resultSet.next()){
                Profile p = new Profile(
                        resultSet.getString("player_uuid"),
                        resultSet.getString("name"),
                        resultSet.getString("ip"),
                        resultSet.getTimestamp("last_online"));
                users.add(p);
            }
        } catch (SQLException e) {
            this.plugin.getLogger().severe("Failed to retrieve profiles from database...");
            return null;
        }

        return users;
    }

    private void createConnection() throws SQLException {
        this.connection = this.dataSource.getConnection();
    }

    private boolean validConnection() {
        //Check if connection is null first
        if(this.connection == null){
            return false;
        }
        //Check if connection is open
        try{
            this.connection.isValid(5);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public void initialiseDatabase() throws SQLException {
        if(this.connection == null){
            throw new SQLException("Database connection is not available");
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
