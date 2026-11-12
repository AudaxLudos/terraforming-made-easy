package terraformingmadeeasy;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;
import com.fs.starfarer.api.impl.codex.CodexDataV2;
import terraformingmadeeasy.codex.MegastructureIndustryCodexEntry;
import terraformingmadeeasy.codex.TerraformingIndustryCodexEntry;
import terraformingmadeeasy.listeners.DeathWorldListener;
import terraformingmadeeasy.listeners.MegastructureIndustryOptionProvider;
import terraformingmadeeasy.listeners.TerraformingIndustryOptionProvider;
import terraformingmadeeasy.loading.SpecStore;

public class ModPlugin extends BaseModPlugin {
    @Override
    public void onApplicationLoad() {
        SpecStore.loadTerraformingProjectSpecs();
        SpecStore.loadMegastructureProjectSpecs();
        Settings.load();
    }

    @Override
    public void onCodexDataGenerated() {
        Settings.makeTerraformingSpecsCompatibleWithAoTD();
        TerraformingIndustryCodexEntry.replaceCodexEntry(TMEIds.AGRICULTURAL_LABORATORY);
        TerraformingIndustryCodexEntry.replaceCodexEntry(TMEIds.ATMOSPHERE_REGULATOR);
        TerraformingIndustryCodexEntry.replaceCodexEntry(TMEIds.ELEMENT_SYNTHESIZER);
        TerraformingIndustryCodexEntry.replaceCodexEntry(TMEIds.GEOMORPHOLOGY_STATION);
        TerraformingIndustryCodexEntry.replaceCodexEntry(TMEIds.MINERAL_REPLICATOR);
        TerraformingIndustryCodexEntry.replaceCodexEntry(TMEIds.PLANETARY_HOLOGRAM);
        TerraformingIndustryCodexEntry.replaceCodexEntry(TMEIds.STELLAR_MANUFACTORY);
        TerraformingIndustryCodexEntry.replaceCodexEntry(TMEIds.TERRESTRIAL_ENGINE);
        TerraformingIndustryCodexEntry.replaceCodexEntry(TMEIds.UNIFICATION_CENTER);
        MegastructureIndustryCodexEntry.replaceCodexEntry(TMEIds.CONSTRUCTION_GRID);
        CodexDataV2.linkRelatedEntries();
    }

    @Override
    public void onGameLoad(boolean newGame) {
        addScriptsIfNeeded();
    }

    public void addScriptsIfNeeded() {
        SectorAPI sector = Global.getSector();

        // Add listeners
        ListenerManagerAPI listenerManager = sector.getListenerManager();
        if (!listenerManager.hasListenerOfClass(DeathWorldListener.class)) {
            listenerManager.addListener(new DeathWorldListener(), true);
        }
        if (!listenerManager.hasListenerOfClass(TerraformingIndustryOptionProvider.class)) {
            listenerManager.addListener(new TerraformingIndustryOptionProvider(), true);
        }
        if (!listenerManager.hasListenerOfClass(MegastructureIndustryOptionProvider.class)) {
            listenerManager.addListener(new MegastructureIndustryOptionProvider(), true);
        }
    }
}
