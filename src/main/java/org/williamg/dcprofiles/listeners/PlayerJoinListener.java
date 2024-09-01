package org.williamg.dcprofiles.listeners;

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
        //Check if player exists in system
        Player player = event.getPlayer();
        Profile profile = null;
        try{
            profile = this.plugin.getDatabaseManager().getProfile(player.getUniqueId());
        }catch (SQLException e){
            this.plugin.getLogger().warning("Error retrieving profile for " + player.getName());
            return;
        }

        //If a profile was not found create one
        if(profile == null){
            try{
                createProfile(player);
            } catch (SQLException e) {
                this.plugin.getLogger().severe("Could not create profile for " + player.getName());
            }
            return;
        }

        //Update profile
        boolean updateName = !profile.getCurrentName().equals(player.getName());
        try {
            this.plugin.getDatabaseManager().updateProfile(player, updateName);
        } catch (SQLException e) {
            this.plugin.getLogger().severe("Could not update profile for " + player.getName());
        }

    }

    private void createProfile(Player player) throws SQLException {
        List<String> names = new ArrayList<>();
        names.add(player.getName());
        Profile p = new Profile(
                player.getUniqueId().toString(),
                names,
                Objects.requireNonNull(player.getAddress()).getHostString(),
                new Timestamp(System.currentTimeMillis())
        );
        this.plugin.getDatabaseManager().insertProfile(p);
    }

}
