package terraformingmadeeasy.dialogs.tooltips;

import com.fs.starfarer.api.ui.BaseTooltipCreator;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class OrbitFocusFieldTooltip extends BaseTooltipCreator {
    @Override
    public float getTooltipWidth(Object tooltipParam) {
        return 380f;
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
        tooltip.addPara("The %s where the megastructure orbits", 0f, Misc.getHighlightColor(), "main entity");
        tooltip.addSpacer(10f);
        tooltip.addPara("Only %s are eligible", 0f, Misc.getHighlightColor(), "planets and stars");
    }
}
