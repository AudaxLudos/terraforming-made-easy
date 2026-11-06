package terraformingmadeeasy.ui.tooltips;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetSpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.codex.CodexDataV2;
import com.fs.starfarer.api.ui.BaseTooltipCreator;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import terraformingmadeeasy.loading.specs.TerraformingProjectSpec;

import java.awt.*;
import java.util.Objects;

public class TerraformingProjectTooltip extends BaseTooltipCreator {
    public final TerraformingProjectSpec project;
    public final MarketAPI market;

    public TerraformingProjectTooltip(TerraformingProjectSpec project, MarketAPI market) {
        this.project = project;
        this.market = market;
    }

    @Override
    public float getTooltipWidth(Object tooltipParam) {
        return 380f;
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
        displayPreferences(tooltip, this.project.getLikedConditions(), false, true, 0f);
        displayPreferences(tooltip, this.project.getLikedIndustries(), false, false, 10f);
        displayPreferences(tooltip, this.project.getHatedConditions(), true, true, 10f);
        Color textColor = this.project.isChangeGasGiants() ? Misc.getHighlightColor() : Misc.getNegativeHighlightColor();
        String textFormat = this.project.isChangeGasGiants() ? "Can" : "Cannot";
        tooltip.addPara("%s be used on gas giants", 10f, textColor, textFormat);
        if (this.project.getPlanetSpecOverride() != null) {
            for (PlanetSpecAPI spec : Global.getSettings().getAllPlanetSpecs()) {
                if (spec.isStar()) {
                    continue;
                }
                if (Objects.equals(spec.getPlanetType(), this.project.getPlanetSpecOverride())) {
                    tooltip.addPara("The planet will be terraformed into a %s world", 10f, Misc.getHighlightColor(), spec.getName());
                    break;
                }
            }
        }
        if (!Global.getSettings().isShowingCodex()) {
            tooltip.setCodexEntryId(CodexDataV2.getConditionEntryId(this.project.getConditionId()));
        }
    }

    public void displayPreferences(TooltipMakerAPI tooltip, String textExpression, boolean isHated, boolean isCondition, float pad) {
        boolean conditionForRemoval = this.market != null && this.market.hasCondition(this.project.getConditionId());
        String titlePrefix = isHated ? "Removes " : "Required ";
        String titleSuffix = isCondition ? "Conditions" : "Industries";

        if (textExpression == null || textExpression.isEmpty() || conditionForRemoval) {
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
                if (this.market != null) {
                    boolean hasRequirement = isCondition ? this.market.hasCondition(id) : this.market.hasIndustry(id);
                    Color color = isHated ? Misc.getHighlightColor() : hasRequirement ? Misc.getStoryBrightColor() : Misc.getNegativeHighlightColor();
                    if (isCondition && Global.getSettings().getMarketConditionSpec(id) != null) {
                        String name = Global.getSettings().getMarketConditionSpec(id).getName();
                        tooltip.addPara(name, color, 0f);
                    } else if (!isCondition && Global.getSettings().getIndustrySpec(id) != null) {
                        String name = Global.getSettings().getIndustrySpec(id).getName();
                        tooltip.addPara(name, color, 0f);
                    }
                } else {
                    Color color = Misc.getHighlightColor();
                    if (isCondition && Global.getSettings().getMarketConditionSpec(id) != null) {
                        String name = Global.getSettings().getMarketConditionSpec(id).getName();
                        tooltip.addPara(name, color, 0f);
                    } else if (!isCondition && Global.getSettings().getIndustrySpec(id) != null) {
                        String name = Global.getSettings().getIndustrySpec(id).getName();
                        tooltip.addPara(name, color, 0f);
                    }
                }
            }
            tooltip.setBulletedListMode(null);
        }
    }
}
