package terraformingmadeeasy.ui.dialogs;

import com.fs.starfarer.api.campaign.BaseCustomDialogDelegate;
import com.fs.starfarer.api.ui.ButtonAPI;

import java.util.ArrayList;
import java.util.List;

public class TMEBaseDialogDelegate extends BaseCustomDialogDelegate {
    public static float WIDTH = 800f;
    public static float HEIGHT = 400f;
    public Object selected = null;
    public List<ButtonAPI> buttons = new ArrayList<>();

    @Override
    public boolean hasCancelButton() {
        return true;
    }

    @Override
    public String getCancelText() {
        return "Cancel";
    }
}
