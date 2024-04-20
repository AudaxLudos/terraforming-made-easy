package terraformingmadeeasy.ui;

import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.ui.ButtonAPI;
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.dialogs.MegastructureDialogDelegate;
import terraformingmadeeasy.dialogs.TMEBaseDialogDelegate;

public class ButtonPanelPlugin extends BaseCustomUIPanelPlugin {
    public TMEBaseDialogDelegate delegate;

    public ButtonPanelPlugin(TMEBaseDialogDelegate delegate) {
        this.delegate = delegate;
    }

    public void buttonPressed(Object buttonId) {
        if (buttonId instanceof Utils.BuildableMegastructure) {
            this.delegate.selected = buttonId;
        } else if (buttonId instanceof Utils.ModifiableCondition) {
            this.delegate.selected = buttonId;
        }

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
