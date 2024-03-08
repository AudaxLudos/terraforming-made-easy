package terraformingmadeeasy.dialogs.tooltips;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import terraformingmadeeasy.Utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static terraformingmadeeasy.Utils.capitalizeString;

public class TerraformTooltip implements TooltipMakerAPI.TooltipCreator {
    public Utils.ModifiableCondition condition;

    public TerraformTooltip(Utils.ModifiableCondition condition) {
        this.condition = condition;
    }

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
}
