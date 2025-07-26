package terraformingmadeeasy.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.kaysaar.aotd.vok.scripts.research.AoTDMainResearchManager;
import org.apache.log4j.Logger;
import terraformingmadeeasy.Utils;

import java.awt.*;
import java.util.List;
import java.util.Objects;

public class TMEBaseIndustryTest extends BaseIndustry {
    public static final Logger log = Global.getLogger(TMEBaseIndustryTest.class);
    public static final float GAMMA_BUILD_TIME_MULT = 0.20f;
    public static final float BETA_BUILD_TIME_MULT = 0.30f;
    public static final float ALPHA_BUILD_TIME_MULT = 0.50f;
    protected Utils.ProjectData project = null;
    protected Boolean isAICoreBuildTimeMultApplied = false;
    protected float aiCoreBuildProgressRemoved = 0f;
    protected String prevAICoreId = null;

    @Override
    public void apply() {
        super.apply(true);
    }

    @Override
    protected void applyNoAICoreModifiers() {
        if (this.isAICoreBuildTimeMultApplied && !Objects.equals(getAICoreId(), this.prevAICoreId)) {
            this.buildProgress = this.buildProgress - this.aiCoreBuildProgressRemoved;
            this.aiCoreBuildProgressRemoved = 0f;
            this.isAICoreBuildTimeMultApplied = false;
        }
    }

    @Override
    protected void applyGammaCoreModifiers() {
        applyBuildTimeMultiplier(GAMMA_BUILD_TIME_MULT);
    }

    @Override
    protected void applyBetaCoreModifiers() {
        applyBuildTimeMultiplier(BETA_BUILD_TIME_MULT);
    }

    @Override
    protected void applyAlphaCoreModifiers() {
        applyBuildTimeMultiplier(ALPHA_BUILD_TIME_MULT);
    }

    protected void applyBuildTimeMultiplier(float mult) {
        if (!this.isAICoreBuildTimeMultApplied) {
            applyNoAICoreModifiers();
            float daysLeft = this.buildTime - this.buildProgress;
            this.aiCoreBuildProgressRemoved = daysLeft * mult;
            this.buildProgress = this.buildTime - (daysLeft - this.aiCoreBuildProgressRemoved);
            this.isAICoreBuildTimeMultApplied = true;
            this.prevAICoreId = getAICoreId();
        }
    }

    @Override
    public boolean isUpgrading() {
        return this.building && this.project != null;
    }

    @Override
    public String getBuildOrUpgradeProgressText() {
        if (isDisrupted()) {
            int left = (int) getDisruptedDays();
            if (left < 1) {
                left = 1;
            }
            String days = "days";
            if (left == 1) {
                days = "day";
            }

            return "Disrupted: " + left + " " + days + " left";
        }

        int left = (int) (this.buildTime - this.buildProgress);
        if (left < 1) {
            left = 1;
        }
        String days = "days";
        if (left == 1) {
            days = "day";
        }

        return getBuildingText() + ": " + left + " " + days + " left";
    }

    protected String getBuildingText() {
        return "building";
    }

    @Override
    public void finishBuildingOrUpgrading() {
        this.building = false;
        this.buildProgress = 0;
        this.buildTime = 1f;
        this.isAICoreBuildTimeMultApplied = false;
        if (this.project != null) {
            completeProject();
            sendCompletedMessage();
        } else {
            buildingFinished();
            reapply();
        }
    }

    @Override
    public void startUpgrading() {
        if (this.project != null) {
            this.building = true;
            this.buildProgress = 0;
            this.isAICoreBuildTimeMultApplied = false;
            this.buildTime = this.project.buildTime * Utils.BUILD_TIME_MULTIPLIER;
        }
    }

    @Override
    public void cancelUpgrade() {
        this.building = false;
        this.buildProgress = 0;
        this.aiCoreBuildProgressRemoved = 0f;
        this.isAICoreBuildTimeMultApplied = false;
        this.project = null;
    }

    @Override
    public boolean isAvailableToBuild() {
        if (Utils.isAOTDVOKEnabled()) {
            return AoTDMainResearchManager.getInstance().isAvailableForThisMarket(getAOTDVOKTechId(), this.market) && this.market.getPlanetEntity() != null && super.isAvailableToBuild();
        }
        return this.market.getPlanetEntity() != null && super.isAvailableToBuild();
    }

