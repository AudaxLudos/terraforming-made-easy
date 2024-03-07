package terraformingmadeeasy.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.TextFieldAPI;

import java.util.Arrays;
import java.util.List;

public class TextFieldPanelPlugin extends BaseCustomUIPanelPlugin {
    public TextFieldAPI field = Global.getSettings().createTextField("", Fonts.DEFAULT_SMALL);
    public float width = 0f;
    public float height = 0f;
    public boolean shouldRecaptureFocus = false;

    public void setTextField(TextFieldAPI field, float width, float height) {
        if (field == null)
            return;
        this.field = field;
        this.width = width;
        this.height = height;
    }

    protected boolean clickedOutsideTextArea(InputEventAPI event) {
        return !this.field.getTextLabelAPI().getPosition().containsEvent(event);
    }

    @Override
    public void advance(float amount) {
        this.field.setText(this.field.getText().replaceAll("[^0-9]", ""));
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
        this.field.getTextLabelAPI().getPosition().setSize(this.width, this.height);
        if (this.field.hasFocus() && !this.shouldRecaptureFocus)
            this.shouldRecaptureFocus = true;
        for (InputEventAPI event : events) {
            if (event.isMouseDownEvent() && clickedOutsideTextArea(event)) {
                this.shouldRecaptureFocus = false;
                continue;
            }
            if (!this.shouldRecaptureFocus && !event.isKeyboardEvent())
                continue;
            if (event.getEventValue() == 1) {
                this.shouldRecaptureFocus = false;
                continue;
            }
            if (event.getEventValue() > 0 && !Arrays.asList(2, 3, 4, 5, 6, 7, 8, 9, 10, 11).contains(event.getEventValue())) {
                Global.getSoundPlayer().playUISound("ui_button_disabled_pressed", 1f, 1f);
            }
        }
    }
}
