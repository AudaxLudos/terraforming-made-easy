package terraformingmadeeasy.industries;

import terraformingmadeeasy.Utils;
import terraformingmadeeasy.ids.TMEIds;

public class UnificationCenter extends TMEBaseIndustry {
    public UnificationCenter() {
        getTerraformingOptions(TMEIds.UNIFICATION_CENTER);
    }

    @Override
    public boolean hasLikedConditions(Utils.ModifiableCondition condition) {
        // Checks if market has all conditions
        this.hasAtLeastOneLikedCondition = false;
        if (!condition.likedConditions.isEmpty()) {
            for (String conditionId : condition.likedConditions) {
                if (!this.market.hasCondition(conditionId)) {
                    return false;
                }
            }
        }
        return true;
    }
}
