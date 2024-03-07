package terraformingmadeeasy.ui;

import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import terraformingmadeeasy.dialogs.MegastructureDialogDelegate;

public class DropDownPanelPlugin extends BaseCustomUIPanelPlugin {
    public MegastructureDialogDelegate delegate;

    public DropDownPanelPlugin(MegastructureDialogDelegate delegate) {
        this.delegate = delegate;
    }

    public void buttonPressed(Object buttonId) {
        if (buttonId instanceof SectorEntityToken) {
            this.delegate.orbitFocusField = (SectorEntityToken) buttonId;
        this.delegate.showDropDown = !this.delegate.showDropDown;
        this.delegate.refreshPanel();
        }
    }
}
