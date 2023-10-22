package terraformingmadeeasy;

import com.fs.starfarer.api.BaseModPlugin;
import terraformingmadeeasy.listeners.TMEIndustryOptionProvider;

public class TMEModPlugin extends BaseModPlugin {
    @Override
    public void onGameLoad(boolean newGame) {
        super.onGameLoad(newGame);

        TMEIndustryOptionProvider.register();
    }
}
