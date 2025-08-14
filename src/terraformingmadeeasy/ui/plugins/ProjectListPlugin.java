package terraformingmadeeasy.ui.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.ids.TMEIds;
import terraformingmadeeasy.industries.BaseDevelopmentIndustry;
import terraformingmadeeasy.industries.BaseTerraformingIndustry;
import terraformingmadeeasy.ui.tooltips.MegastructureTooltip;
import terraformingmadeeasy.ui.tooltips.TerraformTooltip;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProjectListPlugin extends BaseCustomUIPanelPlugin {
    public CustomPanelAPI projectsPanel;
    public Utils.ProjectData selected;
    public List<ButtonAPI> buttons = new ArrayList<>();

    @SuppressWarnings("RedundantArrayCreation")
    public ProjectListPlugin(CustomPanelAPI panel, BaseDevelopmentIndustry industry, String industryId, List<Utils.ProjectData> projects, float width, float height, boolean isForCodex) {
        String nameTooltipText = "Name of the condition to terraform on a planet";
        String timeTooltipText = "Build time, in days. Until the terraforming project finishes.";
        String costTooltipText = "One-time cost to begin terraforming project, in credits";
        if (industryId.equals(TMEIds.CONSTRUCTION_GRID)) {
            nameTooltipText = "Name of megastructure to build";
            timeTooltipText = "Build time, in days. Until the megastructure project finishes.";
            costTooltipText = "One-time cost to begin megastructure project, in credits";
        } else if (industryId.equals(TMEIds.PLANETARY_HOLOGRAM)) {
            nameTooltipText = "Name of planet type to change into";
            timeTooltipText = "Build time, in days. Until a planet's visual changes.";
            costTooltipText = "One-time cost to change a planet's visual, in credits";
        }

        float columnOneWidth = width / 3f + 100f;
        float columnWidth = (width - columnOneWidth) / 2f;

        this.projectsPanel = panel.createCustomPanel(width, height, null);
        TooltipMakerAPI projectsHeaderElement = this.projectsPanel.createUIElement(width, height, false);
        projectsHeaderElement.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
                0f, false, true,
                new Object[]{"Name", columnOneWidth, "Build time", columnWidth, "Cost", columnWidth - 6f});
        projectsHeaderElement.addTableHeaderTooltip(0, nameTooltipText);
        projectsHeaderElement.addTableHeaderTooltip(1, timeTooltipText);
        projectsHeaderElement.addTableHeaderTooltip(2, costTooltipText);
        projectsHeaderElement.addTable("", 0, 0f);
        projectsHeaderElement.getPrev().getPosition().setXAlignOffset(0f);
        projectsHeaderElement.getPosition().inTMid(0);
        this.projectsPanel.addUIElement(projectsHeaderElement);
        if (isForCodex) {
            this.projectsPanel.getPosition().setSize(width, projectsHeaderElement.getHeightSoFar() + this.projectsPanel.getPosition().getHeight());
        }

        TooltipMakerAPI projectsElement = this.projectsPanel.createUIElement(width, height - 22f, !isForCodex);
        for (Utils.ProjectData project : projects) {
            String name = project.name;
            String prefix = "";
            String suffix = "";
            String icon = project.icon;
            float cost = Math.round(project.cost * Utils.BUILD_COST_MULTIPLIER);
            float buildTime = Math.round(project.buildTime * Utils.BUILD_TIME_MULTIPLIER);
            boolean canAfford = Global.getSettings().isInCampaignState() && Global.getSector().getPlayerFleet().getCargo().getCredits().get() >= cost;
            boolean canAffordAndBuild = !Utils.canAffordAndBuild(industry, project);
            TooltipMakerAPI.TooltipCreator tooltip = new TerraformTooltip(project, industry);

            if (industryId.equals(TMEIds.CONSTRUCTION_GRID)) {
                prefix = "Construct ";
                tooltip = new MegastructureTooltip(project);
            } else if (industryId.equals(TMEIds.PLANETARY_HOLOGRAM)) {
                if (industry != null) {
                    BaseTerraformingIndustry ind = (BaseTerraformingIndustry) industry;
                    prefix = ind.getMarket().hasCondition(project.id) ? "Remove " : "Add ";
                    if (Objects.equals(ind.getId(), TMEIds.PLANETARY_HOLOGRAM)) {
                        prefix = "Set Visual to ";
                        suffix = " World";
                    }
                }
            }

            if (!isForCodex) {
                prefix = "";
                suffix = "";
            }

            CustomPanelAPI optionPanel = panel.createCustomPanel(width, 44f, this);

            TooltipMakerAPI optionButtonElement = optionPanel.createUIElement(width, 44f, false);
            ButtonAPI optionButton = optionButtonElement.addButton("", project, new Color(0, 195, 255, 190), new Color(0, 0, 0, 255), Alignment.MID, CutStyle.NONE, width, 44f, 0f);
            optionButton.setClickable(!isForCodex);
            if (!isForCodex) {
                Utils.setButtonEnabledOrHighlighted(optionButton, canAffordAndBuild, !canAffordAndBuild);
            }
            optionButtonElement.addTooltipTo(tooltip, optionButton, TooltipMakerAPI.TooltipLocation.RIGHT);
            optionButtonElement.getPosition().setXAlignOffset(-10f);
            optionPanel.addUIElement(optionButtonElement);

            TooltipMakerAPI optionNameElement = optionPanel.createUIElement(columnOneWidth, 40f, false);
            TooltipMakerAPI optionImage = optionNameElement.beginImageWithText(icon, 40f);
            optionImage.addPara(prefix + name + suffix, canAffordAndBuild || isForCodex ? Misc.getBasePlayerColor() : Misc.getNegativeHighlightColor(), 0f);
            optionNameElement.addImageWithText(0f);
            optionNameElement.getPosition().setXAlignOffset(-8f).setYAlignOffset(2f);
            optionPanel.addUIElement(optionNameElement);

            TooltipMakerAPI optionBuildTimeElement = optionPanel.createUIElement(columnWidth, 40f, false);
            optionBuildTimeElement.addPara(Misc.getWithDGS(buildTime), Misc.getHighlightColor(), 12f).setAlignment(Alignment.MID);
            optionBuildTimeElement.getPosition().rightOfMid(optionNameElement, 0f);
            optionPanel.addUIElement(optionBuildTimeElement);

            TooltipMakerAPI optionCostElement = optionPanel.createUIElement(columnWidth, 40f, false);
            optionCostElement.addPara(Misc.getDGSCredits(cost), canAfford || isForCodex ? Misc.getHighlightColor() : Misc.getNegativeHighlightColor(), 12f).setAlignment(Alignment.MID);
            optionCostElement.getPosition().rightOfMid(optionBuildTimeElement, 0f);
            optionPanel.addUIElement(optionCostElement);

            projectsElement.addCustom(optionPanel, 0f);

            if (canAffordAndBuild) {
                this.buttons.add(optionButton);
            }
        }

        this.projectsPanel.addUIElement(projectsElement);
        if (isForCodex) {
            this.projectsPanel.getPosition().setSize(width, projectsElement.getHeightSoFar() + this.projectsPanel.getPosition().getHeight());
        }
    }

    @Override
    public void buttonPressed(Object buttonId) {
        if (!(buttonId instanceof Utils.ProjectData)) {
            return;
        }

        this.selected = (Utils.ProjectData) buttonId;

        boolean isHighlighted = false;
        for (ButtonAPI button : this.buttons) {
            if (button.getCustomData() == buttonId && !button.isHighlighted()) {
                button.highlight();
                isHighlighted = true;
                continue;
            }
            if (button.isHighlighted()) {
                button.unhighlight();
            }
        }

        if (!isHighlighted) {
            this.selected = null;
        }
    }
}
