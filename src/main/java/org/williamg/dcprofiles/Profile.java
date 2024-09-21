package org.williamg.dcprofiles;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

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

    public void printProfile(DCProfiles plugin, CommandSender sender, List<Note> notes) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(this.uuid);
        sender.sendMessage(Component.text("============= Player Profile =============", NamedTextColor.DARK_PURPLE));

        //Display player's rank and use prefix if permissions and chat are hooked
        if(plugin.getPermissions() != null && plugin.getChat() != null) {
            String playerGroup = plugin.getPermissions().getPrimaryGroup(Bukkit.getWorlds().get(0).getName(), offlinePlayer);
            String groupPrefix = Util.colorise(plugin.getChat().getGroupPrefix(Bukkit.getWorlds().get(0).getName(), playerGroup));

            sender.sendMessage(groupPrefix + getCurrentName().getName());
            sender.sendMessage("§7Rank: " + groupPrefix + playerGroup);
        }else{
            sender.sendMessage(Component.text(getCurrentName().getName()));
        }

        //Check if player has permission to view IP addresses
        if(sender.hasPermission("profiler.status.ip")){
            sender.sendMessage(Component.text("IP Address: ", NamedTextColor.GRAY).append(Component.text(ip, NamedTextColor.AQUA)));
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
        sender.sendMessage(Component.text("Last online: ", NamedTextColor.GRAY).append(Component.text(lastOnline.toString(), NamedTextColor.LIGHT_PURPLE)));

        //display notes if player has permission
        if(sender.hasPermission("profiler.status.notes")){
            printNotes(sender, notes);
        }
    }

    public void printNotes(CommandSender sender, List<Note> notes) {
        notes.sort(Comparator.comparing(Note::getTimestamp));
        sender.sendMessage(Component.text("------------ Notes -------------", NamedTextColor.BLUE));
        //Use reversed list to put most recent notes at the top
        notes.reversed().forEach(n -> {
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
        if(timeStamp == null){
            this.lastOnline = new Timestamp(Calendar.getInstance().getTime().getTime());
        }else{
            this.lastOnline = timeStamp;
        }
    }

}
