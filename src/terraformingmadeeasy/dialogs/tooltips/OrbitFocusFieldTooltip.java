package terraformingmadeeasy.dialogs.tooltips;

import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class OrbitFocusFieldTooltip implements TooltipMakerAPI.TooltipCreator {
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
        tooltip.addPara("The main entity where the megastructure orbits", 0f);
    }
}
