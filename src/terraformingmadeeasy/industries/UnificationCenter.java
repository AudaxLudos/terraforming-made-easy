package terraformingmadeeasy.industries;

import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.conditions.DeathWorld;
import terraformingmadeeasy.ids.TMEIds;

public class UnificationCenter extends TMEBaseIndustry {
    public UnificationCenter() {
        getTerraformingOptions(TMEIds.UNIFICATION_CENTER);
    }

    @Override
    public void apply() {
        super.apply();

        modifyStabilityWithBaseMod();
        int size = this.market.getSize();
        demand(Commodities.MARINES, size);
        demand(Commodities.SUPPLIES, size);
    }

    @Override
    public void unapply() {
        super.unapply();

        unmodifyStabilityWithBaseMod();
    }

    @Override
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode) {
        super.addRightAfterDescriptionSection(tooltip, mode);
    }

    @Override
    protected int getBaseStabilityMod() {
        return 1;
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
