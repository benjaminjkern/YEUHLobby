package kern.nations.perks;

import org.bukkit.event.Listener;

import kern.nations.Nation;

public abstract class NationPerk implements Listener {
    Nation nation;
    public String name;
    public String description;
    public double endTime;
}
