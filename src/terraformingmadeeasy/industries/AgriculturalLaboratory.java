package terraformingmadeeasy.industries;

import data.kaysaar.aotd.vok.scripts.research.AoTDMainResearchManager;
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.ids.TMEIds;

public class AgriculturalLaboratory extends TMEBaseIndustry {
    public AgriculturalLaboratory() {
        setModifiableConditions(Utils.AGRICULTURAL_LABORATORY_OPTIONS);
    }

    @Override
    public String getAOTDVOKTechId() {
        return TMEIds.AGRICULTURAL_LABORATORY_TECH;
    }
}
