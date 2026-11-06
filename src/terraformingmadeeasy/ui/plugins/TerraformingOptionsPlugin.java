package terraformingmadeeasy.ui.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MarketConditionSpecAPI;
import com.fs.starfarer.api.ui.*;
import terraformingmadeeasy.Settings;
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.industries.TerraformingIndustry;
import terraformingmadeeasy.loading.specs.TerraformingProjectSpec;
import terraformingmadeeasy.ui.tooltips.TerraformingProjectTooltip;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TerraformingOptionsPlugin extends RefreshableUIPlugin {
    protected TooltipMakerAPI parentTooltip;
    protected List<TerraformingProjectSpec> projectOptions;
    protected MarketAPI market;
    protected List<ButtonAPI> buttons = new ArrayList<>();
    protected TerraformingProjectSpec selectedProject;
    protected String nameFilter = "";
    protected boolean forCodex = false;

    public TerraformingOptionsPlugin(TooltipMakerAPI parentTooltip, List<TerraformingProjectSpec> projectOptions, float width, float height, boolean forCodex) {
        super(width, height);
        this.parentTooltip = parentTooltip;
        this.projectOptions = new ArrayList<>(projectOptions);
        this.forCodex = forCodex;

        this.mainPanel = Global.getSettings().createCustom(this.width, this.height, null);
        createUI();
    }

    public TerraformingOptionsPlugin(TooltipMakerAPI parentTooltip, TerraformingIndustry industry, float width, float height) {
        super(width, height);
        this.parentTooltip = parentTooltip;
        this.projectOptions = new ArrayList<>(industry.getProjects());
        this.market = industry.getMarket();

        this.mainPanel = Global.getSettings().createCustom(this.width, this.height, this);
        createUI();
    }

    @Override
    public void createUI() {
        float columnOneWidth = this.width / 3f + 100f;
        float columnWidth = (this.width - columnOneWidth) / 2f;

        this.mainElem = this.mainPanel.createUIElement(this.width, this.height, !this.forCodex);

        List<TerraformingProjectSpec> filteredProjects = this.projectOptions;
        filteredProjects.sort(Comparator.comparing((TerraformingProjectSpec project) -> {
            MarketConditionSpecAPI spec = Global.getSettings().getMarketConditionSpec(project.getConditionId());
            return !this.nameFilter.isBlank() && spec.getName().toLowerCase().contains(this.nameFilter);
        }).reversed());

        for (TerraformingProjectSpec project : filteredProjects) {
            MarketConditionSpecAPI spec = Global.getSettings().getMarketConditionSpec(project.getConditionId());
            boolean marketHasCondition = this.market != null && this.market.hasCondition(spec.getId());
            float buildTime = Math.round(project.getBuildTime() * Settings.BUILD_TIME_MULTIPLIER);
            float baseCost = project.getCost();
            float removalCostMult = !marketHasCondition ? 1f : Settings.REMOVAL_COST_MULTIPLIER;
            float totalCost = Math.round(baseCost * removalCostMult * Settings.BUILD_COST_MULTIPLIER);
            boolean canAfford = Utils.canAfford(totalCost);
            boolean canAffordAndBuild = canAfford && Utils.canPerformProject(this.market, project);
            String name = spec.getName();
            String prefix = marketHasCondition ? "Remove " : "Add ";
            String suffix = "";
            String icon = spec.getIcon();

            if (this.forCodex) {
                prefix = "";
                suffix = "";
            }

            CustomPanelAPI rowPanel = this.mainPanel.createCustomPanel(this.width, 44f, this);
            TooltipMakerAPI rowButtonElem = rowPanel.createUIElement(this.width, 44f, false);
            ButtonAPI rowButton = rowButtonElem.addButton("", canAffordAndBuild ? project : null, new Color(0, 195, 255, 190), new Color(0, 0, 0, 255), Alignment.MID, CutStyle.NONE, this.width, 44f, 0f);
            rowButton.setClickable(!this.forCodex);
            if (!this.forCodex) {
                Utils.setButtonEnabledOrHighlighted(rowButton, canAffordAndBuild, !canAffordAndBuild);
            }
            rowButtonElem.addTooltipTo(new TerraformingProjectTooltip(project, this.market), rowButtonElem, TooltipMakerAPI.TooltipLocation.RIGHT);
            rowPanel.addUIElement(rowButtonElem).setXAlignOffset(-10f);

            TooltipMakerAPI colNameElem = rowPanel.createUIElement(columnOneWidth, 44f, false);
            TooltipMakerAPI colName = colNameElem.beginImageWithText(icon, 40f);
            colName.addPara(prefix + name + suffix, canAffordAndBuild || this.forCodex ? com.fs.starfarer.api.util.Misc.getBasePlayerColor() : com.fs.starfarer.api.util.Misc.getNegativeHighlightColor(), 0f);
            colNameElem.addImageWithText(2f).getPosition().setXAlignOffset(2f);
            rowPanel.addUIElement(colNameElem).setXAlignOffset(-5f);

            TooltipMakerAPI colTimeElem = rowPanel.createUIElement(columnWidth, 44f, false);
            colTimeElem.addPara(com.fs.starfarer.api.util.Misc.getWithDGS(buildTime), com.fs.starfarer.api.util.Misc.getHighlightColor(), 13f).setAlignment(Alignment.MID);
            colTimeElem.getPosition().rightOfTop(colNameElem, 0f);
            rowPanel.addUIElement(colTimeElem);

            TooltipMakerAPI colCostElem = rowPanel.createUIElement(columnWidth, 44f, false);
            colCostElem.addPara(com.fs.starfarer.api.util.Misc.getDGSCredits(totalCost), canAfford || this.forCodex ? com.fs.starfarer.api.util.Misc.getHighlightColor() : com.fs.starfarer.api.util.Misc.getNegativeHighlightColor(), 13f).setAlignment(Alignment.MID);
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
        if (buttonId instanceof TerraformingProjectSpec project) {
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

    public TerraformingProjectSpec getSelectedProject() {
        return this.selectedProject;
    }
}
