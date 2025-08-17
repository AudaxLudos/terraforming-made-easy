package terraformingmadeeasy.ui.dialogs;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomDialogDelegate;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.ids.TMEIds;
import terraformingmadeeasy.industries.BaseDevelopmentIndustry;
import terraformingmadeeasy.industries.ConstructionGrid;
import terraformingmadeeasy.ui.plugins.DropdownPluginV2;
import terraformingmadeeasy.ui.plugins.ProjectListPlugin;
import terraformingmadeeasy.ui.plugins.TextFieldPluginV2;
import terraformingmadeeasy.ui.tooltips.*;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DevelopmentDialogDelegate extends BaseCustomDialogDelegate {
    public float width;
    public float height;
    public boolean hasCustomInputs;
    public BaseDevelopmentIndustry industry;
    public Object data = null;
    public Object data2 = null;
    public Object data3 = null;
    public Object data4 = null;
    public Object data5 = null;

    public DevelopmentDialogDelegate(float width, float height, boolean hasCustomInputs, BaseDevelopmentIndustry industry) {
        this.width = width;
        this.height = height;
        this.industry = industry;
        this.hasCustomInputs = hasCustomInputs;
    }

    @Override
    public void createCustomDialog(CustomPanelAPI panel, CustomDialogCallback callback) {
        addPlayerCredits(panel);
        TooltipMakerAPI mElement = panel.createUIElement(this.width, this.height, false);
        if (this.hasCustomInputs) {
            addOptionList(panel, mElement, 360f);
            addCustomDataInputs(panel, mElement);
        } else {
            addOptionList(panel, mElement, this.height);
        }
        panel.addUIElement(mElement).inTL(0f, 0f).setXAlignOffset(-5f);
    }

    public void addPlayerCredits(CustomPanelAPI panel) {
        TooltipMakerAPI creditsElement = panel.createUIElement(this.width / 4f, 20f, false);
        creditsElement.setParaSmallInsignia();
        LabelAPI creditsLabel = creditsElement.addPara("Credits: %s", 3f, Misc.getGrayColor(), Misc.getHighlightColor(),
                Misc.getWithDGS(Global.getSector().getPlayerFleet().getCargo().getCredits().get()));
        creditsLabel.setHighlightOnMouseover(true);
        creditsElement.addTooltipToPrevious(new TextTooltip("Credits available"), TooltipMakerAPI.TooltipLocation.ABOVE);
        creditsElement.getPosition().setXAlignOffset(-5f).setYAlignOffset(-30f);
        panel.addUIElement(creditsElement);
    }

    @SuppressWarnings("RedundantArrayCreation")
    public void addOptionList(CustomPanelAPI panel, TooltipMakerAPI tooltip, float height) {
        String nameTooltipText = "Name of the condition to terraform on a planet";
        String timeTooltipText = "Build time, in days. Until the terraforming project finishes.";
        String costTooltipText = "One-time cost to begin terraforming project, in credits";
        if (this.industry.getId().equals(TMEIds.CONSTRUCTION_GRID)) {
            nameTooltipText = "Name of megastructure to build";
            timeTooltipText = "Build time, in days. Until the megastructure project finishes.";
            costTooltipText = "One-time cost to begin megastructure project, in credits";
        } else if (this.industry.getId().equals(TMEIds.PLANETARY_HOLOGRAM)) {
            nameTooltipText = "Name of planet type to change into";
            timeTooltipText = "Build time, in days. Until a planet's visual changes.";
            costTooltipText = "One-time cost to change a planet's visual, in credits";
        }

        float columnOneWidth = this.width / 3f + 100f;
        float columnWidth = (this.width - columnOneWidth) / 2f;
        CustomPanelAPI projectsPanel = panel.createCustomPanel(this.width, 23f, null);
        TooltipMakerAPI projectsElement = projectsPanel.createUIElement(this.width, 23f, false);
        projectsElement.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
                0f, false, true,
                new Object[]{"Name", columnOneWidth, "Build time", columnWidth, "Cost", columnWidth - 6f});
        projectsElement.addTableHeaderTooltip(0, nameTooltipText);
        projectsElement.addTableHeaderTooltip(1, timeTooltipText);
        projectsElement.addTableHeaderTooltip(2, costTooltipText);
        projectsElement.addTable("", 0, 0f);
        projectsElement.getPrev().getPosition().setXAlignOffset(0f);
        projectsPanel.addUIElement(projectsElement).inTL(0f, 0f);
        tooltip.addCustom(projectsPanel, 0f);
        List<Utils.ProjectData> projects = this.industry.getProjects();
        Collections.sort(projects, new Utils.SortCanAffordAndBuild(this.industry));
        this.data = new ProjectListPlugin(panel, this.industry, this.industry.getId(), projects, this.width, height - 23f, false);
        tooltip.addCustom(((ProjectListPlugin) this.data).panel, 0f);
    }

    @SuppressWarnings("RedundantArrayCreation")
    public void addCustomDataInputs(CustomPanelAPI panel, TooltipMakerAPI tooltip) {
        // Inputs
        float orbitInputsPanelHeight = 63f;
        CustomPanelAPI inputsPanel = panel.createCustomPanel(this.width, orbitInputsPanelHeight, null);

        // Inputs header
        TooltipMakerAPI inputsHeaderElement = inputsPanel.createUIElement(this.width, orbitInputsPanelHeight, false);
        inputsHeaderElement.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
                0f, false, true,
                new Object[]{"Orbit Data", this.width});
        inputsHeaderElement.addTableHeaderTooltip(0, "Determines the location where the megastructure will be constructed");
        inputsHeaderElement.addTable("", 0, 0f);
        inputsHeaderElement.getPrev().getPosition().setXAlignOffset(0f);
        inputsPanel.addUIElement(inputsHeaderElement);

        // Inputs body
        TooltipMakerAPI inputsBodyElement = inputsPanel.createUIElement(this.width, orbitInputsPanelHeight - 22f, false);

        // Orbit focus input
        CustomPanelAPI orbitFocusPanel = inputsPanel.createCustomPanel(this.width / 4f, 40f, null);
        TooltipMakerAPI orbitFocusElement = orbitFocusPanel.createUIElement(this.width / 4f, 40f, false);
        orbitFocusElement.addPara("Orbit focus", 0f).setAlignment(Alignment.MID);
        orbitFocusElement.setParaSmallInsignia();
        orbitFocusElement.addSpacer(3f);
        // Orbit focus input dropdown
        Map<String, Object> options = new LinkedHashMap<>();
        for (SectorEntityToken planet : this.industry.getMarket().getStarSystem().getPlanets()) {
            options.put(planet.getName(), planet);
        }
        this.data2 = new DropdownPluginV2(orbitFocusPanel, 190f, 25f, options);
        ((DropdownPluginV2) this.data2).setSelected(this.industry.getMarket().getPrimaryEntity());
        orbitFocusElement.addCustom(((DropdownPluginV2) this.data2).panel, 0f);
        orbitFocusPanel.addUIElement(orbitFocusElement);
        inputsBodyElement.addComponent(orbitFocusPanel).setXAlignOffset(-5f);
        inputsBodyElement.addTooltipTo(new OrbitFocusFieldTooltip(), orbitFocusPanel, TooltipMakerAPI.TooltipLocation.BELOW);

        // Start angle input
        CustomPanelAPI startAnglePanel = inputsPanel.createCustomPanel(this.width / 4f, 40f, null);
        TooltipMakerAPI startAngleElement = startAnglePanel.createUIElement(this.width / 4f, 40f, false);
        startAngleElement.addPara("Start angle", 0f).setAlignment(Alignment.MID);
        startAngleElement.setParaSmallInsignia();
        startAngleElement.addSpacer(3f);
        // Start angle input text field
        this.data3 = new TextFieldPluginV2(startAnglePanel, 190f, 25f);
        ((TextFieldPluginV2) this.data3).textField.setMaxChars(7);
        ((TextFieldPluginV2) this.data3).textField.setText("0");
        startAngleElement.addCustom(((TextFieldPluginV2) this.data3).textFieldPanel, 0f);
        startAnglePanel.addUIElement(startAngleElement);
        inputsBodyElement.addComponent(startAnglePanel).rightOfMid(orbitFocusPanel, 0f);
        inputsBodyElement.addTooltipTo(new StartingAngleFieldTooltip(), startAnglePanel, TooltipMakerAPI.TooltipLocation.BELOW);

        // Orbit radius input
        CustomPanelAPI orbitRadiusPanel = inputsPanel.createCustomPanel(this.width / 4f, 40f, null);
        TooltipMakerAPI orbitRadiusElement = orbitRadiusPanel.createUIElement(this.width / 4f, 40f, false);
        orbitRadiusElement.addPara("Orbit radius", 0f).setAlignment(Alignment.MID);
        orbitRadiusElement.setParaSmallInsignia();
        orbitRadiusElement.addSpacer(3f);
        // Orbit radius input text field
        this.data4 = new TextFieldPluginV2(orbitRadiusPanel, 190f, 25f);
        ((TextFieldPluginV2) this.data4).textField.setMaxChars(7);
        ((TextFieldPluginV2) this.data4).textField.setText("1000");
        orbitRadiusElement.addCustom(((TextFieldPluginV2) this.data4).textFieldPanel, 0f);
        orbitRadiusPanel.addUIElement(orbitRadiusElement);
        inputsBodyElement.addComponent(orbitRadiusPanel).rightOfMid(startAnglePanel, 0f);
        inputsBodyElement.addTooltipTo(new OrbitRadiusFieldTooltip(), orbitRadiusPanel, TooltipMakerAPI.TooltipLocation.BELOW);

        // Orbit days input panel
        CustomPanelAPI orbitDaysPanel = inputsPanel.createCustomPanel(this.width / 4f, 40f, null);
        TooltipMakerAPI orbitDaysElement = orbitDaysPanel.createUIElement(this.width / 4f, 40f, false);
        orbitDaysElement.addPara("Orbit days", 0f).setAlignment(Alignment.MID);
        orbitDaysElement.setParaSmallInsignia();
        orbitDaysElement.addSpacer(3f);
        // Orbit days input text field
        this.data5 = new TextFieldPluginV2(orbitDaysPanel, 190f, 25f);
        ((TextFieldPluginV2) this.data5).textField.setMaxChars(7);
        ((TextFieldPluginV2) this.data5).textField.setText("100");
        orbitDaysElement.addCustom(((TextFieldPluginV2) this.data5).textFieldPanel, 0f);
        orbitDaysPanel.addUIElement(orbitDaysElement);
        inputsBodyElement.addComponent(orbitDaysPanel).rightOfMid(orbitRadiusPanel, 0f);
        inputsBodyElement.addTooltipTo(new OrbitDaysFieldTooltip(), orbitDaysPanel, TooltipMakerAPI.TooltipLocation.BELOW);

        inputsPanel.addUIElement(inputsBodyElement);
        tooltip.addCustom(inputsPanel, 0f);
    }

    @Override
    public void customDialogConfirm() {
        if (!(this.industry instanceof ConstructionGrid)) {
            return;
        }

        if (!(this.data instanceof ProjectListPlugin)
                || !(this.data2 instanceof DropdownPluginV2)
                || !(this.data3 instanceof TextFieldPluginV2)
                || !(this.data4 instanceof TextFieldPluginV2)
                || !(this.data5 instanceof TextFieldPluginV2)) {
            return;
        }

        Utils.ProjectData project = ((ProjectListPlugin) this.data).selected;
        SectorEntityToken orbitFocus = (SectorEntityToken) ((DropdownPluginV2) this.data2).getSelected();
        float startAngle = Float.parseFloat(((TextFieldPluginV2) this.data3).getText());
        float orbitRadius = Float.parseFloat(((TextFieldPluginV2) this.data4).getText());
        float orbitDays = Float.parseFloat(((TextFieldPluginV2) this.data5).getText());

        if (project == null || orbitFocus == null || startAngle < 0 || orbitRadius < 100 || orbitDays < 100) {
            return;
        }

        ((ConstructionGrid) this.industry).orbitData = new Utils.OrbitData(orbitFocus, startAngle, orbitRadius, orbitDays);
        this.industry.setProject(project);
        this.industry.startUpgrading();
        Global.getSector().getPlayerFleet().getCargo().getCredits().subtract(project.cost * Utils.BUILD_COST_MULTIPLIER);
        Global.getSoundPlayer().playSound("ui_upgrade_industry", 1f, 1f, Global.getSoundPlayer().getListenerPos(), Misc.ZERO);
    }

    @Override
    public String getConfirmText() {
        return "Build";
    }

    @Override
    public boolean hasCancelButton() {
        return true;
    }

    @Override
    public String getCancelText() {
        return "Cancel";
    }
}
