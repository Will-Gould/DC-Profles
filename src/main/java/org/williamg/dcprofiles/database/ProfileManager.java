package org.williamg.dcprofiles.database;

import org.bukkit.entity.Player;
import org.williamg.dcprofiles.DCProfiles;
import org.williamg.dcprofiles.Name;
import org.williamg.dcprofiles.Profile;

import java.sql.*;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("SqlNoDataSourceInspection")
public class ProfileManager {

    private final DCProfiles plugin;
    private final DatabaseManager dbManager;
    private final String prefix;

    public ProfileManager(DCProfiles plugin){
        this.plugin = plugin;
        this.dbManager = plugin.getDatabaseManager();
        this.prefix = plugin.getDatabaseManager().getPrefix();
    }

    public Profile getProfile(UUID uuid, List<Name> names){
        Profile profile = null;
        Connection c = dbManager.getConnection();

        try{
            PreparedStatement profileStmt = c.prepareStatement("SELECT * FROM " + prefix + "profiles WHERE player_uuid=?");
            profileStmt.setString(1, uuid.toString());
            ResultSet resultSet = profileStmt.executeQuery();
            while(resultSet.next()){
                profile = new Profile(
                        UUID.fromString(resultSet.getString("player_uuid")),
                        names,
                        resultSet.getString("ip"),
                        resultSet.getTimestamp("last_online")
                );
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to retrieve profile records from database for UUID: " + uuid);
            return null;
        }

        return profile;
    }

    public void updateProfile(Player player){
        Connection c = dbManager.getConnection();

        try{
            PreparedStatement profileStmt = c.prepareStatement("UPDATE " + prefix + "profiles SET ip=?, last_online=? WHERE player_uuid=?");
            profileStmt.setString(1, Objects.requireNonNull(player.getAddress()).getAddress().getHostAddress());
            profileStmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            profileStmt.setString(3, player.getUniqueId().toString());
            profileStmt.executeUpdate();
            profileStmt.close();
        } catch (SQLException e) {
            this.plugin.getLogger().severe("Failed to update profile records for UUID: " + player.getUniqueId());
        }
    }

    public void insertProfile(Profile profile){
        Connection c = dbManager.getConnection();

        try {
            PreparedStatement profileStmt = c.prepareStatement("INSERT INTO " + prefix + "profiles (player_uuid, ip, last_online) VALUES (?, ?, ?)");
            profileStmt.setString(1, profile.getUuid().toString());
            profileStmt.setString(2, profile.getIp());
            profileStmt.setTimestamp(3, profile.getLastOnline());
            profileStmt.executeUpdate();
            profileStmt.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to insert profile for player: " + profile.getUuid());
        }
    }

    public void initialiseProfilesTable() throws SQLException {
        Connection c = dbManager.getConnection();

        PreparedStatement profilesStmt = c.prepareStatement("CREATE TABLE IF NOT EXISTS " + prefix + "profiles(" +
                "profile_id serial PRIMARY KEY, " +
                "player_uuid varchar(255), " +
                "ip varchar(39), " +
                "last_online timestamp DEFAULT CURRENT_TIMESTAMP " +
                ");"
        );
        profilesStmt.executeUpdate();
        profilesStmt.close();
    }

}
