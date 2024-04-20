package terraformingmadeeasy.ui.plugins;

import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import terraformingmadeeasy.ui.dialogs.MegastructureDialogDelegate;

public class DropDownPlugin extends BaseCustomUIPanelPlugin {
    public MegastructureDialogDelegate delegate;

    public DropDownPlugin(MegastructureDialogDelegate delegate) {
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
