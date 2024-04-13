package terraformingmadeeasy.industries;

import terraformingmadeeasy.Utils;
import terraformingmadeeasy.ids.TMEIndustries;

public class UnificationCenter extends TMEBaseIndustry {
    public UnificationCenter() {
        getTerraformingOptions(TMEIndustries.UNIFICATION_CENTER);
    }

    @Override
    public boolean hasLikedConditions(Utils.ModifiableCondition condition) {
        // Checks if market has all conditions
        hasAtLeastOneLikedCondition = false;
        if (!condition.likedConditions.isEmpty()) {
            for (String conditionId : condition.likedConditions) {
                if (!market.hasCondition(conditionId)) {
                    return false;
                }
            }
        }
        return true;
    }
}
