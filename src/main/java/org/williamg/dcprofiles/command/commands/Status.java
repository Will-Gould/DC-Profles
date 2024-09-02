package org.williamg.dcprofiles.command.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.williamg.dcprofiles.Name;
import org.williamg.dcprofiles.database.DatabaseManager;
import org.williamg.dcprofiles.Profile;
import org.williamg.dcprofiles.command.Command;
import org.williamg.dcprofiles.command.CommandHandler;
import org.williamg.dcprofiles.command.CommandInfo;

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

        //Display known profiles
        displayProfiles(sender, profiles);

        return true;
    }

    private void displayProfiles(CommandSender sender, List<Profile> profiles) {
        if(profiles.size() > 1) {
            sender.sendMessage(Component.text("Multiple profiles matching that name were found", NamedTextColor.LIGHT_PURPLE));
        }
        profiles.forEach(profile -> {
            sender.sendMessage(Component.text("============= Player Profile ============="));
            displayNames(sender, profile);
        });
    }

    private void displayNames(CommandSender sender, Profile profile) {
        List<Name> names = profile.getNames();
        sender.sendMessage(Component.text("Name: " + profile.getCurrentName().getName()));
        if(names.size() > 1) {
            sender.sendMessage(Component.text("Formerly known as: ", NamedTextColor.GRAY));
        }
        for(Name name : names) {
            if(name.equals(profile.getCurrentName())) {
                continue;
            }
            sender.sendMessage(Component.text(" - " + name.getName()));
        }
    }
}
