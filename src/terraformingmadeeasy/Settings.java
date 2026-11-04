package terraformingmadeeasy;

import com.fs.starfarer.api.Global;
import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;
import terraformingmadeeasy.ids.TMEIds;
import terraformingmadeeasy.loading.specs.TerraformingProjectSpec;

import java.util.ArrayList;
import java.util.List;

public class Settings {
    public static final float REMOVAL_COST_MULTIPLIER = 0.2f;
    public static float BUILD_TIME_MULTIPLIER = 1.0f;
    public static float BUILD_COST_MULTIPLIER = 1.0f;

    public static void load() {
        loadSettings();
    }

    public static void loadSettings() {
        if (isLunaLibEnabled()) {
            LunaSettings.addSettingsListener(new TMELunaSettingsListener());
        }
        setSettings();
    }

    public static void setSettings() {
        BUILD_TIME_MULTIPLIER = getBuildCostValue(getString("tme_build_time_settings"), "tme_custom_build_time_settings");
        BUILD_COST_MULTIPLIER = getBuildCostValue(getString("tme_build_cost_settings"), "tme_custom_build_cost_settings");
    }

    public static float getBuildCostValue(String setting, String customFieldId) {
        return switch (setting.toLowerCase()) {
            case "low", "fast" -> 0.5f;
            case "high", "slow" -> 2.0f;
            case "custom" -> getFloat(customFieldId);
            default -> 1f; // Normal setting
        };
    }

    public static float getFloat(String fieldId) {
        Float val = Global.getSettings().getFloat(fieldId);
        if (isLunaLibEnabled()) {
            val = LunaSettings.getFloat(TMEIds.MOD_ID, fieldId);
        }
        if (val == null) {
            return 1f;
        }
        return val;
    }

    public static String getString(String fieldId) {
        String val = Global.getSettings().getString(fieldId);
        if (isLunaLibEnabled()) {
            val = LunaSettings.getString(TMEIds.MOD_ID, fieldId);
        }
        if (val == null || val.isEmpty()) {
            return "";
        }
        return val;
    }

    public static boolean isLunaLibEnabled() {
        return Global.getSettings().getModManager().isModEnabled("lunalib");
    }

    public static boolean isAoTDVoKEnabled() {
        return Global.getSettings().getModManager().isModEnabled("aotd_vok");
    }

    public static List<TerraformingProjectSpec> getAllTerraformingProjectSpecs() {
        return new ArrayList<>(Global.getSettings().getAllSpecs(TerraformingProjectSpec.class));
    }

    public static TerraformingProjectSpec getTerraformingProjectSpec(String id) {
        return (TerraformingProjectSpec) Global.getSettings().getSpec(TerraformingProjectSpec.class, id, true);
    }

    public static class TMELunaSettingsListener implements LunaSettingsListener {
        @Override
        public void settingsChanged(String s) {
            Settings.setSettings();
        }
    }
}
