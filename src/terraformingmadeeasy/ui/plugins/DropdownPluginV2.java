package terraformingmadeeasy.ui.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DropdownPluginV2 extends BaseCustomUIPanelPlugin {
    public CustomPanelAPI panel;
    public CustomPanelAPI dropdownPanel;
    public CustomPanelAPI menuPanel;
    public ButtonAPI button;
    public LabelAPI label;
    public boolean isDropped = false;
    public boolean isRendered = false;
    public Map<String, Object> options;
    public List<ButtonAPI> buttons = new ArrayList<>();
    public Object selected;

    public DropdownPluginV2(CustomPanelAPI panel, float width, float height, Map<String, Object> options) {
        this.panel = panel;
        this.options = options;
        this.dropdownPanel = this.panel.createCustomPanel(width, height, this);
        TooltipMakerAPI dropdownElement = this.dropdownPanel.createUIElement(width, height, false);
        this.dropdownPanel.addUIElement(dropdownElement);

        String name = "";
        Object data = null;
        if (options != null && !options.isEmpty()) {
            Map.Entry<String, Object> entry = options.entrySet().iterator().next();
            name = entry.getKey();
            data = entry.getValue();
        }
        this.button = dropdownElement.addButton("", data, new Color(0, 195, 255, 190), new Color(0, 0, 0, 255), Alignment.MID, CutStyle.NONE, width, height, 0f);
        this.label = dropdownElement.addPara(name, Misc.getBrightPlayerColor(), 0f);
        float yPad = height / 2f - this.label.computeTextHeight(this.label.getText()) / 2f;
        this.label.getPosition().inTL(10f, yPad);

        this.menuPanel = this.dropdownPanel.createCustomPanel(width, height * 4f, null);
        TooltipMakerAPI menuElement = this.menuPanel.createUIElement(width, height * 3.5f - 1f, true);
        for (Map.Entry<String, Object> entry : this.options.entrySet()) {
            CustomPanelAPI optionPanel = this.menuPanel.createCustomPanel(width, height, this);

            TooltipMakerAPI optionButtonElement = optionPanel.createUIElement(width, height, false);
            ButtonAPI optionButton = optionButtonElement.addButton("", entry.getValue(), new Color(0, 195, 255, 190), new Color(0, 0, 0, 255), Alignment.MID, CutStyle.NONE, width, height, 0f);
            optionButtonElement.getPosition().setXAlignOffset(-10f);
            optionPanel.addUIElement(optionButtonElement);

            TooltipMakerAPI optionNameElement = optionPanel.createUIElement(width, height, false);
            LabelAPI optionName = optionNameElement.addPara(entry.getKey(), Misc.getBasePlayerColor(), 0f);
            float yOptionPad = height / 2f - optionName.computeTextHeight(optionName.getText()) / 2f;
            optionName.getPosition().inTL(0f, yOptionPad);
            optionPanel.addUIElement(optionNameElement);

            this.buttons.add(optionButton);

            menuElement.addCustom(optionPanel, 0f);
        }

        this.menuPanel.addUIElement(menuElement);
        dropdownElement.addCustom(this.menuPanel, 0f).getPosition().setXAlignOffset(-5f).setYAlignOffset(-8f);
        this.menuPanel.setOpacity(0f);
    }

    public static void renderQuadBorder(float x, float y, float width, float height, Color color, float alphaMult, float thickness) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ZERO);

        GL11.glColor4f(
                color.getRed() / 255f,
                color.getGreen() / 255f,
                color.getBlue() / 255f,
                (color.getAlpha() / 255f) * alphaMult
        );
        GL11.glLineWidth(thickness);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        {
            float offset = thickness / 2f;
            GL11.glVertex2f(x + offset, y + offset);
            GL11.glVertex2f(x + offset, y + height - offset);
            GL11.glVertex2f(x + width - offset, y + height - offset);
            GL11.glVertex2f(x + width - offset, y + offset);
        }
        GL11.glEnd();
    }

    @Override
    public void render(float alphaMult) {
        if (this.button != null) {
            PositionAPI position = this.button.getPosition();
            float width = position.getWidth();
            float height = position.getHeight();
            float x = position.getX();
            float y = position.getY();
            SpriteAPI sprite = Global.getSettings().getSprite("graphics/ui/buttons/arrow_down.png");
            sprite.setHeight(height);
            sprite.render(x + position.getWidth() - height, y);

            renderQuadBorder(x, y, width, height, Misc.getDarkPlayerColor(), alphaMult, 1f);
        }
        if (this.isDropped && !this.isRendered) {
            this.isRendered = true;
            this.menuPanel.setOpacity(1f);
        }
        if (this.isRendered) {
            PositionAPI position = this.menuPanel.getPosition();
            float width = position.getWidth();
            float height = position.getHeight();
            float x = position.getX();
            float y = position.getY();

            renderQuadBorder(x, y, width, height, Misc.getDarkPlayerColor(), alphaMult, 1f);
        }
    }

    @Override
    public void buttonPressed(Object buttonId) {
        this.isDropped = !this.isDropped;
        if (this.isDropped) {
            this.button.highlight();
        } else {
            this.button.unhighlight();
            this.isRendered = false;
        }

        this.selected = buttonId;
        this.label.setText(((SectorEntityToken) buttonId).getName());
        this.button.setCustomData(this.selected);
        float yPad = this.button.getPosition().getHeight() / 2f - this.label.computeTextHeight(this.label.getText()) / 2f;
        this.label.getPosition().inTL(10f, yPad);
        this.isRendered = false;

        for (ButtonAPI button : this.buttons) {
            if (button.getCustomData() == buttonId) {
                button.highlight();
                continue;
            }
            if (button.isHighlighted()) {
                button.unhighlight();
            }
        }

        if (!this.isRendered) {
            this.menuPanel.setOpacity(0f);
        }
    }

    public void setSelected(Object data) {
        for (Map.Entry<String, Object> entry : this.options.entrySet()) {
            if (entry.getValue() == entry) {
                this.button.setCustomData(data);
                this.selected = data;
                this.label.setText(entry.getKey());
                return;
            }
        }
    }

    public Object getSelected() {
        return this.selected;
    }
}
