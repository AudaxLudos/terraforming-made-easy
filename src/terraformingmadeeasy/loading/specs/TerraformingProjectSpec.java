package terraformingmadeeasy.loading.specs;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MarketConditionSpecAPI;
import terraformingmadeeasy.Settings;

public class TerraformingProjectSpec {
    private String id;
    private String structureId;
    private String conditionId;
    private String planetSpecOverride;

    private boolean unlocked;
    private boolean changeGasGiants;

    private float cost;
    private float buildTime;

    private String likedConditions;
    private String hatedConditions;
    private String likedIndustries;
    private String hatedIndustries;

    public String getName(MarketAPI market) {
        MarketConditionSpecAPI conditionSpec = getConditionSpec();
        boolean hasCondition = market.hasCondition(this.conditionId);
        String prefix = hasCondition ? "Remove " : "Add ";
        return prefix + conditionSpec.getName();
    }

    public MarketConditionSpecAPI getConditionSpec() {
        return Global.getSettings().getMarketConditionSpec(this.conditionId);
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStructureId() {
        return this.structureId;
    }

    public void setStructureId(String structureId) {
        this.structureId = structureId;
    }

    public String getConditionId() {
        return this.conditionId;
    }

    public void setConditionId(String conditionId) {
        this.conditionId = conditionId;
    }

    public String getPlanetSpecOverride() {
        return this.planetSpecOverride;
    }

    public void setPlanetSpecOverride(String planetSpecOverride) {
        this.planetSpecOverride = planetSpecOverride;
    }

    public boolean isUnlocked() {
        return this.unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public boolean isChangeGasGiants() {
        return this.changeGasGiants;
    }

    public void setChangeGasGiants(boolean changeGasGiants) {
        this.changeGasGiants = changeGasGiants;
    }

    public float getCost() {
        return this.cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public float getCost(MarketAPI market) {
        boolean hasCondition = market.hasCondition(this.conditionId);
        float removalMult = hasCondition ? Settings.REMOVAL_COST_MULTIPLIER : 1f;
        return this.cost * removalMult * Settings.BUILD_COST_MULTIPLIER;
    }

    public float getBuildTime() {
        return this.buildTime;
    }

    public void setBuildTime(float buildTime) {
        this.buildTime = buildTime;
    }

    public String getLikedConditions() {
        return this.likedConditions;
    }

    public void setLikedConditions(String likedConditions) {
        this.likedConditions = likedConditions;
    }

    public String getHatedConditions() {
        return this.hatedConditions;
    }

    public void setHatedConditions(String hatedConditions) {
        this.hatedConditions = hatedConditions;
    }

    public String getLikedIndustries() {
        return this.likedIndustries;
    }

    public void setLikedIndustries(String likedIndustries) {
        this.likedIndustries = likedIndustries;
    }

    public String getHatedIndustries() {
        return this.hatedIndustries;
    }

    public void setHatedIndustries(String hatedIndustries) {
        this.hatedIndustries = hatedIndustries;
    }
}
