package terraformingmadeeasy.conditions;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class ParadiseWorld extends BaseMarketConditionPlugin {
    public static float INCOME_MULT = 0.50f;
    public static float ACCESSIBILITY_MOD = 0.50f;
    public static int STABILITY_BONUS = 4;
    public static int SUPPLY_BONUS = 3;
    public String[] industryIds = {
            Industries.POPULATION, Industries.FARMING, Industries.LIGHTINDUSTRY
    };

    @Override
    public void apply(String id) {
        market.getIncomeMult().modifyMult(id, 1f + INCOME_MULT, "Paradise World");
        market.getAccessibilityMod().modifyFlat(id, ACCESSIBILITY_MOD, "Paradise World");
        market.getStability().modifyFlat(id, STABILITY_BONUS, "Paradise World");
        for (String industryId : industryIds) {
            if (!market.hasIndustry(industryId)) continue;

            Industry ind = market.getIndustry(industryId);
            ind.getSupplyBonusFromOther().modifyFlat(id, SUPPLY_BONUS, "Paradise World");
        }
    }

    @Override
    public void unapply(String id) {
        market.getStability().unmodify(id);
        market.getIncomeMult().unmodify(id);
        market.getAccessibilityMod().unmodify(id);
        for (String industryId : industryIds) {
            if (!market.hasIndustry(industryId)) continue;

            Industry ind = market.getIndustry(industryId);
            ind.getSupplyBonusFromOther().unmodify(id);
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        tooltip.addSpacer(10f);
        tooltip.addPara("%s income", 0f, Misc.getHighlightColor(), "+" + Math.round(INCOME_MULT * 100f) + "%");
        tooltip.addSpacer(10f);
        tooltip.addPara("%s accessibility", 0f, Misc.getHighlightColor(), "+" + Math.round(ACCESSIBILITY_MOD * 100f) + "%");
        tooltip.addSpacer(10f);
        tooltip.addPara("%s stability", 0f, Misc.getHighlightColor(), "+" + STABILITY_BONUS);
        tooltip.addSpacer(10f);
        tooltip.addPara("%s production to population & infrastructure, farming, and light industry", 0f, Misc.getHighlightColor(), "+" + SUPPLY_BONUS);
    }
}
