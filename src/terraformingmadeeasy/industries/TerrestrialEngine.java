package terraformingmadeeasy.industries;

import data.kaysaar.aotd.vok.scripts.research.AoTDMainResearchManager;
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.ids.TMEIds;

public class TerrestrialEngine extends TMEBaseIndustry {
    public TerrestrialEngine() {
        setModifiableConditions(Utils.TERRESTRIAL_ENGINE_OPTIONS);
    }

    @Override
    public String getAOTDVOKTechId() {
        return TMEIds.TERRESTRIAL_ENGINE_TECH;
    }
}
