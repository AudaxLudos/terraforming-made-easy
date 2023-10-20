package terraformingmadeeasy;

import com.fs.starfarer.api.BaseModPlugin;

public class TMEModPlugin extends BaseModPlugin {
    @Override
    public void onApplicationLoad() throws Exception {
        super.onApplicationLoad();

        throw new RuntimeException("Mod Loaded");
    }
}
