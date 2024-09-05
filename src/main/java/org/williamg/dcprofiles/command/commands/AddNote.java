package org.williamg.dcprofiles.command.commands;

import com.google.common.base.Joiner;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.williamg.dcprofiles.Note;
import org.williamg.dcprofiles.Profile;
import org.williamg.dcprofiles.command.Command;
import org.williamg.dcprofiles.command.CommandHandler;
import org.williamg.dcprofiles.command.CommandInfo;
import org.williamg.dcprofiles.database.DatabaseManager;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@CommandInfo(
        maxArgs = -1,
        minimumArgs = 2,
        needsPlayer = true,
        pattern = "addnote",
        permission = "dcprofiles.addnote",
        usage = "/addnote <targetplayer> <note>"
)
public class AddNote implements Command {
    @Override
    public boolean execute(CommandHandler cmdHandler, CommandSender sender, String[] args) throws Exception {
        DatabaseManager dbManager = cmdHandler.getPlugin().getDatabaseManager();
        Player noter = (Player) sender;
        Player targetPlayer = Bukkit.getPlayer(args[0]);
        String note = Joiner.on(" ").join(ArrayUtils.subarray(args, 1, args.length));
        UUID targetPlayerUUID = null;
        if(targetPlayer == null) {
            //Bukkit can't find player check if name is in database
            List<Profile> profiles = dbManager.getProfilesByName(args[0]);
            if(profiles.isEmpty()) {
                //No player found alert
                sender.sendMessage(Component.text("Could not find player with name: ", NamedTextColor.RED).append(Component.text(args[0], NamedTextColor.DARK_GRAY)));
                return true;
            }

            //Check if the name is current for the profiles
            List<Profile> currentProfiles = dbManager.getCurrentProfilesByName(args[0]);
            if(currentProfiles.isEmpty()) {
                sender.sendMessage(Component.text("Could not find current player with name: ", NamedTextColor.RED).append(Component.text(args[0], NamedTextColor.DARK_GRAY)));
                didYouMean(sender, profiles);
                return true;
            }

            //Check if current names greater than 1 if so there is a collision that needs to be resolved
            //TODO Add mojang API lookup to resolve collision
            //For now just don't allow the note to be added
            if(currentProfiles.size() > 1) {
                sender.sendMessage(Component.text("More than one player is currently using this name on our system, note could not be added", NamedTextColor.RED));
                return true;
            }

            //Only one possible target therefore use this target's UUID
            targetPlayerUUID = currentProfiles.get(0).getUuid();
        }else{
            //Bukkit could find player use this instead of database lookup
            targetPlayerUUID = targetPlayer.getUniqueId();
        }

        Note n = new Note(targetPlayerUUID, note, noter.getUniqueId(), new Timestamp(System.currentTimeMillis()));

        dbManager.addNote(n);

        sender.sendMessage(Component.text("Note added", NamedTextColor.GREEN));

        return false;
    }

    private void didYouMean(CommandSender sender, List<Profile> profiles) {
        sender.sendMessage(Component.text("Did you mean..."));
        profiles.forEach(profile -> {
            sender.sendMessage(profile.getCurrentName().getName());
        });
    }

}
