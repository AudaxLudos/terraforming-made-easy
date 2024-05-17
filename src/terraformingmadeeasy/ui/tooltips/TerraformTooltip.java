package terraformingmadeeasy.ui.tooltips;

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
        displayPreferences(tooltip, this.condition.likedConditions, true, false, this.industry.hasAtLeastOneLikedCondition);
        tooltip.addSpacer(10f);
        displayPreferences(tooltip, this.condition.hatedConditions, false, false, this.industry.hasAtLeastOneLikedCondition);
        tooltip.addSpacer(10f);
        displayPreferences(tooltip, this.condition.likedIndustries, true, true, this.industry.hasAtLeastOneLikedCondition);
        tooltip.addSpacer(10f);
        displayPreferences(tooltip, this.condition.hatedIndustries, false, true, this.industry.hasAtLeastOneLikedCondition);
        tooltip.addSpacer(10f);
        Color textColor = this.condition.canChangeGasGiants ? Misc.getHighlightColor() : Misc.getNegativeHighlightColor();
        String textFormat = this.condition.canChangeGasGiants ? "Can" : "Cannot";
        tooltip.addPara("%s be used on gas giants", 0f, textColor, textFormat);
        if (this.condition.planetSpecOverride != null) {
            for (PlanetSpecAPI spec : Global.getSettings().getAllPlanetSpecs()) {
                if (spec.isStar()) {
                    continue;
                }
                if (Objects.equals(spec.getPlanetType(), this.condition.planetSpecOverride)) {
                    tooltip.addSpacer(10f);
                    tooltip.addPara("The planet will be terraformed into a %s world", 0f, Misc.getHighlightColor(), spec.getName());
                    break;
                }
            }
        }
    }

    public void displayPreferences(TooltipMakerAPI tooltip, List<String> preferences, boolean likes, boolean isIndustry, boolean hasAtLeastOneLikedCondition) {
        String preferenceText = likes ? "Requires " : "Removes ";
        preferenceText = likes && hasAtLeastOneLikedCondition && !isIndustry && preferences.size() > 1 ? preferenceText + "at least one of these " : preferenceText;
        preferenceText = !isIndustry ? preferenceText + "conditions: " : preferenceText + "industries: ";
        String industryText = "none";
        if (!preferences.isEmpty()) {
            StringBuilder text = new StringBuilder();
            List<String> names = new ArrayList<>();
            for (Iterator<String> itr = preferences.iterator(); itr.hasNext(); ) {
                if (isIndustry) {
                    names.add(Global.getSettings().getIndustrySpec(itr.next()).getName());
                } else {
                    names.add(Global.getSettings().getMarketConditionSpec(itr.next()).getName());
                }

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
