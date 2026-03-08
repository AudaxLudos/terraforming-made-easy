package terraformingmadeeasy;

import com.fs.starfarer.api.Global;
import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;
import terraformingmadeeasy.ids.TMEIds;

public class Settings implements LunaSettingsListener {
    public static float BUILD_TIME_MULTIPLIER = 1.0f;
    public static float BUILD_COST_MULTIPLIER = 1.0f;
    public static float REMOVAL_COST_MULTIPLIER = 0.2f;

    public static void load() {
        loadSettings();
    }

    public static void loadSettings() {
        if (isLunaLibEnabled()) {
            LunaSettings.addSettingsListener(new Settings());
        }
        setSettings();
    }

    public static void setSettings() {
        BUILD_TIME_MULTIPLIER = getBuildCostValue(getString("tme_build_time_settings"), "tme_custom_build_time_settings");
        BUILD_COST_MULTIPLIER = getBuildCostValue(getString("tme_build_cost_settings"), "tme_custom_build_cost_settings");
        REMOVAL_COST_MULTIPLIER = getFloat("tme_removal_cost_setting");
    }

    public static float getBuildCostValue(String setting, String customFieldId) {
        float value = 1f;
        switch (setting.toLowerCase()) {
            case "low":
            case "fast":
                value = 0.5f;
                break;
            case "high":
            case "slow":
                value = 2.0f;
                break;
            case "custom":
                if (isLunaLibEnabled()) {
                    value = getFloat(customFieldId);
                } else {
                    value = Global.getSettings().getFloat(customFieldId);
                }
                break;
        }
        return value;
    }

    public static float getFloat(String fieldId) {
        Float val = LunaSettings.getFloat(TMEIds.MOD_ID, fieldId);
        if (val == null) {
            return 1f;
        }
        return val;
    }

    public static String getString(String fieldId) {
        String val = LunaSettings.getString(TMEIds.MOD_ID, fieldId);
        if (val == null || val.isEmpty()) {
            return "normal";
        }
        return val;
    }

    public static boolean isLunaLibEnabled() {
        return Global.getSettings().getModManager().isModEnabled("lunalib");
    }

    public static boolean isAoTDVoKEnabled() {
        return Global.getSettings().getModManager().isModEnabled("aotd_vok");
    }

    @Override
    public void settingsChanged(String s) {

    }
}
