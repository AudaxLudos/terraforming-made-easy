package terraformingmadeeasy.dialogs.tooltips;

import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.ui.BaseTooltipCreator;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.ids.TMEIds;

import java.util.Objects;

public class MegastructureTooltip extends BaseTooltipCreator {
    public Utils.BuildableMegastructure megastructure;

    public MegastructureTooltip(Utils.BuildableMegastructure megastructure) {
        this.megastructure = megastructure;
    }

    @Override
    public float getTooltipWidth(Object tooltipParam) {
        return 380f;
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
        if (Objects.equals(megastructure.id, Entities.CORONAL_TAP)) {
            tooltip.addPara("Only %s can exist in a system", 0f, Misc.getHighlightColor(), "1 " + megastructure.name);
            tooltip.addSpacer(10f);
            tooltip.addPara("Needs a %s in the system", 0f, Misc.getHighlightColor(), "Blue Supergiant Star");
        } else if (Objects.equals(megastructure.id, Entities.DERELICT_CRYOSLEEPER)) {
            tooltip.addPara("Only %s can exist in a system", 0f, Misc.getHighlightColor(), "1 " + megastructure.name);
        } else if (Objects.equals(megastructure.id, Entities.INACTIVE_GATE)) {
            tooltip.addPara("Only %s can exist in a system", 0f, Misc.getHighlightColor(), "1 " + megastructure.name);
        } else if (Objects.equals(megastructure.id, TMEIds.TME_STATION)) {
            tooltip.addPara("Only %s can exist in a system", 0f, Misc.getHighlightColor(), "3 " + megastructure.name);
        } else {
            tooltip.addPara("Only %s can exist in a system", 0f, Misc.getHighlightColor(), "1 " + megastructure.name);
        }
    }
}
