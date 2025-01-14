package terraformingmadeeasy;

import com.fs.starfarer.api.BaseModPlugin;
import terraformingmadeeasy.ids.TMEPeople;
import terraformingmadeeasy.listeners.TMEIndustryOptionProvider;

public class ModPlugin extends BaseModPlugin {
    @Override
    public void onGameLoad(boolean newGame) {
        TMEIndustryOptionProvider.register();
        TMEPeople.register();
    }
}
