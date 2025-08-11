package terraformingmadeeasy.ui.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.TextFieldAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import java.util.List;

public class TextFieldPluginV2 extends BaseCustomUIPanelPlugin {
    public CustomPanelAPI panel;
    public CustomPanelAPI textFieldPanel;
    public TextFieldAPI textField;
    public float width = 0f;
    public float height = 0f;
    public boolean shouldRecaptureFocus = false;

    public TextFieldPluginV2(CustomPanelAPI panel, float width, float height) {
        this.panel = panel;
        this.textFieldPanel = this.panel.createCustomPanel(width, height, this);
        TooltipMakerAPI textFieldElement = this.textFieldPanel.createUIElement(width, height, false);
        this.textFieldPanel.addUIElement(textFieldElement);
        this.textField = textFieldElement.addTextField(width, height, Fonts.DEFAULT_SMALL, 0f);
    }

    @Override
    public void advance(float amount) {
        this.textField.setText(this.textField.getText().replaceAll("[^0-9]", ""));
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
        if (this.textField.hasFocus() && !this.shouldRecaptureFocus) {
            this.shouldRecaptureFocus = true;
        }
        for (InputEventAPI event : events) {
            if (event.isMouseDownEvent() && clickedOutsideTextArea(event)) {
                this.shouldRecaptureFocus = false;
                continue;
            }
            if (!this.shouldRecaptureFocus && !event.isKeyboardEvent()) {
                continue;
            }
            if (event.getEventValue() == 1) {
                this.shouldRecaptureFocus = false;
            }
        }
    }

    protected boolean clickedOutsideTextArea(InputEventAPI event) {
        return !this.textField.getTextLabelAPI().getPosition().containsEvent(event);
    }
}
