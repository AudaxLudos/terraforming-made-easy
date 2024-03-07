package terraformingmadeeasy.dialogs;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;
import terraformingmadeeasy.industries.ConstructionGrid;
import terraformingmadeeasy.ui.ButtonPanelPlugin;
import terraformingmadeeasy.ui.DropDownPanelPlugin;
import terraformingmadeeasy.ui.TextFieldPanelPlugin;

import java.awt.*;
import java.util.Objects;

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

    public void refreshPanel() {
        if (this.bgPanel == null) return;
        if (mPanel != null) this.bgPanel.removeComponent(mPanel);

        this.buttons.clear();

        Color baseColor = Misc.getDarkPlayerColor();
        Color bgColour = Misc.getDarkPlayerColor();
        Color brightColor = Misc.getDarkPlayerColor();
        float columnOneWidth = WIDTH / 3f + 100f;
        float columnWidth = (WIDTH - columnOneWidth) / 2f;

        this.mPanel = this.bgPanel.createCustomPanel(WIDTH, HEIGHT, null);
        this.bgPanel.addComponent(this.mPanel);

        TooltipMakerAPI headerElement = this.mPanel.createUIElement(WIDTH, 0f, false);
        headerElement.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
                0f, false, true,
                new Object[]{"Name", columnOneWidth, "Build time", columnWidth, "Cost", columnWidth - 6f});
        headerElement.addTableHeaderTooltip(0, "Name of the megastructure to build");
        headerElement.addTableHeaderTooltip(1, "Build time, in days. Until the megastructure project finishes.");
        headerElement.addTableHeaderTooltip(2, "One-time cost to begin megastructure project, in credits");
        headerElement.addTable("", 0, 0f);
        headerElement.getPrev().getPosition().setXAlignOffset(0f);
        this.mPanel.addUIElement(headerElement).inTL(0f, 0f);

        // list all modifiable megastructure of tme industry
        TooltipMakerAPI megastructuresElement = this.mPanel.createUIElement(WIDTH, HEIGHT - 30f, true);

        for (final ConstructionGrid.BuildableMegastructure buildableMegastructure : this.industry.buildableMegastructures) {
            float cost = buildableMegastructure.cost;
            int buildTime = Math.round(buildableMegastructure.buildTime);
            boolean canAfford = Global.getSector().getPlayerFleet().getCargo().getCredits().get() >= cost;
            boolean canBuild = this.industry.canBuildMegastructure(buildableMegastructure.id);
            boolean canAffordAndBuild = canBuild && canAfford;
            if (!canAfford) {
                baseColor = Misc.getGrayColor();
                bgColour = Misc.getGrayColor();
                brightColor = Misc.getGrayColor();
            }

            CustomPanelAPI megastructurePanel = this.mPanel.createCustomPanel(WIDTH, 50f, new ButtonPanelPlugin(this));
            TooltipMakerAPI megastructureNameElement = megastructurePanel.createUIElement(columnOneWidth, 40f, false);
            TooltipMakerAPI megastructureImage = megastructureNameElement.beginImageWithText(buildableMegastructure.icon, 40f);
            megastructureImage.addPara(buildableMegastructure.name, canAffordAndBuild ? Misc.getTextColor() : Misc.getNegativeHighlightColor(), 0f);
            megastructureNameElement.addImageWithText(0f);
            megastructureNameElement.getPosition().inTL(-5f, 5f);

            TooltipMakerAPI megastructureBuildTimeElement = megastructurePanel.createUIElement(columnWidth, 40f, false);
            megastructureBuildTimeElement.addPara(buildTime + "", Misc.getHighlightColor(), 12f).setAlignment(Alignment.MID);
            megastructureBuildTimeElement.getPosition().rightOfMid(megastructureNameElement, 0f);

            TooltipMakerAPI megastructureCostElement = megastructurePanel.createUIElement(columnWidth, 40f, false);
            megastructureCostElement.addPara(Misc.getDGSCredits(cost), canAfford ? Misc.getHighlightColor() : Misc.getNegativeHighlightColor(), 12f).setAlignment(Alignment.MID);
            megastructureCostElement.getPosition().rightOfMid(megastructureBuildTimeElement, 0f);

            TooltipMakerAPI megastructureButton = megastructurePanel.createUIElement(WIDTH, 50f, false);
            ButtonAPI areaCheckbox = megastructureButton.addAreaCheckbox("", buildableMegastructure, baseColor, bgColour, brightColor, WIDTH, 50f, 0f);
            areaCheckbox.setEnabled(canAffordAndBuild);
            areaCheckbox.setChecked(this.selected == buildableMegastructure);
            megastructureButton.addTooltipTo(addMegastructureTooltip(buildableMegastructure), megastructurePanel, TooltipMakerAPI.TooltipLocation.RIGHT);

            megastructurePanel.addUIElement(megastructureButton).inTL(-10f, 0f);
            megastructurePanel.addUIElement(megastructureNameElement);
            megastructurePanel.addUIElement(megastructureCostElement);
            megastructurePanel.addUIElement(megastructureBuildTimeElement);

            megastructuresElement.addCustom(megastructurePanel, 0f);
            this.buttons.add(areaCheckbox);
        }
        this.mPanel.addUIElement(megastructuresElement).belowMid(headerElement, 0f);

        TooltipMakerAPI orbitInputsHeader = this.mPanel.createUIElement(200f, 0f, false);
        orbitInputsHeader.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
                0f, false, true,
                new Object[]{"Orbit Data", 200f});
        orbitInputsHeader.addTableHeaderTooltip(0, "Determines where the megastructure will be completed");
        orbitInputsHeader.addTable("", 0, 0f);
        orbitInputsHeader.getPrev().getPosition().setXAlignOffset(0f);
        this.mPanel.addUIElement(orbitInputsHeader).rightOfTop(headerElement, 3f);

        TooltipMakerAPI orbitInputsElement = this.mPanel.createUIElement(200f, HEIGHT, true);

        CustomPanelAPI orbitFocusPanel = this.mPanel.createCustomPanel(190f, 50f, new DropDownPanelPlugin(this));
        TooltipMakerAPI orbitFocusElement = orbitFocusPanel.createUIElement(190f, 25f, false);
        orbitFocusElement.addPara("Orbit Focus:", 0f);
        if (this.orbitFocusField != null) {
            orbitFocusElement.addButton(this.orbitFocusField.getName(), this.orbitFocusField, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.NONE, 190f, 25f, 0f);
        } else {
            SectorEntityToken primaryEntity = this.industry.getMarket().getPrimaryEntity();
            orbitFocusElement.addButton(primaryEntity.getName(), primaryEntity, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.NONE, 190f, 25f, 0f);
        }
        orbitFocusElement.addTooltipToPrevious(new TooltipMakerAPI.TooltipCreator() {
            @Override
            public boolean isTooltipExpandable(Object tooltipParam) {
                return false;
            }

            @Override
            public float getTooltipWidth(Object tooltipParam) {
                return 380f;
            }

            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                tooltip.addPara("The main entity where the megastructure orbits", 0f);
            }
        }, TooltipMakerAPI.TooltipLocation.BELOW);
        if (showDropDown) {
            for (SectorEntityToken planet : this.industry.getMarket().getStarSystem().getPlanets()) {
                orbitFocusElement.addButton(planet.getName(), planet, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.NONE, 190f, 25f, 0f);
            }
        }
        orbitFocusPanel.addUIElement(orbitFocusElement).inTL(-5f, 0f);
        this.mPanel.addComponent(orbitFocusPanel).belowMid(orbitInputsHeader, 0f);

        TextFieldPanelPlugin startAnglePlugin = new TextFieldPanelPlugin();
        CustomPanelAPI startAnglePanel = this.mPanel.createCustomPanel(190f, 50f, startAnglePlugin);
        TooltipMakerAPI startAngleElement = startAnglePanel.createUIElement(190f, 25f, false);
        startAngleElement.addPara("Starting Angle:", 0f);
        TextFieldAPI tempStartingAngle = startAngleElement.addTextField(190f, 25f, Fonts.DEFAULT_SMALL, 3f);
        tempStartingAngle.setMaxChars(3);
        tempStartingAngle.setMaxChars(5);
        if (this.startingAngleField == null)
            tempStartingAngle.setText("0");
        else
            tempStartingAngle.setText(this.startingAngleField.getText().trim());
        this.startingAngleField = tempStartingAngle;
        startAnglePlugin.setTextField(this.startingAngleField, 0, 0);
        startAngleElement.addTooltipTo(new TooltipMakerAPI.TooltipCreator() {
            @Override
            public boolean isTooltipExpandable(Object tooltipParam) {
                return false;
            }

            @Override
            public float getTooltipWidth(Object tooltipParam) {
                return 380f;
            }

            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                tooltip.addPara("Input a number between %s", 0f, Misc.getHighlightColor(), "0 - 360");
            }
        }, this.startingAngleField, TooltipMakerAPI.TooltipLocation.BELOW);
        startAnglePanel.addUIElement(startAngleElement).inTL(-5f, 0f);
        this.mPanel.addComponent(startAnglePanel).belowMid(orbitFocusPanel, 0f);

        TextFieldPanelPlugin orbitRadiusPlugin = new TextFieldPanelPlugin();
        CustomPanelAPI orbitRadiusPanel = this.mPanel.createCustomPanel(190f, 50f, orbitRadiusPlugin);
        TooltipMakerAPI orbitRadiusElement = orbitRadiusPanel.createUIElement(190f, 25f, false);
        orbitRadiusElement.addPara("Orbit Radius:", 0f);
        TextFieldAPI tempOrbitRadiusField = orbitRadiusElement.addTextField(190f, 25f, Fonts.DEFAULT_SMALL, 3f);
        tempOrbitRadiusField.setMaxChars(5);
        if (this.orbitRadiusField == null)
            tempOrbitRadiusField.setText("0");
        else
            tempOrbitRadiusField.setText(this.orbitRadiusField.getText().trim());
        this.orbitRadiusField = tempOrbitRadiusField;
        orbitRadiusPlugin.setTextField(this.orbitRadiusField, 0, 0);
        orbitRadiusElement.addTooltipTo(new TooltipMakerAPI.TooltipCreator() {
            @Override
            public boolean isTooltipExpandable(Object tooltipParam) {
                return false;
            }

            @Override
            public float getTooltipWidth(Object tooltipParam) {
                return 380f;
            }

            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                tooltip.addPara("Input a number between %s", 0f, Misc.getHighlightColor(), "100 - 10000");
                tooltip.addPara("Distance from the megastructure to an entity", 0f);
            }
        }, this.orbitRadiusField, TooltipMakerAPI.TooltipLocation.BELOW);
        orbitRadiusPanel.addUIElement(orbitRadiusElement).inTL(-5f, 0f);
        this.mPanel.addComponent(orbitRadiusPanel).belowMid(startAnglePanel, 0f);

        TextFieldPanelPlugin orbitDaysPlugin = new TextFieldPanelPlugin();
        CustomPanelAPI orbitDaysPanel = this.mPanel.createCustomPanel(190f, 50f, orbitDaysPlugin);
        TooltipMakerAPI orbitDaysElement = orbitDaysPanel.createUIElement(190f, 25f, false);
        orbitDaysElement.addPara("Orbit Days:", 0f);
        TextFieldAPI tempOrbitDaysField = orbitDaysElement.addTextField(190f, 25f, Fonts.DEFAULT_SMALL, 3f);
        tempOrbitDaysField.setMaxChars(5);
        if (this.orbitDaysField == null)
            tempOrbitDaysField.setText("0");
        else
            tempOrbitDaysField.setText(this.orbitDaysField.getText().trim());
        this.orbitDaysField = tempOrbitDaysField;
        orbitDaysPlugin.setTextField(this.orbitDaysField, 0, 0);
        orbitDaysElement.addTooltipTo(new TooltipMakerAPI.TooltipCreator() {
            @Override
            public boolean isTooltipExpandable(Object tooltipParam) {
                return false;
            }

            @Override
            public float getTooltipWidth(Object tooltipParam) {
                return 380f;
            }

            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                tooltip.addPara("Input a number between %s", 0f, Misc.getHighlightColor(), "100 - 10000");
                tooltip.addPara("The time it takes for the megastructure encircle an entity", 0f, Misc.getHighlightColor(), "100 - 10000");
            }
        }, this.orbitDaysField, TooltipMakerAPI.TooltipLocation.BELOW);
        orbitDaysPanel.addUIElement(orbitDaysElement).inTL(-5f, 0f);
        this.mPanel.addComponent(orbitDaysPanel).belowMid(orbitRadiusPanel, 0f);

        this.mPanel.addUIElement(orbitInputsElement).belowMid(orbitInputsHeader, 0f);

        // show player credits
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
        if (this.selected == null || this.orbitFocusField == null || this.startingAngleField == null
                || this.orbitRadiusField == null || this.orbitDaysField == null)
            return;
        ConstructionGrid.BuildableMegastructure megastructure = (ConstructionGrid.BuildableMegastructure) this.selected;

        Global.getSoundPlayer().playSound("ui_upgrade_industry", 1f, 1f, Global.getSoundPlayer().getListenerPos(), new Vector2f());
        Global.getSector().getPlayerFleet().getCargo().getCredits().subtract(megastructure.cost);
        ConstructionGrid.OrbitData data = new ConstructionGrid.OrbitData(
                this.orbitFocusField,
                Float.parseFloat(this.startingAngleField.getText().trim()),
                Float.parseFloat(this.orbitRadiusField.getText().trim()),
                Float.parseFloat(this.orbitDaysField.getText().trim()));
        this.industry.startUpgrading(megastructure, data);
    }

    public TooltipMakerAPI.TooltipCreator addMegastructureTooltip(final ConstructionGrid.BuildableMegastructure megastructure) {
        return new TooltipMakerAPI.TooltipCreator() {
            @Override
            public boolean isTooltipExpandable(Object tooltipParam) {
                return false;
            }

            @Override
            public float getTooltipWidth(Object tooltipParam) {
                return 380f;
            }

            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                if (Objects.equals(megastructure.id, Entities.CORONAL_TAP)) {
                    tooltip.addPara("Only %s can exist in a system", 0f, Misc.getHighlightColor(), "1 " + megastructure.name);
                    tooltip.addSpacer(10f);
                    tooltip.addPara("Needs a %s in the system", 0f, Misc.getHighlightColor(), "Blue Supergiant Star");
                } else if (Objects.equals(megastructure.id, Entities.DERELICT_CRYOSLEEPER)) {
                    tooltip.addPara("Only %s can exist in a system", 0f, Misc.getHighlightColor(), "1 " + megastructure.name);
                } else if (Objects.equals(megastructure.id, Entities.INACTIVE_GATE)) {
                    tooltip.addPara("Only %s can exist in a system", 0f, Misc.getHighlightColor(), "1 " + megastructure.name);
                }
            }
        };
    }
}
