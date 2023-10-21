package terraformingmadeeasy

import com.fs.starfarer.api.BaseModPlugin
import java.lang.RuntimeException

class TMEModPlugin : BaseModPlugin() {
    override fun onApplicationLoad() {
        super.onApplicationLoad()

        throw RuntimeException("Mod Loaded")
    }
}