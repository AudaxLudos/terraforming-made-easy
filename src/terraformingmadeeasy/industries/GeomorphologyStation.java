package terraformingmadeeasy.industries;

import data.kaysaar.aotd.vok.scripts.research.AoTDMainResearchManager;
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.ids.TMEIds;

public class GeomorphologyStation extends TMEBaseIndustry {
    public GeomorphologyStation() {
        setModifiableConditions(Utils.GEOMORPHOLOGY_STATION_OPTIONS);
    }

    @Override
    public String getAOTDVOKTechId() {
        return TMEIds.GEOMORPHOLOGY_STATION_TECH;
    }
}
