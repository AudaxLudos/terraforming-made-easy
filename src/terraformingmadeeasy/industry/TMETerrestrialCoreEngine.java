package terraformingmadeeasy.industry;

import com.fs.starfarer.api.impl.campaign.ids.Conditions;

import java.util.Arrays;
import java.util.Collections;

public class TMETerrestrialCoreEngine extends TMEBaseIndustry {
    public TMETerrestrialCoreEngine() {
        this.modifiableConditions.add(new ModifiableCondition(Conditions.LOW_GRAVITY, 2000000f, 360f,
                // restrictions
                Collections.singletonList(Conditions.HIGH_GRAVITY),
                null));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.HIGH_GRAVITY, 4000000f, 720f,
                // restrictions
                Collections.singletonList(Conditions.LOW_GRAVITY),
                // requirements
                null
        ));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.TECTONIC_ACTIVITY, 2000000f, 360f,
                // restrictions
                Collections.singletonList(Conditions.EXTREME_TECTONIC_ACTIVITY),
                // requirements
                null
        ));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.EXTREME_TECTONIC_ACTIVITY, 4000000f, 720f,
                // restrictions
                Arrays.asList(Conditions.TECTONIC_ACTIVITY,
                        Conditions.HABITABLE),
                // requirements
                null
        ));
    }
}
