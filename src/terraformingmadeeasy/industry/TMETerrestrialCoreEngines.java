package terraformingmadeeasy.industry;

import com.fs.starfarer.api.impl.campaign.ids.Conditions;

import java.util.Arrays;

public class TMETerrestrialCoreEngines extends TMEBaseIndustry {
    public TMETerrestrialCoreEngines() {
        this.modifiableConditions.add(new ModifiableCondition(Conditions.LOW_GRAVITY, 2000000f, 360f,
                // restrictions
                Arrays.asList(Conditions.HIGH_GRAVITY),
                null));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.HIGH_GRAVITY, 4000000f, 720f,
                // restrictions
                Arrays.asList(Conditions.LOW_GRAVITY),
                null
        ));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.TECTONIC_ACTIVITY, 2000000f, 360f,
                // restrictions
                Arrays.asList(Conditions.EXTREME_TECTONIC_ACTIVITY),
                // requirements
                null
        ));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.EXTREME_TECTONIC_ACTIVITY, 4000000f, 720f,
                // restrictions
                Arrays.asList(Conditions.TECTONIC_ACTIVITY,
                        Conditions.HABITABLE),
                // requirements
                Arrays.asList(Conditions.TECTONIC_ACTIVITY)
        ));
    }
}
