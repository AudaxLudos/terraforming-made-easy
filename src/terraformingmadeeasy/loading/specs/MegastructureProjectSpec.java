package terraformingmadeeasy.loading.specs;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomEntitySpecAPI;

public class MegastructureProjectSpec {
    private String id;
    private String customEntityId;

    private boolean unlocked;

    private float cost;
    private float buildTime;
    private float buildLimit;

    public CustomEntitySpecAPI getCustomEntitySpec() {
        return Global.getSettings().getCustomEntitySpec(this.customEntityId);
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomEntityId() {
        return this.customEntityId;
    }

    public void setCustomEntityId(String customEntityId) {
        this.customEntityId = customEntityId;
    }

    public boolean isUnlocked() {
        return this.unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public float getCost() {
        return this.cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public float getBuildTime() {
        return this.buildTime;
    }

    public void setBuildTime(float buildTime) {
        this.buildTime = buildTime;
    }

    public float getBuildLimit() {
        return this.buildLimit;
    }

    public void setBuildLimit(float buildLimit) {
        this.buildLimit = buildLimit;
    }
}
