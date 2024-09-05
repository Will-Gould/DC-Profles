package org.williamg.dcprofiles.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.williamg.dcprofiles.DCProfiles;
import org.williamg.dcprofiles.Name;
import org.williamg.dcprofiles.Note;
import org.williamg.dcprofiles.Profile;

import java.sql.*;
import java.util.*;

@SuppressWarnings("SqlNoDataSourceInspection")
public class DatabaseManager {

    private final DCProfiles plugin;
    private Connection connection = null;
    private final HikariDataSource dataSource;
    private final String prefix;
    private ProfileManager profileManager;
    private NameManager nameManager;
    private NotesManager notesManager;

    public DatabaseManager(DCProfiles plugin, String host, String port, String database, String user, String pass, String prefix) {
        this.plugin = plugin;
        this.prefix = prefix;

        String url = "jdbc:postgresql://" + host + ":" + port;

        Properties props = new Properties();
        props.setProperty("dataSourceClassName", "org.postgresql.ds.PGSimpleDataSource");
        props.setProperty("dataSource.user", user);
        props.setProperty("dataSource.password", pass);
        props.setProperty("dataSource.databaseName", database);

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

    public Profile getPlayerProfile(Player player) {
        //Get names
        List<Name> names = this.nameManager.getNames(player.getUniqueId());

        //Get profile
        return this.profileManager.getProfile(player.getUniqueId(), names);
    }

    public List<Profile> getProfilesByName(String name) {
        List<Profile> profiles = new ArrayList<>();

        //Find name record for player
        List<UUID> uuids = this.nameManager.getIDsByName(name);

        //Name not found in records
        if(uuids.isEmpty()){
            return profiles;
        }

        //Find profiles for name
        for(UUID uuid : uuids){
            //Get names
            List<Name> names = this.nameManager.getNames(uuid);
            profiles.add(this.profileManager.getProfile(uuid, names));
        }
        return profiles;
    }

    public List<Profile> getCurrentProfilesByName(String name) {
        List<Profile> profiles = getProfilesByName(name);

        //Remove any profiles which are not using this name as current name
        profiles.removeIf(profile -> !profile.getCurrentName().getName().equals(name));

        return profiles;
    }

    public void updateProfile(Player player) {
        this.nameManager.updateName(player);
        this.profileManager.updateProfile(player);
    }

    public void createNewProfile(Player player){
        Name name = new Name(player.getName(), player.getUniqueId(), new Timestamp(System.currentTimeMillis()));
        List<Name> names = new ArrayList<>();
        names.add(name);
        Profile profile = new Profile(
                player.getUniqueId(),
                names,
                Objects.requireNonNull(player.getAddress()).getAddress().getHostName(),
                new Timestamp(System.currentTimeMillis())
        );
        this.profileManager.insertProfile(profile);
        this.nameManager.insertName(name);
    }

    public List<Note> getNotes(UUID uuid) {
        return this.notesManager.getNotes(uuid);
    }

    public void addNote(Note note) {
        this.notesManager.insertNote(note);
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

    public void initialiseDatabase() {
        this.profileManager = new ProfileManager(plugin);
        this.nameManager = new NameManager(plugin);
        this.notesManager = new NotesManager(plugin);
        try{
            this.plugin.getLogger().info("Initialising Profiles table...");
            this.profileManager.initialiseProfilesTable();
            this.plugin.getLogger().info("Initialising Names table...");
            this.nameManager.initialiseNamesTable();
            this.plugin.getLogger().info("Initialising Notes table...");
            this.notesManager.initialiseNotesTable();
        } catch (SQLException e) {
            this.plugin.getLogger().severe("Failed to initialise database, disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this.plugin);
        }
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public NameManager getNameManager() {
        return nameManager;
    }

    public NotesManager getNotesManager() {
        return notesManager;
    }

    public String getPrefix() {
        return this.prefix;
    }
}
