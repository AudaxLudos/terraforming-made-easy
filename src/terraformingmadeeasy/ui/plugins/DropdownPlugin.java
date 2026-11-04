package terraformingmadeeasy.ui.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.*;
import terraformingmadeeasy.Utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DropdownPlugin extends BaseCustomUIPanelPlugin {
    protected TooltipMakerAPI parentTooltip;
    protected Map<String, Object> options;
    protected float width;
    protected CustomPanelAPI mainPanel;
    protected TooltipMakerAPI mainElem;
    protected ButtonAPI dropdownButton;
    protected LabelAPI dropdownLabel;
    protected CustomPanelAPI menuPanel;
    protected TooltipMakerAPI menuElem;
    protected List<ButtonAPI> optionButtons = new ArrayList<>();
    protected boolean openMenu = false;
    protected Object selectedOption;

    public DropdownPlugin(TooltipMakerAPI parentTooltip, Map<String, Object> options, float width) {
        this.parentTooltip = parentTooltip;
        this.options = options;
        this.width = width;

        this.mainPanel = Global.getSettings().createCustom(width, 25f, this);
        createUI();
    }

    public void createUI() {
        String optionId = "";
        if (this.options != null && !this.options.isEmpty()) {
            Map.Entry<String, Object> entry = this.options.entrySet().iterator().next();
            optionId = entry.getKey();
        }

        this.mainElem = this.mainPanel.createUIElement(this.width, 25f, false);

        // Dropdown button elem START
        CustomPanelAPI dropdownButtonPanel = this.mainPanel.createCustomPanel(this.width, 25f, this);
        TooltipMakerAPI dropdownButtonElem = dropdownButtonPanel.createUIElement(this.width, 25f, false);
        this.dropdownButton = dropdownButtonElem.addButton("", optionId, new Color(0, 195, 255, 190), new Color(0, 0, 0, 255), Alignment.MID, CutStyle.NONE, this.width, 25f, 0f);
        dropdownButtonPanel.addUIElement(dropdownButtonElem).setXAlignOffset(-5f);

        TooltipMakerAPI dropdownLabelElem = dropdownButtonPanel.createUIElement(this.width, 25f, false);
        this.dropdownLabel = dropdownLabelElem.addPara(optionId, com.fs.starfarer.api.util.Misc.getBasePlayerColor(), 0f);
        this.dropdownLabel.getPosition().setSize(this.width, 25f);
        this.dropdownLabel.setAlignment(Alignment.MID);
        dropdownButtonPanel.addUIElement(dropdownLabelElem).setXAlignOffset(-5f);
        this.mainElem.addCustom(dropdownButtonPanel, 0f);
        // Dropdown button elem END

        // Dropdown menu elem START
        this.menuPanel = this.mainPanel.createCustomPanel(this.width, 25f * 3.5f, this);
        this.menuElem = this.menuPanel.createUIElement(this.width, 25f * 3.5f, true);
        if (this.options != null) {
            for (Map.Entry<String, Object> entry : this.options.entrySet()) {
                CustomPanelAPI optionPanel = this.menuPanel.createCustomPanel(this.width, 25f, this);

                TooltipMakerAPI optionButtonElement = optionPanel.createUIElement(this.width, 25f, false);
                ButtonAPI optionButton = optionButtonElement.addButton("", entry.getKey(), new Color(0, 195, 255, 190), new Color(0, 0, 0, 255), Alignment.MID, CutStyle.NONE, this.width, 25f, 0f);
                optionPanel.addUIElement(optionButtonElement).setXAlignOffset(-10f);

                TooltipMakerAPI optionNameElement = optionPanel.createUIElement(this.width, 25f, false);
                LabelAPI optionLabel = optionNameElement.addPara(entry.getKey(), com.fs.starfarer.api.util.Misc.getBasePlayerColor(), 0f);
                optionLabel.getPosition().setSize(this.width, 25f);
                optionLabel.setAlignment(Alignment.MID);
                optionPanel.addUIElement(optionNameElement).setXAlignOffset(-10f);

                this.menuElem.addCustom(optionPanel, 0f);

                this.optionButtons.add(optionButton);
            }
        }
        this.menuPanel.setOpacity(0f);
        this.menuPanel.addUIElement(this.menuElem);
        this.mainElem.addCustomDoNotSetPosition(this.menuPanel).getPosition().belowMid(dropdownButtonPanel, 3f);
        // Dropdown menu elem END

        this.mainPanel.addUIElement(this.mainElem).setXAlignOffset(-5f);
    }

    @Override
    public void render(float alphaMult) {
        if (this.dropdownButton != null) {
            PositionAPI position = this.dropdownButton.getPosition();
            float width = position.getWidth();
            float height = position.getHeight();
            float x = position.getX();
            float y = position.getY();
            SpriteAPI sprite = Global.getSettings().getSprite("graphics/ui/buttons/arrow_down.png");
            sprite.setHeight(height);
            sprite.render(x + position.getWidth() - height, y);

            Utils.renderQuadBorder(x, y, width, height, com.fs.starfarer.api.util.Misc.getDarkPlayerColor(), alphaMult, 1f);
        }
        if (this.openMenu && this.menuPanel != null) {
            PositionAPI position = this.menuPanel.getPosition();
            float width = position.getWidth();
            float height = position.getHeight();
            float x = position.getX();
            float y = position.getY();

            Utils.renderQuadBorder(x, y, width, height, com.fs.starfarer.api.util.Misc.getDarkPlayerColor(), alphaMult, 1f);
        }
    }

    @Override
    public void buttonPressed(Object buttonId) {
        this.openMenu = !this.openMenu;
        if (this.openMenu) {
            this.dropdownButton.highlight();
            this.menuPanel.setOpacity(1f);
        } else {
            this.dropdownButton.unhighlight();
            this.menuPanel.setOpacity(0f);
        }

        if (buttonId instanceof String data) {
            for (ButtonAPI button : this.optionButtons) {
                if (button.getCustomData() == buttonId) {
                    this.dropdownButton.setCustomData(data);
                    this.dropdownLabel.setText(data);
                    this.selectedOption = this.options.get(data);
                    button.highlight();
                    continue;
                }
                if (button.isHighlighted()) {
                    button.unhighlight();
                }
            }
        }
    }

    public CustomPanelAPI getMainPanel() {
        return this.mainPanel;
    }

    public Object getSelectedOption() {
        return this.selectedOption;
    }
}
