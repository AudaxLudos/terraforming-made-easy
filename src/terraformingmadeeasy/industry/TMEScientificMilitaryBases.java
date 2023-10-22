package terraformingmadeeasy.industry;

import com.fs.starfarer.api.impl.campaign.ids.Conditions;

public class TMEScientificMilitaryBases extends TMEBaseIndustry {
    public TMEScientificMilitaryBases (){
        this.modifiableConditions.add(new ModifiableCondition(Conditions.INIMICAL_BIOSPHERE, 100000f, 730f));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.DECIVILIZED, 100000f, 180f));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.DECIVILIZED_SUBPOP, 100000f, 365f));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.POLLUTION, 100000f, 365f));
    }
}
