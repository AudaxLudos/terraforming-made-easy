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
import terraformingmadeeasy.Settings;
import terraformingmadeeasy.TMEIds;
import terraformingmadeeasy.loading.specs.TerraformingProjectSpec;
import terraformingmadeeasy.ui.plugins.TerraformingOptionsPlugin;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class TerraformingIndustryCodexEntry extends CodexEntryV2 implements CustomUIPanelPlugin {
    protected final IndustrySpecAPI spec;
    protected final List<TerraformingProjectSpec> projects;
    protected CustomPanelAPI mainPanel;
    protected UIPanelAPI relatedEntries;
    protected UIPanelAPI box;
    protected CodexDialogAPI codex;

    public TerraformingIndustryCodexEntry(String id, String title, String icon, Object param) {
        super(id, title, icon, param);
        this.spec = (IndustrySpecAPI) param;
        this.projects = Settings.getAllTerraformingProjectSpecs().stream().filter(p -> p.getStructureId().equals(getId())).toList();

        for (TerraformingProjectSpec project : this.projects) {
            if (Objects.equals(this.spec.getId(), TMEIds.PLANETARY_HOLOGRAM)) {
                addRelatedEntry(CodexDataV2.getPlanetEntryId(project.getPlanetSpecOverride()));
            } else {
                addRelatedEntry(CodexDataV2.getConditionEntryId(project.getConditionId()));
            }
        }
    }

    public static void replaceCodexEntry(String industryId) {
        IndustrySpecAPI spec = Global.getSettings().getIndustrySpec(industryId);

        // Remove existing entry from parent
        String entryId = CodexDataV2.getIndustryEntryId(spec.getId());
        CodexEntryV2 currEntry = (CodexEntryV2) CodexDataV2.ENTRIES.get(entryId);
        CodexEntryV2 parentEntry = (CodexEntryV2) currEntry.getParent();
        parentEntry.getChildren().remove(currEntry);

        // Replace vanilla entry with custom one
        TerraformingIndustryCodexEntry codexEntry = new TerraformingIndustryCodexEntry(spec.getId(), spec.getName(), spec.getImageName(), spec);
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
        this.mainPanel = null;
        this.relatedEntries = null;
        this.box = null;
        this.codex = null;
    }

    @Override
    public void createCustomDetail(CustomPanelAPI panel, UIPanelAPI relatedEntries, CodexDialogAPI codex) {
        this.mainPanel = panel;
        this.relatedEntries = relatedEntries;
        this.codex = codex;
        IndustrySpecAPI spec = (IndustrySpecAPI) this.param;

        float oPad = 10f;
        float width = panel.getPosition().getWidth();
        float horizontalBoxPad = 30f;
        // the right width for a tooltip wrapped in a box to fit next to relatedEntries
        // 290 is the width of the related entries widget, but it may be null
        float tw = width - 290f - oPad - horizontalBoxPad + 10f;
        float columnOneWidth = (tw - 10f) / 3f + 100f;
        float columnWidth = (tw - 10f - columnOneWidth) / 2f;
        boolean structure = spec.hasTag(Industries.TAG_STRUCTURE);
        String type = "Industry";
        if (structure) {
            type = "Structure";
        }
        String optionsText = "Terraforming";
        String optionsDesc = "Planetary conditions can be added or removed at any time. Once a terraforming project is completed, the planet will be terraformed immediately based on its current conditions. Removing planetary conditions costs 80% less";
        String nameTooltipText = "Name of the condition to terraform on a planet";
        String timeTooltipText = "Build time, in days. Until the terraforming project finishes.";
        String costTooltipText = "One-time cost to begin terraforming project, in credits";
        if (Objects.equals(this.id, TMEIds.PLANETARY_HOLOGRAM)) {
            optionsText = "Visual";
            optionsDesc = "A planet's visual can be changed at any time. Removing the structure will revert the planet's visual to its original state.";
            nameTooltipText = "Name of planet type to change into";
            timeTooltipText = "Build time, in days. Until a planet's visual changes.";
            costTooltipText = "One-time cost to change a planet's visual, in credits";
        }

        // Info elem START
        TooltipMakerAPI infoElem = this.mainPanel.createUIElement(tw, 20f, false);
        infoElem.addPara("Type: %s", oPad, Misc.getGrayColor(), Global.getSettings().getDesignTypeColor(type), type);
        infoElem.setParaSmallInsignia();
        infoElem.addPara(spec.getDesc(), oPad);

        infoElem.addSectionHeading(optionsText + " Options", Alignment.MID, oPad);
        infoElem.addPara(optionsDesc, oPad);
        infoElem.addSpacer(oPad);

        // noinspection RedundantArrayCreation
        infoElem.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
                0f, false, true,
                new Object[]{"Name", columnOneWidth, "Build time", columnWidth, "Cost", columnWidth - 6f});
        infoElem.addTableHeaderTooltip(0, nameTooltipText);
        infoElem.addTableHeaderTooltip(1, timeTooltipText);
        infoElem.addTableHeaderTooltip(2, costTooltipText);
        infoElem.addTable("", 0, 0f);
        TerraformingOptionsPlugin optionsPlugin = new TerraformingOptionsPlugin(infoElem, this.projects, tw - 10f, 0f, true);
        infoElem.addCustom(optionsPlugin.getMainPanel(), 0f);

        infoElem.setParaFontDefault();
        infoElem.addPara("Construction cost: %s", oPad, Misc.getGrayColor(), Misc.getHighlightColor(), Misc.getDGSCredits(spec.getCost()));
        this.mainPanel.addUIElement(infoElem);
        // Info elem END

        // Info elem image START
        TooltipMakerAPI infoImageElem = this.mainPanel.createUIElement(290f, 0f, false);
        infoImageElem.addImage(this.icon, 290f, 0f);
        infoImageElem.getPosition().inTR(5f, 0f);
        this.mainPanel.addUIElement(infoImageElem);
        // Info elem image END

        this.box = panel.wrapTooltipWithBox(infoElem);
        panel.addComponent(this.box).inTL(0f, 0f);
        if (relatedEntries != null) {
            float yPad = infoImageElem.getPosition().getHeight();
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
