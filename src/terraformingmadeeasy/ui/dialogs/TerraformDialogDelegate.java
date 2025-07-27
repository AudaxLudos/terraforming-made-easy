package terraformingmadeeasy.ui.dialogs;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.ids.TMEIds;
import terraformingmadeeasy.industries.BaseTerraformingIndustry;
import terraformingmadeeasy.ui.tooltips.TextTooltip;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TerraformDialogDelegate extends TMEBaseDialogDelegate {
    public BaseTerraformingIndustry industry;

    public TerraformDialogDelegate(float width, float height, BaseTerraformingIndustry industry) {
        WIDTH = width;
        HEIGHT = height;
        this.industry = industry;
    }

    @SuppressWarnings("RedundantArrayCreation")
    @Override
    public void createCustomDialog(CustomPanelAPI panel, CustomDialogCallback callback) {
        this.buttons.clear();

        TooltipMakerAPI mElement = panel.createUIElement(WIDTH, HEIGHT, false);
        panel.addUIElement(mElement).setXAlignOffset(-5f);

        float columnOneWidth = WIDTH / 3f + 100f;
        float columnWidth = (WIDTH - columnOneWidth) / 2f;
        float rowHeight = 440f;

        String optionNameText = "Name of the condition to terraform on a planet";
        String optionDurationText = "Build time, in days. Until the terraforming project finishes.";
        String optionCostText = "One-time cost to begin terraforming project, in credits";
        if (Objects.equals(this.industry.getId(), TMEIds.PLANETARY_HOLOGRAM)) {
            optionNameText = "Name of planet type to change into";
            optionDurationText = "Build time, in days. Until a planet's visual changes.";
            optionCostText = "One-time cost to change a planet's visual, in credits";
        }

        // Terraforming options selection area
        CustomPanelAPI conditionsPanel = panel.createCustomPanel(WIDTH, rowHeight, null);
        TooltipMakerAPI conditionsHeader = conditionsPanel.createUIElement(WIDTH, rowHeight, false);
        conditionsHeader.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
                0f, false, true,
                new Object[]{"Name", columnOneWidth, "Build time", columnWidth, "Cost", columnWidth - 6f});
        conditionsHeader.addTableHeaderTooltip(0, optionNameText);
        conditionsHeader.addTableHeaderTooltip(1, optionDurationText);
        conditionsHeader.addTableHeaderTooltip(2, optionCostText);
        conditionsHeader.addTable("", 0, 0f);
        conditionsHeader.getPrev().getPosition().setXAlignOffset(0f);
        conditionsPanel.addUIElement(conditionsHeader);

        TooltipMakerAPI conditionsBody = conditionsPanel.createUIElement(WIDTH, rowHeight - 22f, true);
        List<Utils.ProjectData> projects = this.industry.getProjects();
        Collections.sort(projects, new Utils.SortCanAffordAndBuild(this.industry));
        for (Utils.ProjectData condition : projects) {
            CustomPanelAPI conditionPanel = Utils.addCustomButton(panel, condition, this.industry, this.buttons, WIDTH, this);
            conditionsBody.addCustom(conditionPanel, 0f);
        }
        conditionsPanel.addUIElement(conditionsBody);
        mElement.addCustom(conditionsPanel, 0f);

        // Show player credits
        TooltipMakerAPI creditsElement = panel.createUIElement(WIDTH, 0f, false);
        TooltipMakerAPI creditsSubElement = creditsElement.beginSubTooltip(380f);
        creditsSubElement.setParaSmallInsignia();
        LabelAPI creditsLabel = creditsSubElement.addPara("Credits: %s", 3f, Misc.getGrayColor(), Misc.getHighlightColor(),
                Misc.getWithDGS(Global.getSector().getPlayerFleet().getCargo().getCredits().get()));
        creditsLabel.setHighlightOnMouseover(true);
        creditsLabel.getPosition().setXAlignOffset(0f);
        creditsElement.endSubTooltip();
        creditsElement.addCustom(creditsSubElement, 0f);
        creditsElement.addTooltipToPrevious(new TextTooltip("Credits available"), TooltipMakerAPI.TooltipLocation.ABOVE);
        panel.addUIElement(creditsElement).inBL(0f, -32f);
    }

    @Override
    public String getConfirmText() {
        return "Terraform";
    }

    @Override
    public void customDialogConfirm() {
        if (this.selected == null) {
            return;
        }
        Utils.ProjectData selectedProject = (Utils.ProjectData) this.selected;
        this.industry.setProject(selectedProject);
        this.industry.startUpgrading();
        Global.getSector().getPlayerFleet().getCargo().getCredits().subtract(selectedProject.cost * Utils.BUILD_COST_MULTIPLIER);
        Global.getSoundPlayer().playSound("ui_upgrade_industry", 1f, 1f, Global.getSoundPlayer().getListenerPos(), new Vector2f());
    }
}
