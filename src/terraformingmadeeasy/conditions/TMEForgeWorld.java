package terraformingmadeeasy.conditions;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.Objects;

public class TMEForgeWorld extends BaseMarketConditionPlugin {
    public static float FORGE_WORLD_FLEET_SIZE_MULT = 0.50f;
    public static float FORGE_WORLD_FLEET_QUALITY_MULT = 0.50f;
    public static float FORGE_WORLD_GROUND_DEFENSE_MULT = 0.50f;
    public static int FORGE_WORLD_NUM_PATROL_BONUS = 1;
    public static int FORGE_WORLD_SUPPLY_BONUS = 3;
    public String[] industryIds = {
            Industries.HIGHCOMMAND, Industries.ORBITALWORKS, Industries.MINING, Industries.REFINING
    };

    @Override
    public void apply(String id) {
        for (String industryId : industryIds) {
            if (market.hasIndustry(industryId)) {
                Industry ind = market.getIndustry(industryId);
                ind.getSupplyBonusFromOther().modifyFlat(id, FORGE_WORLD_SUPPLY_BONUS, "Forge World");

                if (Objects.equals(industryId, Industries.HIGHCOMMAND)) {
                    if (ind.getSpec().hasTag(Industries.TAG_PATROL))
                        market.getStats().getDynamic().getMod(Stats.PATROL_NUM_MEDIUM_MOD).modifyFlat(id, FORGE_WORLD_NUM_PATROL_BONUS);
                    else
                        market.getStats().getDynamic().getMod(Stats.PATROL_NUM_HEAVY_MOD).modifyFlat(id, FORGE_WORLD_NUM_PATROL_BONUS);
                }
            }
        }
        market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).modifyFlat(id, FORGE_WORLD_FLEET_QUALITY_MULT, "Forge World");
        market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyMult(id, 1f + FORGE_WORLD_FLEET_SIZE_MULT, "Forge World");
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(id, 1f + FORGE_WORLD_GROUND_DEFENSE_MULT, "Forge World");
    }

    @Override
    public void unapply(String id) {
        for (String industryId : industryIds) {
            if (market.hasIndustry(industryId)) {
                Industry ind = market.getIndustry(industryId);
                ind.getSupplyBonusFromOther().unmodify(id);

                market.getStats().getDynamic().getMod(Stats.PATROL_NUM_MEDIUM_MOD).unmodify(id);
                market.getStats().getDynamic().getMod(Stats.PATROL_NUM_HEAVY_MOD).unmodify(id);
            }
        }
        market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodify(id);
        market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodify(id);
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        tooltip.addSpacer(10f);
        tooltip.addPara("%s fleet size", 0f, Misc.getHighlightColor(), "+" + Math.round(FORGE_WORLD_FLEET_SIZE_MULT * 100f) + "%");
        tooltip.addSpacer(10f);
        tooltip.addPara("%s ship quality", 0f, Misc.getHighlightColor(), "+" + Math.round(FORGE_WORLD_FLEET_QUALITY_MULT * 100f) + "%");
        tooltip.addSpacer(10f);
        tooltip.addPara("%s ground defense", 0f, Misc.getHighlightColor(), "+" + Math.round(FORGE_WORLD_GROUND_DEFENSE_MULT * 100f) + "%");
        tooltip.addSpacer(10f);
        tooltip.addPara("%s number of heavy patrols", 0f, Misc.getHighlightColor(), "+" + FORGE_WORLD_NUM_PATROL_BONUS);
        tooltip.addSpacer(10f);
        tooltip.addPara("%s production to orbital works, refining, mining, high command", 0f, Misc.getHighlightColor(), "+" + FORGE_WORLD_SUPPLY_BONUS);
    }
}
