package terraformingmadeeasy.industries;

import terraformingmadeeasy.Utils;
import terraformingmadeeasy.ids.TMEIds;

import java.util.List;

public class GeomorphologyStation extends BaseTerraformingIndustry {
    @Override
    public String getAOTDVOKTechId() {
        return TMEIds.GEOMORPHOLOGY_STATION_TECH;
    }

    @Override
    public List<Utils.ProjectData> getProjects() {
        return Utils.GEOMORPHOLOGY_STATION_OPTIONS;
    }
}
