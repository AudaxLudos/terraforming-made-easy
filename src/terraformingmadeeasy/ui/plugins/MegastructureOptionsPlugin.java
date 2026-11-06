package terraformingmadeeasy.ui.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomEntitySpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import terraformingmadeeasy.Settings;
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.industries.MegastructureIndustry;
import terraformingmadeeasy.loading.specs.MegastructureProjectSpec;
import terraformingmadeeasy.ui.tooltips.MegastructureProjectTooltip;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MegastructureOptionsPlugin extends RefreshableUIPlugin {
    protected TooltipMakerAPI parentTooltip;
    protected List<MegastructureProjectSpec> projectOptions;
    protected MarketAPI market;
    protected List<ButtonAPI> buttons = new ArrayList<>();
    protected MegastructureProjectSpec selectedProject;
    protected boolean forCodex = false;

    public MegastructureOptionsPlugin(TooltipMakerAPI parentTooltip, List<MegastructureProjectSpec> projectOptions, float width, float height, boolean forCodex) {
        super(width, height);
        this.parentTooltip = parentTooltip;
        this.projectOptions = new ArrayList<>(projectOptions);
        this.forCodex = forCodex;

        this.mainPanel = Global.getSettings().createCustom(this.width, this.height, null);
        createUI();
    }

    public MegastructureOptionsPlugin(TooltipMakerAPI parentTooltip, MegastructureIndustry industry, float width, float height) {
        super(width, height);
        this.parentTooltip = parentTooltip;
        this.projectOptions = new ArrayList<>(industry.getProjects());
        this.market = industry.getMarket();

        this.mainPanel = Global.getSettings().createCustom(this.width, this.height, this);
        createUI();
    }

    public void createUI() {
        float columnOneWidth = this.width / 3f + 100f;
        float columnWidth = (this.width - columnOneWidth) / 2f;

        this.mainElem = this.mainPanel.createUIElement(this.width, this.height, !this.forCodex);

        for (MegastructureProjectSpec project : this.projectOptions) {
            CustomEntitySpecAPI spec = Global.getSettings().getCustomEntitySpec(project.getCustomEntityId());
            float buildTime = Math.round(project.getBuildTime() * Settings.BUILD_TIME_MULTIPLIER);
            float totalCost = Math.round(project.getCost() * Settings.BUILD_COST_MULTIPLIER);
            boolean canAfford = Utils.canAfford(totalCost);
            boolean canAffordAndBuild = canAfford && MegastructureIndustry.canBuildMegastructure(this.market, spec.getId());
            String name = spec.getDefaultName();
            String icon = spec.getInteractionImage();

            CustomPanelAPI rowPanel = this.mainPanel.createCustomPanel(this.width, 44f, this);
            TooltipMakerAPI rowButtonElem = rowPanel.createUIElement(this.width, 44f, false);
            ButtonAPI rowButton = rowButtonElem.addButton("", canAffordAndBuild ? project : null, new Color(0, 195, 255, 190), new Color(0, 0, 0, 255), Alignment.MID, CutStyle.NONE, this.width, 44f, 0f);
            rowButton.setClickable(!this.forCodex);
            if (!this.forCodex) {
                Utils.setButtonEnabledOrHighlighted(rowButton, canAffordAndBuild, !canAffordAndBuild);
            }
            rowButtonElem.addTooltipTo(new MegastructureProjectTooltip(project), rowButtonElem, TooltipMakerAPI.TooltipLocation.RIGHT);
            rowPanel.addUIElement(rowButtonElem).setXAlignOffset(-10f);

            TooltipMakerAPI colNameElem = rowPanel.createUIElement(columnOneWidth, 44f, false);
            TooltipMakerAPI colName = colNameElem.beginImageWithText(icon, 40f);
            colName.addPara(name, canAffordAndBuild || this.forCodex ? Misc.getBasePlayerColor() : Misc.getNegativeHighlightColor(), 0f);
            colNameElem.addImageWithText(2f).getPosition().setXAlignOffset(2f);
            rowPanel.addUIElement(colNameElem).setXAlignOffset(-5f);

            TooltipMakerAPI colTimeElem = rowPanel.createUIElement(columnWidth, 44f, false);
            colTimeElem.addPara(Misc.getWithDGS(buildTime), Misc.getHighlightColor(), 13f).setAlignment(Alignment.MID);
            colTimeElem.getPosition().rightOfTop(colNameElem, 0f);
            rowPanel.addUIElement(colTimeElem);

            TooltipMakerAPI colCostElem = rowPanel.createUIElement(columnWidth, 44f, false);
            colCostElem.addPara(Misc.getDGSCredits(totalCost), canAfford || this.forCodex ? Misc.getHighlightColor() : Misc.getNegativeHighlightColor(), 13f).setAlignment(Alignment.MID);
            colCostElem.getPosition().rightOfTop(colTimeElem, 0f);
            rowPanel.addUIElement(colCostElem);

            this.mainElem.addCustom(rowPanel, 0f);

            if (canAffordAndBuild) {
                this.buttons.add(rowButton);
            }
        }
        this.mainPanel.addUIElement(this.mainElem);
        if (this.forCodex) {
            // Codex specific as the size is set during codex creation
            this.mainPanel.getPosition().setSize(this.width, this.mainElem.getHeightSoFar());
        }
    }

    @Override
    public void buttonPressed(Object buttonId) {
        if (buttonId instanceof MegastructureProjectSpec project) {
            this.selectedProject = project;

            boolean isHighlighted = false;
            for (ButtonAPI button : this.buttons) {
                if (button.getCustomData() == project && !button.isHighlighted()) {
                    button.highlight();
                    isHighlighted = true;
                    continue;
                }
                if (button.isHighlighted()) {
                    button.unhighlight();
                }
            }

            if (!isHighlighted) {
                this.selectedProject = null;
            }
        }
    }

    public MegastructureProjectSpec getSelectedProject() {
        return this.selectedProject;
    }
}
