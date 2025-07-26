package terraformingmadeeasy.industries;

import terraformingmadeeasy.Utils;
import terraformingmadeeasy.ids.TMEIds;

import java.util.List;

public class AtmosphereRegulator extends BaseTerraformingIndustry {
    @Override
    public String getAOTDVOKTechId() {
        return TMEIds.ATMOSPHERE_REGULATOR_TECH;
    }

    @Override
    public List<Utils.ProjectData> getProjects() {
        return Utils.ATMOSPHERE_REGULATOR_OPTIONS;
    }
}
