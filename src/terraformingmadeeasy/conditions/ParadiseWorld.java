package terraformingmadeeasy.conditions;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import terraformingmadeeasy.Utils;

public class ParadiseWorld extends BaseMarketConditionPlugin {
    public static float INCOME_MULT = 0.50f;
    public static float ACCESSIBILITY_MOD = 0.50f;
    public static int STABILITY_BONUS = 4;
    public static int SUPPLY_BONUS = 1;
    public String[] industryIds = {
            Industries.POPULATION, Industries.FARMING, Industries.AQUACULTURE, Industries.LIGHTINDUSTRY
    };
    public String[] aotdVokIndustryIds = {
            "artifarming", "subfarming",
            "hightech", "druglight", "consumerindustry"
    };

    @Override
    public void apply(String id) {
        this.market.getIncomeMult().modifyMult(id, 1f + INCOME_MULT, "Paradise World");
        this.market.getAccessibilityMod().modifyFlat(id, ACCESSIBILITY_MOD, "Paradise World");
        this.market.getStability().modifyFlat(id, STABILITY_BONUS, "Paradise World");
        for (String industryId : this.industryIds) {
            if (!this.market.hasIndustry(industryId)) {
                continue;
            }

            Industry ind = this.market.getIndustry(industryId);
            ind.getSupplyBonusFromOther().modifyFlat(id, SUPPLY_BONUS, "Paradise World");
        }
        if (Utils.isAOTDVOKEnabled()) {
            for (String industryId : this.aotdVokIndustryIds) {
                if (!this.market.hasIndustry(industryId)) {
                    continue;
                }
                Industry ind = this.market.getIndustry(industryId);
                ind.getSupplyBonusFromOther().modifyFlat(id, SUPPLY_BONUS, "Paradise World");
            }
        }
    }

    @Override
    public void unapply(String id) {
        this.market.getStability().unmodify(id);
        this.market.getIncomeMult().unmodify(id);
        this.market.getAccessibilityMod().unmodify(id);
        for (String industryId : this.industryIds) {
            if (!this.market.hasIndustry(industryId)) {
                continue;
            }

            Industry ind = this.market.getIndustry(industryId);
            ind.getSupplyBonusFromOther().unmodify(id);
        }
        if (Utils.isAOTDVOKEnabled()) {
            for (String industryId : this.aotdVokIndustryIds) {
                if (!this.market.hasIndustry(industryId)) {
                    continue;
                }

                Industry ind = this.market.getIndustry(industryId);
                ind.getSupplyBonusFromOther().unmodify(id);
            }
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        tooltip.addPara("%s income", 10f, Misc.getHighlightColor(), "+" + Math.round(INCOME_MULT * 100f) + "%");
        tooltip.addPara("%s accessibility", 10f, Misc.getHighlightColor(), "+" + Math.round(ACCESSIBILITY_MOD * 100f) + "%");
        tooltip.addPara("%s stability", 10f, Misc.getHighlightColor(), "+" + STABILITY_BONUS);
        tooltip.addPara("%s production to population & infrastructure, farming, and light industry. Applies to all similar structures.", 10f, Misc.getHighlightColor(), "+" + SUPPLY_BONUS);
    }
}
