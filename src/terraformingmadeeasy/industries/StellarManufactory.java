package terraformingmadeeasy.industries;

import com.fs.starfarer.api.impl.campaign.ids.Conditions;

import java.util.Arrays;
import java.util.Collections;

public class StellarManufactory extends BaseIndustry {
    public StellarManufactory() {
        this.modifiableConditions.add(new ModifiableCondition(Conditions.SOLAR_ARRAY, 4000000, 360f, false,
                // Likes conditions
                Collections.singletonList(Conditions.HABITABLE),
                // Hates conditions
                null));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.HOT, 1000000f, 180f, true,
                // Likes conditions
                null,
                // Hates conditions
                Arrays.asList(Conditions.VERY_HOT,
                        Conditions.COLD,
                        Conditions.VERY_COLD)));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.VERY_HOT, 2000000f, 360f, true,
                // Likes conditions
                null,
                // Hates conditions
                Arrays.asList(Conditions.HOT,
                        Conditions.COLD,
                        Conditions.VERY_COLD,
                        Conditions.HABITABLE,
                        Conditions.MILD_CLIMATE)));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.COLD, 1000000f, 180f, true,
                // Likes conditions
                null,
                // Hates conditions
                Arrays.asList(Conditions.VERY_COLD,
                        Conditions.HOT,
                        Conditions.VERY_HOT)));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.VERY_COLD, 2000000f, 360f, true,
                // Likes conditions
                null,
                // Hates conditions
                Arrays.asList(Conditions.COLD,
                        Conditions.HOT,
                        Conditions.VERY_HOT,
                        Conditions.HABITABLE,
                        Conditions.MILD_CLIMATE)));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.POOR_LIGHT, 1000000f, 180f, true,
                // Likes conditions
                null,
                // Hates conditions
                Collections.singletonList(Conditions.DARK)));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.DARK, 2000000f, 360f, true,
                // Likes conditions
                null,
                // Hates conditions
                Arrays.asList(Conditions.POOR_LIGHT, Conditions.HABITABLE)));
    }
}
