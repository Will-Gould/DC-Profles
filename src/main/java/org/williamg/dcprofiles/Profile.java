package org.williamg.dcprofiles;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.UUID;

public class Profile {

    private final UUID uuid;
    private final String name;
    private final String ip;
    private Timestamp lastOnline;

    public Profile(String uuid, String name, String ip, Timestamp lastOnline) {
        this.uuid = UUID.fromString(uuid);
        this.name = name;
        this.ip = ip;
        setTimestamp(lastOnline);
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
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
