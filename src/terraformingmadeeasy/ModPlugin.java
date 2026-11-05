package terraformingmadeeasy;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;
import com.fs.starfarer.api.impl.codex.CodexDataV2;
import terraformingmadeeasy.codex.TerraformingIndustryCodexEntry;
import terraformingmadeeasy.ids.TMEIds;
import terraformingmadeeasy.listeners.DeathWorldScript;
import terraformingmadeeasy.listeners.MegastructureIndustryOptionProvider;
import terraformingmadeeasy.listeners.TerraformingIndustryOptionProvider;
import terraformingmadeeasy.loading.SpecStore;

public class ModPlugin extends BaseModPlugin {
    @Override
    public void onApplicationLoad() {
        SpecStore.loadTerraformingProjectSpecs();
        SpecStore.loadMegastructureProjectSpecs();
    }

    @Override
    public void onCodexDataGenerated() {
        TerraformingIndustryCodexEntry.replaceIndustryCodex(TMEIds.AGRICULTURAL_LABORATORY);
        TerraformingIndustryCodexEntry.replaceIndustryCodex(TMEIds.ATMOSPHERE_REGULATOR);
        TerraformingIndustryCodexEntry.replaceIndustryCodex(TMEIds.ELEMENT_SYNTHESIZER);
        TerraformingIndustryCodexEntry.replaceIndustryCodex(TMEIds.GEOMORPHOLOGY_STATION);
        TerraformingIndustryCodexEntry.replaceIndustryCodex(TMEIds.MINERAL_REPLICATOR);
        TerraformingIndustryCodexEntry.replaceIndustryCodex(TMEIds.PLANETARY_HOLOGRAM);
        TerraformingIndustryCodexEntry.replaceIndustryCodex(TMEIds.STELLAR_MANUFACTORY);
        TerraformingIndustryCodexEntry.replaceIndustryCodex(TMEIds.TERRESTRIAL_ENGINE);
        TerraformingIndustryCodexEntry.replaceIndustryCodex(TMEIds.UNIFICATION_CENTER);
        CodexDataV2.linkRelatedEntries();
    }

    @Override
    public void onGameLoad(boolean newGame) {
        addModSettings();
        addScriptsIfNeeded();
    }

    public void addScriptsIfNeeded() {
        SectorAPI sector = Global.getSector();

        // Add listeners
        ListenerManagerAPI listenerManager = sector.getListenerManager();
        if (!listenerManager.hasListenerOfClass(DeathWorldScript.class)) {
            listenerManager.addListener(new DeathWorldScript(), true);
        }
        if (!listenerManager.hasListenerOfClass(TerraformingIndustryOptionProvider.class)) {
            listenerManager.addListener(new TerraformingIndustryOptionProvider(), true);
        }
        if (!listenerManager.hasListenerOfClass(MegastructureIndustryOptionProvider.class)) {
            listenerManager.addListener(new MegastructureIndustryOptionProvider(), true);
        }
    }

    public void addModSettings() {
        Settings.load();
    }
}
