package terraformingmadeeasy.dialogs;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.dialogs.tooltips.*;
import terraformingmadeeasy.industries.ConstructionGrid;
import terraformingmadeeasy.ui.ButtonPanelPlugin;
import terraformingmadeeasy.ui.DropDownPanelPlugin;
import terraformingmadeeasy.ui.TextFieldPanelPlugin;

public class MegastructureDialogDelegate extends TMEBaseDialogDelegate {
    public ConstructionGrid industry;
    public SectorEntityToken orbitFocusField = null;
    public TextFieldAPI startingAngleField = null;
    public TextFieldAPI orbitRadiusField = null;
    public TextFieldAPI orbitDaysField = null;
    public CustomPanelAPI bgPanel = null;
    public CustomPanelAPI mPanel = null;
    public boolean showDropDown = false;

    public MegastructureDialogDelegate(float width, float height, ConstructionGrid industry) {
        WIDTH = width;
        HEIGHT = height;
        this.industry = industry;
    }

    @Override
    public void createCustomDialog(CustomPanelAPI panel, CustomDialogCallback callback) {
        this.bgPanel = panel;
        refreshPanel();
    }

    @SuppressWarnings("RedundantArrayCreation")
    public void refreshPanel() {
        if (this.bgPanel == null) return;
        if (mPanel != null) this.bgPanel.removeComponent(mPanel);

        this.buttons.clear();

        float columnOneWidth = WIDTH / 3f + 100f;
        float columnWidth = (WIDTH - columnOneWidth) / 2f;

        this.mPanel = this.bgPanel.createCustomPanel(WIDTH, HEIGHT, null);
        this.bgPanel.addComponent(this.mPanel);

        TooltipMakerAPI mElement = this.mPanel.createUIElement(WIDTH, HEIGHT, false);
        this.mPanel.addUIElement(mElement).inTL(0f, 0f).setXAlignOffset(-5f);

        // megastructures selection area
        CustomPanelAPI headerPanel = this.mPanel.createCustomPanel(WIDTH, 25f, null);
        TooltipMakerAPI headerElement = headerPanel.createUIElement(WIDTH, 25f, false);
        headerPanel.addUIElement(headerElement);
        mElement.addCustom(headerPanel, 0f);
        headerElement.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
                0f, false, true,
                new Object[]{"Name", columnOneWidth, "Build time", columnWidth, "Cost", columnWidth - 6f});
        headerElement.addTableHeaderTooltip(0, "Name of the megastructure to build");
        headerElement.addTableHeaderTooltip(1, "Build time, in days. Until the megastructure project finishes.");
        headerElement.addTableHeaderTooltip(2, "One-time cost to begin megastructure project, in credits");
        headerElement.addTable("", 0, 0f);
        headerElement.getPrev().getPosition().setXAlignOffset(0f);

        // selectable megastructures list
        CustomPanelAPI megaStructsPanel = this.mPanel.createCustomPanel(WIDTH, 360f, null);
        TooltipMakerAPI megaStructsElement = megaStructsPanel.createUIElement(WIDTH, 360f, true);
        megaStructsPanel.addUIElement(megaStructsElement);
        mElement.addCustom(megaStructsPanel, 0f);
        for (Utils.BuildableMegastructure buildableMegastructure : this.industry.buildableMegastructures) {
            float cost = buildableMegastructure.cost;
            int buildTime = Math.round(buildableMegastructure.buildTime);
            boolean canAfford = Global.getSector().getPlayerFleet().getCargo().getCredits().get() >= cost;
            boolean canBuild = this.industry.canBuildMegastructure(buildableMegastructure.id);
            boolean canAffordAndBuild = canBuild && canAfford;

            CustomPanelAPI megaStructPanel = this.mPanel.createCustomPanel(WIDTH, 50f, new ButtonPanelPlugin(this));

            TooltipMakerAPI megaStructNameElement = megaStructPanel.createUIElement(columnOneWidth, 40f, false);
            TooltipMakerAPI megastructureImage = megaStructNameElement.beginImageWithText(buildableMegastructure.icon, 40f);
            megastructureImage.addPara(buildableMegastructure.name, canAffordAndBuild ? Misc.getTextColor() : Misc.getNegativeHighlightColor(), 0f);
            megaStructNameElement.addImageWithText(0f);
            megaStructNameElement.getPosition().inTL(-5f, 5f);

            TooltipMakerAPI megaStructBuildTimeElement = megaStructPanel.createUIElement(columnWidth, 40f, false);
            megaStructBuildTimeElement.addPara(buildTime + "", Misc.getHighlightColor(), 12f).setAlignment(Alignment.MID);
            megaStructBuildTimeElement.getPosition().rightOfMid(megaStructNameElement, 0f);

            TooltipMakerAPI megaStructCostElement = megaStructPanel.createUIElement(columnWidth, 40f, false);
            megaStructCostElement.addPara(Misc.getDGSCredits(cost), canAfford ? Misc.getHighlightColor() : Misc.getNegativeHighlightColor(), 12f).setAlignment(Alignment.MID);
            megaStructCostElement.getPosition().rightOfMid(megaStructBuildTimeElement, 0f);

            TooltipMakerAPI megaStructButtonElement = megaStructPanel.createUIElement(WIDTH, 50f, false);
            ButtonAPI megaStructButton = megaStructButtonElement.addAreaCheckbox("", buildableMegastructure, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(), WIDTH, 50f, 0f);
            megaStructButton.setEnabled(canAffordAndBuild);
            megaStructButton.setChecked(this.selected == buildableMegastructure);
            megaStructButtonElement.addTooltipTo(new MegastructureTooltip(buildableMegastructure), megaStructPanel, TooltipMakerAPI.TooltipLocation.RIGHT);

            megaStructPanel.addUIElement(megaStructButtonElement).inTL(-10f, 0f);
            megaStructPanel.addUIElement(megaStructNameElement);
            megaStructPanel.addUIElement(megaStructBuildTimeElement);
            megaStructPanel.addUIElement(megaStructCostElement);
            megaStructsElement.addCustom(megaStructPanel, 0f);

            this.buttons.add(megaStructButton);
        }

