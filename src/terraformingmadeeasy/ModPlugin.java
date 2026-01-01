package terraformingmadeeasy;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.codex.CodexDataV2;
import com.fs.starfarer.api.loading.IndustrySpecAPI;
import com.fs.starfarer.api.util.Misc;
import lunalib.lunaSettings.LunaSettings;
import terraformingmadeeasy.codex.TMECodexEntry;
import terraformingmadeeasy.ids.TMEIds;
import terraformingmadeeasy.ids.TMEPeople;
import terraformingmadeeasy.listeners.DeathWorldScript;
import terraformingmadeeasy.listeners.DevelopmentIndustryOptionProvider;

import java.util.*;
import java.util.stream.Collectors;

public class ModPlugin extends BaseModPlugin {
    @Override
    public void onCodexDataGenerated() {
        // Have to set it here as planet specs are not loaded yet if I do it outside
        Utils.AGRICULTURAL_LABORATORY_OPTIONS = Utils.getTerraformingOptions(TMEIds.AGRICULTURAL_LABORATORY);
        Utils.ATMOSPHERE_REGULATOR_OPTIONS = Utils.getTerraformingOptions(TMEIds.ATMOSPHERE_REGULATOR);
        Utils.CONSTRUCTION_GRID_OPTIONS = Utils.getMegastructureOptions();
        Utils.ELEMENT_SYNTHESIZER_OPTIONS = Utils.getTerraformingOptions(TMEIds.ELEMENT_SYNTHESIZER);
        Utils.GEOMORPHOLOGY_STATION_OPTIONS = Utils.getTerraformingOptions(TMEIds.GEOMORPHOLOGY_STATION);
        Utils.MINERAL_REPLICATOR_OPTIONS = Utils.getTerraformingOptions(TMEIds.MINERAL_REPLICATOR);
        Utils.PLANETARY_HOLOGRAM_OPTIONS = Utils.getPlanetaryHologramOptions();
        Utils.STELLAR_MANUFACTORY_OPTIONS = Utils.getTerraformingOptions(TMEIds.STELLAR_MANUFACTORY);
        Utils.TERRESTRIAL_ENGINE_OPTIONS = Utils.getTerraformingOptions(TMEIds.TERRESTRIAL_ENGINE);
        Utils.UNIFICATION_CENTER_OPTIONS = Utils.getTerraformingOptions(TMEIds.UNIFICATION_CENTER);

        if (Utils.isAOTDVOKEnabled()) {
            List<Utils.ProjectData> projectsCopy = new ArrayList<>(Utils.UNIFICATION_CENTER_OPTIONS);

            for (Utils.ProjectData condition : projectsCopy) {
                StringBuilder needOne = new StringBuilder();
                StringBuilder needAll = new StringBuilder("needAll:");
                Set<String> ids = Utils.getUniqueIds(condition.likedIndustries);

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
                                    "needOne:%s|%s|%s, ",
                                    "refining",
                                    "aotd_crystalizator",
                                    "aotd_enrichment_facility");
                            needOne.append(newExpression);
                            break;
                        }
                        case Industries.LIGHTINDUSTRY: {
                            String newExpression = String.format(
                                    "needOne:%s|%s|%s|%s, ",
                                    "lightindustry",
                                    "aotd_hightech_industry",
                                    "aotd_druglight",
                                    "consumerindustry");
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
                        case Industries.HIGHCOMMAND:
                        case Industries.COMMERCE:
                        case Industries.MEGAPORT: {
                            needAll.append(id).append("|");
                        }
                    }
                }

                condition.likedIndustries = needOne.append(needAll.toString().replaceFirst(".$", "")).toString().replaceAll("\\s", "").trim();
            }
            Utils.UNIFICATION_CENTER_OPTIONS = projectsCopy;
        }

        TMECodexEntry.replaceTMEIndustryCodex(TMEIds.AGRICULTURAL_LABORATORY, Utils.AGRICULTURAL_LABORATORY_OPTIONS);
        TMECodexEntry.replaceTMEIndustryCodex(TMEIds.ATMOSPHERE_REGULATOR, Utils.ATMOSPHERE_REGULATOR_OPTIONS);
        TMECodexEntry.replaceTMEIndustryCodex(TMEIds.CONSTRUCTION_GRID, Utils.CONSTRUCTION_GRID_OPTIONS);
        TMECodexEntry.replaceTMEIndustryCodex(TMEIds.ELEMENT_SYNTHESIZER, Utils.ELEMENT_SYNTHESIZER_OPTIONS);
        TMECodexEntry.replaceTMEIndustryCodex(TMEIds.GEOMORPHOLOGY_STATION, Utils.GEOMORPHOLOGY_STATION_OPTIONS);
        TMECodexEntry.replaceTMEIndustryCodex(TMEIds.MINERAL_REPLICATOR, Utils.MINERAL_REPLICATOR_OPTIONS);
        TMECodexEntry.replaceTMEIndustryCodex(TMEIds.PLANETARY_HOLOGRAM, Utils.PLANETARY_HOLOGRAM_OPTIONS);
        TMECodexEntry.replaceTMEIndustryCodex(TMEIds.STELLAR_MANUFACTORY, Utils.STELLAR_MANUFACTORY_OPTIONS);
        TMECodexEntry.replaceTMEIndustryCodex(TMEIds.TERRESTRIAL_ENGINE, Utils.TERRESTRIAL_ENGINE_OPTIONS);
        TMECodexEntry.replaceTMEIndustryCodex(TMEIds.UNIFICATION_CENTER, Utils.UNIFICATION_CENTER_OPTIONS);
        CodexDataV2.linkRelatedEntries();
    }

    @Override
    public void onGameLoad(boolean newGame) {
        // Fix for coronal taps made from this that are stuck on repairing
        for (StarSystemAPI system : Misc.getPlayerSystems(false)) {
            List<SectorEntityToken> entities = system.getAllEntities();
            for (SectorEntityToken entity : entities) {
                if (!Objects.equals(entity.getCustomEntityType(), Entities.CORONAL_TAP)) {
                    continue;
                }
                if (!entity.getMemoryWithoutUpdate().getBoolean("$usable") && !entity.getMemoryWithoutUpdate().getBoolean("$beingRepaired")) {
                    continue;
                }
                entity.getMemoryWithoutUpdate().set("$beingRepaired", true, 5f);
            }
        }

        DeathWorldScript.register();
        DevelopmentIndustryOptionProvider.register();
        TMEPeople.register();

        if (Utils.isLunaLibEnabled()) {
            Utils.loadLunaSettings();
            LunaSettings.addSettingsListener(new TMELunaSettingsListener());
        } else {
            Utils.BUILD_TIME_MULTIPLIER = Utils.getBuildCostSettingValue(Global.getSettings().getString("tme_build_time_setting"), "tme_custom_build_time_settings");
            Utils.BUILD_COST_MULTIPLIER = Utils.getBuildCostSettingValue(Global.getSettings().getString("tme_build_cost_setting"), "tme_custom_build_cost_settings");
        }
    }
}
