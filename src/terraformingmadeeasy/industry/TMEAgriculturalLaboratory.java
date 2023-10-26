package terraformingmadeeasy.industry;

import com.fs.starfarer.api.impl.campaign.ids.Conditions;

import java.util.Arrays;
import java.util.Collections;

public class TMEAgriculturalLaboratory extends TMEBaseIndustry {
    public TMEAgriculturalLaboratory() {
        this.modifiableConditions.add(new ModifiableCondition(Conditions.FARMLAND_POOR, 2000000f, 90f,
                // restrictions
                Arrays.asList(Conditions.FARMLAND_ADEQUATE,
                        Conditions.FARMLAND_RICH,
                        Conditions.FARMLAND_BOUNTIFUL),
                // requirements
                Collections.singletonList(Conditions.HABITABLE)
        ));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.FARMLAND_ADEQUATE, 4000000f, 180f,
                // restrictions
                Arrays.asList(Conditions.FARMLAND_POOR,
                        Conditions.FARMLAND_RICH,
                        Conditions.FARMLAND_BOUNTIFUL),
                // requirements
                Collections.singletonList(Conditions.HABITABLE)
        ));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.FARMLAND_RICH, 6000000f, 270f,
                // restrictions
                Arrays.asList(Conditions.FARMLAND_POOR,
                        Conditions.FARMLAND_ADEQUATE,
                        Conditions.FARMLAND_BOUNTIFUL),
                // requirements
                Collections.singletonList(Conditions.HABITABLE)
        ));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.FARMLAND_BOUNTIFUL, 12000000f, 540f,
                // restrictions
                Arrays.asList(Conditions.FARMLAND_POOR,
                        Conditions.FARMLAND_ADEQUATE,
                        Conditions.FARMLAND_RICH),
                // requirements
                Collections.singletonList(Conditions.HABITABLE)
        ));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.ORGANICS_TRACE, 2000000f, 90f,
                // restrictions
                Arrays.asList(Conditions.ORGANICS_COMMON,
                        Conditions.ORGANICS_ABUNDANT,
                        Conditions.ORGANICS_PLENTIFUL),
                // requirements
                null
        ));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.ORGANICS_COMMON, 4000000f, 180f,
                // restrictions
                Arrays.asList(Conditions.ORGANICS_TRACE,
                        Conditions.ORGANICS_ABUNDANT,
                        Conditions.ORGANICS_PLENTIFUL),
                // requirements
                null
        ));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.ORGANICS_ABUNDANT, 6000000f, 270f,
                // restrictions
                Arrays.asList(Conditions.ORGANICS_TRACE,
                        Conditions.ORGANICS_COMMON,
                        Conditions.ORGANICS_PLENTIFUL),
                // requirements
                null
        ));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.ORGANICS_PLENTIFUL, 12000000f, 540f,
                // restrictions
                Arrays.asList(Conditions.ORGANICS_TRACE,
                        Conditions.ORGANICS_COMMON,
                        Conditions.ORGANICS_ABUNDANT),
                // requirements
                null
        ));
    }
}
