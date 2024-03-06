package terraformingmadeeasy;

import com.fs.starfarer.api.BaseModPlugin;
import terraformingmadeeasy.listeners.MegastructureOptionProvider;
import terraformingmadeeasy.listeners.TerraformOptionProvider;

public class ModPlugin extends BaseModPlugin {
    @Override
    public void onGameLoad(boolean newGame) {
        TerraformOptionProvider.register();
        MegastructureOptionProvider.register();
    }
}
