package terraformingmadeeasy.industries;

import terraformingmadeeasy.Utils;
import terraformingmadeeasy.ids.TMEIds;

import java.util.List;

public class TerrestrialEngine extends BaseTerraformingIndustry {
    @Override
    public String getAOTDVOKTechId() {
        return TMEIds.TERRESTRIAL_ENGINE_TECH;
    }

    @Override
    public List<Utils.ProjectData> getProjects() {
        return Utils.TERRESTRIAL_ENGINE_OPTIONS;
    }
}
