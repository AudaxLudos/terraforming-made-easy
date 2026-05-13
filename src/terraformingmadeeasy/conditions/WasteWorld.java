package terraformingmadeeasy.conditions;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.ids.TMEIds;

public class WasteWorld extends BaseMarketConditionPlugin {
    public static final float OTHER_MARKET_ACCESS_MOD = 0.25f;
    public static final float OTHER_MARKET_HAZARD_MOD = 0.1f;
    public static final float MARKET_HAZARD_MOD = 0.25f;
    public static final float TECH_MINING_MULT = 0.25f;
    public static final int SUPPLY_MOD = 1;
    public final String[] industryIds = {
            // Vanilla
            Industries.POPULATION, Industries.MINING, Industries.REFINING,
            // AotD
            "fracking", "mining_megaplex", "pluto_station",
            "crystalizator", "isotope_separator", "policrystalizator", "cascade_reprocesor",
    };

    @Override
    public void apply(String id) {
        MarketAPI wasteWorldMarket = Utils.getLargestMarketWithCondition(this.market.getContainingLocation(), this.market.getFactionId(), "tme_waste_world");
        if (wasteWorldMarket != null) {
            for (MarketAPI otherMarket : Misc.getMarketsInLocation(this.market.getContainingLocation(), this.market.getFactionId())) {
                if (otherMarket.hasCondition(TMEIds.WASTE_WORLD)) {
                    otherMarket.getAccessibilityMod().unmodify(id);
                    otherMarket.getHazard().unmodify(id);
                    continue;
                }
                otherMarket.getAccessibilityMod().modifyFlat(id, OTHER_MARKET_ACCESS_MOD, "Nearby waste world");
                otherMarket.getHazard().modifyFlat(id, -OTHER_MARKET_HAZARD_MOD, "Nearby waste world");
            }
        }

        this.market.getHazard().modifyFlat(id, MARKET_HAZARD_MOD, "Waste World");
        this.market.getStats().getDynamic().getStat(Stats.TECH_MINING_MULT).modifyMult(id, 1f + TECH_MINING_MULT);

        for (String industryId : this.industryIds) {
            if (!this.market.hasIndustry(industryId)) {
                continue;
            }

            Industry ind = this.market.getIndustry(industryId);
            ind.getSupplyBonusFromOther().modifyFlat(id, SUPPLY_MOD, "Hive World");
        }
    }

    @Override
    public void unapply(String id) {
        MarketAPI wasteWorldMarket = Utils.getLargestMarketWithCondition(this.market.getContainingLocation(), this.market.getFactionId(), "tme_waste_world");
        if (wasteWorldMarket == null) {
            for (MarketAPI otherMarket : Misc.getMarketsInLocation(this.market.getContainingLocation(), this.market.getFactionId())) {
                otherMarket.getAccessibilityMod().unmodify(id);
                otherMarket.getHazard().unmodify(id);
            }
        }

        this.market.getHazard().unmodify(id);
        this.market.getStats().getDynamic().getStat(Stats.TECH_MINING_MULT).unmodify(id);

        for (String industryId : this.industryIds) {
            if (!this.market.hasIndustry(industryId)) {
                continue;
            }

            Industry ind = this.market.getIndustry(industryId);
            ind.getSupplyBonusFromOther().unmodify(id);
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        tooltip.addPara("%s hazard rating", 10f, Misc.getHighlightColor(), "+" + Math.round(MARKET_HAZARD_MOD * 100f) + "%");
        tooltip.addPara("%s tech mining finds", 10f, Misc.getHighlightColor(), "+" + Math.round(TECH_MINING_MULT * 100f) + "%");
        tooltip.addPara("%s accessibility bonus to all markets in the system", 10f, Misc.getHighlightColor(), "+" + Math.round(OTHER_MARKET_ACCESS_MOD * 100f) + "%");
        tooltip.addPara("%s hazard rating to all markets in the system", 10f, Misc.getHighlightColor(), "-" + Math.round(OTHER_MARKET_HAZARD_MOD * 100f) + "%");
        tooltip.addPara("%s production to population & infrastructure, refining, heavy industry and similar", 10f, Misc.getHighlightColor(), "+" + SUPPLY_MOD);
    }
}
