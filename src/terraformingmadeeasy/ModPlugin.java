package terraformingmadeeasy;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import terraformingmadeeasy.ids.TMEPeople;
import terraformingmadeeasy.industries.AgriculturalLaboratory;
import terraformingmadeeasy.listeners.TMEIndustryOptionProvider;

public class ModPlugin extends BaseModPlugin {
    @Override
    public void onGameLoad(boolean newGame) {
        TMEIndustryOptionProvider.register();
        TMEPeople.register();
    }
}
