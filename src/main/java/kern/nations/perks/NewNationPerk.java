package kern.nations.perks;

import java.util.Date;

public class NewNationPerk extends NationPerk {

    public static final float DEFAULT_TIME = 1000 * 60 * 60 * 24; // one day

    public NewNationPerk() {
        name = "New Nation Perk";
        description = "";
        endTime = new Date().getTime() + DEFAULT_TIME;
    }

}
