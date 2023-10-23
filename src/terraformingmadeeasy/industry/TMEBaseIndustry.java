package terraformingmadeeasy.industry;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.characters.MarketConditionSpecAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.util.Misc;

import java.util.ArrayList;
import java.util.List;

public class TMEBaseIndustry extends BaseIndustry {
    public static class ModifiableCondition {
        public MarketConditionSpecAPI spec;
        public float cost;
        public float buildTime;
        public List<String> restrictions = new ArrayList<>();
        public List<String> requirements = new ArrayList<>();

        public ModifiableCondition(String conditionSpecId, float cost, float buildTime, List<String> restrictions, List<String> requirements) {
            this.spec = Global.getSettings().getMarketConditionSpec(conditionSpecId);
            this.cost = cost;
            this.buildTime = buildTime;
            if(restrictions != null) this.restrictions = restrictions;
            if(requirements != null) this.requirements = requirements;
        }
    }

    public List<ModifiableCondition> modifiableConditions = new ArrayList<>();
    public ModifiableCondition modifiableCondition = null;

    @Override
    public void apply() {
        apply(true);
    }

    public boolean isUpgrading() {
        return building && modifiableCondition != null;
    }

    @Override
    public String getBuildOrUpgradeProgressText() {
        if (isDisrupted()) {
            int left = (int) getDisruptedDays();
            if (left < 1) left = 1;
            String days = "days";
            if (left == 1) days = "day";

            return "Disrupted: " + left + " " + days + " left";
        }

        int left = (int) (buildTime - buildProgress);
        if (left < 1) left = 1;
        String days = "days";
        if (left == 1) days = "day";

        if (isUpgrading()) {
            return "Terraforming: " + left + " " + days + " left";
        } else {
            return "Building: " + left + " " + days + " left";
        }
    }

    public void finishBuildingOrUpgrading() {
        building = false;
        buildProgress = 0;
        buildTime = 1f;
        System.out.println("Working");
        if (modifiableCondition != null) {
            market.removeIndustry(getId(), null, true);
            market.addIndustry(getId());
            BaseIndustry industry = (BaseIndustry) market.getIndustry(getId());
            industry.setAICoreId(getAICoreId());
            industry.setImproved(isImproved());
            sendTerraformingMessage();
            setSpecialItem(industry.getSpecialItem());
            changePlanetConditions(modifiableCondition);
            industry.reapply();
        } else {
            buildingFinished();
            reapply();
        }
    }

    public void startUpgrading(ModifiableCondition condition) {
        // Will be called from TMEIndustryDialogueDelegate to start terraforming
        building = true;
        buildProgress = 0;
        modifiableCondition = condition;
        buildTime = condition.buildTime;
    }

    public void cancelUpgrade() {
        building = false;
        buildProgress = 0;
        modifiableCondition = null;
    }

    public void sendTerraformingMessage() {
        if (market.isPlayerOwned()) {
            MessageIntel intel = new MessageIntel(getCurrentName() + " at " + market.getName(), Misc.getBasePlayerColor());
            intel.addLine(BaseIntelPlugin.BULLET + "Terraforming completed");
            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
        }
    }

    public void changePlanetConditions(ModifiableCondition condition) {
        if (getMarket().hasCondition(condition.spec.getId())) {
            getMarket().removeCondition(condition.spec.getId());
        } else {
            getMarket().addCondition(condition.spec.getId());
            for (String restriction : condition.restrictions) {
                if (getMarket().hasCondition(restriction))
                    getMarket().removeCondition(restriction);
            }
        }
    }

    public Boolean canTerraformCondition(ModifiableCondition condition) {
        for (String requirement : condition.requirements)
            if (!getMarket().hasCondition(requirement))
                return false;
        return true;
    }
}
