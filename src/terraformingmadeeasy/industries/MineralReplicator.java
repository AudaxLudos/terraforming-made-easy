package terraformingmadeeasy.industries;

import terraformingmadeeasy.Utils;
import terraformingmadeeasy.ids.TMEIds;

public class MineralReplicator extends TMEBaseIndustry {
    public MineralReplicator() {
        setModifiableConditions(Utils.MINERAL_REPLICATOR_OPTIONS);
    }

    @Override
    public String getAOTDVOKTechId() {
        return TMEIds.MINERAL_REPLICATOR_TECH;
    }
}
