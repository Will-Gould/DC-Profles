package org.williamg.dcprofiles.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.williamg.dcprofiles.DCProfiles;
import org.williamg.dcprofiles.Profile;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlayerJoinListener implements Listener {

    private final DCProfiles plugin;

    public PlayerJoinListener(DCProfiles plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            //Check if player exists in system
            Player player = event.getPlayer();
            Profile profile = null;
            profile = this.plugin.getDatabaseManager().getPlayerProfile(player);

            //If a profile was not found create one
            if(profile == null){
                this.plugin.getDatabaseManager().createNewProfile(player);
                return;
            }

            //Profile was found therefore update profile
            plugin.getDatabaseManager().updateProfile(player);
        });
    }

}
