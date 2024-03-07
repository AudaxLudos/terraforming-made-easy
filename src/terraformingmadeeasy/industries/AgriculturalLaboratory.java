package terraformingmadeeasy.industries;

import com.fs.starfarer.api.impl.campaign.ids.Conditions;

import java.util.Arrays;
import java.util.Collections;

public class AgriculturalLaboratory extends BaseIndustry {
    public AgriculturalLaboratory() {
        this.modifiableConditions.add(new ModifiableCondition(Conditions.FARMLAND_POOR, 2000000f, 90f, false,
                // Likes conditions
                Collections.singletonList(Conditions.HABITABLE),
                // Hates conditions
                Arrays.asList(Conditions.FARMLAND_ADEQUATE,
                        Conditions.FARMLAND_RICH,
                        Conditions.FARMLAND_BOUNTIFUL)));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.FARMLAND_ADEQUATE, 4000000f, 180f, false,
                // Likes conditions
                Collections.singletonList(Conditions.HABITABLE),
                // Hates conditions
                Arrays.asList(Conditions.FARMLAND_POOR,
                        Conditions.FARMLAND_RICH,
                        Conditions.FARMLAND_BOUNTIFUL)));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.FARMLAND_RICH, 6000000f, 270f, false,
                // Likes conditions
                Collections.singletonList(Conditions.HABITABLE),
                // Hates conditions
                Arrays.asList(Conditions.FARMLAND_POOR,
                        Conditions.FARMLAND_ADEQUATE,
                        Conditions.FARMLAND_BOUNTIFUL)));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.FARMLAND_BOUNTIFUL, 12000000f, 540f, false,
                // Likes conditions
                Collections.singletonList(Conditions.HABITABLE),
                // Hates conditions
                Arrays.asList(Conditions.FARMLAND_POOR,
                        Conditions.FARMLAND_ADEQUATE,
                        Conditions.FARMLAND_RICH)));
    }
}
