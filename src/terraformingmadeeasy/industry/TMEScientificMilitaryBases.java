package terraformingmadeeasy.industry;

import com.fs.starfarer.api.impl.campaign.ids.Conditions;

public class TMEScientificMilitaryBases extends TMEBaseIndustry {
    public TMEScientificMilitaryBases() {
        this.modifiableConditions.add(new ModifiableCondition(Conditions.INIMICAL_BIOSPHERE, 2000000f, 360f,
                null, null));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.DECIVILIZED, 4000000f, 180f,
                null, null));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.POLLUTION, 1000000f, 360f,
                null, null));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.IRRADIATED, 1000000f, 360f,
                null, null));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.WATER_SURFACE, 1000000f, 360f,
                null, null));
    }
}
