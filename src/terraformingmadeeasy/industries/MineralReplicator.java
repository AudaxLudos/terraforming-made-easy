package terraformingmadeeasy.industries;

import terraformingmadeeasy.Utils;
import terraformingmadeeasy.ids.TMEIds;

import java.util.List;

public class MineralReplicator extends BaseTerraformingIndustry {
    @Override
    public String getAOTDVOKTechId() {
        return TMEIds.MINERAL_REPLICATOR_TECH;
    }

    @Override
    public List<Utils.ProjectData> getProjects() {
        return Utils.MINERAL_REPLICATOR_OPTIONS;
    }
}
