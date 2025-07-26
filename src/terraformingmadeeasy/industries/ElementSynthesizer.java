package terraformingmadeeasy.industries;

import terraformingmadeeasy.Utils;
import terraformingmadeeasy.ids.TMEIds;

import java.util.List;

public class ElementSynthesizer extends BaseTerraformingIndustry {
    @Override
    public String getAOTDVOKTechId() {
        return TMEIds.ELEMENT_SYNTHESIZER_TECH;
    }

    @Override
    public List<Utils.ProjectData> getProjects() {
        return Utils.ELEMENT_SYNTHESIZER_OPTIONS;
    }
}
