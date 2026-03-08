package terraformingmadeeasy.ui.tooltips;

import com.fs.starfarer.api.ui.TooltipMakerAPI;

public record TextTooltip(String text) implements TooltipMakerAPI.TooltipCreator {
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
        tooltip.addPara(this.text, 0f);
    }
}
