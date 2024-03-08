package terraformingmadeeasy.industries;

import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import terraformingmadeeasy.Utils;

import java.util.Arrays;
import java.util.Collections;

public class GeomorphologyStation extends TMEBaseIndustry {
    public GeomorphologyStation() {
        this.modifiableConditions.add(new Utils.ModifiableCondition(Conditions.INIMICAL_BIOSPHERE, 2000000f, 360f, false,
                // Likes conditions
                Arrays.asList(Conditions.HABITABLE, Conditions.THIN_ATMOSPHERE),
                // Hates conditions
                null));
        this.modifiableConditions.add(new Utils.ModifiableCondition(Conditions.DECIVILIZED, 4000000f, 180f, true,
                // Likes conditions
                null,
                // Hates conditions
                null));
        this.modifiableConditions.add(new Utils.ModifiableCondition(Conditions.DECIVILIZED_SUBPOP, 2000000f, 90f, true,
                // Likes conditions
                null,
                // Hates conditions
                null));
        this.modifiableConditions.add(new Utils.ModifiableCondition(Conditions.IRRADIATED, 1000000f, 360f, true,
                // Likes conditions
                null,
                // Hates conditions
                Arrays.asList(Conditions.HABITABLE,
                        Conditions.WATER_SURFACE)));
        this.modifiableConditions.add(new Utils.ModifiableCondition(Conditions.WATER_SURFACE, 1000000f, 360f, false,
                // Likes conditions
                Collections.singletonList(Conditions.HABITABLE),
                // Hates conditions
                null));
        this.modifiableConditions.add(new Utils.ModifiableCondition(Conditions.VOLTURNIAN_LOBSTER_PENS, 1000000f, 360f, false,
                // Likes conditions
                Collections.singletonList(Conditions.WATER_SURFACE),
                // Hates conditions
                null));
    }
}
