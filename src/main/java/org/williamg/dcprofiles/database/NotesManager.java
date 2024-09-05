package org.williamg.dcprofiles.database;

import org.williamg.dcprofiles.DCProfiles;
import org.williamg.dcprofiles.Note;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("SqlNoDataSourceInspection")
public class NotesManager {

    private final DCProfiles plugin;
    private final DatabaseManager dbManager;
    private final String prefix;

    public NotesManager(DCProfiles plugin){
        this.plugin = plugin;
        this.dbManager = plugin.getDatabaseManager();
        this.prefix = plugin.getDatabaseManager().getPrefix();
    }

    public List<Note> getNotes(UUID uuid){
        List<Note> notes = new ArrayList<>();
        Connection c = dbManager.getConnection();

        try{
            PreparedStatement noteStmt = c.prepareStatement("SELECT * FROM " + prefix + "notes WHERE player_uuid=?");
            noteStmt.setString(1, uuid.toString());
            ResultSet rs = noteStmt.executeQuery();
            while(rs.next()){
                notes.add(new Note(
                        UUID.fromString(rs.getString("player_uuid")),
                        rs.getString("note"),
                        UUID.fromString(rs.getString("staff_uuid")),
                        rs.getTimestamp("time"))
                );
            }
            rs.close();
        }catch (SQLException e){
            this.plugin.getLogger().warning("Failed to retrieve notes from database for player: " + uuid);
        }
        return notes;
    }

    public void insertNote(Note note){
        Connection c = dbManager.getConnection();

        try{
            PreparedStatement noteStmt = c.prepareStatement("INSERT INTO " + prefix + "notes (player_uuid, time, staff_uuid, note) VALUES (?, ?, ?, ?)");
            noteStmt.setString(1, note.getPlayerUUID().toString());
            noteStmt.setTimestamp(2, note.getTimestamp());
            noteStmt.setString(3, note.getStaffUUID().toString());
            noteStmt.setString(4, note.getNote());
            noteStmt.executeUpdate();
            noteStmt.close();
        } catch (SQLException e) {
            this.plugin.getLogger().warning("Failed to insert note for player: " + note.getPlayerUUID());
        }
    }

    public void initialiseNotesTable() throws SQLException {
        Connection c = this.dbManager.getConnection();

        PreparedStatement notesStmt = c.prepareStatement("CREATE TABLE IF NOT EXISTS " + prefix + "notes(" +
                "note_id serial PRIMARY KEY, " +
                "player_uuid varchar(255), " +
                "time timestamp DEFAULT CURRENT_TIMESTAMP, " +
                "staff_uuid varchar(255), " +
                "note varchar(255) " +
                ");"
        );
        notesStmt.executeUpdate();
        notesStmt.close();
    }
}
