package org.williamg.dcprofiles;

import net.kyori.adventure.text.Component;

import java.util.List;

public class Util {

    public static Name determineCurrentName(List<Name> names){
        Name currentName = null;
        for(Name name : names){
            if(currentName == null || currentName.getLastUsed().before(name.getLastUsed())){
                currentName = name;
            }
        }
        return currentName;
    }

}
