package terraformingmadeeasy.conditions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.procgen.ConditionGenDataSpec;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import terraformingmadeeasy.listeners.TMEDeathWorldScript;

import java.util.ArrayList;
import java.util.List;

public class DeathWorld extends BaseMarketConditionPlugin {
    public static int SUPPRESS_CONDITION_PER_MONTH_MOD = 6;
    public static float MARINES_TO_TRAIN_MULT = 0.05f;
    public static float GROUND_DEFENSE_MULT = 1.00f;
    public static int SUPPLY_BONUS = 3;
    public List<String> conditionIds = new ArrayList<>();
    public String[] industryIds = {
            Industries.POPULATION, Industries.ORBITALWORKS, Industries.HIGHCOMMAND, Industries.FUELPROD
    };

    @Override
    public void init(MarketAPI market, MarketConditionAPI condition) {
        super.init(market, condition);
        if (!Global.getSector().getListenerManager().hasListenerOfClass(TMEDeathWorldScript.class)) {
            Global.getSector().getListenerManager().addListener(new TMEDeathWorldScript(), true);
        }

        for (ConditionGenDataSpec c : Global.getSettings().getAllSpecs(ConditionGenDataSpec.class)) {
            if (c.getHazard() > 0f) {
                this.conditionIds.add(c.getId());
            }
        }
    }

    @Override
    public void apply(String id) {
        this.market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(id, 1f + GROUND_DEFENSE_MULT, "Death World");
        for (String cId : this.conditionIds) {
            this.market.suppressCondition(cId);
        }
        for (String industryId : this.industryIds) {
            if (!this.market.hasIndustry(industryId)) {
                continue;
            }
            Industry ind = this.market.getIndustry(industryId);
            ind.getSupplyBonusFromOther().modifyFlat(id, SUPPLY_BONUS, "Death World");
        }
    }

    @Override
    public void unapply(String id) {
        this.market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodify(id);
        for (String cId : this.conditionIds) {
            this.market.unsuppressCondition(cId);
        }
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
        tooltip.addSpacer(10f);
        tooltip.addPara("%s negative conditions every %s months", 0f, Misc.getHighlightColor(), "Suppresses", SUPPRESS_CONDITION_PER_MONTH_MOD + "");
        tooltip.addSpacer(10f);
        tooltip.addPara("Trains %s of marines in stockpile for all markets in the system every %s", 0f, Misc.getHighlightColor(), Math.round(MARINES_TO_TRAIN_MULT * 100f) + "%", "month");
        tooltip.addSpacer(10f);
        tooltip.addPara("%s ground defense", 0f, Misc.getHighlightColor(), "+" + Math.round(GROUND_DEFENSE_MULT * 100f) + "%");
        tooltip.addSpacer(10f);
        tooltip.addPara("%s production to population & infrastructure, orbital works, high command, and fuel production", 0f, Misc.getHighlightColor(), "+" + SUPPLY_BONUS);
        tooltip.addSpacer(10f);
    }
}
