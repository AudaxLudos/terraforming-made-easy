package terraformingmadeeasy.dialogue;

import com.fs.starfarer.api.campaign.CustomDialogDelegate;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.ui.CustomPanelAPI;

public class TMEConfirmDialgueDelegate implements CustomDialogDelegate {
    @Override
    public void createCustomDialog(CustomPanelAPI panel, CustomDialogCallback callback) {
    }

    @Override
    public boolean hasCancelButton() {
        return false;
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

    }

    @Override
    public void customDialogCancel() {

    }

    @Override
    public CustomUIPanelPlugin getCustomPanelPlugin() {
        return null;
    }
}
