package terraformingmadeeasy;

import lunalib.lunaSettings.LunaSettingsListener;

public class TMELunaSettingsListener implements LunaSettingsListener {
    @Override
    public void settingsChanged(String modId) {
        Utils.loadLunaSettings();
    }
}
