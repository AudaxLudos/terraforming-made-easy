package terraformingmadeeasy.ui.dialogs;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomDialogDelegate;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class ConfirmDialogDelegate extends BaseCustomDialogDelegate {
    public static final float WIDTH = 564f;
    public static final float HEIGHT = 104f;
    public Industry industry;
    public float cost;

    public ConfirmDialogDelegate(Industry industry, float cost) {
        this.industry = industry;
        this.cost = cost;
    }

    @Override
    public void createCustomDialog(CustomPanelAPI panel, CustomDialogCallback callback) {
        TooltipMakerAPI panelTooltip = panel.createUIElement(WIDTH, HEIGHT, true);
        panelTooltip.setParaInsigniaLarge();
        panelTooltip.addPara(
                "Cancelling the terraforming project for %s will refund you the full upgrade cost of %s and will take effect immediately",
                0f,
                new Color[]{Misc.getTextColor(), Misc.getHighlightColor()},
                this.industry.getCurrentName(), Misc.getDGSCredits(this.cost));
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
        Global.getSector().getPlayerFleet().getCargo().getCredits().add(this.cost);
        this.industry.cancelUpgrade();
    }
}
