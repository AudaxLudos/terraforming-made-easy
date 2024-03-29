package terraformingmadeeasy.dialogs.tooltips;

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
        if (!condition.likesConditions.isEmpty()) {
            StringBuilder text = new StringBuilder();
            List<String> conditions = new ArrayList<>();
            for (Iterator<String> itr = condition.likesConditions.iterator(); itr.hasNext(); ) {
                conditions.add(Utils.capitalizeString(itr.next()));
                if (condition.likesConditions.size() == 1)
                    text.append("%s");
                else if (!itr.hasNext())
                    text.append("and %s");
                else
                    text.append("%s, ");
            }
            tooltip.addPara("Requires " + text, 0f, Misc.getHighlightColor(), conditions.toArray(new String[0]));
        } else {
            tooltip.addPara("Requires %s", 0f, Misc.getTextColor(), "no conditions");
        }
        tooltip.addSpacer(10f);
        if (!condition.hatesConditions.isEmpty()) {
            StringBuilder text = new StringBuilder();
            List<String> conditions = new ArrayList<>();
            for (Iterator<String> itr = condition.hatesConditions.iterator(); itr.hasNext(); ) {
                conditions.add(Utils.capitalizeString(itr.next()));
                if (condition.hatesConditions.size() == 1)
                    text.append("%s");
                else if (!itr.hasNext())
                    text.append("and %s");
                else
                    text.append("%s, ");
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
}
