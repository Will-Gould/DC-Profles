package org.williamg.dcprofiles.database;

import org.bukkit.entity.Player;
import org.williamg.dcprofiles.DCProfiles;
import org.williamg.dcprofiles.Name;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("SqlNoDataSourceInspection")
public class NameManager {

    private final DCProfiles plugin;
    private final DatabaseManager dbManager;
    private final String prefix;

    public NameManager(DCProfiles plugin){
        this.plugin = plugin;
        this.dbManager = plugin.getDatabaseManager();
        this.prefix = plugin.getDatabaseManager().getPrefix();
    }

    public List<Name> getNames(UUID uuid) {
        //Return names so that the current name is always element 0 in returned array
        Connection c = dbManager.getConnection();
        List<Name> names = new ArrayList<>();
        try{
            PreparedStatement nameQuery = c.prepareStatement("SELECT * FROM " + prefix + "names WHERE player_uuid=?");
            nameQuery.setString(1, uuid.toString());
            ResultSet resultSet = nameQuery.executeQuery();
            while(resultSet.next()){
                names.add(new Name(resultSet.getString("name"), UUID.fromString(resultSet.getString("player_uuid")), resultSet.getTimestamp("last_used")));
            }
            resultSet.close();
            nameQuery.close();
        } catch (SQLException e) {
            this.plugin.getLogger().severe("Could not get names from database");
        }
        return names;
    }

    public void updateName(Player p){
        UUID uuid = p.getUniqueId();
        String name = p.getName();
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        Name currentName = new Name(name, uuid, currentTimestamp);

        //Look for existing use of name
        List<Name> playerNames = new ArrayList<>();
        playerNames = getNames(uuid);

        //Check if any name matches
        boolean matchFound = false;
        for(Name n : playerNames){
            if (n.equals(currentName)) {
                matchFound = true;
                break;
            }
        }

        //If match found update timestamp on existing entry
        if(matchFound){
            updateTimestamp(currentName);
            return;
        }

        //No match found create new name entry
        insertName(currentName);
    }

    public List<UUID> getIDsByName(String name){
        Connection c = dbManager.getConnection();

        List<UUID> ids = new ArrayList<>();
        try{
            PreparedStatement nameStmt = c.prepareStatement("SELECT DISTINCT player_uuid FROM " + prefix + "names WHERE name=?");
            nameStmt.setString(1, name);
            ResultSet resultSet = nameStmt.executeQuery();
            while(resultSet.next()){
                ids.add(UUID.fromString(resultSet.getString("player_uuid")));
            }
        } catch (SQLException e) {
            this.plugin.getLogger().severe("Could not retrieve names from the database");
        }
        return ids;
    }

    private void updateTimestamp(Name name) {
        Connection c = dbManager.getConnection();

        try{
            PreparedStatement nameStmt = c.prepareStatement("UPDATE " + prefix + "names SET last_used=? WHERE player_uuid=? AND name=?");
            nameStmt.setTimestamp(1, name.getLastUsed());
            nameStmt.setString(2, name.getUuid().toString());
            nameStmt.setString(3, name.getName());
            nameStmt.executeUpdate();
            nameStmt.close();
        } catch (SQLException e) {
            this.plugin.getLogger().severe("Could not update timestamp for player: " + name);
        }
    }

    public void insertName(Name name) {
        Connection c = dbManager.getConnection();

        try{
            PreparedStatement nameStmt = c.prepareStatement("INSERT INTO " + prefix + "names (player_uuid, name, last_used) VALUES (?, ?, ?)");
            nameStmt.setString(1, name.getUuid().toString());
            nameStmt.setString(2, name.getName());
            nameStmt.setTimestamp(3, name.getLastUsed());
            nameStmt.executeUpdate();
            nameStmt.close();
        } catch (SQLException e) {
            this.plugin.getLogger().severe("Could not insert new name for player: " + name.getUuid());
        }
    }

    public void initialiseNamesTable() throws SQLException {
        Connection c = dbManager.getConnection();

        PreparedStatement namesStmt = c.prepareStatement("CREATE TABLE IF NOT EXISTS " + prefix + "names(" +
                "id serial PRIMARY KEY," +
                "player_uuid varchar(255), " +
                "name varchar(20), " +
                "last_used timestamp DEFAULT CURRENT_TIMESTAMP" +
                ");"
        );
        namesStmt.executeUpdate();
        namesStmt.close();
    }
}
