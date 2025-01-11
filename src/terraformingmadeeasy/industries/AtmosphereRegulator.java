package terraformingmadeeasy.industries;

import terraformingmadeeasy.Utils;

public class AtmosphereRegulator extends TMEBaseIndustry {
    public AtmosphereRegulator() {
        setModifiableConditions(Utils.ATMOSPHERE_REGULATOR_OPTIONS);
    }
}
