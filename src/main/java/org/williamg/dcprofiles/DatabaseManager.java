package org.williamg.dcprofiles;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;

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
        ArrayList<Profile> profiles = new ArrayList<>();
        Connection c = getConnection();

        //Get list of UUIDs that this name is current for
        List<String> uuids = new ArrayList<>();
        try{
            PreparedStatement nameQuery = c.prepareStatement("SELECT * FROM " + prefix + "names WHERE name=?");
            nameQuery.setString(1, n);
            ResultSet resultSet = nameQuery.executeQuery();
            while(resultSet.next()){
                String uuid = resultSet.getString("player_uuid");
                boolean currentName = resultSet.getBoolean("current_name");
                if(currentName && !uuids.contains(uuid)){
                    uuids.add(uuid);
                }
            }
            resultSet.close();
            nameQuery.close();
        } catch (SQLException e) {
            e.printStackTrace();
            this.plugin.getLogger().severe("Failed to retrieve name records from database...");
            return null;
        }

        //No users found
        if(uuids.isEmpty()){
            return profiles;
        }

        //Retrieve profile records for the UUIDs that were found and add to list
        uuids.forEach(uuid -> {
            try{
                PreparedStatement profileQuery = c.prepareStatement("SELECT * FROM " + prefix + "profiles WHERE player_uuid=?");
                profileQuery.setString(1, uuid);
                ResultSet resultSet = profileQuery.executeQuery();
                while(resultSet.next()){
                    Profile p = new Profile(
                            resultSet.getString("player_uuid"),
                            getNames(UUID.fromString(uuid)),
                            resultSet.getString("ip"),
                            resultSet.getTimestamp("last_online")
                    );
                    profiles.add(p);
                }
            } catch (SQLException e) {
                this.plugin.getLogger().severe("Failed to retrieve profile records from database for UUID: " + uuid);
            }
        });

        return profiles;
    }

    public Profile getProfile(UUID uuid) throws SQLException {
        Connection c = getConnection();
        Profile p = null;

        //Get names
        List<String> names = getNames(uuid);

        //Get profile
        PreparedStatement profileQuery = c.prepareStatement("SELECT * FROM " + prefix + "profiles WHERE player_uuid=?");
        profileQuery.setString(1, uuid.toString());
        ResultSet resultSet = profileQuery.executeQuery();
        String id;
        String ip;
        Timestamp lastOnline;
        while(resultSet.next()){
            id = resultSet.getString("player_uuid");
            ip = resultSet.getString("ip");
            lastOnline = resultSet.getTimestamp("last_online");
            p = new Profile(
                    id,
                    names,
                    ip,
                    lastOnline
            );
        }
        resultSet.close();
        profileQuery.close();
        return p;
    }

    public List<String> getNames(UUID uuid) throws SQLException {
        //Return names so that the current name is always element 0 in returned array
        Connection c = getConnection();
        List<String> names = new ArrayList<>();

        PreparedStatement nameQuery = c.prepareStatement("SELECT * FROM " + prefix + "names WHERE player_uuid=?");
        nameQuery.setString(1, uuid.toString());
        ResultSet resultSet = nameQuery.executeQuery();
        while(resultSet.next()){
            if(resultSet.getBoolean("current_name")){
                names.addFirst(resultSet.getString("name"));
                continue;
            }
            names.add(resultSet.getString("name"));
        }
        resultSet.close();
        nameQuery.close();
        return names;
    }

    public void insertProfile(Profile p) throws SQLException {
        Connection c = getConnection();
        PreparedStatement profileInsert = c.prepareStatement(
                "INSERT INTO " + prefix + "profiles(player_uuid, ip, last_online)" +
                "VALUES (?, ?, ?);"
        );
        profileInsert.setString(1, p.getUuid().toString());
        profileInsert.setString(2, p.getIp());
        profileInsert.setTimestamp(3, new Timestamp(System.currentTimeMillis()));

        profileInsert.executeUpdate();
        profileInsert.close();

        //update name records
        updateName(p.getUuid(), p.getCurrentName());
    }

    public void updateProfile(Player player, boolean updateName) throws SQLException{
        if(updateName){
            updateName(player.getUniqueId(), player.getName());
        }

        Connection c = getConnection();
        PreparedStatement profileUpdate = c.prepareStatement("UPDATE " + prefix + "profiles SET last_online=?, ip=? WHERE player_uuid=?");
        profileUpdate.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
        profileUpdate.setString(2, Objects.requireNonNull(player.getAddress()).getHostString());
        profileUpdate.setString(3, player.getUniqueId().toString());
        profileUpdate.executeUpdate();
        profileUpdate.close();
    }

    public void updateName(UUID uuid, String name) throws SQLException {
        List<String> names = getNames(uuid);
        Connection c = getConnection();

        //Set all names' current_name value to false
        try{
            PreparedStatement nameStatus = c.prepareStatement("UPDATE " + prefix + "names SET current_name=? WHERE player_uuid=?");
            nameStatus.setBoolean(1, false);
            nameStatus.setString(2, uuid.toString());
            nameStatus.executeUpdate();
            nameStatus.close();
        } catch (SQLException e) {
            this.plugin.getLogger().severe("Failed to update name records from database, aborting operation...");
            return;
        }

        if(names.contains(name)){
            //Player has used this name before, update it to current name and set all others to false
            try{
                PreparedStatement currentName = c.prepareStatement("UPDATE " + prefix + "names SET current_name=? WHERE player_uuid=? AND name=?");
                currentName.setBoolean(1, true);
                currentName.setString(2, uuid.toString());
                currentName.setString(3, name);
                currentName.executeUpdate();
                currentName.close();
            } catch (Exception e) {
                this.plugin.getLogger().severe("Failed to update name records from database, aborting operation...");
                return;
            }
            return;
        }

        //Create new name record
        try{
            PreparedStatement newName = c.prepareStatement("INSERT INTO " + prefix + "names(player_uuid, name, current_name) VALUES (?, ?, ?);");
            newName.setString(1, uuid.toString());
            newName.setString(2, name);
            newName.setBoolean(3, true);
            newName.executeUpdate();
            newName.close();
        } catch (SQLException e) {
            this.plugin.getLogger().severe("Failed to create new name record, aborting operation...");
        }
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
                "ip varchar(39), " +
                "last_online timestamp DEFAULT CURRENT_TIMESTAMP " +
                ");"
        );
        PreparedStatement notesStmt = this.connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + prefix + "notes(" +
                "note_id serial PRIMARY KEY, " +
                "player_uuid varchar(255), " +
                "time timestamp DEFAULT CURRENT_TIMESTAMP, " +
                "staff_uuid varchar(255), " +
                "note varchar(255) " +
                ");"
        );
        PreparedStatement namesStmt = this.connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + prefix + "names(" +
                "player_uuid varchar(255) PRIMARY KEY, " +
                "name varchar(20), " +
                "current_name boolean DEFAULT TRUE, " +
                "first_used timestamp DEFAULT CURRENT_TIMESTAMP" +
                ");"
        );
        notesStmt.executeUpdate();
        notesStmt.close();
        profilesStmt.executeUpdate();
        profilesStmt.close();
        namesStmt.executeUpdate();
        namesStmt.close();
    }

}
