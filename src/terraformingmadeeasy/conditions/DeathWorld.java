package terraformingmadeeasy.conditions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import terraformingmadeeasy.listeners.TMEDeathWorldScript;

import java.util.ArrayList;
import java.util.List;

public class DeathWorld extends BaseMarketConditionPlugin {
    public static int SUPPRESS_CONDITION_PER_MONTH_MOD = 6;
    public static float MARINES_TO_TRAIN_MULT = 0.05f;
    public static float GROUND_DEFENSE_MULT = 0.50f;
    public static int SUPPLY_BONUS = 1;
    public List<MarketConditionAPI> suppressedConditions = new ArrayList<>();
    public int monthsActive = 0;
    public String[] industryIds = {
            Industries.POPULATION, Industries.ORBITALWORKS, Industries.HIGHCOMMAND, Industries.MINING
    };

    @Override
    public void init(MarketAPI market, MarketConditionAPI condition) {
        super.init(market, condition);
        if (!Global.getSector().getListenerManager().hasListenerOfClass(TMEDeathWorldScript.class)) {
            Global.getSector().getListenerManager().addListener(new TMEDeathWorldScript(), true);
        }
    }

    @Override
    public void apply(String id) {
        this.market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(id, 1f + GROUND_DEFENSE_MULT, "Death World");
        for (MarketConditionAPI c : this.suppressedConditions) {
            this.market.suppressCondition(c.getId());
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
        for (MarketConditionAPI c : this.suppressedConditions) {
            this.market.unsuppressCondition(c.getId());
        }
        for (String industryId : this.industryIds) {
            if (!this.market.hasIndustry(industryId)) {
                continue;
            }

            Industry ind = this.market.getIndustry(industryId);
            ind.getSupplyBonusFromOther().unmodify(id);
        }
    }

    @SuppressWarnings("RedundantArrayCreation")
    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        float oPad = 10f;

        tooltip.addPara("%s a random hazardous condition once every %s months", oPad, Misc.getHighlightColor(), "Suppresses", SUPPRESS_CONDITION_PER_MONTH_MOD + "");
        tooltip.addPara("Trains %s of marines in the market's stockpile every %s", oPad, Misc.getHighlightColor(), Math.round(MARINES_TO_TRAIN_MULT * 100f) + "%", "month");
        tooltip.addPara("%s ground defense", oPad, Misc.getHighlightColor(), "+" + Math.round(GROUND_DEFENSE_MULT * 100f) + "%");
        tooltip.addPara("%s production to population & infrastructure, orbital works, high command, and mining", oPad, Misc.getHighlightColor(), "+" + SUPPLY_BONUS);
        tooltip.beginTable2(this.market.getFaction(), 20f, true, true,
                new Object[]{"Suppressed Conditions", tooltip.getWidthSoFar() / 2f});
        tooltip.addTableHeaderTooltip(0, "Name of the suppressed conditions");
        if (this.suppressedConditions.isEmpty()) {
            tooltip.addRow(Alignment.MID, Misc.getGrayColor(), "No conditions suppressed");
        }
        for (MarketConditionAPI c : this.suppressedConditions) {
            tooltip.addRow(Alignment.MID, Misc.getTextColor(), c.getName());
        }
        tooltip.addTable("", 0, 10f);
        tooltip.addSpacer(3f);
    }
}
