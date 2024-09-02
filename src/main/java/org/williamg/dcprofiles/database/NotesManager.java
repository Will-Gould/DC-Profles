package org.williamg.dcprofiles.database;

import org.williamg.dcprofiles.DCProfiles;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
