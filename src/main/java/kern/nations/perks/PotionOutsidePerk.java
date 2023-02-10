package kern.nations.perks;

import org.bukkit.potion.PotionEffectType;

public class PotionOutsidePerk extends NationPerk {
    public PotionOutsidePerk(PotionEffectType p) {
        name = p.getName() + " Outside";
        description = "When this perk is activated, all members of this nation receive a " + p.getName()
                + " potion effect while in enemy nations.";
    }
}
