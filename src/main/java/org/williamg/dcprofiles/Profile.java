package org.williamg.dcprofiles;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.*;

public class Profile {

    private final UUID uuid;
    private final String ip;
    private final List<Name> names;
    private Timestamp lastOnline;

    public Profile(UUID uuid, List<Name> names, String ip, Timestamp lastOnline) {
        this.uuid = uuid;
        this.names = names;
        this.ip = ip;
        setTimestamp(lastOnline);
    }

    public void printProfile(CommandSender sender, List<Note> notes) {
        Player player = Bukkit.getPlayer(this.uuid);
        sender.sendMessage(Component.text("============= Player Profile ============="));
        //If bukkit cannot find player use value retrieved from db, otherwise use display name
        if(player == null) {
            sender.sendMessage(Component.text("-------------" + getCurrentName() + "-------------"));
        }else {
            sender.sendMessage(player.displayName().asComponent());
        }

        //Display previously used names if available
        if(this.names.size() > 1){
            sender.sendMessage(Component.text("Previously known as:", NamedTextColor.DARK_AQUA));
            names.forEach(n -> {
                if(!n.equals(getCurrentName())) {
                    sender.sendMessage(Component.text(" - ", NamedTextColor.DARK_GRAY).append(Component.text(n.getName(), NamedTextColor.WHITE)));
                }
            });
        }

        //Display last online
        sender.sendMessage(Component.text("Last online: ", NamedTextColor.DARK_GRAY).append(Component.text(lastOnline.toString(), NamedTextColor.LIGHT_PURPLE)));

        //display notes
        printNotes(sender, notes);
    }

    public void printNotes(CommandSender sender, List<Note> notes) {
        notes.sort(Comparator.comparing(Note::getTimestamp));
        sender.sendMessage(Component.text("------------ Notes -------------", NamedTextColor.BLUE));
        notes.forEach(n -> {
           n.printNote(sender);
           sender.sendMessage(Component.text("_______________________________________", NamedTextColor.DARK_AQUA));
        });
    }

    public UUID getUuid() {
        return uuid;
    }

    public List<Name> getNames() {
        return names;
    }

    public Name getCurrentName() {
        return Util.determineCurrentName(names);
    }

    public String getIp() {
        return ip;
    }

    public Timestamp getLastOnline() {
        return lastOnline;
    }

    private void setTimestamp(Timestamp timeStamp){
        /**
         * @param timestamp = null current timestamp will be used
         **/
        if(timeStamp == null){
            this.lastOnline = new Timestamp(Calendar.getInstance().getTime().getTime());
        }else{
            this.lastOnline = timeStamp;
        }
    }

}
