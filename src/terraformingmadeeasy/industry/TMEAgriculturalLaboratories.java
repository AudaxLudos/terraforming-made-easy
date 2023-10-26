package terraformingmadeeasy.industry;

import com.fs.starfarer.api.impl.campaign.ids.Conditions;

import java.util.Arrays;
import java.util.Collections;

public class TMEAgriculturalLaboratories extends TMEBaseIndustry {
    public TMEAgriculturalLaboratories () {
        this.modifiableConditions.add(new ModifiableCondition(Conditions.FARMLAND_POOR, 1000000f, 90f,
                // restrictions
                Arrays.asList(Conditions.FARMLAND_ADEQUATE,
                        Conditions.FARMLAND_RICH,
                        Conditions.FARMLAND_BOUNTIFUL),
                // requirements
                null
        ));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.FARMLAND_ADEQUATE, 2000000f, 180f,
                // restrictions
                Arrays.asList(Conditions.FARMLAND_POOR,
                        Conditions.FARMLAND_RICH,
                        Conditions.FARMLAND_BOUNTIFUL),
                // requirements
                Collections.singletonList(Conditions.FARMLAND_POOR)
        ));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.FARMLAND_RICH, 3000000f, 270f,
                // restrictions
                Arrays.asList(Conditions.FARMLAND_POOR,
                        Conditions.FARMLAND_ADEQUATE,
                        Conditions.FARMLAND_BOUNTIFUL),
                // requirements
                Collections.singletonList(Conditions.FARMLAND_ADEQUATE)
        ));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.FARMLAND_BOUNTIFUL, 4000000f, 360f,
                // restrictions
                Arrays.asList(Conditions.FARMLAND_POOR,
                        Conditions.FARMLAND_ADEQUATE,
                        Conditions.FARMLAND_RICH),
                // requirements
                Collections.singletonList(Conditions.FARMLAND_RICH)
        ));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.ORGANICS_TRACE, 1000000f, 90f,
                // restrictions
                Arrays.asList(Conditions.ORGANICS_COMMON,
                        Conditions.ORGANICS_ABUNDANT,
                        Conditions.ORGANICS_PLENTIFUL),
                // requirements
                null
        ));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.ORGANICS_COMMON, 2000000f, 180f,
                // restrictions
                Arrays.asList(Conditions.ORGANICS_TRACE,
                        Conditions.ORGANICS_ABUNDANT,
                        Conditions.ORGANICS_PLENTIFUL),
                // requirements
                Collections.singletonList(Conditions.ORGANICS_TRACE)
        ));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.ORGANICS_ABUNDANT, 3000000f, 270f,
                // restrictions
                Arrays.asList(Conditions.ORGANICS_TRACE,
                        Conditions.ORGANICS_COMMON,
                        Conditions.ORGANICS_PLENTIFUL),
                // requirements
                Collections.singletonList(Conditions.ORGANICS_COMMON)
        ));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.ORGANICS_PLENTIFUL, 4000000f, 360f,
                // restrictions
                Arrays.asList(Conditions.ORGANICS_TRACE,
                        Conditions.ORGANICS_COMMON,
                        Conditions.ORGANICS_ABUNDANT),
                // requirements
                Collections.singletonList(Conditions.ORGANICS_ABUNDANT)
        ));
    }
}
