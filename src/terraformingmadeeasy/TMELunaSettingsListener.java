package terraformingmadeeasy;

import lunalib.lunaSettings.LunaSettingsListener;
import org.jetbrains.annotations.NotNull;

public class TMELunaSettingsListener implements LunaSettingsListener {
    @Override
    public void settingsChanged(@NotNull String modId) {
        Utils.loadLunaSettings();
    }
}
