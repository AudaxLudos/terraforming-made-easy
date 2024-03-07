package terraformingmadeeasy.industries;

import com.fs.starfarer.api.impl.campaign.ids.Conditions;

import java.util.Arrays;
import java.util.Collections;

public class MineralReplicator extends BaseIndustry {
    public MineralReplicator() {
        // ORGANICS
        this.modifiableConditions.add(new ModifiableCondition(Conditions.ORGANICS_TRACE, 2000000f, 90f, false,
                // Likes conditions
                Arrays.asList(Conditions.HABITABLE, Conditions.THIN_ATMOSPHERE, Conditions.TOXIC_ATMOSPHERE),
                // Hates conditions
                Arrays.asList(Conditions.ORGANICS_COMMON,
                        Conditions.ORGANICS_ABUNDANT,
                        Conditions.ORGANICS_PLENTIFUL)));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.ORGANICS_COMMON, 4000000f, 180f, false,
                // Likes conditions
                Arrays.asList(Conditions.HABITABLE, Conditions.THIN_ATMOSPHERE, Conditions.TOXIC_ATMOSPHERE),
                // Hates conditions
                Arrays.asList(Conditions.ORGANICS_TRACE,
                        Conditions.ORGANICS_ABUNDANT,
                        Conditions.ORGANICS_PLENTIFUL)));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.ORGANICS_ABUNDANT, 6000000f, 270f, false,
                // Likes conditions
                Collections.singletonList(Conditions.HABITABLE),
                // Hates conditions
                Arrays.asList(Conditions.ORGANICS_TRACE,
                        Conditions.ORGANICS_COMMON,
                        Conditions.ORGANICS_PLENTIFUL)));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.ORGANICS_PLENTIFUL, 12000000f, 540f, false,
                // Likes conditions
                Collections.singletonList(Conditions.HABITABLE),
                // Hates conditions
                Arrays.asList(Conditions.ORGANICS_TRACE,
                        Conditions.ORGANICS_COMMON,
                        Conditions.ORGANICS_ABUNDANT)));
        // ORE
        this.modifiableConditions.add(new ModifiableCondition(Conditions.ORE_SPARSE, 2000000f, 90f, false,
                // Likes conditions
                Arrays.asList(Conditions.TECTONIC_ACTIVITY,
                        Conditions.HOT),
                // Hates conditions
                Arrays.asList(Conditions.ORE_MODERATE,
                        Conditions.ORE_ABUNDANT,
                        Conditions.ORE_RICH,
                        Conditions.ORE_ULTRARICH)));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.ORE_MODERATE, 4000000f, 180f, false,
                // Likes conditions
                Arrays.asList(Conditions.TECTONIC_ACTIVITY,
                        Conditions.HOT),
                // Hates conditions
                Arrays.asList(Conditions.ORE_SPARSE,
                        Conditions.ORE_ABUNDANT,
                        Conditions.ORE_RICH,
                        Conditions.ORE_ULTRARICH)));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.ORE_ABUNDANT, 6000000f, 270f, false,
                // Likes conditions
                Arrays.asList(Conditions.EXTREME_TECTONIC_ACTIVITY,
                        Conditions.VERY_HOT),
                // Hates conditions
                Arrays.asList(Conditions.ORE_SPARSE,
                        Conditions.ORE_MODERATE,
                        Conditions.ORE_RICH,
                        Conditions.ORE_ULTRARICH)));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.ORE_RICH, 12000000f, 540f, false,
                // Likes conditions
                Arrays.asList(Conditions.EXTREME_TECTONIC_ACTIVITY,
                        Conditions.VERY_HOT),
                // Hates conditions
                Arrays.asList(Conditions.ORE_SPARSE,
                        Conditions.ORE_MODERATE,
                        Conditions.ORE_ABUNDANT,
                        Conditions.ORE_ULTRARICH)));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.ORE_ULTRARICH, 18000000f, 1080f, false,
                // Likes conditions
                Arrays.asList(Conditions.EXTREME_TECTONIC_ACTIVITY,
                        Conditions.VERY_HOT),
                // Hates conditions
                Arrays.asList(Conditions.ORE_SPARSE,
                        Conditions.ORE_MODERATE,
                        Conditions.ORE_ABUNDANT,
                        Conditions.ORE_RICH)));
    }
}
