package terraformingmadeeasy.industry;

import com.fs.starfarer.api.impl.campaign.ids.Conditions;

public class TMEStellarFactoryRelays extends TMEBaseIndustry {
    public TMEStellarFactoryRelays() {
        this.modifiableConditions.add(new ModifiableCondition(Conditions.HOT, 1000000f, 180f));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.VERY_HOT, 2000000f, 360f));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.COLD, 1000000f, 180f));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.VERY_COLD, 2000000f, 360f));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.POOR_LIGHT, 1000000f, 180f));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.DARK, 2000000f, 360f));
    }
}
