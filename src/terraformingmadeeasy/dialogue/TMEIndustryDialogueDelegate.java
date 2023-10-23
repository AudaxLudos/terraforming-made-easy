package terraformingmadeeasy.dialogue;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.CustomDialogDelegate;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;
import terraformingmadeeasy.industry.TMEBaseIndustry;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TMEIndustryDialogueDelegate implements CustomDialogDelegate {
    public static final float WIDTH = 600f;

    public static final float HEIGHT = 400f;

    public TMEBaseIndustry industry;

    public TMEBaseIndustry.ModifiableCondition selected = null;

    public List<ButtonAPI> buttons = new ArrayList<>();

    public TMEIndustryDialogueDelegate(Industry industry) {
        this.industry = (TMEBaseIndustry) industry;
    }

    @Override
    public void createCustomDialog(CustomPanelAPI panel, CustomDialogCallback callback) {
        this.buttons.clear();
        Color baseColor = Misc.getButtonTextColor();
        Color bgColour = Misc.getDarkPlayerColor();
        Color brightColor = Misc.getBrightPlayerColor();

        TooltipMakerAPI panelTooltip = panel.createUIElement(WIDTH, HEIGHT, true);
        panelTooltip.addSectionHeading("Select planet condition to terraform", Alignment.MID, 0f);

        for (final TMEBaseIndustry.ModifiableCondition modifiableCondition : this.industry.modifiableConditions) {
            float cost = modifiableCondition.cost;
            int buildTime = Math.round(modifiableCondition.buildTime);
            boolean canAfford = Global.getSector().getPlayerFleet().getCargo().getCredits().get() >= cost;
            boolean canBuild = this.industry.canTerraformCondition(modifiableCondition);
            boolean hasCondition = this.industry.getMarket().hasCondition(modifiableCondition.spec.getId());
            if (!canAfford) {
                baseColor = Color.darkGray;
                bgColour = Color.lightGray;
                brightColor = Color.gray;
            }

            // Clickable button for condition
            CustomPanelAPI conditionButtonPanel = panel.createCustomPanel(WIDTH, 40f, new ButtonReportingCustomPanel(this));
            String addOrRemoveText = hasCondition ? "Remove " : "Add ";
            TooltipMakerAPI buttonElement2 = conditionButtonPanel.createUIElement(WIDTH, 40f, false);
            buttonElement2.beginGrid(WIDTH / 6f, 3);
            buttonElement2.addToGrid(0, 0, addOrRemoveText + modifiableCondition.spec.getName(), "");
            buttonElement2.addToGrid(2, 0, "", buildTime + "", Misc.getHighlightColor());
            buttonElement2.addToGrid(4, 0, "", Misc.getDGSCredits(cost), canAfford ? Misc.getHighlightColor() : Misc.getNegativeHighlightColor());
            buttonElement2.addGrid(12f);

            TooltipMakerAPI anchor = conditionButtonPanel.createUIElement(WIDTH, 40f, false);
            ButtonAPI areaCheckbox = anchor.addAreaCheckbox("", modifiableCondition.spec.getId(),
                    baseColor, bgColour, brightColor, WIDTH - 5f, 40f, 0f);
            areaCheckbox.setChecked((this.selected == modifiableCondition));
            areaCheckbox.setEnabled((canAfford && canBuild));

            // Add tooltip to modifiable condition
            TooltipMakerAPI.TooltipCreator conditionTooltipCreator = new TooltipMakerAPI.TooltipCreator() {
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
                    if (!modifiableCondition.restrictions.isEmpty())
                        tooltip.addPara(
                                "Restrictions: %s", 0f, Misc.getNegativeHighlightColor(),
                                modifiableCondition.restrictions.toString()
                        );
                    else
                        tooltip.addPara("Restrictions: %s", 0f, Misc.getHighlightColor(), "No restrictions found");
                    tooltip.addSpacer(10f);
                    if (!modifiableCondition.requirements.isEmpty())
                        tooltip.addPara(
                                "Requirements: %s", 0f, Misc.getHighlightColor(),
                                modifiableCondition.requirements.toString()
                        );
                    else
                        tooltip.addPara("Requirements: %s", 0f, Misc.getHighlightColor(), "No requirements found");
                }
            };
            buttonElement2.addTooltipTo(conditionTooltipCreator, conditionButtonPanel, TooltipMakerAPI.TooltipLocation.RIGHT);

            // UI elements go over each other
            conditionButtonPanel.addUIElement(anchor).inTL(-10f, 0f);
            conditionButtonPanel.addUIElement(buttonElement2).inTL(0f, 0f);
            panelTooltip.addCustom(conditionButtonPanel, 0f);
            this.buttons.add(areaCheckbox);
        }
        panel.addUIElement(panelTooltip);

        // Show player credits
        CustomPanelAPI creditsPanel = panel.createCustomPanel(WIDTH, 0f, null);
        TooltipMakerAPI creditsElement = creditsPanel.createUIElement(WIDTH, 0f, false);
        creditsElement.setParaSmallInsignia();
        creditsElement.addPara("Credits: %s", 0f, Misc.getGrayColor(), Misc.getHighlightColor(),
                Misc.getWithDGS(Global.getSector().getPlayerFleet().getCargo().getCredits().get()));
        creditsPanel.addUIElement(creditsElement);
        panel.addComponent(creditsPanel).inBR(0f, -30f);
    }

    @Override
    public boolean hasCancelButton() {
        return true;
    }

    @Override
    public String getConfirmText() {
        return "Confirm";
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

    @Override
    public void customDialogCancel() {
    }

    @Override
    public CustomUIPanelPlugin getCustomPanelPlugin() {
        return null;
    }

    public void reportButtonPressed(Object id) {
        if (id instanceof String)
            for (TMEBaseIndustry.ModifiableCondition modifiableCondition : this.industry.modifiableConditions) {
                if (modifiableCondition.spec.getId().equals(id)) {
                    this.selected = modifiableCondition;
                    break;
                }
            }
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

    public static class ButtonReportingCustomPanel extends BaseCustomUIPanelPlugin {
        public TMEIndustryDialogueDelegate delegate;

        public ButtonReportingCustomPanel(TMEIndustryDialogueDelegate delegate) {
            this.delegate = delegate;
        }

        public void buttonPressed(Object buttonId) {
            super.buttonPressed(buttonId);
            this.delegate.reportButtonPressed(buttonId);
        }
    }
}
