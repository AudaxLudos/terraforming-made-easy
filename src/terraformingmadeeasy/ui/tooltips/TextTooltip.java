package terraformingmadeeasy.ui.tooltips;

import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class TextTooltip implements TooltipMakerAPI.TooltipCreator {
    protected String text;

    public TextTooltip(String text) {
        this.text = text;
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
        tooltip.addPara(this.text, 0f);
    }
}
