package terraformingmadeeasy.codex;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.impl.SharedUnlockData;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.codex.CodexDataV2;
import com.fs.starfarer.api.impl.codex.CodexDialogAPI;
import com.fs.starfarer.api.impl.codex.CodexEntryV2;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.IndustrySpecAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.ids.TMEIds;
import terraformingmadeeasy.ui.tooltips.MegastructureTooltip;
import terraformingmadeeasy.ui.tooltips.TerraformTooltip;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class TMECodexEntry extends CodexEntryV2 implements CustomUIPanelPlugin {
    protected CustomPanelAPI panel;
    protected UIPanelAPI relatedEntries;
    protected UIPanelAPI box;
    protected CodexDialogAPI codex;
    protected IndustrySpecAPI spec;

    public TMECodexEntry(String id, String title, String icon, Object param, Object param2) {
        super(id, title, icon, param);
        this.param2 = param2;
        this.spec = (IndustrySpecAPI) this.param;
    }

    public static TMECodexEntry replaceTMEIndustryCodex(String industryId, Object options) {
        IndustrySpecAPI spec = Global.getSettings().getIndustrySpec(industryId);

        // Remove existing entry from parent
        String entryId = CodexDataV2.getIndustryEntryId(spec.getId());
        CodexEntryV2 currEntry = (CodexEntryV2) CodexDataV2.ENTRIES.get(entryId);
        CodexEntryV2 parentEntry = (CodexEntryV2) currEntry.getParent();
        parentEntry.getChildren().remove(currEntry);

        // Replace vanilla entry with custom one
        TMECodexEntry codexEntry = new TMECodexEntry(spec.getId(), spec.getName(), spec.getImageName(), spec, options);
        parentEntry.addChild(codexEntry);
        CodexDataV2.ENTRIES.put(entryId, codexEntry);

        return codexEntry;
    }

    @Override
    public void createTitleForList(TooltipMakerAPI info, float width, ListMode mode) {
        info.addPara(this.spec.getName(), Misc.getBasePlayerColor(), 0f);
        boolean structure = this.spec.hasTag(Industries.TAG_STRUCTURE);
        String type = "Industry";
        if (structure) {
            type = "Structure";
        }
        info.addPara(type, Misc.getGrayColor(), 0f);
    }

    @Override
    public boolean matchesTags(Set<String> tags) {
        boolean industry = this.spec.hasTag(Industries.TAG_INDUSTRY);
        boolean structure = this.spec.hasTag(Industries.TAG_STRUCTURE);
        boolean station = this.spec.hasTag(Industries.TAG_STATION);
        if (tags.contains(CodexDataV2.OTHER) && !industry && !structure && !station) {
            return true;
        }
        if (tags.contains(CodexDataV2.INDUSTRIES) && industry) {
            return true;
        }
        if (tags.contains(CodexDataV2.STRUCTURES) && structure && !station) {
            return true;
        }
        return tags.contains(CodexDataV2.STATIONS) && station;
    }

    @Override
    public boolean hasCustomDetailPanel() {
        return true;
    }

    @Override
    public CustomUIPanelPlugin getCustomPanelPlugin() {
        return this;
    }

    @Override
    public void destroyCustomDetail() {
        this.panel = null;
        this.relatedEntries = null;
        this.box = null;
        this.codex = null;
    }

    @SuppressWarnings("RedundantArrayCreation")
    @Override
    public void createCustomDetail(CustomPanelAPI panel, UIPanelAPI relatedEntries, CodexDialogAPI codex) {
        this.panel = panel;
        this.relatedEntries = relatedEntries;
        this.codex = codex;
        IndustrySpecAPI spec = (IndustrySpecAPI) this.param;
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        float oPad = 10f;
        float width = panel.getPosition().getWidth();
        float initPad;
        float horizontalBoxPad = 30f;
        // the right width for a tooltip wrapped in a box to fit next to relatedEntries
        // 290 is the width of the related entries widget, but it may be null
        float tw = width - 290f - oPad - horizontalBoxPad + 10f;

        TooltipMakerAPI tooltip = panel.createUIElement(tw, 0, false);
        tooltip.setParaSmallInsignia();
        tooltip.setParaFontDefault();
        boolean structure = spec.hasTag(Industries.TAG_STRUCTURE);
        String type = "Industry";
        if (structure) {
            type = "Structure";
        }
        tooltip.addPara("Type: %s", oPad, g, Global.getSettings().getDesignTypeColor(type), type);
        tooltip.setParaSmallInsignia();
        initPad = oPad;

        tooltip.addPara(spec.getDesc(), initPad);
        String optionsText = "Terraforming";
        String optionsDesc = "Planetary conditions can be added or removed at any time. Once a terraforming project is completed, the planet will be terraformed immediately based on its current conditions.";
        String optionNameText = "Name of the condition to terraform on a planet";
        String optionDurationText = "Build time, in days. Until the terraforming project finishes.";
        String optionCostText = "One-time cost to begin terraforming project, in credits";
        if (Objects.equals(this.id, TMEIds.CONSTRUCTION_GRID)) {
            optionsText = "Megastructure";
            optionsDesc = "A construction grid can only be used once. When a megastructure project is completed, the construction grid is consumed.";
            optionNameText = "Name of megastructure to build";
            optionDurationText = "Build time, in days. Until the megastructure project finishes.";
            optionCostText = "One-time cost to begin megastructure project, in credits";
        }
        tooltip.addSectionHeading(optionsText + " Options", Alignment.MID, initPad);
        tooltip.addPara(optionsDesc, initPad);
        tooltip.addSpacer(oPad);
        float columnOneWidth = tw / 3f + 100f;
        float columnWidth = (tw - columnOneWidth) / 2f;
        CustomPanelAPI conditionsPanel = panel.createCustomPanel(tw, 0, null);
        TooltipMakerAPI conditionsHeader = conditionsPanel.createUIElement(tw, 0, false);
        conditionsHeader.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
                0f, false, true,
                new Object[]{"Name", columnOneWidth, "Build time", columnWidth, "Cost", columnWidth - 6f});
        conditionsHeader.addTableHeaderTooltip(0, optionNameText);
        conditionsHeader.addTableHeaderTooltip(1, optionDurationText);
        conditionsHeader.addTableHeaderTooltip(2, optionCostText);
        conditionsHeader.addTable("", 0, 0f);
        conditionsHeader.getPrev().getPosition().setXAlignOffset(0f);
        conditionsHeader.getPosition().inTMid(0);
        conditionsPanel.addUIElement(conditionsHeader);
        conditionsPanel.getPosition().setSize(tw, conditionsHeader.getHeightSoFar() + conditionsPanel.getPosition().getHeight());

        TooltipMakerAPI conditionsBody = conditionsPanel.createUIElement(tw, 0, false);
        if (this.param2 instanceof List<?>) {
            List<?> options = (List<?>) this.param2;
            for (Object option : options) {
                CustomPanelAPI conditionPanel = addOptionInfo(panel, option, tw);
                conditionsBody.addCustom(conditionPanel, 0f);
            }
        }

        conditionsPanel.addUIElement(conditionsBody);
        conditionsPanel.getPosition().setSize(tw, conditionsBody.getHeightSoFar() + conditionsPanel.getPosition().getHeight());
        conditionsPanel.updateUIElementSizeAndMakeItProcessInput(conditionsBody);
        tooltip.addCustom(conditionsPanel, 0f).getPosition().setXAlignOffset(-5);

        tooltip.setParaFontDefault();
        tooltip.addPara("Construction cost: %s", oPad, g, h, Misc.getDGSCredits(spec.getCost()));
        tooltip.setParaSmallInsignia();

        panel.updateUIElementSizeAndMakeItProcessInput(tooltip);

        TooltipMakerAPI imageTooltip = this.panel.createUIElement(290f, 0f, false);
        imageTooltip.addImage(this.icon, 290f, 0f);
        imageTooltip.getPosition().inTR(0f, 0f);
        imageTooltip.getPosition().setXAlignOffset(-5);
        this.panel.addUIElement(imageTooltip);

        this.box = panel.wrapTooltipWithBox(tooltip);
        panel.addComponent(this.box).inTL(0f, 0f);
        if (relatedEntries != null) {
            float yPad = imageTooltip.getPosition().getHeight();
            panel.addComponent(relatedEntries).inTR(0f, yPad + oPad);
        }

        float height = this.box.getPosition().getHeight();
        if (relatedEntries != null) {
            height = Math.max(height, relatedEntries.getPosition().getHeight());
        }
        panel.getPosition().setSize(width, height);
    }

    public CustomPanelAPI addOptionInfo(CustomPanelAPI panel, Object data, float width) {
        float columnOneWidth = width / 3f + 100f;
        float columnWidth = (width - columnOneWidth) / 2f;
        float cost = 0;
        int buildTime = 0;
        String icon = "";
        String name = "";
        TooltipMakerAPI.TooltipCreator tooltip = null;

        if (data instanceof Utils.ModifiableCondition) {
            Utils.ModifiableCondition cond = (Utils.ModifiableCondition) data;
            cost = cond.cost;
            buildTime = Math.round(cond.buildTime);
            icon = cond.icon;
            name = cond.name;
            tooltip = new TerraformTooltip(cond, null);
        } else if (data instanceof Utils.ProjectData) {
            Utils.ProjectData project = (Utils.ProjectData) data;
            cost = project.cost;
            buildTime = Math.round(project.buildTime);
            icon = project.icon;
            name = project.name;
            tooltip = new MegastructureTooltip(project);
        }

        CustomPanelAPI optionPanel = panel.createCustomPanel(width, 44f, null);

        TooltipMakerAPI optionButtonElement = optionPanel.createUIElement(width, 44f, false);
        ButtonAPI optionButton = optionButtonElement.addButton("", data, new Color(0, 195, 255, 190), new Color(0, 0, 0, 255), Alignment.MID, CutStyle.NONE, width, 44f, 0f);
        optionButton.setClickable(false);
        optionButtonElement.addTooltipTo(tooltip, optionButton, TooltipMakerAPI.TooltipLocation.RIGHT);
        optionButtonElement.getPosition().setXAlignOffset(-10f);
        optionPanel.addUIElement(optionButtonElement);

        TooltipMakerAPI optionNameElement = optionPanel.createUIElement(columnOneWidth, 40f, false);
        TooltipMakerAPI optionImage = optionNameElement.beginImageWithText(icon, 40f);
        optionImage.addPara(name, Misc.getTextColor(), 0f);
        optionNameElement.addImageWithText(0f);
        optionNameElement.getPosition().setXAlignOffset(-8f).setYAlignOffset(2f);
        optionPanel.addUIElement(optionNameElement);

        TooltipMakerAPI optionBuildTimeElement = optionPanel.createUIElement(columnWidth, 40f, false);
        optionBuildTimeElement.addPara(Math.round(buildTime * Utils.BUILD_TIME_MULTIPLIER) + "", Misc.getHighlightColor(), 12f).setAlignment(Alignment.MID);
        optionBuildTimeElement.getPosition().rightOfMid(optionNameElement, 0f);
        optionPanel.addUIElement(optionBuildTimeElement);

        TooltipMakerAPI optionCostElement = optionPanel.createUIElement(columnWidth, 40f, false);
        optionCostElement.addPara(Misc.getDGSCredits(cost * Utils.BUILD_COST_MULTIPLIER), Misc.getHighlightColor(), 12f).setAlignment(Alignment.MID);
        optionCostElement.getPosition().rightOfMid(optionBuildTimeElement, 0f);
        optionPanel.addUIElement(optionCostElement);

        return optionPanel;
    }

    @Override
    public boolean isVignetteIcon() {
        return true;
    }

    @Override
    public Set<String> getUnlockRelatedTags() {
        return this.spec.getTags();
    }

    @Override
    public boolean isUnlockedIfRequiresUnlock() {
        return SharedUnlockData.get().isPlayerAwareOfIndustry(this.spec.getId());
    }

    @Override
    public void positionChanged(PositionAPI position) {

    }

    @Override
    public void renderBelow(float alphaMult) {
    }

    @Override
    public void render(float alphaMult) {
    }

    @Override
    public void advance(float amount) {

    }

    @Override
    public void processInput(List<InputEventAPI> events) {

    }

    @Override
    public void buttonPressed(Object buttonId) {

    }
}
