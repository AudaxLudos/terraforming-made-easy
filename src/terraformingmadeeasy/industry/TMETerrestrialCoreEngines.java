package terraformingmadeeasy.industry;

import com.fs.starfarer.api.impl.campaign.ids.Conditions;

public class TMETerrestrialCoreEngines extends TMEBaseIndustry {
    public TMETerrestrialCoreEngines() {
        this.modifiableConditions.add(new ModifiableCondition(Conditions.LOW_GRAVITY, 100000f, 365f));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.HIGH_GRAVITY, 100000f, 730f));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.TECTONIC_ACTIVITY, 100000f, 365f));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.EXTREME_TECTONIC_ACTIVITY, 100000f, 730f));
    }
}
