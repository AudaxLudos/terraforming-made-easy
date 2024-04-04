package terraformingmadeeasy.dialogs.tooltips;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.BaseTooltipCreator;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import terraformingmadeeasy.Utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TerraformTooltip extends BaseTooltipCreator {
    public Utils.ModifiableCondition condition;

    public TerraformTooltip(Utils.ModifiableCondition condition) {
        this.condition = condition;
    }

    @Override
    public float getTooltipWidth(Object tooltipParam) {
        return 380f;
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
        displayPreferences(tooltip, condition.likedConditions, true, false);
        tooltip.addSpacer(10f);
        displayPreferences(tooltip, condition.hatedConditions, false, false);
        tooltip.addSpacer(10f);
        displayPreferences(tooltip, condition.likedIndustries, true, true);
        tooltip.addSpacer(10f);
        displayPreferences(tooltip, condition.hatedIndustries, false, true);
        tooltip.addSpacer(10f);
        Color textColor = condition.canChangeGasGiants ? Misc.getHighlightColor() : Misc.getNegativeHighlightColor();
        String textFormat = condition.canChangeGasGiants ? "Can" : "Cannot";
        tooltip.addPara("%s be used on gas giants", 0f, textColor, textFormat);
    }

    public void displayPreferences(TooltipMakerAPI tooltip, List<String> preferences, boolean likes, boolean isIndustry) {
        String preferenceText = likes ? "Requires " : "Removes ";
        String industryText = isIndustry ? "no industries" : "no conditions";
        if (!preferences.isEmpty()) {
            StringBuilder text = new StringBuilder();
            List<String> names = new ArrayList<>();
            for (Iterator<String> itr = preferences.iterator(); itr.hasNext();) {
                if (isIndustry)
                    names.add(Global.getSettings().getIndustrySpec(itr.next()).getName());
                else
                    names.add(Global.getSettings().getMarketConditionSpec(itr.next()).getName());

                if (preferences.size() == 1)
                    text.append("%s");
                else if (!itr.hasNext())
                    text.append("and %s");
                else
                    text.append("%s, ");
            }
            tooltip.addPara(preferenceText + text, 0f, Misc.getHighlightColor(), names.toArray(new String[0]));
        } else {
            tooltip.addPara(preferenceText + "%s", 0f, Misc.getTextColor(), industryText);
        }
    }
}
