package terraformingmadeeasy.dialogue;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomDialogDelegate;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;
import terraformingmadeeasy.industry.TMEBaseIndustry;

import java.awt.*;

public class TMEConfirmDialogueDelegate implements CustomDialogDelegate {
    public static final float WIDTH = 564f;

    public static final float HEIGHT = 104f;

    public TMEBaseIndustry industry;

    public TMEConfirmDialogueDelegate(Industry industry) {
        this.industry = (TMEBaseIndustry) industry;
    }

    @Override
    public void createCustomDialog(CustomPanelAPI panel, CustomDialogCallback callback) {
        TooltipMakerAPI panelTooltip = panel.createUIElement(WIDTH, HEIGHT, true);
        panelTooltip.setParaInsigniaLarge();
        panelTooltip.addPara(
                "Cancelling the terraforming project for %s will refund you the full upgrade cost of %s and will take effect immediately",
                0f,
                new Color[]{Misc.getTextColor(), Misc.getHighlightColor()},
                industry.getCurrentName(), Misc.getDGSCredits(industry.modifiableCondition.cost));
        panel.addUIElement(panelTooltip);
    }

    @Override
    public boolean hasCancelButton() {
        return true;
    }

    @Override
    public String getConfirmText() {
        return "Confirm";
    }

    @Override
    public String getCancelText() {
        return "Cancel";
    }

    @Override
    public void customDialogConfirm() {
        Global.getSoundPlayer().playSound("ui_cancel_construction_or_upgrade_industry", 1f, 1f, Global.getSoundPlayer().getListenerPos(), new Vector2f());
        Global.getSector().getPlayerFleet().getCargo().getCredits().add(industry.modifiableCondition.cost);
        this.industry.cancelUpgrade();
    }

    @Override
    public void customDialogCancel() {

    }

    @Override
    public CustomUIPanelPlugin getCustomPanelPlugin() {
        return null;
    }
}
