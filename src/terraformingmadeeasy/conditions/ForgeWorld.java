package terraformingmadeeasy.conditions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class ForgeWorld extends BaseMarketConditionPlugin {
    public static float FLEET_QUALITY_MOD = 0.50f;
    public static float CUSTOM_PRODUCTION_MULT = 0.50f;
    public static int MAX_INDUSTRIES_BONUS = 1;
    public static int SUPPLY_BONUS = 3;
    public String[] industryIds = {
            Industries.ORBITALWORKS, Industries.MINING, Industries.REFINING, Industries.FUELPROD
    };

    @Override
    public void apply(String id) {
        market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).modifyFlat(id, FLEET_QUALITY_MOD, "Forge World");
        Global.getSector().getPlayerStats().getDynamic().getMod(Stats.CUSTOM_PRODUCTION_MOD).modifyMult(id, 1f + CUSTOM_PRODUCTION_MULT, "Forge World (" + market.getName() + ")");
        market.getStats().getDynamic().getMod(Stats.MAX_INDUSTRIES).modifyFlat(id, MAX_INDUSTRIES_BONUS, "Forge World");
        for (String industryId : industryIds) {
            if (!market.hasIndustry(industryId)) continue;

            Industry ind = market.getIndustry(industryId);
            ind.getSupplyBonusFromOther().modifyFlat(id, SUPPLY_BONUS, "Forge World");
        }
    }

    @Override
    public void unapply(String id) {
        market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodify(id);
        Global.getSector().getPlayerStats().getDynamic().getMod(Stats.CUSTOM_PRODUCTION_MOD).unmodify(id);
        market.getStats().getDynamic().getMod(Stats.MAX_INDUSTRIES).unmodify(id);
        for (String industryId : industryIds) {
            if (!market.hasIndustry(industryId)) continue;

            Industry ind = market.getIndustry(industryId);
            ind.getSupplyBonusFromOther().unmodify(id);
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        tooltip.addSpacer(10f);
        tooltip.addPara("%s ship quality", 0f, Misc.getHighlightColor(), "+" + Math.round(FLEET_QUALITY_MOD * 100f) + "%");
        tooltip.addSpacer(10f);
        tooltip.addPara("%s maximum value of custom ship and weapon production per month", 0f, Misc.getHighlightColor(), "+" + Math.round(CUSTOM_PRODUCTION_MULT * 100f) + "%");
        tooltip.addSpacer(10f);
        tooltip.addPara("%s maximum number of industries", 0f, Misc.getHighlightColor(), "+" + MAX_INDUSTRIES_BONUS);
        tooltip.addSpacer(10f);
        tooltip.addPara("%s production to orbital works, refining, mining and fuel production", 0f, Misc.getHighlightColor(), "+" + SUPPLY_BONUS);
    }
}
