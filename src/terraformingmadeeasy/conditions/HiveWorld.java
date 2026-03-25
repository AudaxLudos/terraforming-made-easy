package terraformingmadeeasy.conditions;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import terraformingmadeeasy.Settings;

public class HiveWorld extends BaseMarketConditionPlugin implements MarketImmigrationModifier {
    public static final int MAX_MARKET_SIZE_MOD = 1;
    public static final int SUPPLY_MOD = 1;
    public static final float IMMIGRATION_MOD = 10f;
    public final String[] industryIds = {
            Industries.POPULATION, Industries.FARMING, Industries.MINING, Industries.LIGHTINDUSTRY, Industries.HEAVYINDUSTRY
    };
    public final String[] aotdVokIndustryIds = {
            "artifarming", "subfarming",
            "fracking", "mining_megaplex", "pluto_station",
            "hightech", "druglight", "consumerindustry",
            "supplyheavy", "weaponheavy", "triheavy", "hegeheavy", "orbitalheavy", "stella_manufactorium", "nidavelir_complex"
    };

    @Override
    public void apply(String id) {
        for (MarketAPI market : Misc.getMarketsInLocation(this.market.getContainingLocation(), Factions.PLAYER)) {
            boolean hasHiveWorldBonus = false;
            for (MarketImmigrationModifier modifier : market.getTransientImmigrationModifiers()) {
                if (modifier instanceof HiveWorld) {
                    hasHiveWorldBonus = true;
                    break;
                }
            }
            if (!hasHiveWorldBonus) {
                market.addTransientImmigrationModifier(this);
            }
        }

        this.market.getStats().getDynamic().getStat(Stats.MAX_MARKET_SIZE).modifyFlat(id, MAX_MARKET_SIZE_MOD);

        for (String industryId : this.industryIds) {
            if (!this.market.hasIndustry(industryId)) {
                continue;
            }

            Industry ind = this.market.getIndustry(industryId);
            ind.getSupplyBonusFromOther().modifyFlat(id, SUPPLY_MOD, "Hive World");
        }
        if (Settings.isAoTDVoKEnabled()) {
            for (String industryId : this.aotdVokIndustryIds) {
                if (!this.market.hasIndustry(industryId)) {
                    continue;
                }
                Industry ind = this.market.getIndustry(industryId);
                ind.getSupplyBonusFromOther().modifyFlat(id, SUPPLY_MOD, "Hive World");
            }
        }
    }

    @Override
    public void unapply(String id) {
        for (MarketAPI market : Misc.getMarketsInLocation(this.market.getContainingLocation(), Factions.PLAYER)) {
            market.removeTransientImmigrationModifier(this);
        }

        this.market.getStats().getDynamic().getStat(Stats.MAX_MARKET_SIZE).unmodify(id);

        for (String industryId : this.industryIds) {
            if (!this.market.hasIndustry(industryId)) {
                continue;
            }

            Industry ind = this.market.getIndustry(industryId);
            ind.getSupplyBonusFromOther().unmodify(id);
        }
        if (Settings.isAoTDVoKEnabled()) {
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
    public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
        float bonus = getImmigrationBonus();
        if (bonus > 0) {
            incoming.add(this.market.getFactionId(), bonus);
            incoming.getWeight().modifyFlat(getModId(), bonus, "Hive World");
        }
    }

    protected float getImmigrationBonus() {
        return IMMIGRATION_MOD * this.market.getSize();
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        tooltip.addPara("%s immigration bonus to all markets in the system, based on this market's size", 10f, Misc.getHighlightColor(), "+" + Math.round(getImmigrationBonus()));
        tooltip.addPara("%s max market size, this market can reach up to a colony size of %s", 10f, Misc.getHighlightColor(), "+" + MAX_MARKET_SIZE_MOD, "" + Misc.getMaxMarketSize(this.market));
        tooltip.addPara("%s production to population & infrastructure, farming, light industry, heavy industry and similar.", 10f, Misc.getHighlightColor(), "+" + SUPPLY_MOD);
    }
}
