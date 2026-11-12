package terraformingmadeeasy;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.loading.IndustrySpecAPI;
import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;
import terraformingmadeeasy.loading.specs.MegastructureProjectSpec;
import terraformingmadeeasy.loading.specs.TerraformingProjectSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Settings {
    public static final float REMOVAL_COST_MULTIPLIER = 0.2f;
    public static float BUILD_TIME_MULTIPLIER = 1.0f;
    public static float BUILD_COST_MULTIPLIER = 1.0f;

    public static void load() {
        if (isLunaLibEnabled()) {
            LunaSettings.addSettingsListener(new TMELunaSettingsListener());
        }
        setSettings();
    }

    public static void setSettings() {
        BUILD_TIME_MULTIPLIER = getBuildCostOrTimeMult(getString("tme_build_time_multiplier"), "tme_custom_build_time_multiplier");
        BUILD_COST_MULTIPLIER = getBuildCostOrTimeMult(getString("tme_build_cost_multiplier"), "tme_custom_build_cost_multiplier");
    }

    public static float getBuildCostOrTimeMult(String setting, String customFieldId) {
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

    public static List<MegastructureProjectSpec> getAllMegastructureProjectSpecs() {
        return new ArrayList<>(Global.getSettings().getAllSpecs(MegastructureProjectSpec.class));
    }

    public static MegastructureProjectSpec getMegastructureProjectSpec(String id) {
        return (MegastructureProjectSpec) Global.getSettings().getSpec(MegastructureProjectSpec.class, id, true);
    }

    public static void makeTerraformingSpecsCompatibleWithAoTD() {
        // Have to set it here as planet specs are not loaded yet if I do it outside
        if (Settings.isAoTDVoKEnabled()) {
            List<TerraformingProjectSpec> projectSpecs = Settings.getAllTerraformingProjectSpecs().stream().filter(p -> p.getStructureId().equals(TMEIds.UNIFICATION_CENTER)).toList();

            for (TerraformingProjectSpec project : projectSpecs) {
                StringBuilder needOne = new StringBuilder();
                StringBuilder needAll = new StringBuilder("needAll:");
                Set<String> ids = Utils.getUniqueIds(project.getLikedIndustries());

                // Skip the project conversion if the data contains aotd_vok industry ids already
                boolean skip = Global.getSettings().getAllIndustrySpecs().stream()
                        .filter(s -> s.getSourceMod() != null && Objects.equals(s.getSourceMod().getId(), "aotd_vok"))
                        .map(IndustrySpecAPI::getId)
                        .anyMatch(ids::contains);
                if (skip) {
                    continue;
                }

                for (String id : ids) {
                    switch (id) {
                        case Industries.MINING: {
                            String newExpression = String.format(
                                    "needOne:%s|%s|%s|%s, ",
                                    "mining",
                                    "aotd_plasma_harvester",
                                    "aotd_mining_megaplex",
                                    "pluto_station");
                            needOne.append(newExpression);
                            break;
                        }
                        case Industries.REFINING: {
                            String newExpression = String.format(
                                    "needOne:%s|%s|%s|%s, ",
                                    "refining",
                                    "aotd_crystalizator",
                                    "aotd_enrichment_facility",
                                    "pluto_station");
                            needOne.append(newExpression);
                            break;
                        }
                        case Industries.LIGHTINDUSTRY: {
                            String newExpression = String.format(
                                    "needOne:%s|%s|%s|%s, ",
                                    "lightindustry",
                                    "aotd_hightech_industry",
                                    "aotd_druglight",
                                    "aotd_commercial_manu");
                            needOne.append(newExpression);
                            break;
                        }
                        case Industries.ORBITALWORKS:
                        case Industries.HEAVYINDUSTRY: {
                            String newExpression = String.format(
                                    "needOne:%s|%s|%s|%s|%s|%s|%s|%s|%s, ",
                                    "orbitalworks",
                                    "supplyheavy",
                                    "aotd_macro_industrial_complex",
                                    "weaponheavy",
                                    "triheavy",
                                    "hegeheavy",
                                    "orbitalheavy",
                                    "stella_manufactorium",
                                    "nidavelir_complex");
                            needOne.append(newExpression);
                            break;
                        }
                        case Industries.FARMING: {
                            String newExpression = String.format(
                                    "needOne:%s|%s|%s|%s, ",
                                    "farming",
                                    "aotd_artisanal_farming",
                                    "aotd_subsidised_farming",
                                    "aotd_fishing_harbour");
                            needOne.append(newExpression);
                            break;
                        }
                        case Industries.FUELPROD: {
                            String newExpression = String.format(
                                    "needOne:%s|%s, ",
                                    "fuelprod",
                                    "aotd_fuel_refinery");
                            needOne.append(newExpression);
                            break;
                        }
                        case Industries.COMMERCE: {
                            String newExpression = String.format(
                                    "needOne:%s|%s, ",
                                    "commerce",
                                    "aotd_underworld");
                            needOne.append(newExpression);
                            break;
                        }
                        case Industries.MEGAPORT: {
                            String newExpression = String.format(
                                    "needOne:%s|%s, ",
                                    "megaport",
                                    "aotd_daedalus_array");
                            needOne.append(newExpression);
                            break;
                        }
                        case Industries.HIGHCOMMAND: {
                            needAll.append(id).append("|");
                        }
                    }
                }

                if (needAll.length() > "needAll:".length()) {
                    project.setLikedIndustries(needOne.append(needAll.toString().replaceFirst(".$", "")).toString().replaceAll("\\s", "").trim());
                } else {
                    project.setLikedIndustries(needOne.toString().replaceAll("\\s", "").trim());
                }
            }
        }
    }

    public static class TMELunaSettingsListener implements LunaSettingsListener {
        @Override
        public void settingsChanged(String s) {
            Settings.setSettings();
        }
    }
}
