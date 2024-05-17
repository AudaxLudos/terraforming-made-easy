package terraformingmadeeasy.ui.dialogs;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.industries.ConstructionGrid;
import terraformingmadeeasy.ui.plugins.DropDownPlugin;
import terraformingmadeeasy.ui.plugins.TextFieldPlugin;
import terraformingmadeeasy.ui.tooltips.OrbitDaysFieldTooltip;
import terraformingmadeeasy.ui.tooltips.OrbitFocusFieldTooltip;
import terraformingmadeeasy.ui.tooltips.OrbitRadiusFieldTooltip;
import terraformingmadeeasy.ui.tooltips.StartingAngleFieldTooltip;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


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
        if (this.bgPanel == null) {
            return;
        }
        if (this.mPanel != null) {
            this.bgPanel.removeComponent(this.mPanel);
        }

        this.buttons.clear();

        float columnOneWidth = WIDTH / 3f + 100f;
        float columnWidth = (WIDTH - columnOneWidth) / 2f;
        float rowHeight = 360f;

        this.mPanel = this.bgPanel.createCustomPanel(WIDTH, HEIGHT, null);
        this.bgPanel.addComponent(this.mPanel);

        TooltipMakerAPI mElement = this.mPanel.createUIElement(WIDTH, HEIGHT, false);
        this.mPanel.addUIElement(mElement).setXAlignOffset(-5f);

        // Megastructures selection area
        CustomPanelAPI megaStructsPanel = this.mPanel.createCustomPanel(WIDTH, rowHeight, null);
        TooltipMakerAPI megaStructHeader = megaStructsPanel.createUIElement(WIDTH, rowHeight, false);
        megaStructHeader.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
                0f, false, true,
                new Object[]{"Name", columnOneWidth, "Build time", columnWidth, "Cost", columnWidth - 6f});
        megaStructHeader.addTableHeaderTooltip(0, "Name of the megastructure to build");
        megaStructHeader.addTableHeaderTooltip(1, "Build time, in days. Until the megastructure project finishes.");
        megaStructHeader.addTableHeaderTooltip(2, "One-time cost to begin megastructure project, in credits");
        megaStructHeader.addTable("", 0, 0f);
        megaStructHeader.getPrev().getPosition().setXAlignOffset(0f);
        megaStructsPanel.addUIElement(megaStructHeader);

        TooltipMakerAPI megaStructsElement = megaStructsPanel.createUIElement(WIDTH, rowHeight - 22f, true);
        List<Utils.BuildableMegastructure> megastructures = this.industry.buildableMegastructures;
        Collections.sort(megastructures, new SortCanAffordAndBuild(this.industry));
        for (Utils.BuildableMegastructure megastructure : megastructures) {
            CustomPanelAPI megaStructPanel = Utils.addCustomButton(this.mPanel, megastructure, this.industry, this.buttons, WIDTH, this);
            megaStructsElement.addCustom(megaStructPanel, 0f);
        }
        megaStructsPanel.addUIElement(megaStructsElement);
        mElement.addCustom(megaStructsPanel, 0f);

        // Inputs for megastructure orbit area
        CustomPanelAPI orbitInputsHeaderPanel = this.mPanel.createCustomPanel(WIDTH, 25f, null);
        TooltipMakerAPI orbitInputsHeaderElement = orbitInputsHeaderPanel.createUIElement(WIDTH, 25f, false);
        orbitInputsHeaderPanel.addUIElement(orbitInputsHeaderElement);
        mElement.addCustom(orbitInputsHeaderPanel, 0f);
        orbitInputsHeaderElement.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
                0f, false, true,
                new Object[]{"Orbit Data", WIDTH});
        orbitInputsHeaderElement.addTableHeaderTooltip(0, "Determines the location where the megastructure will be constructed");
        orbitInputsHeaderElement.addTable("", 0, 0f);
        orbitInputsHeaderElement.getPrev().getPosition().setXAlignOffset(0f);

        CustomPanelAPI orbitInputsPanel = this.mPanel.createCustomPanel(WIDTH, 50f, null);
        TooltipMakerAPI orbitInputsElement = orbitInputsPanel.createUIElement(WIDTH, 50f, false);
        orbitInputsPanel.addUIElement(orbitInputsElement);
        mElement.addCustom(orbitInputsPanel, 0f).getPosition().setXAlignOffset(-5f);

        // Orbit focus field
        CustomPanelAPI orbitFocusPanel = this.mPanel.createCustomPanel(WIDTH / 4f, 40f, new DropDownPlugin(this));
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
        if (this.showDropDown) {
            for (SectorEntityToken planet : this.industry.getMarket().getStarSystem().getPlanets()) {
                ButtonAPI currBtn = orbitFocusElement.addButton(planet.getName(), planet, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.NONE, 190f, 25f, 0f);
                if (prevBtn != null) {
                    currBtn.getPosition().aboveMid(prevBtn, 0f);
                }
                prevBtn = currBtn;
            }
        }
        orbitFocusElement.addTooltipTo(new OrbitFocusFieldTooltip(), orbitFocusPanel, TooltipMakerAPI.TooltipLocation.BELOW);

        // Start angle field
        TextFieldPlugin startAnglePlugin = new TextFieldPlugin();
        CustomPanelAPI startAnglePanel = this.mPanel.createCustomPanel(WIDTH / 4f, 40f, startAnglePlugin);
        TooltipMakerAPI startAngleElement = startAnglePanel.createUIElement(WIDTH / 4f, 40f, false);
        startAnglePanel.addUIElement(startAngleElement);
        orbitInputsElement.addCustom(startAnglePanel, 0f).getPosition().rightOfMid(orbitFocusPanel, 0f);
        this.startingAngleField = addCustomTextField(startAnglePanel, startAngleElement, this.startingAngleField, "360", 3, startAnglePlugin, new StartingAngleFieldTooltip());
        startAnglePlugin.setTextField(this.startingAngleField, 0, 0);

        // Orbit radius field
        TextFieldPlugin orbitRadiusPlugin = new TextFieldPlugin();
        CustomPanelAPI orbitRadiusPanel = this.mPanel.createCustomPanel(WIDTH / 4f, 40f, orbitRadiusPlugin);
        TooltipMakerAPI orbitRadiusElement = orbitRadiusPanel.createUIElement(WIDTH / 4f, 40f, false);
        orbitRadiusPanel.addUIElement(orbitRadiusElement);
        orbitInputsElement.addCustom(orbitRadiusPanel, 0f).getPosition().rightOfMid(startAnglePanel, 0f);
        this.orbitRadiusField = addCustomTextField(orbitRadiusPanel, orbitRadiusElement, this.orbitRadiusField, "1000", 3, orbitRadiusPlugin, new OrbitRadiusFieldTooltip());
        orbitRadiusPlugin.setTextField(this.orbitRadiusField, 0, 0);

        // Orbit days field
        TextFieldPlugin orbitDaysPlugin = new TextFieldPlugin();
        CustomPanelAPI orbitDaysPanel = this.mPanel.createCustomPanel(WIDTH / 4f, 40f, orbitDaysPlugin);
        TooltipMakerAPI orbitDaysElement = orbitDaysPanel.createUIElement(WIDTH / 4f, 40f, false);
        orbitDaysPanel.addUIElement(orbitDaysElement);
        orbitInputsElement.addCustom(orbitDaysPanel, 0f).getPosition().rightOfMid(orbitRadiusPanel, 0f);
        this.orbitDaysField = addCustomTextField(orbitDaysPanel, orbitDaysElement, this.orbitDaysField, "100", 5, orbitDaysPlugin, new OrbitDaysFieldTooltip());
        orbitDaysPlugin.setTextField(this.orbitDaysField, 0, 0);

        // Display player credits
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
        if (this.selected == null || this.orbitFocusField == null || this.startingAngleField == null || this.orbitRadiusField == null || this.orbitDaysField == null) {
            return;
        }

        Utils.BuildableMegastructure megastructure = (Utils.BuildableMegastructure) this.selected;
        Utils.OrbitData orbitData = new Utils.OrbitData(
                this.orbitFocusField,
                Float.parseFloat(this.startingAngleField.getText().trim()),
                Float.parseFloat(this.orbitRadiusField.getText().trim()),
                Float.parseFloat(this.orbitDaysField.getText().trim()));
        this.industry.buildableMegastructure = megastructure;
        this.industry.megastructureOrbitData = orbitData;
        this.industry.startUpgrading();
        Global.getSector().getPlayerFleet().getCargo().getCredits().subtract(megastructure.cost);
        Global.getSoundPlayer().playSound("ui_upgrade_industry", 1f, 1f, Global.getSoundPlayer().getListenerPos(), new Vector2f());
    }

    public TextFieldAPI addCustomTextField(CustomPanelAPI panel, TooltipMakerAPI tooltip, TextFieldAPI textField, String tempNum, int maxChar, TextFieldPlugin plugin, TooltipMakerAPI.TooltipCreator tip) {
        tooltip.addPara("Orbit Radius", 0f).setAlignment(Alignment.MID);
        tooltip.addSpacer(3f);
        TextFieldAPI tempField = tooltip.addTextField(190f, 25f, Fonts.DEFAULT_SMALL, 0f);
        tempField.setMaxChars(maxChar);
        if (textField == null) {
            tempField.setText(tempNum);
        } else {
            tempField.setText(textField.getText().trim());
        }
        plugin.setTextField(textField, 0, 0);
        tooltip.addTooltipTo(tip, panel, TooltipMakerAPI.TooltipLocation.BELOW);
        return tempField;
    }

    public static class SortCanAffordAndBuild implements Comparator<Utils.BuildableMegastructure> {
        ConstructionGrid industry;

        public SortCanAffordAndBuild(ConstructionGrid industry) {
            this.industry = industry;
        }

        @Override
        public int compare(Utils.BuildableMegastructure o1, Utils.BuildableMegastructure o2) {
            return Boolean.compare(canAffordAndBuild(o1), canAffordAndBuild(o2));
        }

        public boolean canAffordAndBuild(Utils.BuildableMegastructure structure) {
            return !(Global.getSector().getPlayerFleet().getCargo().getCredits().get() >= structure.cost) || !this.industry.canBuildMegastructure(structure.id);
        }
    }
}
