package terraformingmadeeasy.industry;

import com.fs.starfarer.api.impl.campaign.ids.Conditions;

public class TMEAtmosphereRegulator extends TMEBaseIndustry {
    public TMEAtmosphereRegulator() {
        this.modifiableConditions.add(new ModifiableCondition(Conditions.NO_ATMOSPHERE, 100000f, 180f));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.THIN_ATMOSPHERE, 100000f, 365f));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.DENSE_ATMOSPHERE, 100000f, 550f));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.TOXIC_ATMOSPHERE, 100000f, 730f));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.EXTREME_WEATHER, 100000f, 365f));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.MILD_CLIMATE, 100000f, 365f));
    }
}
