package terraformingmadeeasy.dialogs.tooltips;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class OrbitDaysFieldTooltip implements TooltipMakerAPI.TooltipCreator {
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
        tooltip.addPara("Input a number between %s", 0f, Misc.getHighlightColor(), "100 - 10000");
        tooltip.addPara("The time it takes for the megastructure encircle an entity", 0f, Misc.getHighlightColor(), "100 - 10000");
    }
}
