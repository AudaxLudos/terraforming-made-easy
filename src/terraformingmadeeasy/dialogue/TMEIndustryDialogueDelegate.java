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

    public static final float HEIGHT = 600f;

    public TMEBaseIndustry industry;

    public TMEBaseIndustry.ModifiableCondition selected = null;

    public List<ButtonAPI> buttons = new ArrayList<>();

    public TMEIndustryDialogueDelegate(Industry industry) {
        this.industry = (TMEBaseIndustry) industry;
    }

    @Override
    public void createCustomDialog(CustomPanelAPI panel, CustomDialogCallback callback) {
        float oPad = 10f;
        float sPad = 2f;
        this.buttons.clear();

        TooltipMakerAPI panelTooltip = panel.createUIElement(WIDTH, HEIGHT, true);
        panelTooltip.addSectionHeading("Select planet condition to terraform", Alignment.MID, 0f);

        for (TMEBaseIndustry.ModifiableCondition modifiableCondition : this.industry.modifiableConditions) {
            int buildTime = Math.round(modifiableCondition.buildTime);
            float cost = modifiableCondition.cost;
            boolean canAfford = (Global.getSector().getPlayerFleet().getCargo().getCredits().get() >= cost);
            boolean canBuild = this.industry.canTerraformCondition(modifiableCondition);
            boolean hasCondition = this.industry.getMarket().hasCondition(modifiableCondition.spec.getId());
            Color baseColor = Misc.getButtonTextColor();
            Color bgColour = Misc.getDarkPlayerColor();
            Color brightColor = Misc.getBrightPlayerColor();
            if (!canAfford) {
                baseColor = Color.darkGray;
                bgColour = Color.lightGray;
                brightColor = Color.gray;
            }
            CustomPanelAPI conditionButtonPanel = panel.createCustomPanel(WIDTH, 40f, new ButtonReportingCustomPanel(this));
            TooltipMakerAPI buttonElement = conditionButtonPanel.createUIElement(WIDTH, 40f, false);
            buttonElement.getPosition().inTL(-10f, 0f);

            String textAddOrRemove = hasCondition ? "Remove" : "Add";
            ButtonAPI areaCheckbox = buttonElement.addAreaCheckbox(textAddOrRemove + " " + modifiableCondition.spec.getName(), modifiableCondition.spec.getId(), baseColor, bgColour, brightColor, WIDTH, 40f, 0f, true);
            areaCheckbox.setChecked((this.selected == modifiableCondition));
            areaCheckbox.setEnabled((canAfford && canBuild));
            conditionButtonPanel.addUIElement(buttonElement);
            panelTooltip.addCustom(conditionButtonPanel, 0f);
            this.buttons.add(areaCheckbox);
        }
        panel.addUIElement(panelTooltip).inTL(0f, 0f);
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
