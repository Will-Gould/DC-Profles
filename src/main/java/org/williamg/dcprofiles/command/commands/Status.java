package org.williamg.dcprofiles.command.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.williamg.dcprofiles.DCProfiles;
import org.williamg.dcprofiles.Name;
import org.williamg.dcprofiles.Note;
import org.williamg.dcprofiles.database.DatabaseManager;
import org.williamg.dcprofiles.Profile;
import org.williamg.dcprofiles.command.Command;
import org.williamg.dcprofiles.command.CommandHandler;
import org.williamg.dcprofiles.command.CommandInfo;

import java.util.ArrayList;
import java.util.List;

@CommandInfo(
        maxArgs = 1,
        minimumArgs = 1,
        needsPlayer = false,
        pattern = "status",
        permission = "profiler.status",
        usage = "/status <player>"
)
public class Status implements Command {

    @Override
    public boolean execute(CommandHandler cmdHandler, CommandSender sender, String[] args) throws Exception {

        DatabaseManager dbManager = cmdHandler.getPlugin().getDatabaseManager();
        String targetPlayer = args[0];

        List<Profile> profiles = dbManager.getProfilesByName(targetPlayer);
        //Null means there was a database error
        if(profiles == null) {
            sender.sendMessage(Component.text("There was a database error while running this command, contact system administrator", NamedTextColor.RED));
            return false;
        }
        //Check if there were 0 matches
        if(profiles.isEmpty()) {
            sender.sendMessage(Component.text("No profiles matching that name were found", NamedTextColor.GRAY));
            return true;
        }

        //If there are multiple profiles that have used that name check if any of them are the current user of that name

        //Get notes and display known profiles
        displayProfiles(cmdHandler.getPlugin(), sender, profiles);

        return true;
    }

    private void displayProfiles(DCProfiles plugin, CommandSender sender, List<Profile> profiles) {
        if(profiles.size() > 1) {
            sender.sendMessage(Component.text("Multiple profiles matching that name were found", NamedTextColor.LIGHT_PURPLE));
        }
        profiles.forEach(profile -> {
            List<Note> notes = plugin.getDatabaseManager().getNotes(profile.getUuid());
            profile.printProfile(plugin, sender, notes);
        });
    }

}
