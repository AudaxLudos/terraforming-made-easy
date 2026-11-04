package terraformingmadeeasy.ui.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.TextFieldAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class TextFieldPlugin extends BaseCustomUIPanelPlugin {
    protected TooltipMakerAPI parentTooltip;
    protected float width;
    protected CustomPanelAPI mainPanel;
    protected TooltipMakerAPI mainElem;
    protected TextFieldAPI textField;
    protected boolean allowLetters = true;
    protected boolean allowNumbers = true;
    protected boolean allowSymbols = true;

    public TextFieldPlugin(TooltipMakerAPI parentTooltip, float width) {
        this.parentTooltip = parentTooltip;
        this.width = width;

        this.mainPanel = Global.getSettings().createCustom(width, 25f, this);
        createUI();
    }

    public void createUI() {
        this.mainElem = this.mainPanel.createUIElement(this.width, 25f, false);

        this.textField = this.mainElem.addTextField(this.width, 25f, Fonts.DEFAULT_SMALL, 0f);

        this.mainPanel.addUIElement(this.mainElem).setXAlignOffset(-5f);
    }

    @Override
    public void advance(float amount) {
        if (this.textField.hasFocus()) {
            String text = this.textField.getText();
            String filtered = text;

            if (!this.allowLetters) {
                filtered = filtered.replaceAll("[a-zA-Z]", "");
            }
            if (!this.allowNumbers) {
                filtered = filtered.replaceAll("[0-9]", "");
            }
            if (!this.allowSymbols) {
                filtered = filtered.replaceAll("[^a-zA-Z0-9\\s]", "");
            }

            if (!filtered.equals(text)) {
                this.textField.setText(filtered);
            }
        }
    }

    public CustomPanelAPI getMainPanel() {
        return this.mainPanel;
    }

    public String getTextFieldText() {
        return this.textField.getText();
    }

    public void setTextFieldText(String text) {
        this.textField.setText(text);
    }

    public void setTextFieldMaxChar(int maxChar) {
        this.textField.setMaxChars(maxChar);
    }

    public void setAllowLetters(boolean allowLetters) {
        this.allowLetters = allowLetters;
    }

    public void setAllowNumbers(boolean allowNumbers) {
        this.allowNumbers = allowNumbers;
    }

    public void setAllowSymbols(boolean allowSymbols) {
        this.allowSymbols = allowSymbols;
    }
}
