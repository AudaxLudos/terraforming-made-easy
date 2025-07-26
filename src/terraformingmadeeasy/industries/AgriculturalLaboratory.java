package terraformingmadeeasy.industries;

import terraformingmadeeasy.Utils;
import terraformingmadeeasy.ids.TMEIds;

import java.util.List;

public class AgriculturalLaboratory extends BaseTerraformingIndustry {
    @Override
    public String getAOTDVOKTechId() {
        return TMEIds.AGRICULTURAL_LABORATORY_TECH;
    }

    @Override
    public List<Utils.ProjectData> getProjects() {
        return Utils.AGRICULTURAL_LABORATORY_OPTIONS;
    }
}
