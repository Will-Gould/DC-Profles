package org.williamg.dcprofiles;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.UUID;

public class Note {

    private final UUID playerUUID;
    private final String note;
    private final UUID staffUUID;
    private final Timestamp timestamp;


    public Note(UUID playerUUID, String note, UUID staffUUID, Timestamp timestamp) {
        this.playerUUID = playerUUID;
        this.note = note;
        this.staffUUID = staffUUID;
        this.timestamp = timestamp;
    }

    public void printNote(CommandSender sender) {
        Player staffMember = Bukkit.getPlayer(staffUUID);
        OfflinePlayer staff = Bukkit.getOfflinePlayer(staffUUID);

        if(staffMember != null) {
            sender.sendMessage(Component.text("Reporter: ", NamedTextColor.GRAY).append(staffMember.displayName().asComponent()).color(NamedTextColor.BLUE));
        }
        sender.sendMessage(Component.text("Date: ", NamedTextColor.GRAY).append(Component.text(timestamp.toString(), NamedTextColor.LIGHT_PURPLE)));
        sender.sendMessage(Component.text("\"", NamedTextColor.DARK_GRAY).append(Component.text(note, NamedTextColor.WHITE)).append(Component.text("\"", NamedTextColor.DARK_GRAY)));
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getNote() {
        return note;
    }

    public UUID getStaffUUID() {
        return staffUUID;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

}
