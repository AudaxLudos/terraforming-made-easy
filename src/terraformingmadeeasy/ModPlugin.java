package terraformingmadeeasy;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;
import lunalib.lunaSettings.LunaSettings;
import terraformingmadeeasy.ids.TMEIds;
import terraformingmadeeasy.ids.TMEPeople;
import terraformingmadeeasy.listeners.TMEIndustryOptionProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ModPlugin extends BaseModPlugin {
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
                entity.getMemoryWithoutUpdate().set("$beingRepaired", true , 5f);
            }
        }

        TMEIndustryOptionProvider.register();
        TMEPeople.register();

        if (Utils.isLunaLibEnabled()) {
            Utils.loadLunaSettings();
            LunaSettings.addSettingsListener(new TMELunaSettingsListener());
        } else {
            Utils.BUILD_TIME_MULTIPLIER = Utils.getBuildCostSettingValue(Global.getSettings().getString("tme_build_time_setting"), "tme_custom_build_time_settings");
            Utils.BUILD_COST_MULTIPLIER = Utils.getBuildCostSettingValue(Global.getSettings().getString("tme_build_cost_setting"), "tme_custom_build_cost_settings");
        }

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
            List<Utils.ModifiableCondition> modifiableConditionsCopy = new ArrayList<>(Utils.UNIFICATION_CENTER_OPTIONS);

            for (Utils.ModifiableCondition condition : modifiableConditionsCopy) {
                StringBuilder needOne = new StringBuilder();
                StringBuilder needAll = new StringBuilder("needAll:");
                String[] ids = Utils.getUniqueIds(condition.likedIndustries);

                for (String id : ids) {
                    switch (id) {
                        case Industries.MINING: {
                            String newExpression = String.format(
                                    "needOne:%s|%s|%s|%s, ",
                                    "mining",
                                    "fracking",
                                    "mining_megaplex",
                                    "pluto_station");
                            needOne.append(newExpression);
                            break;
                        }
                        case Industries.REFINING: {
                            String newExpression = String.format(
                                    "needOne:%s|%s|%s|%s|%s, ",
                                    "refining",
                                    "crystalizator",
                                    "isotope_separator",
                                    "policrystalizator",
                                    "cascade_reprocesor");
                            needOne.append(newExpression);
                            break;
                        }
                        case Industries.LIGHTINDUSTRY: {
                            String newExpression = String.format(
                                    "needOne:%s|%s|%s|%s, ",
                                    "lightindustry",
                                    "hightech",
                                    "druglight",
                                    "consumerindustry");
                            needOne.append(newExpression);
                            break;
                        }
                        case Industries.ORBITALWORKS:
                        case Industries.HEAVYINDUSTRY: {
                            String newExpression = String.format(
                                    "needOne:%s|%s|%s|%s|%s|%s|%s|%s, ",
                                    "orbitalworks",
                                    "supplyheavy",
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
                                    "needOne:%s|%s|%s, ",
                                    "farming",
                                    "artifarming",
                                    "subfarming");
                            needOne.append(newExpression);
                            break;
                        }
                        case Industries.FUELPROD: {
                            String newExpression = String.format(
                                    "needOne:%s|%s, ",
                                    "fuelprod",
                                    "blast_processing");
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
            Utils.UNIFICATION_CENTER_OPTIONS = modifiableConditionsCopy;
        }
    }
}
