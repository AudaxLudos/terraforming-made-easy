package terraformingmadeeasy;

import com.fs.starfarer.api.BaseModPlugin;
import terraformingmadeeasy.ids.TMEPeople;
import terraformingmadeeasy.listeners.TMEOptionProvider;

public class ModPlugin extends BaseModPlugin {
    @Override
    public void onGameLoad(boolean newGame) {
        TMEOptionProvider.register();
        TMEPeople.register();
    }
}
