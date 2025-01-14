package terraformingmadeeasy.industries;

import terraformingmadeeasy.Utils;
import terraformingmadeeasy.ids.TMEIds;

public class ElementSynthesizer extends TMEBaseIndustry {
    public ElementSynthesizer() {
        setModifiableConditions(Utils.ELEMENT_SYNTHESIZER_OPTIONS);
    }

    @Override
    public String getAOTDVOKTechId() {
        return TMEIds.ELEMENT_SYNTHESIZER_TECH;
    }
}
