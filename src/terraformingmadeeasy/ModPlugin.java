package terraformingmadeeasy;

import com.fs.starfarer.api.BaseModPlugin;
import lunalib.lunaSettings.LunaSettings;
import terraformingmadeeasy.ids.TMEIds;
import terraformingmadeeasy.ids.TMEPeople;
import terraformingmadeeasy.listeners.TMEIndustryOptionProvider;

import java.util.List;

public class ModPlugin extends BaseModPlugin {
    @Override
    public void onGameLoad(boolean newGame) {
        TMEIndustryOptionProvider.register();
        TMEPeople.register();

        if (Utils.isLunaLibEnabled()) {
            Utils.loadLunaSettings();
            LunaSettings.addSettingsListener(new TMELunaSettingsListener());
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
    }
}
