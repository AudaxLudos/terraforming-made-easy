package terraformingmadeeasy.industries;

import data.kaysaar.aotd.vok.scripts.research.AoTDMainResearchManager;
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.ids.TMEIds;

public class AtmosphereRegulator extends TMEBaseIndustry {
    public AtmosphereRegulator() {
        setModifiableConditions(Utils.ATMOSPHERE_REGULATOR_OPTIONS);
    }

    @Override
    public String getAOTDVOKTechId() {
        return TMEIds.ATMOSPHERE_REGULATOR_TECH;
    }
}
