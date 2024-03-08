package terraformingmadeeasy.industries;

import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import terraformingmadeeasy.Utils;

import java.util.Arrays;
import java.util.Collections;

public class AtmosphereRegulator extends TMEBaseIndustry {
    public AtmosphereRegulator() {
        this.modifiableConditions.add(new Utils.ModifiableCondition(Conditions.NO_ATMOSPHERE, 1000000f, 90f, false,
                // Likes conditions
                null,
                // Hates conditions
                Arrays.asList(Conditions.THIN_ATMOSPHERE,
                        Conditions.DENSE_ATMOSPHERE,
                        Conditions.TOXIC_ATMOSPHERE,
                        Conditions.MILD_CLIMATE,
                        Conditions.HABITABLE)));
        this.modifiableConditions.add(new Utils.ModifiableCondition(Conditions.THIN_ATMOSPHERE, 2000000f, 180f, false,
                // Likes conditions
                null,
                // Hates conditions
                Arrays.asList(Conditions.NO_ATMOSPHERE,
                        Conditions.DENSE_ATMOSPHERE,
                        Conditions.TOXIC_ATMOSPHERE,
                        Conditions.MILD_CLIMATE,
                        Conditions.HABITABLE)));
        this.modifiableConditions.add(new Utils.ModifiableCondition(Conditions.DENSE_ATMOSPHERE, 3000000f, 270f, true,
                // Likes conditions
                null,
                // Hates conditions
                Arrays.asList(Conditions.NO_ATMOSPHERE,
                        Conditions.THIN_ATMOSPHERE,
                        Conditions.TOXIC_ATMOSPHERE,
                        Conditions.MILD_CLIMATE,
                        Conditions.HABITABLE)));
        this.modifiableConditions.add(new Utils.ModifiableCondition(Conditions.TOXIC_ATMOSPHERE, 6000000f, 540f, true,
                // Likes conditions
                null,
                // Hates conditions
                Arrays.asList(Conditions.NO_ATMOSPHERE,
                        Conditions.THIN_ATMOSPHERE,
                        Conditions.DENSE_ATMOSPHERE,
                        Conditions.MILD_CLIMATE,
                        Conditions.HABITABLE)));
        this.modifiableConditions.add(new Utils.ModifiableCondition(Conditions.POLLUTION, 1000000f, 360f, false,
                // Likes conditions
                Arrays.asList(Conditions.HABITABLE, Conditions.THIN_ATMOSPHERE),
                // Hates conditions
                null));
        this.modifiableConditions.add(new Utils.ModifiableCondition(Conditions.EXTREME_WEATHER, 2000000f, 360f, true,
                // Likes conditions
                null,
                // Hates conditions
                Arrays.asList(Conditions.NO_ATMOSPHERE,
                        Conditions.MILD_CLIMATE)));
        this.modifiableConditions.add(new Utils.ModifiableCondition(Conditions.MILD_CLIMATE, 12000000f, 720f, false,
                // Likes conditions
                Collections.singletonList(Conditions.HABITABLE),
                // Hates conditions
                null));
    }
}
