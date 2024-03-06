package terraformingmadeeasy.dialogue;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomDialogDelegate;
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;
import terraformingmadeeasy.industry.ConstructionGrid;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MegastructureDialogueDelegate extends BaseCustomDialogDelegate {
    public static final float WIDTH = 800f;
    public static final float HEIGHT = 400f;

    public ConstructionGrid industry;
    public ConstructionGrid.BuildableMegastructure selected = null;
    public List<ButtonAPI> buttons = new ArrayList<>();

    public MegastructureDialogueDelegate(ConstructionGrid industry) {
        this.industry = industry;
    }

    @Override
    public void createCustomDialog(CustomPanelAPI panel, CustomDialogCallback callback) {
        this.buttons.clear();

        Color baseColor = Misc.getDarkPlayerColor();
        Color bgColour = Misc.getDarkPlayerColor();
        Color brightColor = Misc.getDarkPlayerColor();
        float columnOneWidth = WIDTH / 3f + 100f;
        float columnWidth = (WIDTH - columnOneWidth) / 2f;

        TooltipMakerAPI headerElement = panel.createUIElement(WIDTH, 0f, false);
        headerElement.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
                0f, false, true,
                new Object[]{"Name", columnOneWidth, "Build time", columnWidth, "Cost", columnWidth - 6f});
        headerElement.addTableHeaderTooltip(0, "Name of the megastructure to build");
        headerElement.addTableHeaderTooltip(1, "Build time, in days. Until the megastructure project finishes.");
        headerElement.addTableHeaderTooltip(2, "One-time cost to begin megastructure project, in credits");
        headerElement.addTable("", 0, 0f);
        headerElement.getPrev().getPosition().setXAlignOffset(0f);
        panel.addUIElement(headerElement).inTL(0f, 0f);

        // list all modifiable megastructure of tme industry
        TooltipMakerAPI megastructuresElement = panel.createUIElement(WIDTH, HEIGHT - 30f, true);

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

            CustomPanelAPI megastructurePanel = panel.createCustomPanel(WIDTH, 50f, new MegastructureDialogueDelegate.ButtonReportingCustomPanel(this));
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
            megastructureButton.addTooltipTo(addMegastructureTooltip(buildableMegastructure), megastructurePanel, TooltipMakerAPI.TooltipLocation.RIGHT);

            megastructurePanel.addUIElement(megastructureButton).inTL(-10f, 0f);
            megastructurePanel.addUIElement(megastructureNameElement);
            megastructurePanel.addUIElement(megastructureCostElement);
            megastructurePanel.addUIElement(megastructureBuildTimeElement);

            megastructuresElement.addCustom(megastructurePanel, 0f);
            this.buttons.add(areaCheckbox);
        }
        panel.addUIElement(megastructuresElement).belowMid(headerElement, 0f);

        // show player credits
        TooltipMakerAPI creditsElement = panel.createUIElement(WIDTH, 0f, false);
        creditsElement.setParaSmallInsignia();
        creditsElement.addPara("Credits: %s", 0f, Misc.getGrayColor(), Misc.getHighlightColor(),
                Misc.getWithDGS(Global.getSector().getPlayerFleet().getCargo().getCredits().get()));
        panel.addUIElement(creditsElement).inBL(0f, -32f);
    }

    @Override
    public boolean hasCancelButton() {
        return true;
    }

    @Override
    public String getConfirmText() {
        return "Construct";
    }

    @Override
    public String getCancelText() {
        return "Cancel";
    }

    @Override
    public void customDialogConfirm() {
        if (this.selected == null) return;
        Global.getSoundPlayer().playSound("ui_upgrade_industry", 1f, 1f, Global.getSoundPlayer().getListenerPos(), new Vector2f());
        Global.getSector().getPlayerFleet().getCargo().getCredits().subtract(this.selected.cost);
        this.industry.startUpgrading(this.selected);
    }

    public void reportButtonPressed(Object id) {
        if (id instanceof ConstructionGrid.BuildableMegastructure)
            this.selected = (ConstructionGrid.BuildableMegastructure) id;
        boolean anyChecked = false;
        for (ButtonAPI button : this.buttons) {
            if (button.isChecked() && button.getCustomData() != id)
                button.setChecked(false);
            if (button.isChecked())
                anyChecked = true;
        }
        if (!anyChecked)
            this.selected = null;
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

    public static class ButtonReportingCustomPanel extends BaseCustomUIPanelPlugin {
        public MegastructureDialogueDelegate delegate;

        public ButtonReportingCustomPanel(MegastructureDialogueDelegate delegate) {
            this.delegate = delegate;
        }

        public void buttonPressed(Object buttonId) {
            this.delegate.reportButtonPressed(buttonId);
        }
    }
}
