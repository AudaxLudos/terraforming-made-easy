package terraformingmadeeasy.ui.plugins;

import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class RefreshableUIPlugin extends BaseCustomUIPanelPlugin {
    protected float width;
    protected float height;
    protected CustomPanelAPI mainPanel;
    protected TooltipMakerAPI mainElem;

    public RefreshableUIPlugin(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public void createUI() {
    }

    public void recreateUI() {
        if (this.mainElem != null) {
            if (this.mainElem.getExternalScroller() != null) {
                this.mainPanel.removeComponent(this.mainElem.getExternalScroller());
            } else {
                this.mainPanel.removeComponent(this.mainElem);
            }

            createUI();
        }
    }

    public CustomPanelAPI getMainPanel() {
        return this.mainPanel;
    }
}
