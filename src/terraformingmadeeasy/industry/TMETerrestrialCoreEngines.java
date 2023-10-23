package terraformingmadeeasy.industry;

import com.fs.starfarer.api.impl.campaign.ids.Conditions;

public class TMETerrestrialCoreEngines extends TMEBaseIndustry {
    public TMETerrestrialCoreEngines() {
        this.modifiableConditions.add(new ModifiableCondition(Conditions.LOW_GRAVITY, 2000000f, 360f));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.HIGH_GRAVITY, 4000000f, 720f));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.TECTONIC_ACTIVITY, 2000000f, 360f));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.EXTREME_TECTONIC_ACTIVITY, 4000000f, 720f));
    }
}
