package terraformingmadeeasy.industry;

import com.fs.starfarer.api.impl.campaign.ids.Conditions;

public class TMEScientificMilitaryBases extends TMEBaseIndustry {
    public TMEScientificMilitaryBases() {
        this.modifiableConditions.add(new ModifiableCondition(Conditions.INIMICAL_BIOSPHERE, 2000000f, 360f));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.DECIVILIZED, 4000000f, 180f));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.DECIVILIZED_SUBPOP, 4000000f, 360f));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.POLLUTION, 1000000f, 360f));
    }
}
