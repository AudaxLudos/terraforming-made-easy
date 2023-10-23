package terraformingmadeeasy.industry;

import com.fs.starfarer.api.impl.campaign.ids.Conditions;

import java.util.Arrays;

public class TMEAtmosphereRegulators extends TMEBaseIndustry {
    public TMEAtmosphereRegulators() {
        this.modifiableConditions.add(new ModifiableCondition(Conditions.NO_ATMOSPHERE, 1000000f, 180f,
                // restrictions
                Arrays.asList(Conditions.THIN_ATMOSPHERE,
                        Conditions.DENSE_ATMOSPHERE,
                        Conditions.TOXIC_ATMOSPHERE,
                        Conditions.MILD_CLIMATE,
                        Conditions.HABITABLE),
                // requirements
                null
        ));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.THIN_ATMOSPHERE, 2000000f, 360f,
                // restrictions
                Arrays.asList(Conditions.NO_ATMOSPHERE,
                        Conditions.DENSE_ATMOSPHERE,
                        Conditions.TOXIC_ATMOSPHERE,
                        Conditions.MILD_CLIMATE,
                        Conditions.HABITABLE),
                // requirements
                null
        ));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.DENSE_ATMOSPHERE, 3000000f, 540f,
                // restrictions
                Arrays.asList(Conditions.NO_ATMOSPHERE,
                        Conditions.THIN_ATMOSPHERE,
                        Conditions.TOXIC_ATMOSPHERE,
                        Conditions.MILD_CLIMATE,
                        Conditions.HABITABLE),
                // requirements
                null
        ));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.TOXIC_ATMOSPHERE, 4000000f, 720f,
                // restrictions
                Arrays.asList(Conditions.NO_ATMOSPHERE,
                        Conditions.THIN_ATMOSPHERE,
                        Conditions.DENSE_ATMOSPHERE,
                        Conditions.MILD_CLIMATE,
                        Conditions.HABITABLE),
                // requirements
                null
        ));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.EXTREME_WEATHER, 2000000f, 360f,
                // restrictions
                Arrays.asList(Conditions.NO_ATMOSPHERE,
                        Conditions.MILD_CLIMATE),
                // requirements
                null
        ));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.MILD_CLIMATE, 12000000f, 720f,
                // restrictions
                Arrays.asList(Conditions.EXTREME_WEATHER,
                        Conditions.NO_ATMOSPHERE,
                        Conditions.THIN_ATMOSPHERE,
                        Conditions.DENSE_ATMOSPHERE,
                        Conditions.TOXIC_ATMOSPHERE,
                        Conditions.VERY_HOT,
                        Conditions.VERY_COLD),
                // requirements
                null
        ));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.HABITABLE, 12000000f, 720f,
                // restrictions
                Arrays.asList(Conditions.EXTREME_WEATHER,
                        Conditions.NO_ATMOSPHERE,
                        Conditions.THIN_ATMOSPHERE,
                        Conditions.DENSE_ATMOSPHERE,
                        Conditions.TOXIC_ATMOSPHERE,
                        Conditions.VERY_HOT,
                        Conditions.VERY_COLD),
                // requirements
                null
        ));
    }
}
