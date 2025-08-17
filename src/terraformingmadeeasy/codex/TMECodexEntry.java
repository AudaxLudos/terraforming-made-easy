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
import terraformingmadeeasy.ui.plugins.ProjectListPlugin;

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
    protected List<Utils.ProjectData> projects;

    public TMECodexEntry(String id, String title, String icon, Object param, Object param2) {
        super(id, title, icon, param);
        this.spec = (IndustrySpecAPI) this.param;
        this.param2 = param2;
        this.projects = Utils.castList(param2, Utils.ProjectData.class);

        for (Utils.ProjectData project : this.projects) {
            if (Objects.equals(this.spec.getId(), TMEIds.PLANETARY_HOLOGRAM)) {
                addRelatedEntry(CodexDataV2.getPlanetEntryId(project.id));
            } else if (Objects.equals(this.spec.getId(), TMEIds.CONSTRUCTION_GRID)) {
                // No codex data exists for project
                return;
            } else {
                addRelatedEntry(CodexDataV2.getConditionEntryId(project.id));
            }
        }
    }

    public static void replaceTMEIndustryCodex(String industryId, Object options) {
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
        String nameTooltipText = "Name of the condition to terraform on a planet";
        String timeTooltipText = "Build time, in days. Until the terraforming project finishes.";
        String costTooltipText = "One-time cost to begin terraforming project, in credits";
        if (Objects.equals(this.id, TMEIds.CONSTRUCTION_GRID)) {
            optionsText = "Megastructure";
            optionsDesc = "The construction grid can only be used once. Once a megastructure project is completed, the structure is used and the megastructure is created.";
            nameTooltipText = "Name of megastructure to build";
            timeTooltipText = "Build time, in days. Until the megastructure project finishes.";
            costTooltipText = "One-time cost to begin megastructure project, in credits";
        } else if (Objects.equals(this.id, TMEIds.PLANETARY_HOLOGRAM)) {
            optionsText = "Visual";
            optionsDesc = "A planet's visual can be changed at any time. Removing the structure will revert the planet's visual to its original state.";
            nameTooltipText = "Name of planet type to change into";
            timeTooltipText = "Build time, in days. Until a planet's visual changes.";
            costTooltipText = "One-time cost to change a planet's visual, in credits";
        }

        tooltip.addSectionHeading(optionsText + " Options", Alignment.MID, initPad);
        tooltip.addPara(optionsDesc, initPad);
        tooltip.addSpacer(oPad);

        float columnOneWidth = tw / 3f + 100f;
        float columnWidth = (tw - columnOneWidth) / 2f;
        CustomPanelAPI projectsPanel = panel.createCustomPanel(tw, 23f, null);
        TooltipMakerAPI projectsElement = projectsPanel.createUIElement(tw, 23f, false);
        projectsElement.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
                0f, false, true,
                new Object[]{"Name", columnOneWidth, "Build time", columnWidth, "Cost", columnWidth - 6f});
        projectsElement.addTableHeaderTooltip(0, nameTooltipText);
        projectsElement.addTableHeaderTooltip(1, timeTooltipText);
        projectsElement.addTableHeaderTooltip(2, costTooltipText);
        projectsElement.addTable("", 0, 0f);
        projectsPanel.addUIElement(projectsElement).setXAlignOffset(-10f);
        tooltip.addCustom(projectsPanel, 0f);

        List<Utils.ProjectData> projects = this.projects;
        ProjectListPlugin projectListPlugin = new ProjectListPlugin(panel, null, this.spec.getId(), projects, tw, 0f, true);
        tooltip.addCustom(projectListPlugin.panel, 0f).getPosition().setXAlignOffset(-5f);

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
