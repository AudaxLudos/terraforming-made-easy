package terraformingmadeeasy.ui.tooltips;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetSpecAPI;
import com.fs.starfarer.api.ui.BaseTooltipCreator;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.industries.TMEBaseIndustry;

import java.awt.*;
import java.util.Objects;

public class TerraformTooltip extends BaseTooltipCreator {
    public Utils.ModifiableCondition condition;
    public TMEBaseIndustry industry;

    public TerraformTooltip(Utils.ModifiableCondition condition, TMEBaseIndustry industry) {
        this.condition = condition;
        this.industry = industry;
    }

    @Override
    public float getTooltipWidth(Object tooltipParam) {
        return 380f;
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
        displayPreferences(tooltip, this.condition.likedConditions, false, true, 0f);
        displayPreferences(tooltip, this.condition.likedIndustries, false, false, 10f);
        displayPreferences(tooltip, this.condition.hatedConditions, true, true, 10f);
        Color textColor = this.condition.canChangeGasGiants ? Misc.getHighlightColor() : Misc.getNegativeHighlightColor();
        String textFormat = this.condition.canChangeGasGiants ? "Can" : "Cannot";
        tooltip.addPara("%s be used on gas giants", 10f, textColor, textFormat);
        if (this.condition.planetSpecOverride != null) {
            for (PlanetSpecAPI spec : Global.getSettings().getAllPlanetSpecs()) {
                if (spec.isStar()) {
                    continue;
                }
                if (Objects.equals(spec.getPlanetType(), this.condition.planetSpecOverride)) {
                    tooltip.addPara("The planet will be terraformed into a %s world", 10f, Misc.getHighlightColor(), spec.getName());
                    break;
                }
            }
        }
    }

    public void displayPreferences(TooltipMakerAPI tooltip, String textExpression, boolean isHated, boolean isCondition, float pad) {
        String titlePrefix = isHated ? "Removes " : "Required ";
        String titleSuffix = isCondition ? "Conditions" : "Industries";

        if (textExpression == null || textExpression.isEmpty()) {
            tooltip.addPara(titlePrefix + titleSuffix + ": %s", pad, Misc.getHighlightColor(), "None");
            return;
        }

        String[] expressions = textExpression.split(",");
        tooltip.addPara(titlePrefix + titleSuffix + ":", pad, Misc.getTextColor());

        for (String s : expressions) {
            String expression = s;
            String bulletPrefix = isHated ? "Removes " : "Needs ";
            String bulletTitle = "";
            if (expression.contains("needAll")) {
                bulletTitle = bulletPrefix + "All:";
                expression = expression.replaceAll("needAll:", "");
            } else if (expression.contains("needOne")) {
                bulletTitle = bulletPrefix + "One:";
                expression = expression.replaceAll("needOne:", "");
            }
            String[] ids = expression.split("\\|");

            if (!isHated) {
                tooltip.setBulletedListMode("    ");
                tooltip.addPara(bulletTitle, 3f, Misc.getTextColor());
            }

            tooltip.setBulletedListMode("        ");
            for (String id : ids) {
                boolean hasRequirement = isCondition ? this.industry.getMarket().hasCondition(id) : this.industry.getMarket().hasIndustry(id);
                Color color = isHated ? Misc.getHighlightColor() : hasRequirement ? Misc.getStoryBrightColor() : Misc.getNegativeHighlightColor();
                if (isCondition && Global.getSettings().getMarketConditionSpec(id) != null) {
                    String name = Global.getSettings().getMarketConditionSpec(id).getName();
                    tooltip.addPara(name, color, 0f);
                } else if (!isCondition && Global.getSettings().getIndustrySpec(id) != null) {
                    String name = Global.getSettings().getIndustrySpec(id).getName();
                    tooltip.addPara(name, color, 0f);
                }
            }
            tooltip.setBulletedListMode(null);
        }
    }
}
