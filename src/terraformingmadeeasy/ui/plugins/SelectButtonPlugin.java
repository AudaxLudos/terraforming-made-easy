package terraformingmadeeasy.ui.plugins;

import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.ui.ButtonAPI;
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.ui.dialogs.MegastructureDialogDelegate;
import terraformingmadeeasy.ui.dialogs.TMEBaseDialogDelegate;

public class SelectButtonPlugin extends BaseCustomUIPanelPlugin {
    public TMEBaseDialogDelegate delegate;

    public SelectButtonPlugin(TMEBaseDialogDelegate delegate) {
        this.delegate = delegate;
    }

    public void buttonPressed(Object buttonId) {
        if (buttonId == null) {
            return;
        }

        this.delegate.selected = buttonId;

        boolean isHighlighted = false;
        for (ButtonAPI button : this.delegate.buttons) {
            if (button.getCustomData() == buttonId && !button.isHighlighted()) {
                button.highlight();
                isHighlighted = true;
                continue;
            }
            if (button.isHighlighted()) {
                button.unhighlight();
            }
        }

        if (!isHighlighted) {
            this.delegate.selected = null;
        }

        if (this.delegate instanceof MegastructureDialogDelegate) {
            MegastructureDialogDelegate megastructureDelegate = (MegastructureDialogDelegate) this.delegate;
            megastructureDelegate.startingAngleField.setText("0");
            megastructureDelegate.orbitRadiusField.setText("1000");
            megastructureDelegate.orbitDaysField.setText("100");
        }
    }
}
