package terraformingmadeeasy.industry;

import com.fs.starfarer.api.impl.campaign.ids.Conditions;

import java.util.Arrays;
import java.util.Collections;

public class TMEGeomorphologyStation extends TMEBaseIndustry {
    public TMEGeomorphologyStation() {
        this.modifiableConditions.add(new ModifiableCondition(Conditions.INIMICAL_BIOSPHERE, 2000000f, 360f, false,
                // restrictions
                null,
                // requirements
                Arrays.asList(Conditions.HABITABLE, Conditions.THIN_ATMOSPHERE)));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.DECIVILIZED, 4000000f, 180f, true,
                // restrictions
                null,
                null));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.IRRADIATED, 1000000f, 360f, true,
                // restrictions
                Arrays.asList(Conditions.HABITABLE,
                        Conditions.WATER_SURFACE),
                // requirements
                null));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.WATER_SURFACE, 1000000f, 360f, false,
                // restrictions
                Collections.singletonList(Conditions.IRRADIATED),
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
