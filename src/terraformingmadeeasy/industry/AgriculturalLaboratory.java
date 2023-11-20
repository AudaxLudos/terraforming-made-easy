package terraformingmadeeasy.industry;

import com.fs.starfarer.api.impl.campaign.ids.Conditions;

import java.util.Arrays;
import java.util.Collections;

public class AgriculturalLaboratory extends BaseIndustry {
    public AgriculturalLaboratory() {
        this.modifiableConditions.add(new ModifiableCondition(Conditions.FARMLAND_POOR, 2000000f, 90f, false,
                // restrictions
                Arrays.asList(Conditions.FARMLAND_ADEQUATE,
                        Conditions.FARMLAND_RICH,
                        Conditions.FARMLAND_BOUNTIFUL),
                // requirements
                Collections.singletonList(Conditions.HABITABLE)));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.FARMLAND_ADEQUATE, 4000000f, 180f, false,
                // restrictions
                Arrays.asList(Conditions.FARMLAND_POOR,
                        Conditions.FARMLAND_RICH,
                        Conditions.FARMLAND_BOUNTIFUL),
                // requirements
                Collections.singletonList(Conditions.HABITABLE)));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.FARMLAND_RICH, 6000000f, 270f, false,
                // restrictions
                Arrays.asList(Conditions.FARMLAND_POOR,
                        Conditions.FARMLAND_ADEQUATE,
                        Conditions.FARMLAND_BOUNTIFUL),
                // requirements
                Collections.singletonList(Conditions.HABITABLE)));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.FARMLAND_BOUNTIFUL, 12000000f, 540f, false,
                // restrictions
                Arrays.asList(Conditions.FARMLAND_POOR,
                        Conditions.FARMLAND_ADEQUATE,
                        Conditions.FARMLAND_RICH),
                // requirements
                Collections.singletonList(Conditions.HABITABLE)));
    }
}