        // Inputs for megastructure orbit area
        CustomPanelAPI orbitInputsHeaderPanel = this.mPanel.createCustomPanel(WIDTH, 25f, null);
        TooltipMakerAPI orbitInputsHeaderElement = orbitInputsHeaderPanel.createUIElement(WIDTH, 25f, false);
        orbitInputsHeaderPanel.addUIElement(orbitInputsHeaderElement);
        mElement.addCustom(orbitInputsHeaderPanel, 0f);
        orbitInputsHeaderElement.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
                0f, false, true,
                new Object[]{"Orbit Data", WIDTH});
        orbitInputsHeaderElement.addTableHeaderTooltip(0, "Determines the location where the megastructure will be completed");
        orbitInputsHeaderElement.addTable("", 0, 0f);
        orbitInputsHeaderElement.getPrev().getPosition().setXAlignOffset(0f);

        CustomPanelAPI orbitInputsPanel = this.mPanel.createCustomPanel(WIDTH, 50f, null);
        TooltipMakerAPI orbitInputsElement = orbitInputsPanel.createUIElement(WIDTH, 50f, false);
        orbitInputsPanel.addUIElement(orbitInputsElement);
        mElement.addCustom(orbitInputsPanel, 0f).getPosition().setXAlignOffset(-5f);

        // orbit focus field
        CustomPanelAPI orbitFocusPanel = this.mPanel.createCustomPanel(WIDTH / 4f, 40f, new DropDownPanelPlugin(this));
        TooltipMakerAPI orbitFocusElement = orbitFocusPanel.createUIElement(WIDTH / 4f, 40f, false);
        orbitFocusPanel.addUIElement(orbitFocusElement);
        orbitInputsElement.addCustom(orbitFocusPanel, 0f);
        orbitFocusElement.addPara("Orbit Focus", 0f).setAlignment(Alignment.MID);
        orbitFocusElement.addSpacer(3f);
        orbitFocusElement.setParaSmallInsignia();
        ButtonAPI mainBtn;
        if (this.orbitFocusField != null) {
            mainBtn = orbitFocusElement.addButton(this.orbitFocusField.getName(), this.orbitFocusField, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.NONE, 190f, 25f, 0f);
        } else {
            SectorEntityToken primaryEntity = this.industry.getMarket().getPrimaryEntity();
            this.orbitFocusField = primaryEntity;
            mainBtn = orbitFocusElement.addButton(primaryEntity.getName(), primaryEntity, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.NONE, 190f, 25f, 0f);
        }
        ButtonAPI prevBtn = mainBtn;
        if (showDropDown) {
            for (SectorEntityToken planet : this.industry.getMarket().getStarSystem().getPlanets()) {
                ButtonAPI currBtn = orbitFocusElement.addButton(planet.getName(), planet, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.NONE, 190f, 25f, 0f);
                if (prevBtn != null)
                    currBtn.getPosition().aboveMid(prevBtn, 0f);
                prevBtn = currBtn;
            }
        }
        orbitFocusElement.addTooltipTo(new OrbitFocusFieldTooltip(), orbitFocusPanel, TooltipMakerAPI.TooltipLocation.BELOW);

        // start angle field
        TextFieldPanelPlugin startAnglePlugin = new TextFieldPanelPlugin();
        CustomPanelAPI startAnglePanel = this.mPanel.createCustomPanel(WIDTH / 4f, 40f, startAnglePlugin);
        TooltipMakerAPI startAngleElement = startAnglePanel.createUIElement(WIDTH / 4f, 40f, false);
        startAnglePanel.addUIElement(startAngleElement);
        orbitInputsElement.addCustom(startAnglePanel, 0f).getPosition().rightOfMid(orbitFocusPanel, 0f);
        startAngleElement.addPara("Starting Angle", 0f).setAlignment(Alignment.MID);
        startAngleElement.addSpacer(3f);
        TextFieldAPI tempStartingAngle = startAngleElement.addTextField(190f, 25f, Fonts.DEFAULT_SMALL, 0f);
        tempStartingAngle.setMaxChars(3);
        if (this.startingAngleField == null)
            tempStartingAngle.setText("0");
        else
            tempStartingAngle.setText(this.startingAngleField.getText().trim());
        this.startingAngleField = tempStartingAngle;
        startAnglePlugin.setTextField(this.startingAngleField, 0, 0);
        startAngleElement.addTooltipTo(new StartingAngleFieldTooltip(), startAnglePanel, TooltipMakerAPI.TooltipLocation.BELOW);

        // orbit radius field
        TextFieldPanelPlugin orbitRadiusPlugin = new TextFieldPanelPlugin();
        CustomPanelAPI orbitRadiusPanel = this.mPanel.createCustomPanel(WIDTH / 4f, 40f, orbitRadiusPlugin);
        TooltipMakerAPI orbitRadiusElement = orbitRadiusPanel.createUIElement(WIDTH / 4f, 40f, false);
        orbitRadiusPanel.addUIElement(orbitRadiusElement);
        orbitInputsElement.addCustom(orbitRadiusPanel, 0f).getPosition().rightOfMid(startAnglePanel, 0f);
        orbitRadiusElement.addPara("Orbit Radius", 0f).setAlignment(Alignment.MID);
        orbitRadiusElement.addSpacer(3f);
        TextFieldAPI tempOrbitRadiusField = orbitRadiusElement.addTextField(190f, 25f, Fonts.DEFAULT_SMALL, 0f);
        tempOrbitRadiusField.setMaxChars(5);
        if (this.orbitRadiusField == null)
            tempOrbitRadiusField.setText("1000");
        else
            tempOrbitRadiusField.setText(this.orbitRadiusField.getText().trim());
        this.orbitRadiusField = tempOrbitRadiusField;
        orbitRadiusPlugin.setTextField(this.orbitRadiusField, 0, 0);
        orbitRadiusElement.addTooltipTo(new OrbitRadiusFieldTooltip(), orbitRadiusPanel, TooltipMakerAPI.TooltipLocation.BELOW);

        // orbit days field
        TextFieldPanelPlugin orbitDaysPlugin = new TextFieldPanelPlugin();
        CustomPanelAPI orbitDaysPanel = this.mPanel.createCustomPanel(WIDTH / 4f, 40f, orbitDaysPlugin);
        TooltipMakerAPI orbitDaysElement = orbitDaysPanel.createUIElement(WIDTH / 4f, 40f, false);
        orbitDaysPanel.addUIElement(orbitDaysElement);
        orbitInputsElement.addCustom(orbitDaysPanel, 0f).getPosition().rightOfMid(orbitRadiusPanel, 0f);
        orbitDaysElement.addPara("Orbit Days", 0f).setAlignment(Alignment.MID);
        orbitDaysElement.addSpacer(3f);
        TextFieldAPI tempOrbitDaysField = orbitDaysElement.addTextField(190f, 25f, Fonts.DEFAULT_SMALL, 0f);
        tempOrbitDaysField.setMaxChars(5);
        if (this.orbitDaysField == null)
            tempOrbitDaysField.setText("100");
        else
            tempOrbitDaysField.setText(this.orbitDaysField.getText().trim());
        this.orbitDaysField = tempOrbitDaysField;
        orbitDaysPlugin.setTextField(this.orbitDaysField, 0, 0);
        orbitDaysElement.addTooltipTo(new OrbitDaysFieldTooltip(), orbitDaysElement, TooltipMakerAPI.TooltipLocation.BELOW);

        // display player credits
        TooltipMakerAPI creditsElement = this.mPanel.createUIElement(WIDTH, 0f, false);
        creditsElement.setParaSmallInsignia();
        creditsElement.addPara("Credits: %s", 0f, Misc.getGrayColor(), Misc.getHighlightColor(),
                Misc.getWithDGS(Global.getSector().getPlayerFleet().getCargo().getCredits().get()));
        this.mPanel.addUIElement(creditsElement).inBL(0f, -32f);
    }

    @Override
    public String getConfirmText() {
        return "Construct";
    }

    @Override
    public void customDialogConfirm() {
        if (this.selected == null || this.orbitFocusField == null || this.startingAngleField == null || this.orbitRadiusField == null || this.orbitDaysField == null)
            return;

        Utils.BuildableMegastructure megastructure = (Utils.BuildableMegastructure) this.selected;

        Global.getSoundPlayer().playSound("ui_upgrade_industry", 1f, 1f, Global.getSoundPlayer().getListenerPos(), new Vector2f());
        Global.getSector().getPlayerFleet().getCargo().getCredits().subtract(megastructure.cost);
        Utils.OrbitData data = new Utils.OrbitData(
                this.orbitFocusField,
                Float.parseFloat(this.startingAngleField.getText().trim()),
                Float.parseFloat(this.orbitRadiusField.getText().trim()),
                Float.parseFloat(this.orbitDaysField.getText().trim()));
        this.industry.startUpgrading(megastructure, data);
    }
}
