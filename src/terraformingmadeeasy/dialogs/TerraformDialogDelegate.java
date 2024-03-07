package terraformingmadeeasy.dialogs;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;
import terraformingmadeeasy.industries.TMEBaseIndustry;
import terraformingmadeeasy.ui.ButtonPanelPlugin;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TerraformDialogDelegate extends TMEBaseDialogDelegate {
    public TMEBaseIndustry industry;

    public TerraformDialogDelegate(float width, float height, Industry industry) {
        WIDTH = width;
        HEIGHT = height;
        this.industry = (TMEBaseIndustry) industry;
    }

    public static String capitalizeString(String givenString) {
        String text = givenString.replace("_", " ");
        text = Character.toUpperCase(text.charAt(0)) + text.substring(1);
        return text;
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
        headerElement.addTableHeaderTooltip(0, "Name of the condition to terraform on a planet");
        headerElement.addTableHeaderTooltip(1, "Build time, in days. Until the terraforming project finishes.");
        headerElement.addTableHeaderTooltip(2, "One-time cost to begin terraforming project, in credits");
        headerElement.addTable("", 0, 0f);
        headerElement.getPrev().getPosition().setXAlignOffset(0f);
        panel.addUIElement(headerElement).inTL(0f, 0f);

        // list all modifiable condition of tme industry
        TooltipMakerAPI conditionsElement = panel.createUIElement(WIDTH, HEIGHT - 30f, true);

        for (final TMEBaseIndustry.ModifiableCondition modifiableCondition : this.industry.modifiableConditions) {
            float cost = modifiableCondition.cost;
            int buildTime = Math.round(modifiableCondition.buildTime);
            boolean canAfford = Global.getSector().getPlayerFleet().getCargo().getCredits().get() >= cost;
            boolean canBeRemoved = this.industry.getMarket().hasCondition(modifiableCondition.id);
            boolean canBuild = this.industry.canTerraformCondition(modifiableCondition) || canBeRemoved;
            if (this.industry.getMarket().getPlanetEntity().isGasGiant())
                canBuild = canBuild && modifiableCondition.canChangeGasGiants;
            boolean canAffordAndBuild = canBuild && canAfford;
            if (!canAfford) {
                baseColor = Misc.getGrayColor();
                bgColour = Misc.getGrayColor();
                brightColor = Misc.getGrayColor();
            }

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
            ButtonAPI areaCheckbox = conditionButton.addAreaCheckbox("", modifiableCondition, baseColor, bgColour, brightColor, WIDTH, 50f, 0f);
            areaCheckbox.setEnabled(canAffordAndBuild);
            conditionButton.addTooltipTo(addConditionTooltip(modifiableCondition), conditionPanel, TooltipMakerAPI.TooltipLocation.RIGHT);

            conditionPanel.addUIElement(conditionButton).inTL(-10f, 0f);
            conditionPanel.addUIElement(conditionNameElement);
            conditionPanel.addUIElement(conditionCostElement);
            conditionPanel.addUIElement(conditionBuildTimeElement);

            conditionsElement.addCustom(conditionPanel, 0f);
            this.buttons.add(areaCheckbox);
        }
        panel.addUIElement(conditionsElement).belowMid(headerElement, 0f);

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

        TMEBaseIndustry.ModifiableCondition selectedCondition = (TMEBaseIndustry.ModifiableCondition) this.selected;

        Global.getSoundPlayer().playSound("ui_upgrade_industry", 1f, 1f, Global.getSoundPlayer().getListenerPos(), new Vector2f());
        Global.getSector().getPlayerFleet().getCargo().getCredits().subtract(selectedCondition.cost);
        this.industry.startUpgrading(selectedCondition);
    }

    public TooltipMakerAPI.TooltipCreator addConditionTooltip(final TMEBaseIndustry.ModifiableCondition condition) {
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
                if (!condition.likesConditions.isEmpty()) {
                    int i = 0;
                    String text = "";
                    List<String> conditions = new ArrayList<>();
                    for (String cond : condition.likesConditions) {
                        conditions.add(capitalizeString(cond));
                        i++;
                        if (i != condition.likesConditions.size())
                            text = text + "%s or ";
                        else
                            text = text + "%s to add";
                    }
                    tooltip.addPara("Requires " + text, 0f, Misc.getHighlightColor(), conditions.toArray(new String[0]));
                } else {
                    tooltip.addPara("Requires %s", 0f, Misc.getTextColor(), "no conditions");
                }
                tooltip.addSpacer(10f);
                if (!condition.hatesConditions.isEmpty()) {
                    int i = 0;
                    String text = "";
                    List<String> conditions = new ArrayList<>();
                    for (String cond : condition.hatesConditions) {
                        conditions.add(capitalizeString(cond));
                        i++;
                        if (i != condition.hatesConditions.size())
                            text = text + "%s, ";
                        else
                            text = text + "and %s";
                    }
                    tooltip.addPara("Removes " + text, 0f, Misc.getHighlightColor(), conditions.toArray(new String[0]));
                } else {
                    tooltip.addPara("Removes %s", 0f, Misc.getTextColor(), "no conditions");
                }
                tooltip.addSpacer(10f);
                Color textColor = condition.canChangeGasGiants ? Misc.getHighlightColor() : Misc.getNegativeHighlightColor();
                String textFormat = condition.canChangeGasGiants ? "Can" : "Cannot";
                tooltip.addPara("%s be used on gas giants", 0f, textColor, textFormat);
            }
        };
    }
}
