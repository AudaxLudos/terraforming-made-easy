package terraformingmadeeasy.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.ids.TMEConditions;

import java.util.Arrays;
import java.util.Collections;

public class UnificationCenter extends TMEBaseIndustry {
    public UnificationCenter() {
        this.modifiableConditions.add(new Utils.ModifiableCondition(Global.getSettings().getMarketConditionSpec(TMEConditions.FORGE_WORLD), 12000000, 1080f, false,
                // Likes conditions
                Collections.singletonList(Conditions.AI_CORE_ADMIN),
                // Hates conditions
                Arrays.asList(TMEConditions.FORTRESS_WORLD, TMEConditions.PARADISE_WORLD),
                // Likes industries
                Arrays.asList(Industries.ORBITALWORKS, Industries.MINING, Industries.REFINING, Industries.FUELPROD),
                // Hates industries
                null));
        this.modifiableConditions.add(new Utils.ModifiableCondition(Global.getSettings().getMarketConditionSpec(TMEConditions.FORTRESS_WORLD), 12000000, 1080f, false,
                // Likes conditions
                Collections.singletonList(Conditions.AI_CORE_ADMIN),
                // Hates conditions
                Arrays.asList(TMEConditions.FORGE_WORLD, TMEConditions.PARADISE_WORLD),
                // Likes industries
                Arrays.asList(Industries.ORBITALWORKS, Industries.HIGHCOMMAND, Industries.REFINING, Industries.FUELPROD),
                // Hates industries
                null));
        this.modifiableConditions.add(new Utils.ModifiableCondition(Global.getSettings().getMarketConditionSpec(TMEConditions.PARADISE_WORLD), 12000000, 1080f, false,
                // Likes conditions
                Collections.singletonList(Conditions.AI_CORE_ADMIN),
                // Hates conditions
                Arrays.asList(TMEConditions.FORGE_WORLD, TMEConditions.FORTRESS_WORLD),
                // Likes industries
                Arrays.asList(Industries.FARMING, Industries.LIGHTINDUSTRY, Industries.REFINING, Industries.FUELPROD),
                // Hates industries
                null));
    }
}
