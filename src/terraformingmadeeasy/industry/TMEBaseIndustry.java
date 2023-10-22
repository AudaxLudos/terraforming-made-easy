package terraformingmadeeasy.industry;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.characters.MarketConditionSpecAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
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

        public ModifiableCondition(String conditionSpecId, float cost, float buildTime) {
            this.spec = Global.getSettings().getMarketConditionSpec(conditionSpecId);
            this.cost = cost;
            this.buildTime = buildTime;
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
        if(getMarket().hasCondition(condition.spec.getId()))
            getMarket().removeCondition(condition.spec.getId());
        else
            getMarket().addCondition(condition.spec.getId());
    }

    public Boolean canTerraformCondition(ModifiableCondition condition) {
        for (String restriction : condition.restrictions)
            if (getMarket().hasCondition(restriction))
                return false;
        for (String requirement : condition.requirements)
            if (!getMarket().hasCondition(requirement))
                return false;
        return true;
    }
}
