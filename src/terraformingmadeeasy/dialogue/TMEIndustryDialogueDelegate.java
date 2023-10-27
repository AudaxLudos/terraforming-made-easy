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
        float columnOneWidth = WIDTH / 3f + 40f;
        float columnWidth = (WIDTH - columnOneWidth) / 2f;

        TooltipMakerAPI headerElement = panel.createUIElement(WIDTH, 0f, false);
        headerElement.addSectionHeading("Select planet condition to terraform", Alignment.MID, 0f);
        panel.addUIElement(headerElement).inTL(0f,0f);

        // list all modifiable condition of tme industry
        TooltipMakerAPI conditionsElement = panel.createUIElement(WIDTH, HEIGHT - 40f, true);

        for (final TMEBaseIndustry.ModifiableCondition modifiableCondition : this.industry.modifiableConditions) {
            float cost = modifiableCondition.cost;
            int buildTime = Math.round(modifiableCondition.buildTime);
            boolean canAfford = Global.getSector().getPlayerFleet().getCargo().getCredits().get() >= cost;
            boolean canBuild = this.industry.canTerraformCondition(modifiableCondition);
            boolean canAffordAndBuild = canBuild && canAfford;
            boolean hasCondition = this.industry.getMarket().hasCondition(modifiableCondition.spec.getId());
            String addOrRemoveText = hasCondition ? "Remove " : "Add ";
            if (!canAfford) {
                baseColor = Color.darkGray;
                bgColour = Color.lightGray;
                brightColor = Color.gray;
            }

            CustomPanelAPI conditionPanel = panel.createCustomPanel(WIDTH, 50f, new ButtonReportingCustomPanel(this));
            TooltipMakerAPI conditionNameElement = conditionPanel.createUIElement(columnOneWidth, 40f, false);
            TooltipMakerAPI conditionImage = conditionNameElement.beginImageWithText(modifiableCondition.spec.getIcon(), 40f);
            conditionImage.addPara(addOrRemoveText + modifiableCondition.spec.getName(), canAffordAndBuild ? Misc.getTextColor() : Misc.getNegativeHighlightColor(),0f);
            conditionNameElement.addImageWithText(0f);
            conditionNameElement.getPosition().inTL(-5f, 5f);

            TooltipMakerAPI conditionBuildTimeElement = conditionPanel.createUIElement(columnWidth, 40f, false);
            conditionBuildTimeElement.addPara(buildTime + "", Misc.getHighlightColor(),12f).setAlignment(Alignment.MID);
            conditionBuildTimeElement.getPosition().rightOfMid(conditionNameElement, 0f);

            TooltipMakerAPI conditionCostElement = conditionPanel.createUIElement(columnWidth, 40f, false);
            conditionCostElement.addPara(Misc.getDGSCredits(cost), canAfford ? Misc.getHighlightColor() : Misc.getNegativeHighlightColor(),12f).setAlignment(Alignment.MID);
            conditionCostElement.getPosition().rightOfMid(conditionBuildTimeElement, 0f);

            TooltipMakerAPI conditionButton = conditionPanel.createUIElement(WIDTH, 50f, false);
            ButtonAPI areaCheckbox = conditionButton.addAreaCheckbox("", modifiableCondition, baseColor, bgColour, brightColor, WIDTH, 50f, 0f);
            areaCheckbox.setChecked((this.selected == modifiableCondition));
            areaCheckbox.setEnabled(canAffordAndBuild);
            conditionButton.addTooltipTo(addConditionTooltip(modifiableCondition), conditionPanel, TooltipMakerAPI.TooltipLocation.RIGHT);

            conditionPanel.addUIElement(conditionButton).inTL(-10f, 0f);
            conditionPanel.addUIElement(conditionNameElement);
            conditionPanel.addUIElement(conditionCostElement);
            conditionPanel.addUIElement(conditionBuildTimeElement);

            conditionsElement.addCustom(conditionPanel, 0f);
            this.buttons.add(areaCheckbox);
        }
        panel.addUIElement(conditionsElement).inMid();

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
    public void customDialogCancel() {}

    @Override
    public CustomUIPanelPlugin getCustomPanelPlugin() {
        return null;
    }

    public void reportButtonPressed(Object id) {
        if (id instanceof TMEBaseIndustry.ModifiableCondition)
            this.selected = (TMEBaseIndustry.ModifiableCondition) id;
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

    public TooltipMakerAPI.TooltipCreator addConditionTooltip (final TMEBaseIndustry.ModifiableCondition condition) {
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
                    if (!condition.requirements.isEmpty())
                        tooltip.addPara(
                                "Requirements: %s", 0f, Misc.getHighlightColor(),
                                condition.requirements.toString()
                        );
                    else
                        tooltip.addPara("Requirements: %s", 0f, Misc.getHighlightColor(), "No requirements found");
                    tooltip.addSpacer(10f);
                    if (!condition.restrictions.isEmpty())
                        tooltip.addPara(
                                "Restrictions: %s", 0f, Misc.getNegativeHighlightColor(),
                                condition.restrictions.toString()
                        );
                    else
                        tooltip.addPara("Restrictions: %s", 0f, Misc.getHighlightColor(), "No restrictions found");
                }
            };
    }
}
