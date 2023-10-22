package terraformingmadeeasy.industry;

import com.fs.starfarer.api.impl.campaign.ids.Conditions;

public class TMEStellarFactoryRelays extends TMEBaseIndustry {
    public TMEStellarFactoryRelays() {
        this.modifiableConditions.add(new ModifiableCondition(Conditions.HOT, 100000f, 180f));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.VERY_HOT, 100000f, 365f));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.COLD, 100000f, 180f));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.VERY_COLD, 100000f, 365f));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.POOR_LIGHT, 100000f, 180f));
        this.modifiableConditions.add(new ModifiableCondition(Conditions.DARK, 100000f, 365f));
    }
}
