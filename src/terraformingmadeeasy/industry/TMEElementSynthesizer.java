package terraformingmadeeasy.industry;

import com.fs.starfarer.api.impl.campaign.ids.Conditions;

import java.util.Arrays;
import java.util.Collections;

public class TMEElementSynthesizer extends TMEBaseIndustry {
    public TMEElementSynthesizer() {
        // RARE ORE
        this.modifiableConditions.add(new ModifiableCondition(Conditions.RARE_ORE_SPARSE, 2000000f, 90f, false,
                // restrictions
                Arrays.asList(Conditions.RARE_ORE_MODERATE,
                        Conditions.RARE_ORE_ABUNDANT,
                        Conditions.RARE_ORE_RICH,
                        Conditions.RARE_ORE_ULTRARICH),
                // requirements
                Arrays.asList(Conditions.TECTONIC_ACTIVITY,
                        Conditions.HOT)));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.RARE_ORE_MODERATE, 4000000f, 180f, false,
                // restrictions
                Arrays.asList(Conditions.RARE_ORE_SPARSE,
                        Conditions.RARE_ORE_ABUNDANT,
                        Conditions.RARE_ORE_RICH,
                        Conditions.RARE_ORE_ULTRARICH),
                // requirements
                Arrays.asList(Conditions.TECTONIC_ACTIVITY,
                        Conditions.HOT)));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.RARE_ORE_ABUNDANT, 6000000f, 270f, false,
                // restrictions
                Arrays.asList(Conditions.RARE_ORE_SPARSE,
                        Conditions.RARE_ORE_MODERATE,
                        Conditions.RARE_ORE_RICH,
                        Conditions.RARE_ORE_ULTRARICH),
                // requirements
                Arrays.asList(Conditions.EXTREME_TECTONIC_ACTIVITY,
                        Conditions.VERY_HOT)));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.RARE_ORE_RICH, 12000000f, 540f, false,
                // restrictions
                Arrays.asList(Conditions.RARE_ORE_SPARSE,
                        Conditions.RARE_ORE_MODERATE,
                        Conditions.RARE_ORE_ABUNDANT,
                        Conditions.RARE_ORE_ULTRARICH),
                // requirements
                Arrays.asList(Conditions.EXTREME_TECTONIC_ACTIVITY,
                        Conditions.VERY_HOT)));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.RARE_ORE_ULTRARICH, 18000000f, 1080f, false,
                // restrictions
                Arrays.asList(Conditions.RARE_ORE_SPARSE,
                        Conditions.RARE_ORE_MODERATE,
                        Conditions.RARE_ORE_ABUNDANT,
                        Conditions.RARE_ORE_RICH),
                // requirements
                Arrays.asList(Conditions.EXTREME_TECTONIC_ACTIVITY,
                        Conditions.VERY_HOT)));
        // VOLATILES
        this.modifiableConditions.add(new ModifiableCondition(Conditions.VOLATILES_TRACE, 2000000f, 180f, true,
                // restrictions
                Arrays.asList(Conditions.VOLATILES_DIFFUSE,
                        Conditions.VOLATILES_ABUNDANT,
                        Conditions.VOLATILES_PLENTIFUL),
                // requirements
                Collections.singletonList(Conditions.COLD)));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.VOLATILES_DIFFUSE, 4000000f, 360f, true,
                // restrictions
                Arrays.asList(Conditions.VOLATILES_TRACE,
                        Conditions.VOLATILES_ABUNDANT,
                        Conditions.VOLATILES_PLENTIFUL),
                // requirements
                Collections.singletonList(Conditions.COLD)));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.VOLATILES_ABUNDANT, 6000000f, 540f, true,
                // restrictions
                Arrays.asList(Conditions.VOLATILES_TRACE,
                        Conditions.VOLATILES_DIFFUSE,
                        Conditions.VOLATILES_PLENTIFUL),
                // requirements
                Collections.singletonList(Conditions.VERY_COLD)));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.VOLATILES_PLENTIFUL, 12000000f, 1080f, true,
                // restrictions
                Arrays.asList(Conditions.VOLATILES_TRACE,
                        Conditions.VOLATILES_ABUNDANT,
                        Conditions.VOLATILES_ABUNDANT),
                // requirements
                Collections.singletonList(Conditions.VERY_COLD)));
    }
}
