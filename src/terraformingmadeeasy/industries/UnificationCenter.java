package terraformingmadeeasy.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.ids.TMEConditions;

import java.util.Arrays;

public class UnificationCenter extends TMEBaseIndustry {
    public UnificationCenter() {
        this.modifiableConditions.add(new Utils.ModifiableCondition(Global.getSettings().getMarketConditionSpec(TMEConditions.FORGE_WORLD), 12000000, 1080f, false,
                // Likes conditions
                Arrays.asList(Conditions.ORE_ULTRARICH, Conditions.RARE_ORE_ULTRARICH),
                // Hates conditions
                Arrays.asList(TMEConditions.FORTRESS_WORLD, TMEConditions.PARADISE_WORLD),
                // Likes industries
                Arrays.asList(Industries.MEGAPORT, Industries.ORBITALWORKS, Industries.MINING, Industries.REFINING),
                // Hates industries
                null));
        this.modifiableConditions.add(new Utils.ModifiableCondition(Global.getSettings().getMarketConditionSpec(TMEConditions.FORTRESS_WORLD), 12000000, 1080f, false,
                // Likes conditions
                Arrays.asList(Conditions.VOLATILES_PLENTIFUL, Conditions.HABITABLE),
                // Hates conditions
                Arrays.asList(TMEConditions.FORGE_WORLD, TMEConditions.PARADISE_WORLD),
                // Likes industries
                Arrays.asList(Industries.MEGAPORT, Industries.ORBITALWORKS, Industries.HIGHCOMMAND, Industries.FUELPROD),
                // Hates industries
                null));
        this.modifiableConditions.add(new Utils.ModifiableCondition(Global.getSettings().getMarketConditionSpec(TMEConditions.PARADISE_WORLD), 12000000, 1080f, false,
                // Likes conditions
                Arrays.asList(Conditions.FARMLAND_BOUNTIFUL, Conditions.ORGANICS_PLENTIFUL, Conditions.HABITABLE),
                // Hates conditions
                Arrays.asList(TMEConditions.FORGE_WORLD, TMEConditions.FORTRESS_WORLD),
                // Likes industries
                Arrays.asList(Industries.MEGAPORT, Industries.FARMING, Industries.LIGHTINDUSTRY, Industries.COMMERCE),
                // Hates industries
                null));
    }
}
