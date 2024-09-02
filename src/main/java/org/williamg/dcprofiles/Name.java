package org.williamg.dcprofiles;

import java.sql.Timestamp;
import java.util.UUID;

public class Name {

    private String name;
    private UUID uuid;
    private Timestamp lastUsed;

    public Name(String name, UUID uuid, Timestamp lastUsed) {
        this.name = name;
        this.uuid = uuid;
        this.lastUsed = lastUsed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Timestamp getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(Timestamp lastUsed) {
        this.lastUsed = lastUsed;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Name)) {
            return false;
        }
        Name other = (Name)o;
        return this.getName().equals(other.getName()) && this.getUuid().equals(other.getUuid());
    }
}
