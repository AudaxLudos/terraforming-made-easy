package terraformingmadeeasy.ui.tooltips;

import com.fs.starfarer.api.ui.BaseTooltipCreator;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class OrbitDaysFieldTooltip extends BaseTooltipCreator {
    @Override
    public float getTooltipWidth(Object tooltipParam) {
        return 380f;
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
        tooltip.addPara("Input a number between %s", 0f, Misc.getHighlightColor(), "100 - 10000");
        tooltip.addSpacer(10f);
        tooltip.addPara("How many days it takes for the megastructure to %s an entity", 0f, Misc.getHighlightColor(), "encircle");
    }
}
