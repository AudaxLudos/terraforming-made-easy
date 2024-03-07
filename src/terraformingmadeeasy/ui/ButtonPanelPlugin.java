package terraformingmadeeasy.ui;

import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.ui.ButtonAPI;
import terraformingmadeeasy.dialogs.MegastructureDialogDelegate;
import terraformingmadeeasy.industries.ConstructionGrid;

public class ButtonPanelPlugin extends BaseCustomUIPanelPlugin {
    public MegastructureDialogDelegate delegate;

    public ButtonPanelPlugin(MegastructureDialogDelegate delegate) {
        this.delegate = delegate;
    }

    public void buttonPressed(Object buttonId) {
        if (buttonId instanceof ConstructionGrid.BuildableMegastructure)
            this.delegate.selected = (ConstructionGrid.BuildableMegastructure) buttonId;
        boolean anyChecked = false;
        for (ButtonAPI button :  this.delegate.buttons) {
            if (button.isChecked() && button.getCustomData() != buttonId)
                button.setChecked(false);
            if (button.isChecked())
                anyChecked = true;
        }
        if (!anyChecked)
            this.delegate.selected = null;
        this.delegate.startingAngleField.setText("0");
        this.delegate.orbitRadiusField.setText("0");
        this.delegate.orbitDaysField.setText("0");
    }
}
