package terraformingmadeeasy.conditions;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import terraformingmadeeasy.Settings;
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.ids.TMEIds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HiveWorld extends BaseMarketConditionPlugin implements MarketImmigrationModifier {
    public static final int MAX_MARKET_SIZE_MOD = 1;
    public static final float IMMIGRATION_MOD = 10f;
    public static final int SUPPLY_MOD = 1;
    public static List<String> INDUSTRIES_WITH_SUPPLY_MOD = new ArrayList<>();

    static {
        INDUSTRIES_WITH_SUPPLY_MOD.addAll(Arrays.asList(
                Industries.POPULATION, Industries.FARMING, Industries.MINING, Industries.LIGHTINDUSTRY, Industries.HEAVYINDUSTRY));
        if (Settings.isAoTDVoKEnabled()) {
            INDUSTRIES_WITH_SUPPLY_MOD.addAll(Arrays.asList(
                    "artifarming", "subfarming",
                    "fracking", "mining_megaplex", "pluto_station",
                    "hightech", "druglight", "consumerindustry",
                    "supplyheavy", "weaponheavy", "triheavy", "hegeheavy", "orbitalheavy", "stella_manufactorium", "nidavelir_complex"));
        }
    }

    @Override
    public void apply(String id) {
        boolean nearbyHiveWorld = Utils.getLargestMarketWithCondition(this.market.getContainingLocation(), this.market.getFactionId(), TMEIds.HIVE_WORLD) != null;
        if (nearbyHiveWorld) {
            for (MarketAPI otherMarket : Misc.getMarketsInLocation(this.market.getContainingLocation(), this.market.getFactionId())) {
                boolean hasHiveWorldImmigrationMod = false;
                for (MarketImmigrationModifier modifier : otherMarket.getTransientImmigrationModifiers()) {
                    if (modifier instanceof HiveWorld) {
                        hasHiveWorldImmigrationMod = true;
                        break;
                    }
                }
                if (!hasHiveWorldImmigrationMod) {
                    otherMarket.addTransientImmigrationModifier(this);
                }
            }
        }

        this.market.getStats().getDynamic().getMod(Stats.MAX_MARKET_SIZE).modifyFlat(id, MAX_MARKET_SIZE_MOD);

        for (String industryId : INDUSTRIES_WITH_SUPPLY_MOD) {
            if (!this.market.hasIndustry(industryId)) {
                continue;
            }

            Industry ind = this.market.getIndustry(industryId);
            ind.getSupplyBonusFromOther().modifyFlat(id, SUPPLY_MOD, "Hive World");
        }
    }

    @Override
    public void unapply(String id) {
        boolean nearbyHiveWorld = Utils.getLargestMarketWithCondition(this.market.getContainingLocation(), this.market.getFactionId(), TMEIds.HIVE_WORLD) != null;
        if (!nearbyHiveWorld) {
            for (MarketAPI otherMarket : Misc.getMarketsInLocation(this.market.getContainingLocation(), this.market.getFactionId())) {
                for (MarketImmigrationModifier modifier : otherMarket.getTransientImmigrationModifiers()) {
                    if (modifier instanceof HiveWorld) {
                        otherMarket.addTransientImmigrationModifier(modifier);
                        break;
                    }
                }
            }
        }

        this.market.getStats().getDynamic().getMod(Stats.MAX_MARKET_SIZE).unmodify(id);

        for (String industryId : INDUSTRIES_WITH_SUPPLY_MOD) {
            if (!this.market.hasIndustry(industryId)) {
                continue;
            }

            Industry ind = this.market.getIndustry(industryId);
            ind.getSupplyBonusFromOther().unmodify(id);
        }
    }

    @Override
    public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
        float bonus = getImmigrationBonus();
        if (bonus > 0) {
            MarketAPI otherMarket = Utils.getLargestMarketWithCondition(this.market.getContainingLocation(), this.market.getFactionId(), TMEIds.HIVE_WORLD);
            incoming.add(market.getFactionId(), bonus);
            String nearby = "Nearby hive world";
            if (market == otherMarket) {
                nearby = "Hive world";
            }
            incoming.getWeight().modifyFlat(getModId(), bonus, nearby);
        }
    }

    protected float getImmigrationBonus() {
        MarketAPI market = Utils.getLargestMarketWithCondition(this.market.getContainingLocation(), this.market.getFactionId(), TMEIds.HIVE_WORLD);
        float size = 3f;
        if (market != null) {
            size = market.getSize();
        }
        return IMMIGRATION_MOD * size;
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        tooltip.addPara("%s immigration bonus to all markets in the system, based on the largest market size with hive world", 10f, Misc.getHighlightColor(), "+" + Math.round(getImmigrationBonus()));
        tooltip.addPara("%s max market size, this market can reach up to a colony size of %s", 10f, Misc.getHighlightColor(), "+" + MAX_MARKET_SIZE_MOD, "" + getMaxMarketSize(this.market));
        tooltip.addPara("%s production to population & infrastructure, farming, light industry, heavy industry and similar", 10f, Misc.getHighlightColor(), "+" + SUPPLY_MOD);
    }

    protected int getMaxMarketSize(MarketAPI market) {
        int maxSize = 7;
        if (market != null) {
            maxSize = Misc.getMaxMarketSize(market);
        }
        return maxSize;
    }
}
