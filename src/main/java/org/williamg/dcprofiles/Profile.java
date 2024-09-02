package org.williamg.dcprofiles;

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
