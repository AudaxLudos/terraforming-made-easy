package terraformingmadeeasy.ui.dialogs;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.industries.ConstructionGrid;
import terraformingmadeeasy.ui.plugins.DropdownPluginV2;
import terraformingmadeeasy.ui.plugins.TextFieldPluginV2;
import terraformingmadeeasy.ui.tooltips.*;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class MegastructureDialogDelegateV2 extends TMEBaseDialogDelegate {
    public ConstructionGrid industry;
    public DropdownPluginV2 orbitFocusDropdownPlugin = null;
    public TextFieldPluginV2 startAngleTextFieldPlugin = null;
    public TextFieldPluginV2 orbitRadiusTextFieldPlugin = null;
    public TextFieldPluginV2 orbitDaysTextFieldPlugin = null;

    public MegastructureDialogDelegateV2(float width, float height, ConstructionGrid industry) {
        WIDTH = width;
        HEIGHT = height;
        this.industry = industry;
    }

    @SuppressWarnings("RedundantArrayCreation")
    @Override
    public void createCustomDialog(CustomPanelAPI panel, CustomDialogCallback callback) {
        this.buttons.clear();

        float columnOneWidth = WIDTH / 3f + 100f;
        float columnWidth = (WIDTH - columnOneWidth) / 2f;
        float rowHeight = 360f;

        // Player credits
        // This is here to ensure that it is below new elements
        TooltipMakerAPI creditsElement = panel.createUIElement(WIDTH, 20f, false);
        panel.addUIElement(creditsElement);
        creditsElement.setParaSmallInsignia();
        LabelAPI creditsLabel = creditsElement.addPara("Credits: %s", 3f, Misc.getGrayColor(), Misc.getHighlightColor(),
                Misc.getWithDGS(Global.getSector().getPlayerFleet().getCargo().getCredits().get()));
        creditsLabel.setHighlightOnMouseover(true);
        creditsElement.addTooltipToPrevious(new TextTooltip("Credits available"), TooltipMakerAPI.TooltipLocation.ABOVE);
        creditsElement.getPosition().setXAlignOffset(-5f).setYAlignOffset(-30f);

        TooltipMakerAPI mElement = panel.createUIElement(WIDTH, HEIGHT, false);
        panel.addUIElement(mElement).setXAlignOffset(-5f);

        // Options
        CustomPanelAPI optionsPanel = panel.createCustomPanel(WIDTH, rowHeight, null);
        mElement.addCustom(optionsPanel, 0f);
        TooltipMakerAPI optionsHeaderElement = optionsPanel.createUIElement(WIDTH, rowHeight, false);
        optionsPanel.addUIElement(optionsHeaderElement);
        // Options header
        optionsHeaderElement.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
                0f, false, true,
                new Object[]{"Name", columnOneWidth, "Build time", columnWidth, "Cost", columnWidth - 6f});
        optionsHeaderElement.addTableHeaderTooltip(0, "Name of the megastructure to build");
        optionsHeaderElement.addTableHeaderTooltip(1, "Build time, in days. Until the megastructure project finishes.");
        optionsHeaderElement.addTableHeaderTooltip(2, "One-time cost to begin megastructure project, in credits");
        optionsHeaderElement.addTable("", 0, 0f);
        optionsHeaderElement.getPrev().getPosition().setXAlignOffset(0f);
        // Options body
        TooltipMakerAPI optionsBodyElement = optionsPanel.createUIElement(WIDTH, rowHeight - 22f, true);
        optionsPanel.addUIElement(optionsBodyElement);
        List<Utils.ProjectData> projects = this.industry.getProjects();
        Collections.sort(projects, new Utils.SortCanAffordAndBuild(this.industry));
        for (Utils.ProjectData project : projects) {
            CustomPanelAPI optionPanel = Utils.addCustomButton(panel, project, this.industry, this.buttons, WIDTH, this);
            optionsBodyElement.addCustom(optionPanel, 0f);
        }

        // Inputs
        float orbitInputsPanelHeight = 63f;
        CustomPanelAPI inputsPanel = panel.createCustomPanel(WIDTH, orbitInputsPanelHeight, null);
        mElement.addCustom(inputsPanel, 0f);
        // Inputs header
        TooltipMakerAPI inputsHeaderElement = inputsPanel.createUIElement(WIDTH, orbitInputsPanelHeight, false);
        inputsPanel.addUIElement(inputsHeaderElement);
        inputsHeaderElement.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
                0f, false, true,
                new Object[]{"Orbit Data", WIDTH});
        inputsHeaderElement.addTableHeaderTooltip(0, "Determines the location where the megastructure will be constructed");
        inputsHeaderElement.addTable("", 0, 0f);
        inputsHeaderElement.getPrev().getPosition().setXAlignOffset(0f);
        inputsHeaderElement.addSpacer(3f);
        // Inputs body
        TooltipMakerAPI inputsBodyElement = inputsPanel.createUIElement(WIDTH, orbitInputsPanelHeight - 22f, false);
        inputsPanel.addUIElement(inputsBodyElement);
        // Orbit focus input
        CustomPanelAPI orbitFocusPanel = inputsPanel.createCustomPanel(WIDTH / 4f, 40f, null);
        inputsBodyElement.addCustom(orbitFocusPanel, 0f).getPosition().setXAlignOffset(-5f);
        TooltipMakerAPI orbitFocusElement = orbitFocusPanel.createUIElement(WIDTH / 4f, 40f, false);
        orbitFocusPanel.addUIElement(orbitFocusElement);
        orbitFocusElement.addPara("Orbit focus", 0f).setAlignment(Alignment.MID);
        orbitFocusElement.setParaSmallInsignia();
        orbitFocusElement.addSpacer(3f);
        // Orbit focus input dropdown
        Map<String, Object> options = new LinkedHashMap<>();
        for (SectorEntityToken planet : this.industry.getMarket().getStarSystem().getPlanets()) {
            options.put(planet.getName(), planet);
        }
        this.orbitFocusDropdownPlugin = new DropdownPluginV2(orbitFocusPanel, 190f, 25f, options);
        this.orbitFocusDropdownPlugin.setSelected(this.industry.getMarket().getPrimaryEntity());
        orbitFocusElement.addCustom(this.orbitFocusDropdownPlugin.panel, 0f);
        inputsBodyElement.addTooltipTo(new OrbitFocusFieldTooltip(), orbitFocusPanel, TooltipMakerAPI.TooltipLocation.BELOW);

        // Start angle input
        CustomPanelAPI startAnglePanel = inputsPanel.createCustomPanel(WIDTH / 4f, 40f, null);
        inputsBodyElement.addCustom(startAnglePanel, 0f).getPosition().rightOfMid(orbitFocusPanel, 0f);
        TooltipMakerAPI startAngleElement = startAnglePanel.createUIElement(WIDTH / 4f, 40f, false);
        startAnglePanel.addUIElement(startAngleElement);
        startAngleElement.addPara("Start angle", 0f).setAlignment(Alignment.MID);
        startAngleElement.setParaSmallInsignia();
        startAngleElement.addSpacer(3f);
        // Start angle input text field
        this.startAngleTextFieldPlugin = new TextFieldPluginV2(startAnglePanel, 190f, 25f);
        this.startAngleTextFieldPlugin.textField.setMaxChars(7);
        this.startAngleTextFieldPlugin.textField.setText("0");
        startAngleElement.addCustom(this.startAngleTextFieldPlugin.textFieldPanel, 0f);
        inputsBodyElement.addTooltipTo(new StartingAngleFieldTooltip(), startAnglePanel, TooltipMakerAPI.TooltipLocation.BELOW);

        // Orbit radius input
        CustomPanelAPI orbitRadiusPanel = inputsPanel.createCustomPanel(WIDTH / 4f, 40f, null);
        inputsBodyElement.addCustom(orbitRadiusPanel, 0f).getPosition().rightOfMid(startAnglePanel, 0f);
        TooltipMakerAPI orbitRadiusElement = orbitRadiusPanel.createUIElement(WIDTH / 4f, 40f, false);
        orbitRadiusPanel.addUIElement(orbitRadiusElement);
        orbitRadiusElement.addPara("Orbit radius", 0f).setAlignment(Alignment.MID);
        orbitRadiusElement.setParaSmallInsignia();
        orbitRadiusElement.addSpacer(3f);
        // Orbit radius input text field
        this.orbitRadiusTextFieldPlugin = new TextFieldPluginV2(orbitRadiusPanel, 190f, 25f);
        this.orbitRadiusTextFieldPlugin.textField.setMaxChars(7);
        this.orbitRadiusTextFieldPlugin.textField.setText("1000");
        orbitRadiusElement.addCustom(this.orbitRadiusTextFieldPlugin.textFieldPanel, 0f);
        inputsBodyElement.addTooltipTo(new OrbitRadiusFieldTooltip(), orbitRadiusPanel, TooltipMakerAPI.TooltipLocation.BELOW);

        // Orbit days input panel
        CustomPanelAPI orbitDaysPanel = inputsPanel.createCustomPanel(WIDTH / 4f, 40f, null);
        inputsBodyElement.addCustom(orbitDaysPanel, 0f).getPosition().rightOfMid(orbitRadiusPanel, 0f);
        inputsBodyElement.addTooltipTo(new OrbitDaysFieldTooltip(), orbitDaysPanel, TooltipMakerAPI.TooltipLocation.BELOW);
        TooltipMakerAPI orbitDaysElement = orbitDaysPanel.createUIElement(WIDTH / 4f, 40f, false);
        orbitDaysPanel.addUIElement(orbitDaysElement);
        orbitDaysElement.addPara("Orbit days", 0f).setAlignment(Alignment.MID);
        orbitDaysElement.setParaSmallInsignia();
        orbitDaysElement.addSpacer(3f);
        // Orbit days input text field
        this.orbitDaysTextFieldPlugin = new TextFieldPluginV2(orbitDaysPanel, 190f, 25f);
        this.orbitDaysTextFieldPlugin.textField.setMaxChars(7);
        this.orbitDaysTextFieldPlugin.textField.setText("100");
        orbitDaysElement.addCustom(this.orbitDaysTextFieldPlugin.textFieldPanel, 0f);
    }

    @Override
    public String getConfirmText() {
        return "Construct";
    }

    @Override
    public void customDialogConfirm() {
        if (this.orbitFocusDropdownPlugin == null || this.orbitFocusDropdownPlugin.getSelected() == null
                || this.startAngleTextFieldPlugin == null || this.startAngleTextFieldPlugin.textField == null
                || this.orbitRadiusTextFieldPlugin == null || this.orbitRadiusTextFieldPlugin.textField == null
                || this.orbitDaysTextFieldPlugin == null || this.orbitDaysTextFieldPlugin.textField == null) {
            return;
        }

        this.industry.setProject((Utils.ProjectData) this.selected);
        this.industry.orbitData = new Utils.OrbitData(
                (SectorEntityToken) this.orbitFocusDropdownPlugin.getSelected(),
                Float.parseFloat(this.startAngleTextFieldPlugin.getText().trim()),
                Float.parseFloat(this.orbitRadiusTextFieldPlugin.getText().trim()),
                Float.parseFloat(this.orbitDaysTextFieldPlugin.getText().trim()));
        this.industry.startUpgrading();
        Global.getSector().getPlayerFleet().getCargo().getCredits().subtract(((Utils.ProjectData) this.selected).cost * Utils.BUILD_COST_MULTIPLIER);
        Global.getSoundPlayer().playSound("ui_upgrade_industry", 1f, 1f, Global.getSoundPlayer().getListenerPos(), Misc.ZERO);
    }
}
