package org.williamg.dcprofiles.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.williamg.dcprofiles.DCProfiles;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CommandHandler {

    private LinkedHashMap<String, Command> commands;
    private final DCProfiles plugin;

    public CommandHandler(DCProfiles plugin) {
        this.plugin = plugin;
        this.commands = new LinkedHashMap<>();
    }

    public void handleCommand(CommandSender sender, String label, String[] args) {
        List<Command> matches = getMatches(label);
        if(matches.isEmpty()) {
            sender.sendMessage(Component.text("Unknown command", NamedTextColor.RED));
        }

        //If more than 1 command found send all command's help messages
        if(matches.size() > 1) {
            for(Command command : matches) {
                showUsage(sender, command);
            }
        }

        Command c = matches.get(0);
        CommandInfo i = c.getClass().getAnnotation(CommandInfo.class);

        //Check if sender has permission
        if(!sender.hasPermission(i.permission())) {
            sender.sendMessage(Component.text("You do not have permission to use this command", NamedTextColor.RED));
            return;
        }

        //Check if sender is player
        if(i.needsPlayer() && !(sender instanceof Player)) {
            sender.sendMessage(Component.text("You must be a player to use this command", NamedTextColor.RED));
            return;
        }

        // Now, let's check the size of the arguments passed for min value
        if(args.length < i.minimumArgs()) {
            showUsage(sender, c);
            return;
        }

        // Then, if the maximumArgs doesn't equal -1, we need to check if the size of the arguments passed is greater than the maximum args.
        if(i.maxArgs() != -1 && i.maxArgs() < args.length) {
            showUsage(sender, c);
            return;
        }

        //All clear run the command
        try{
            if(!c.execute(this, sender, args)){
                showUsage(sender, c);
                return;
            }
            return;
        }catch(Exception e){
            //e.printStackTrace();
            this.plugin.getLogger().severe("An error occurred while executing this command");
        }
    }

    private List<Command> getMatches(String command) {
        List<Command> matches = new ArrayList<>();
        for(Map.Entry<String, Command> entry : commands.entrySet()){
            if(command.matches(entry.getKey())){
                matches.add(entry.getValue());
            }
        }
        return matches;
    }

    private void showUsage(CommandSender sender, Command command) {
        CommandInfo info = command.getClass().getAnnotation(CommandInfo.class);
        if(!sender.hasPermission(info.permission())) return;
        sender.sendMessage(info.usage());
    }

    private void loadCommands() {

    }

    private void load(Class<? extends Command> c){
        CommandInfo info = c.getAnnotation(CommandInfo.class);
        if(info == null){
            return;
        }
        try{
            commands.put(info.pattern(), c.getDeclaredConstructor().newInstance());
        }catch (Exception e){
            this.plugin.getLogger().severe("Error loading command " + c.getName());
        }
    }
}
