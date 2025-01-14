package terraformingmadeeasy.conditions;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import terraformingmadeeasy.Utils;

import java.util.Objects;

public class FortressWorld extends BaseMarketConditionPlugin {
    public static float FLEET_SIZE_MULT = 0.50f;
    public static float GROUND_DEFENSE_MULT = 0.50f;
    public static int HEAVY_PATROL_BONUS = 1;
    public static int SUPPLY_BONUS = 1;
    public String[] industryIds = {
            Industries.POPULATION, Industries.ORBITALWORKS, Industries.HIGHCOMMAND, Industries.FUELPROD
    };
    public String[] aotdVokIndustryIds = {
            "supplyheavy", "weaponheavy", "triheavy", "hegeheavy", "orbitalheavy", "stella_manufactorium",
            "blast_processing"
    };

    @Override
    public void apply(String id) {
        this.market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyMult(id, 1f + FLEET_SIZE_MULT, "Fortress World");
        this.market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(id, 1f + GROUND_DEFENSE_MULT, "Fortress World");
        for (String industryId : this.industryIds) {
            if (!this.market.hasIndustry(industryId)) {
                continue;
            }
            Industry ind = this.market.getIndustry(industryId);
            ind.getSupplyBonusFromOther().modifyFlat(id, SUPPLY_BONUS, "Fortress World");

            if (!Objects.equals(industryId, Industries.HIGHCOMMAND)) {
                continue;
            }
            if (ind.getSpec().hasTag(Industries.TAG_PATROL)) {
                this.market.getStats().getDynamic().getMod(Stats.PATROL_NUM_MEDIUM_MOD).modifyFlat(id, HEAVY_PATROL_BONUS);
            } else {
                this.market.getStats().getDynamic().getMod(Stats.PATROL_NUM_HEAVY_MOD).modifyFlat(id, HEAVY_PATROL_BONUS);
            }
        }
        if (Utils.isAOTDVOKEnabled()) {
            for (String industryId : this.aotdVokIndustryIds) {
                if (!this.market.hasIndustry(industryId)) {
                    continue;
                }
                Industry ind = this.market.getIndustry(industryId);
                ind.getSupplyBonusFromOther().modifyFlat(id, SUPPLY_BONUS, "Fortress World");
            }
        }
    }

    @Override
    public void unapply(String id) {
        this.market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodify(id);
        this.market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodify(id);
        this.market.getStats().getDynamic().getMod(Stats.PATROL_NUM_MEDIUM_MOD).unmodify(id);
        this.market.getStats().getDynamic().getMod(Stats.PATROL_NUM_HEAVY_MOD).unmodify(id);
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
        tooltip.addPara("%s fleet size", 10f, Misc.getHighlightColor(), "+" + Math.round(FLEET_SIZE_MULT * 100f) + "%");
        tooltip.addPara("%s ground defense", 10f, Misc.getHighlightColor(), "+" + Math.round(GROUND_DEFENSE_MULT * 100f) + "%");
        tooltip.addPara("%s number of heavy patrols", 10f, Misc.getHighlightColor(), "+" + HEAVY_PATROL_BONUS);
        tooltip.addPara("%s production to population & infrastructure, orbital works, high command, and fuel production", 10f, Misc.getHighlightColor(), "+" + SUPPLY_BONUS);
        if (Utils.isAOTDVOKEnabled()) {
            tooltip.addPara("civilian heavy production, militarized heavy industry, orbital skunkworks facility, orbital fleetwork facility, Orbital Manufactorium, " +
                    "and, blast processing unit", 10f, Misc.getHighlightColor(), "+" + SUPPLY_BONUS);
        }
    }
}
