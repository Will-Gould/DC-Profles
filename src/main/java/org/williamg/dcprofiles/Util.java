package org.williamg.dcprofiles;

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

    public static String colorise(String s){
        return s.replace('&', '§');
    }

}
