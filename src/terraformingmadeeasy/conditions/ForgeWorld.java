package terraformingmadeeasy.conditions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import terraformingmadeeasy.Utils;

public class ForgeWorld extends BaseMarketConditionPlugin {
    public static float FLEET_QUALITY_MOD = 0.50f;
    public static float CUSTOM_PRODUCTION_MULT = 0.50f;
    public static int MAX_INDUSTRIES_BONUS = 1;
    public static int SUPPLY_BONUS = 1;
    public String[] industryIds = {
            Industries.ORBITALWORKS, Industries.MINING, Industries.REFINING, Industries.FUELPROD
    };
    public String[] aotdVokIndustryIds = {
            "supplyheavy", "weaponheavy", "triheavy", "hegeheavy", "orbitalheavy", "stella_manufactorium", "nidavelir_complex",
            "fracking", "mining_megaplex", "pluto_station",
            "crystalizator", "isotope_separator", "policrystalizator", "cascade_reprocesor",
            "blast_processing"
    };

    @Override
    public void apply(String id) {
        this.market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).modifyFlat(id, FLEET_QUALITY_MOD, "Forge World");
        Global.getSector().getPlayerStats().getDynamic().getMod(Stats.CUSTOM_PRODUCTION_MOD).modifyMult(id, 1f + CUSTOM_PRODUCTION_MULT, "Forge World (" + this.market.getName() + ")");
        this.market.getStats().getDynamic().getMod(Stats.MAX_INDUSTRIES).modifyFlat(id, MAX_INDUSTRIES_BONUS, "Forge World");
        for (String industryId : this.industryIds) {
            if (!this.market.hasIndustry(industryId)) {
                continue;
            }

            Industry ind = this.market.getIndustry(industryId);
            ind.getSupplyBonusFromOther().modifyFlat(id, SUPPLY_BONUS, "Forge World");
        }

        if (Utils.isAOTDVOKEnabled()) {
            for (String industryId : this.aotdVokIndustryIds) {
                if (!this.market.hasIndustry(industryId)) {
                    continue;
                }
                Industry ind = this.market.getIndustry(industryId);
                ind.getSupplyBonusFromOther().modifyFlat(id, SUPPLY_BONUS, "Forge World");
            }
        }
    }

    @Override
    public void unapply(String id) {
        this.market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodify(id);
        Global.getSector().getPlayerStats().getDynamic().getMod(Stats.CUSTOM_PRODUCTION_MOD).unmodify(id);
        this.market.getStats().getDynamic().getMod(Stats.MAX_INDUSTRIES).unmodify(id);
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
        tooltip.addPara("%s ship quality", 10f, Misc.getHighlightColor(), "+" + Math.round(FLEET_QUALITY_MOD * 100f) + "%");
        tooltip.addPara("%s maximum value of custom ship and weapon production per month", 10f, Misc.getHighlightColor(), "+" + Math.round(CUSTOM_PRODUCTION_MULT * 100f) + "%");
        tooltip.addPara("%s maximum number of industries", 10f, Misc.getHighlightColor(), "+" + MAX_INDUSTRIES_BONUS);
        tooltip.addPara("%s production to orbital works, refining, mining and fuel production. Applies to all similar structures.", 10f, Misc.getHighlightColor(), "+" + SUPPLY_BONUS);
    }
}
