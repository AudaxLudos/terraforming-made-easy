package terraformingmadeeasy.conditions;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class TMEForgeWorld extends BaseMarketConditionPlugin {
    public static float FLEET_QUALITY_MULT = 0.50f;
    public static float GROUND_DEFENSE_MULT = 0.25f;
    public static int SUPPLY_BONUS = 3;
    public String[] industryIds = {
            Industries.ORBITALWORKS, Industries.MINING, Industries.REFINING, Industries.FUELPROD
    };

    @Override
    public void apply(String id) {
        market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).modifyFlat(id, FLEET_QUALITY_MULT, "Forge World");
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(id, 1f + GROUND_DEFENSE_MULT, "Forge World");
        for (String industryId : industryIds) {
            if (!market.hasIndustry(industryId)) continue;

            Industry ind = market.getIndustry(industryId);
            ind.getSupplyBonusFromOther().modifyFlat(id, SUPPLY_BONUS, "Forge World");
        }
    }

    @Override
    public void unapply(String id) {
        market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodify(id);
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodify(id);
        for (String industryId : industryIds) {
            if (!market.hasIndustry(industryId)) continue;

            Industry ind = market.getIndustry(industryId);
            ind.getSupplyBonusFromOther().unmodify(id);
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        tooltip.addSpacer(10f);
        tooltip.addPara("%s ship quality", 0f, Misc.getHighlightColor(), "+" + Math.round(FLEET_QUALITY_MULT * 100f) + "%");
        tooltip.addSpacer(10f);
        tooltip.addPara("%s ground defense", 0f, Misc.getHighlightColor(), "+" + Math.round(GROUND_DEFENSE_MULT * 100f) + "%");
        tooltip.addSpacer(10f);
        tooltip.addPara("%s production to orbital works, refining, mining and fuel production", 0f, Misc.getHighlightColor(), "+" + SUPPLY_BONUS);
    }
}
