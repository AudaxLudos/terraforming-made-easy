package terraformingmadeeasy.industry;

import com.fs.starfarer.api.impl.campaign.ids.Conditions;

public class TMEAtmosphereRegulators extends TMEBaseIndustry {
    public TMEAtmosphereRegulators() {
        this.modifiableConditions.add(new ModifiableCondition(Conditions.NO_ATMOSPHERE, 1000000f, 180f));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.THIN_ATMOSPHERE, 2000000f, 360f));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.DENSE_ATMOSPHERE, 3000000f, 540f));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.TOXIC_ATMOSPHERE, 4000000f, 720f));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.EXTREME_WEATHER, 2000000f, 360f));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.MILD_CLIMATE, 4000000f, 720f));
    }
}
