package terraformingmadeeasy.industry;

import com.fs.starfarer.api.impl.campaign.ids.Conditions;

import java.util.Arrays;
import java.util.Collections;

public class TerrestrialEngine extends BaseIndustry {
    public TerrestrialEngine() {
        this.modifiableConditions.add(new ModifiableCondition(Conditions.LOW_GRAVITY, 2000000f, 360f, false,
                // restrictions
                Collections.singletonList(Conditions.HIGH_GRAVITY),
                null));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.HIGH_GRAVITY, 4000000f, 720f, true,
                // restrictions
                Collections.singletonList(Conditions.LOW_GRAVITY),
                // requirements
                null));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.TECTONIC_ACTIVITY, 2000000f, 360f, false,
                // restrictions
                Collections.singletonList(Conditions.EXTREME_TECTONIC_ACTIVITY),
                // requirements
                null));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.EXTREME_TECTONIC_ACTIVITY, 4000000f, 720f, false,
                // restrictions
                Arrays.asList(Conditions.TECTONIC_ACTIVITY,
                        Conditions.HABITABLE),
                // requirements
                null));
    }
}