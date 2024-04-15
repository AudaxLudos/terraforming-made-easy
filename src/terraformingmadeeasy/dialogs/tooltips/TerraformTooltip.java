package terraformingmadeeasy.dialogs.tooltips;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetSpecAPI;
import com.fs.starfarer.api.ui.BaseTooltipCreator;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.industries.TMEBaseIndustry;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
        displayPreferences(tooltip, condition.likedConditions, true, false, industry.hasAtLeastOneLikedCondition);
        tooltip.addSpacer(10f);
        displayPreferences(tooltip, condition.hatedConditions, false, false, industry.hasAtLeastOneLikedCondition);
        tooltip.addSpacer(10f);
        displayPreferences(tooltip, condition.likedIndustries, true, true, industry.hasAtLeastOneLikedCondition);
        tooltip.addSpacer(10f);
        displayPreferences(tooltip, condition.hatedIndustries, false, true, industry.hasAtLeastOneLikedCondition);
        tooltip.addSpacer(10f);
        Color textColor = condition.canChangeGasGiants ? Misc.getHighlightColor() : Misc.getNegativeHighlightColor();
        String textFormat = condition.canChangeGasGiants ? "Can" : "Cannot";
        tooltip.addPara("%s be used on gas giants", 0f, textColor, textFormat);
        if (condition.planetSpecOverride != null) {
            for (PlanetSpecAPI spec : Global.getSettings().getAllPlanetSpecs()) {
                if (spec.isStar()) continue;
                if (Objects.equals(spec.getPlanetType(), condition.planetSpecOverride)) {
                    tooltip.addSpacer(10f);
                    tooltip.addPara("The planet will be terraformed into a %s world", 0f, Misc.getHighlightColor(), spec.getName());
                    break;
                }
            }
        }
    }

    public void displayPreferences(TooltipMakerAPI tooltip, List<String> preferences, boolean likes, boolean isIndustry, boolean hasAtLeastOneLikedCondition) {
        String preferenceText = likes ? "Requires " : "Removes ";
        preferenceText = likes && hasAtLeastOneLikedCondition && !isIndustry && !preferences.isEmpty() ? preferenceText + "at least one of these " : preferenceText;
        preferenceText = !isIndustry ? preferenceText + "conditions: " : preferenceText + "industries: ";
        String industryText = "none";
        if (!preferences.isEmpty()) {
            StringBuilder text = new StringBuilder();
            List<String> names = new ArrayList<>();
            for (Iterator<String> itr = preferences.iterator(); itr.hasNext(); ) {
                if (isIndustry)
                    names.add(Global.getSettings().getIndustrySpec(itr.next()).getName());
                else
                    names.add(Global.getSettings().getMarketConditionSpec(itr.next()).getName());

                if (preferences.size() == 1) {
                    text.append("%s");
                } else if (!itr.hasNext()) {
                    if (!isIndustry && hasAtLeastOneLikedCondition && likes) {
                        text.append("or %s");
                    } else {
                        text.append("and %s");
                    }
                } else {
                    text.append("%s, ");
                }
            }
            tooltip.addPara(preferenceText + text, 0f, Misc.getHighlightColor(), names.toArray(new String[0]));
        } else {
            tooltip.addPara(preferenceText + " %s", 0f, Misc.getTextColor(), industryText);
        }
    }
}
