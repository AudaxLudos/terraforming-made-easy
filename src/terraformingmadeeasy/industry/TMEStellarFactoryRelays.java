package terraformingmadeeasy.industry;

import com.fs.starfarer.api.impl.campaign.ids.Conditions;

import java.util.Arrays;
import java.util.Collections;

public class TMEStellarFactoryRelays extends TMEBaseIndustry {
    public TMEStellarFactoryRelays() {
        this.modifiableConditions.add(new ModifiableCondition(Conditions.HOT, 1000000f, 180f,
                // restrictions
                Arrays.asList(Conditions.VERY_HOT,
                        Conditions.COLD,
                        Conditions.VERY_COLD),
                // requirements
                null
        ));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.VERY_HOT, 2000000f, 360f,
                // restrictions
                Arrays.asList(Conditions.HOT,
                        Conditions.COLD,
                        Conditions.VERY_COLD,
                        Conditions.HABITABLE,
                        Conditions.MILD_CLIMATE),
                // requirements
                Arrays.asList(new String[]{
                        Conditions.HOT
                })
        ));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.COLD, 1000000f, 180f,
                // restrictions
                Arrays.asList(Conditions.VERY_COLD,
                        Conditions.HOT,
                        Conditions.VERY_HOT),
                // requirements
                null
        ));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.VERY_COLD, 2000000f, 360f,
                // restrictions
                Arrays.asList(Conditions.COLD,
                        Conditions.HOT,
                        Conditions.VERY_HOT,
                        Conditions.HABITABLE,
                        Conditions.MILD_CLIMATE),
                // requirements
                Arrays.asList(Conditions.COLD)
        ));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.POOR_LIGHT, 1000000f, 180f,
                // restrictions
                Arrays.asList(new String[]{
                        Conditions.DARK,
                }),
                // requirements
                null
        ));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.DARK, 2000000f, 360f,
                // restrictions
                Arrays.asList(Conditions.POOR_LIGHT),
                // requirements
                Arrays.asList(Conditions.POOR_LIGHT)
        ));
    }
}
