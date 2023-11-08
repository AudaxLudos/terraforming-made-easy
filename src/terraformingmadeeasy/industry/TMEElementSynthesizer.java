package terraformingmadeeasy.industry;

import com.fs.starfarer.api.impl.campaign.ids.Conditions;

import java.util.Arrays;

public class TMEElementSynthesizer extends TMEBaseIndustry {
    public TMEElementSynthesizer() {
        // RARE ORE
        this.modifiableConditions.add(new ModifiableCondition(Conditions.RARE_ORE_SPARSE, 12000000f, 540f, false,
                // restrictions
                Arrays.asList(Conditions.RARE_ORE_MODERATE,
                        Conditions.RARE_ORE_ABUNDANT,
                        Conditions.RARE_ORE_RICH,
                        Conditions.RARE_ORE_ULTRARICH),
                // requirements
                null));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.RARE_ORE_MODERATE, 12000000f, 540f, false,
                // restrictions
                Arrays.asList(Conditions.RARE_ORE_SPARSE,
                        Conditions.RARE_ORE_ABUNDANT,
                        Conditions.RARE_ORE_RICH,
                        Conditions.RARE_ORE_ULTRARICH),
                // requirements
                null));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.RARE_ORE_ABUNDANT, 12000000f, 540f, false,
                // restrictions
                Arrays.asList(Conditions.RARE_ORE_SPARSE,
                        Conditions.RARE_ORE_MODERATE,
                        Conditions.RARE_ORE_RICH,
                        Conditions.RARE_ORE_ULTRARICH),
                // requirements
                null));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.RARE_ORE_RICH, 12000000f, 540f, false,
                // restrictions
                Arrays.asList(Conditions.RARE_ORE_SPARSE,
                        Conditions.RARE_ORE_MODERATE,
                        Conditions.RARE_ORE_ABUNDANT,
                        Conditions.RARE_ORE_ULTRARICH),
                // requirements
                null));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.RARE_ORE_ULTRARICH, 12000000f, 540f, false,
                // restrictions
                Arrays.asList(Conditions.RARE_ORE_SPARSE,
                        Conditions.RARE_ORE_MODERATE,
                        Conditions.RARE_ORE_ABUNDANT,
                        Conditions.RARE_ORE_RICH),
                // requirements
                null));
        // VOLATILES
        this.modifiableConditions.add(new ModifiableCondition(Conditions.VOLATILES_TRACE, 12000000f, 540f, true,
                // restrictions
                Arrays.asList(Conditions.VOLATILES_DIFFUSE,
                        Conditions.VOLATILES_ABUNDANT,
                        Conditions.VOLATILES_PLENTIFUL),
                // requirements
                null));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.VOLATILES_DIFFUSE, 12000000f, 540f, true,
                // restrictions
                Arrays.asList(Conditions.VOLATILES_TRACE,
                        Conditions.VOLATILES_ABUNDANT,
                        Conditions.VOLATILES_PLENTIFUL),
                // requirements
                null));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.VOLATILES_ABUNDANT, 12000000f, 540f, true,
                // restrictions
                Arrays.asList(Conditions.VOLATILES_TRACE,
                        Conditions.VOLATILES_DIFFUSE,
                        Conditions.VOLATILES_PLENTIFUL),
                // requirements
                null));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.VOLATILES_PLENTIFUL, 12000000f, 540f, true,
                // restrictions
                Arrays.asList(Conditions.VOLATILES_TRACE,
                        Conditions.VOLATILES_ABUNDANT,
                        Conditions.VOLATILES_ABUNDANT),
                // requirements
                null));
    }

    @Override
    public Boolean canTerraformCondition(ModifiableCondition condition) {
        if (condition.requirements.size() == 3)
            return getMarket().hasCondition(condition.requirements.get(0)) || getMarket().hasCondition(condition.requirements.get(1)) || getMarket().hasCondition(condition.requirements.get(2));
        else if (condition.requirements.size() == 2)
            return getMarket().hasCondition(condition.requirements.get(0)) || getMarket().hasCondition(condition.requirements.get(1));
        else if (condition.requirements.size() == 1)
            return getMarket().hasCondition(condition.requirements.get(0));
        return true;
    }
}
