package terraformingmadeeasy.industry;

import com.fs.starfarer.api.impl.campaign.ids.Conditions;

import java.util.Arrays;
import java.util.Collections;

public class TMEAgriculturalLaboratory extends TMEBaseIndustry {
    public TMEAgriculturalLaboratory() {
        this.modifiableConditions.add(new ModifiableCondition(Conditions.FARMLAND_POOR, 2000000f, 90f, false,
                // restrictions
                Arrays.asList(Conditions.FARMLAND_ADEQUATE,
                        Conditions.FARMLAND_RICH,
                        Conditions.FARMLAND_BOUNTIFUL),
                // requirements
                Collections.singletonList(Conditions.HABITABLE)));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.FARMLAND_ADEQUATE, 4000000f, 180f,false,
                // restrictions
                Arrays.asList(Conditions.FARMLAND_POOR,
                        Conditions.FARMLAND_RICH,
                        Conditions.FARMLAND_BOUNTIFUL),
                // requirements
                Collections.singletonList(Conditions.HABITABLE)));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.FARMLAND_RICH, 6000000f, 270f,false,
                // restrictions
                Arrays.asList(Conditions.FARMLAND_POOR,
                        Conditions.FARMLAND_ADEQUATE,
                        Conditions.FARMLAND_BOUNTIFUL),
                // requirements
                Collections.singletonList(Conditions.HABITABLE)));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.FARMLAND_BOUNTIFUL, 12000000f, 540f,false,
                // restrictions
                Arrays.asList(Conditions.FARMLAND_POOR,
                        Conditions.FARMLAND_ADEQUATE,
                        Conditions.FARMLAND_RICH),
                // requirements
                Collections.singletonList(Conditions.HABITABLE)));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.ORGANICS_TRACE, 2000000f, 90f,false,
                // restrictions
                Arrays.asList(Conditions.ORGANICS_COMMON,
                        Conditions.ORGANICS_ABUNDANT,
                        Conditions.ORGANICS_PLENTIFUL),
                // requirements
                Arrays.asList(Conditions.HABITABLE, Conditions.THIN_ATMOSPHERE, Conditions.TOXIC_ATMOSPHERE)));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.ORGANICS_COMMON, 4000000f, 180f,false,
                // restrictions
                Arrays.asList(Conditions.ORGANICS_TRACE,
                        Conditions.ORGANICS_ABUNDANT,
                        Conditions.ORGANICS_PLENTIFUL),
                // requirements
                Arrays.asList(Conditions.HABITABLE, Conditions.THIN_ATMOSPHERE, Conditions.TOXIC_ATMOSPHERE)));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.ORGANICS_ABUNDANT, 6000000f, 270f,false,
                // restrictions
                Arrays.asList(Conditions.ORGANICS_TRACE,
                        Conditions.ORGANICS_COMMON,
                        Conditions.ORGANICS_PLENTIFUL),
                // requirements
                Collections.singletonList(Conditions.HABITABLE)));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.ORGANICS_PLENTIFUL, 12000000f, 540f,false,
                // restrictions
                Arrays.asList(Conditions.ORGANICS_TRACE,
                        Conditions.ORGANICS_COMMON,
                        Conditions.ORGANICS_ABUNDANT),
                // requirements
                Collections.singletonList(Conditions.HABITABLE)));
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
