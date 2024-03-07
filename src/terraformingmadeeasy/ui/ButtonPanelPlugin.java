package terraformingmadeeasy.ui;

import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.ui.ButtonAPI;
import terraformingmadeeasy.dialogs.MegastructureDialogDelegate;
import terraformingmadeeasy.dialogs.TMEBaseDialogDelegate;
import terraformingmadeeasy.industries.TMEBaseIndustry;
import terraformingmadeeasy.industries.ConstructionGrid;

public class ButtonPanelPlugin extends BaseCustomUIPanelPlugin {
    public TMEBaseDialogDelegate delegate;

    public ButtonPanelPlugin(TMEBaseDialogDelegate delegate) {
        this.delegate = delegate;
    }

    public void buttonPressed(Object buttonId) {
        if (buttonId instanceof ConstructionGrid.BuildableMegastructure) {
            this.delegate.selected = buttonId;
        } else if (buttonId instanceof TMEBaseIndustry.ModifiableCondition) {
            this.delegate.selected = buttonId;
        }

        boolean anyChecked = false;
        for (ButtonAPI button : this.delegate.buttons) {
            if (button.isChecked() && button.getCustomData() != buttonId)
                button.setChecked(false);
            if (button.isChecked())
                anyChecked = true;
        }

        if (!anyChecked)
            this.delegate.selected = null;

        if (this.delegate instanceof MegastructureDialogDelegate) {
            MegastructureDialogDelegate megastructureDelegate = (MegastructureDialogDelegate) this.delegate;
            megastructureDelegate.startingAngleField.setText("0");
            megastructureDelegate.orbitRadiusField.setText("0");
            megastructureDelegate.orbitDaysField.setText("0");
        }
    }
}
