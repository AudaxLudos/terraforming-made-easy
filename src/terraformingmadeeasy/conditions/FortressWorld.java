package terraformingmadeeasy.conditions;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.Objects;

public class FortressWorld extends BaseMarketConditionPlugin {
    public static float FLEET_SIZE_MULT = 0.50f;
    public static float GROUND_DEFENSE_MULT = 0.50f;
    public static int HEAVY_PATROL_BONUS = 1;
    public static int SUPPLY_BONUS = 3;
    public String[] industryIds = {
            Industries.POPULATION, Industries.ORBITALWORKS, Industries.HIGHCOMMAND, Industries.FUELPROD
    };

    @Override
    public void apply(String id) {
        market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyMult(id, 1f + FLEET_SIZE_MULT, "Fortress World");
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(id, 1f + GROUND_DEFENSE_MULT, "Fortress World");
        for (String industryId : industryIds) {
            if (!market.hasIndustry(industryId)) continue;
            Industry ind = market.getIndustry(industryId);
            ind.getSupplyBonusFromOther().modifyFlat(id, SUPPLY_BONUS, "Fortress World");

            if (!Objects.equals(industryId, Industries.HIGHCOMMAND)) continue;
            if (ind.getSpec().hasTag(Industries.TAG_PATROL))
                market.getStats().getDynamic().getMod(Stats.PATROL_NUM_MEDIUM_MOD).modifyFlat(id, HEAVY_PATROL_BONUS);
            else
                market.getStats().getDynamic().getMod(Stats.PATROL_NUM_HEAVY_MOD).modifyFlat(id, HEAVY_PATROL_BONUS);
        }
    }

    @Override
    public void unapply(String id) {
        market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodify(id);
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodify(id);
        market.getStats().getDynamic().getMod(Stats.PATROL_NUM_MEDIUM_MOD).unmodify(id);
        market.getStats().getDynamic().getMod(Stats.PATROL_NUM_HEAVY_MOD).unmodify(id);
        for (String industryId : industryIds) {
            if (!market.hasIndustry(industryId)) continue;

            Industry ind = market.getIndustry(industryId);
            ind.getSupplyBonusFromOther().unmodify(id);
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        tooltip.addSpacer(10f);
        tooltip.addPara("%s fleet size", 0f, Misc.getHighlightColor(), "+" + Math.round(FLEET_SIZE_MULT * 100f) + "%");
        tooltip.addSpacer(10f);
        tooltip.addPara("%s ground defense", 0f, Misc.getHighlightColor(), "+" + Math.round(GROUND_DEFENSE_MULT * 100f) + "%");
        tooltip.addSpacer(10f);
        tooltip.addPara("%s number of heavy patrols", 0f, Misc.getHighlightColor(), "+" + HEAVY_PATROL_BONUS);
        tooltip.addSpacer(10f);
        tooltip.addPara("%s production to population & infrastructure, orbital works, high command, and fuel production", 0f, Misc.getHighlightColor(), "+" + SUPPLY_BONUS);
    }
}