    @Override
    public String getUnavailableReason() {
        if (!super.isAvailableToBuild()) {
            return super.getUnavailableReason();
        }
        return "Requires a planet";
    }

    @Override
    public boolean showWhenUnavailable() {
        if (Utils.isAOTDVOKEnabled()) {
            return AoTDMainResearchManager.getInstance().isAvailableForThisMarket(getAOTDVOKTechId(), this.market) && this.market.getPlanetEntity() != null && super.showWhenUnavailable();
        }
        return super.showWhenUnavailable();
    }

    @Override
    protected void addAlphaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        float oPad = 10f;
        Color highlight = Misc.getHighlightColor();

        String pre = "Alpha-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = "Alpha-level AI core. ";
        }
        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP || mode == AICoreDescriptionMode.MANAGE_CORE_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48);
            text.addPara(pre + "Reduces upkeep cost by %s. Reduces " + getBuildingText() + " time by %s.", oPad, highlight,
                    (int) ((1f - UPKEEP_MULT) * 100f) + "%", (int) (ALPHA_BUILD_TIME_MULT * 100f) + "%");
            tooltip.addImageWithText(oPad);
            return;
        }

        tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces " + getBuildingText() + " time by %s.", oPad, highlight,
                (int) ((1f - UPKEEP_MULT) * 100f) + "%", (int) (ALPHA_BUILD_TIME_MULT * 100f) + "%");
    }

    @Override
    protected void addBetaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        float oPad = 10f;
        Color highlight = Misc.getHighlightColor();

        String pre = "Beta-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = "Beta-level AI core. ";
        }
        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP || mode == AICoreDescriptionMode.MANAGE_CORE_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48);
            text.addPara(pre + "Reduces upkeep cost by %s. Reduces " + getBuildingText() + " time by %s.", oPad, highlight,
                    (int) ((1f - UPKEEP_MULT) * 100f) + "%", (int) (BETA_BUILD_TIME_MULT * 100f) + "%");
            tooltip.addImageWithText(oPad);
            return;
        }

        tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces " + getBuildingText() + " time by %s.", oPad, highlight,
                (int) ((1f - UPKEEP_MULT) * 100f) + "%", (int) (BETA_BUILD_TIME_MULT * 100f) + "%");
    }

    @Override
    protected void addGammaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        float oPad = 10f;
        Color highlight = Misc.getHighlightColor();

        String pre = "Gamma-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = "Gamma-level AI core. ";
        }
        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP || mode == AICoreDescriptionMode.MANAGE_CORE_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48);
            text.addPara(pre + "Reduces upkeep cost by %s. Reduces " + getBuildingText() + " time by %s.", oPad, highlight,
                    (int) ((1f - UPKEEP_MULT) * 100f) + "%", (int) (GAMMA_BUILD_TIME_MULT * 100f) + "%");
            tooltip.addImageWithText(oPad);
            return;
        }

        tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces " + getBuildingText() + " time by %s.", oPad, highlight,
                (int) ((1f - UPKEEP_MULT) * 100f) + "%", (int) (GAMMA_BUILD_TIME_MULT * 100f) + "%");
    }

    @Override
    protected void addPostUpkeepSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode) {
        addProjectSection(tooltip, mode);
    }

    protected void addProjectSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode) {
    }

    @Override
    protected void applyAICoreToIncomeAndUpkeep() {
        if (this.aiCoreId == null) {
            getUpkeep().unmodifyMult("ind_core");
            return;
        }

        float mult = UPKEEP_MULT;
        String name = "AI Core assigned";
        switch (this.aiCoreId) {
            case Commodities.ALPHA_CORE:
                name = "Alpha Core assigned";
                break;
            case Commodities.BETA_CORE:
                name = "Beta Core assigned";
                break;
            case Commodities.GAMMA_CORE:
                name = "Gamma Core assigned";
                break;
        }

        getUpkeep().modifyMult("ind_core", mult, name);
    }

    public String getAOTDVOKTechId() {
        return "";
    }

    public List<Utils.ProjectData> getProjects() {
        return null;
    }

    public Utils.ProjectData getProject() {
        return this.project;
    }

    public void setProject(Utils.ProjectData option) {
        this.project = option;
    }

    public void completeProject() {
    }

    public void sendCompletedMessage() {
    }
}
