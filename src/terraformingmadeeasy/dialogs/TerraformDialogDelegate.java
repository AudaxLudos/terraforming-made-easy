package terraformingmadeeasy.dialogs;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.dialogs.tooltips.TerraformTooltip;
import terraformingmadeeasy.industries.TMEBaseIndustry;
import terraformingmadeeasy.ui.ButtonPanelPlugin;

public class TerraformDialogDelegate extends TMEBaseDialogDelegate {
    public TMEBaseIndustry industry;

    public TerraformDialogDelegate(float width, float height, Industry industry) {
        WIDTH = width;
        HEIGHT = height;
        this.industry = (TMEBaseIndustry) industry;
    }

    @Override
    public void createCustomDialog(CustomPanelAPI panel, CustomDialogCallback callback) {
        this.buttons.clear();

        float columnOneWidth = WIDTH / 3f + 100f;
        float columnWidth = (WIDTH - columnOneWidth) / 2f;

        CustomPanelAPI mPanel = panel.createCustomPanel(WIDTH, HEIGHT, null);
        panel.addComponent(mPanel);

        TooltipMakerAPI mElement = mPanel.createUIElement(WIDTH, HEIGHT, false);
        mPanel.addUIElement(mElement).inTL(0f, 0f).setXAlignOffset(-5f);

        // terraforming options selection area
        CustomPanelAPI headerPanel = mPanel.createCustomPanel(WIDTH, 25f, null);
        TooltipMakerAPI headerElement = headerPanel.createUIElement(WIDTH, 25f, false);
        headerPanel.addUIElement(headerElement);
        mElement.addCustom(headerPanel, 0f);
        headerElement.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
                0f, false, true,
                new Object[]{"Name", columnOneWidth, "Build time", columnWidth, "Cost", columnWidth - 6f});
        headerElement.addTableHeaderTooltip(0, "Name of the condition to terraform on a planet");
        headerElement.addTableHeaderTooltip(1, "Build time, in days. Until the terraforming project finishes.");
        headerElement.addTableHeaderTooltip(2, "One-time cost to begin terraforming project, in credits");
        headerElement.addTable("", 0, 0f);
        headerElement.getPrev().getPosition().setXAlignOffset(0f);

        // selectable terraforming options list
        CustomPanelAPI conditionsPanel = mPanel.createCustomPanel(WIDTH, 439f, null);
        TooltipMakerAPI conditionsElement = conditionsPanel.createUIElement(WIDTH, 439f, true);
        conditionsPanel.addUIElement(conditionsElement);
        mElement.addCustom(conditionsPanel, 0f);
        for (Utils.ModifiableCondition modifiableCondition : this.industry.modifiableConditions) {
            float cost = modifiableCondition.cost;
            int buildTime = Math.round(modifiableCondition.buildTime);
            boolean canAfford = Global.getSector().getPlayerFleet().getCargo().getCredits().get() >= cost;
            boolean canBeRemoved = this.industry.getMarket().hasCondition(modifiableCondition.id);
            boolean canBuild = this.industry.canTerraformCondition(modifiableCondition) || canBeRemoved;
            if (this.industry.getMarket().getPlanetEntity().isGasGiant())
                canBuild = canBuild && modifiableCondition.canChangeGasGiants;
            boolean canAffordAndBuild = canBuild && canAfford;

            CustomPanelAPI conditionPanel = panel.createCustomPanel(WIDTH, 50f, new ButtonPanelPlugin(this));
            TooltipMakerAPI conditionNameElement = conditionPanel.createUIElement(columnOneWidth, 40f, false);
            TooltipMakerAPI conditionImage = conditionNameElement.beginImageWithText(modifiableCondition.icon, 40f);
            String addOrRemoveText = canBeRemoved ? "Remove " : "Add ";
            conditionImage.addPara(addOrRemoveText + modifiableCondition.name, canAffordAndBuild ? Misc.getTextColor() : Misc.getNegativeHighlightColor(), 0f);
            conditionNameElement.addImageWithText(0f);
            conditionNameElement.getPosition().inTL(-5f, 5f);

            TooltipMakerAPI conditionBuildTimeElement = conditionPanel.createUIElement(columnWidth, 40f, false);
            conditionBuildTimeElement.addPara(buildTime + "", Misc.getHighlightColor(), 12f).setAlignment(Alignment.MID);
            conditionBuildTimeElement.getPosition().rightOfMid(conditionNameElement, 0f);

            TooltipMakerAPI conditionCostElement = conditionPanel.createUIElement(columnWidth, 40f, false);
            conditionCostElement.addPara(Misc.getDGSCredits(cost), canAfford ? Misc.getHighlightColor() : Misc.getNegativeHighlightColor(), 12f).setAlignment(Alignment.MID);
            conditionCostElement.getPosition().rightOfMid(conditionBuildTimeElement, 0f);

            TooltipMakerAPI conditionButton = conditionPanel.createUIElement(WIDTH, 50f, false);
            ButtonAPI areaCheckbox = conditionButton.addAreaCheckbox("", modifiableCondition, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(), WIDTH, 50f, 0f);
            areaCheckbox.setEnabled(canAffordAndBuild);
            conditionButton.addTooltipTo(new TerraformTooltip(modifiableCondition), conditionPanel, TooltipMakerAPI.TooltipLocation.RIGHT);

            conditionPanel.addUIElement(conditionButton).inTL(-10f, 0f);
            conditionPanel.addUIElement(conditionNameElement);
            conditionPanel.addUIElement(conditionBuildTimeElement);
            conditionPanel.addUIElement(conditionCostElement);

            conditionsElement.addCustom(conditionPanel, 0f);
            this.buttons.add(areaCheckbox);
        }

        // show player credits
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
}
