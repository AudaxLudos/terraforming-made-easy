package terraformingmadeeasy.ui.tooltips;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomEntitySpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.codex.CodexDataV2;
import com.fs.starfarer.api.ui.BaseTooltipCreator;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import terraformingmadeeasy.TMEIds;
import terraformingmadeeasy.loading.specs.MegastructureProjectSpec;

import java.util.Objects;

public class MegastructureProjectTooltip extends BaseTooltipCreator {
    public final MegastructureProjectSpec project;

    public MegastructureProjectTooltip(MegastructureProjectSpec project) {
        this.project = project;
    }

    @Override
    public float getTooltipWidth(Object tooltipParam) {
        return 380f;
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
        CustomEntitySpecAPI customEntitySpec = Global.getSettings().getCustomEntitySpec(this.project.getCustomEntityId());
        if (Objects.equals(customEntitySpec.getId(), Entities.CORONAL_TAP)) {
            tooltip.addPara("Only %s can exist in a system", 0f, Misc.getHighlightColor(), "1 " + customEntitySpec.getDefaultName());
            tooltip.addPara("Needs a %s in the system", 10f, Misc.getHighlightColor(), "Blue Supergiant Star");
        } else if (Objects.equals(customEntitySpec.getId(), Entities.DERELICT_CRYOSLEEPER)) {
            tooltip.addPara("Only %s can exist in a system", 0f, Misc.getHighlightColor(), "1 " + customEntitySpec.getDefaultName());
        } else if (Objects.equals(customEntitySpec.getId(), Entities.INACTIVE_GATE)) {
            tooltip.addPara("Only %s can exist in a system", 0f, Misc.getHighlightColor(), "1 " + customEntitySpec.getDefaultName());
        } else if (Objects.equals(customEntitySpec.getId(), TMEIds.TME_STATION)) {
            tooltip.addPara("Only %s can exist in a system", 0f, Misc.getHighlightColor(), "3 " + customEntitySpec.getDefaultName());
        } else {
            tooltip.addPara("Only %s can exist in a system", 0f, Misc.getHighlightColor(), "1 " + customEntitySpec.getDefaultName());
        }
        if (!Global.getSettings().isShowingCodex()) {
            String[] strings = customEntitySpec.getInteractionImage().split("/");
            String[] strings2 = strings[strings.length - 1].split("\\.");
            String key = strings2[0];
            tooltip.setCodexEntryId(CodexDataV2.getGalleryEntryId(key));
        }
    }
}
