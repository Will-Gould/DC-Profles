package org.williamg.dcprofiles;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class Profile {

    private final UUID uuid;
    private final String currentName;
    private final String ip;
    private List<String> oldNames;
    private Timestamp lastOnline;

    public Profile(String uuid, List<String> names, String ip, Timestamp lastOnline) {
        this.uuid = UUID.fromString(uuid);
        this.currentName = names.get(0);
        //Add list of old names and remove current name from old names list
        this.oldNames = names;
        this.oldNames.remove(names.get(0));

        this.ip = ip;
        setTimestamp(lastOnline);
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getCurrentName() {
        return currentName;
    }

    public String getIp() {
        return ip;
    }

    public List<String> getOldNames() {
        return oldNames;
    }

    public void setOldNames(List<String> oldNames) {
        this.oldNames = oldNames;
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
