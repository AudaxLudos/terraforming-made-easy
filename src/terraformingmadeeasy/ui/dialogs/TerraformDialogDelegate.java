package terraformingmadeeasy.ui.dialogs;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.industries.TMEBaseIndustry;
import terraformingmadeeasy.ui.plugins.SelectButtonPlugin;
import terraformingmadeeasy.ui.tooltips.TerraformTooltip;

import java.awt.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TerraformDialogDelegate extends TMEBaseDialogDelegate {
    public TMEBaseIndustry industry;

    public TerraformDialogDelegate(float width, float height, Industry industry) {
        WIDTH = width;
        HEIGHT = height;
        this.industry = (TMEBaseIndustry) industry;
    }

    @SuppressWarnings("RedundantArrayCreation")
    @Override
    public void createCustomDialog(CustomPanelAPI panel, CustomDialogCallback callback) {
        this.buttons.clear();

        float columnOneWidth = WIDTH / 3f + 100f;
        float columnWidth = (WIDTH - columnOneWidth) / 2f;

        CustomPanelAPI mPanel = panel.createCustomPanel(WIDTH, HEIGHT, null);
        panel.addComponent(mPanel);

        TooltipMakerAPI mElement = mPanel.createUIElement(WIDTH, HEIGHT, false);
        mPanel.addUIElement(mElement).setXAlignOffset(-5f);

        /*terraforming options selection area*/
        CustomPanelAPI headerPanel = mPanel.createCustomPanel(WIDTH, 25f, null);
        TooltipMakerAPI headerElement = headerPanel.createUIElement(WIDTH, 25f, false);
        headerElement.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
                0f, false, true,
                new Object[]{"Name", columnOneWidth, "Build time", columnWidth, "Cost", columnWidth - 6f});
        headerElement.addTableHeaderTooltip(0, "Name of the condition to terraform on a planet");
        headerElement.addTableHeaderTooltip(1, "Build time, in days. Until the terraforming project finishes.");
        headerElement.addTableHeaderTooltip(2, "One-time cost to begin terraforming project, in credits");
        headerElement.addTable("", 0, 0f);
        headerElement.getPrev().getPosition().setXAlignOffset(0f);
        headerPanel.addUIElement(headerElement);
        mElement.addCustom(headerPanel, 0f);

        /*selectable terraforming options list*/
        CustomPanelAPI conditionsPanel = mPanel.createCustomPanel(WIDTH, 439f, null);
        TooltipMakerAPI conditionsElement = conditionsPanel.createUIElement(WIDTH, 439f, true);

        List<Utils.ModifiableCondition> conditions = this.industry.modifiableConditions;
        Collections.sort(conditions, new SortCanAffordAndBuild(this.industry));

        for (Utils.ModifiableCondition condition : conditions) {
            float cost = condition.cost;
            int buildTime = Math.round(condition.buildTime);
            boolean canAfford = Global.getSector().getPlayerFleet().getCargo().getCredits().get() >= cost;
            boolean canBeRemoved = this.industry.getMarket().hasCondition(condition.id);
            boolean canBuild = this.industry.canTerraformCondition(condition) || canBeRemoved;
            if (this.industry.getMarket().getPlanetEntity().isGasGiant())
                canBuild = canBuild && condition.canChangeGasGiants;
            boolean canAffordAndBuild = canBuild && canAfford;

            CustomPanelAPI conditionPanel = panel.createCustomPanel(WIDTH, 44f, new SelectButtonPlugin(this));
            TooltipMakerAPI conditionNameElement = conditionPanel.createUIElement(columnOneWidth, 40f, false);
            TooltipMakerAPI conditionImage = conditionNameElement.beginImageWithText(condition.icon, 40f);
            String addOrRemoveText = canBeRemoved ? "Remove " : "Add ";
            conditionImage.addPara(addOrRemoveText + condition.name, canAffordAndBuild ? Misc.getBasePlayerColor() : Misc.getNegativeHighlightColor(), 0f);
            conditionNameElement.addImageWithText(0f);
            conditionNameElement.getPosition().setXAlignOffset(-8f).setYAlignOffset(2f);

            TooltipMakerAPI conditionBuildTimeElement = conditionPanel.createUIElement(columnWidth, 40f, false);
            conditionBuildTimeElement.addPara(buildTime + "", Misc.getHighlightColor(), 12f).setAlignment(Alignment.MID);
            conditionBuildTimeElement.getPosition().rightOfMid(conditionNameElement, 0f);

            TooltipMakerAPI conditionCostElement = conditionPanel.createUIElement(columnWidth, 40f, false);
            conditionCostElement.addPara(Misc.getDGSCredits(cost), canAfford ? Misc.getHighlightColor() : Misc.getNegativeHighlightColor(), 12f).setAlignment(Alignment.MID);
            conditionCostElement.getPosition().rightOfMid(conditionBuildTimeElement, 0f);

            TooltipMakerAPI conditionButtonElement = conditionPanel.createUIElement(WIDTH, 44f, false);
            ButtonAPI conditionButton = conditionButtonElement.addButton("", condition, new Color(0, 195, 255, 190), new Color(0, 0, 0, 255), Alignment.MID, CutStyle.NONE, WIDTH, 44f, 0f);
            if (canAffordAndBuild) {
                conditionButton.setHighlightBrightness(0.6f);
                conditionButton.setGlowBrightness(0.56f);
                conditionButton.setQuickMode(true);
            } else {
                conditionButton.setCustomData(null);
                conditionButton.setButtonPressedSound("ui_button_disabled_pressed");
                conditionButton.setGlowBrightness(1.2f);
                conditionButton.setHighlightBrightness(0.6f);
                conditionButton.highlight();
            }
            conditionButtonElement.addTooltipTo(new TerraformTooltip(condition, industry), conditionPanel, TooltipMakerAPI.TooltipLocation.RIGHT);
            conditionButtonElement.getPosition().setXAlignOffset(-10f);

            conditionPanel.addUIElement(conditionButtonElement);
            conditionPanel.addUIElement(conditionNameElement);
            conditionPanel.addUIElement(conditionBuildTimeElement);
            conditionPanel.addUIElement(conditionCostElement);
            conditionsElement.addCustom(conditionPanel, 0f);

            if (conditionButton.getCustomData() != null)
                this.buttons.add(conditionButton);
        }
        conditionsPanel.addUIElement(conditionsElement);
        mElement.addCustom(conditionsPanel, 0f);

        /*show player credits*/
        TooltipMakerAPI creditsElement = panel.createUIElement(WIDTH, 0f, false);
        creditsElement.setParaSmallInsignia();
        creditsElement.addPara("Credits: %s", 0f, Misc.getGrayColor(), Misc.getHighlightColor(),
                Misc.getWithDGS(Global.getSector().getPlayerFleet().getCargo().getCredits().get()));
        panel.addUIElement(creditsElement).inBL(0f, -32f);
    }

    @Override
    public String getConfirmText() {
        return "Terraform";
    }

    @Override
    public void customDialogConfirm() {
        if (this.selected == null) return;
        Utils.ModifiableCondition selectedCondition = (Utils.ModifiableCondition) this.selected;
        Global.getSoundPlayer().playSound("ui_upgrade_industry", 1f, 1f, Global.getSoundPlayer().getListenerPos(), new Vector2f());
        Global.getSector().getPlayerFleet().getCargo().getCredits().subtract(selectedCondition.cost);
        this.industry.startUpgrading(selectedCondition);
    }

    public static class SortCanAffordAndBuild implements Comparator<Utils.ModifiableCondition> {
        TMEBaseIndustry industry;

        public SortCanAffordAndBuild(TMEBaseIndustry industry) {
            this.industry = industry;
        }

        @Override
        public int compare(Utils.ModifiableCondition o1, Utils.ModifiableCondition o2) {
            return Boolean.compare(canAffordAndBuild(o1), canAffordAndBuild(o2));
        }

        public boolean canAffordAndBuild(Utils.ModifiableCondition condition) {
            boolean canAfford = Global.getSector().getPlayerFleet().getCargo().getCredits().get() >= condition.cost;
            boolean canBeRemoved = this.industry.getMarket().hasCondition(condition.id);
            boolean canBuild = this.industry.canTerraformCondition(condition) || canBeRemoved;
            if (this.industry.getMarket().getPlanetEntity().isGasGiant())
                canBuild = canBuild && condition.canChangeGasGiants;
            return !(canBuild && canAfford);
        }
    }
}
