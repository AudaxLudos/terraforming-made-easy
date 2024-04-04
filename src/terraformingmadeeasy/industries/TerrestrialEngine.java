package terraformingmadeeasy.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import terraformingmadeeasy.Utils;

import java.util.Arrays;
import java.util.Collections;

public class TerrestrialEngine extends TMEBaseIndustry {
    public TerrestrialEngine() {
        this.modifiableConditions.add(new Utils.ModifiableCondition(Global.getSettings().getMarketConditionSpec(Conditions.LOW_GRAVITY), 2000000f, 360f, false,
                // Likes conditions
                null,
                // Hates conditions
                Collections.singletonList(Conditions.HIGH_GRAVITY)));
        this.modifiableConditions.add(new Utils.ModifiableCondition(Global.getSettings().getMarketConditionSpec(Conditions.HIGH_GRAVITY), 4000000f, 720f, true,
                // Likes conditions
                null,
                // Hates conditions
                Collections.singletonList(Conditions.LOW_GRAVITY)));
        this.modifiableConditions.add(new Utils.ModifiableCondition(Global.getSettings().getMarketConditionSpec(Conditions.TECTONIC_ACTIVITY), 2000000f, 360f, false,
                // Likes conditions
                null,
                // Hates conditions
                Collections.singletonList(Conditions.EXTREME_TECTONIC_ACTIVITY)));
        this.modifiableConditions.add(new Utils.ModifiableCondition(Global.getSettings().getMarketConditionSpec(Conditions.EXTREME_TECTONIC_ACTIVITY), 4000000f, 720f, false,
                // Likes conditions
                null,
                // Hates conditions
                Arrays.asList(Conditions.TECTONIC_ACTIVITY,
                        Conditions.HABITABLE)));
    }
}
