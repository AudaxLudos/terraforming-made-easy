package terraformingmadeeasy.ui.dialogs;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomDialogDelegate;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;
import terraformingmadeeasy.Settings;
import terraformingmadeeasy.TMEIds;
import terraformingmadeeasy.industries.StellarManufactory;
import terraformingmadeeasy.industries.TerraformingIndustry;
import terraformingmadeeasy.loading.specs.TerraformingProjectSpec;
import terraformingmadeeasy.ui.plugins.DropdownPlugin;
import terraformingmadeeasy.ui.plugins.TerraformingOptionsPlugin;
import terraformingmadeeasy.ui.tooltips.TextTooltip;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class TerraformingDialogDelegate extends BaseCustomDialogDelegate {
    public static final float WIDTH = 800f;
    public static final float HEIGHT = 464f;
    public final TerraformingIndustry industry;
    public TooltipMakerAPI mainElem;
    public TerraformingOptionsPlugin projectListPlugin;
    public DropdownPlugin mirrorDropdownPlugin;
    public DropdownPlugin shadeDropdownPlugin;

    public TerraformingDialogDelegate(TerraformingIndustry industry) {
        this.industry = industry;
    }

    @Override
    public void createCustomDialog(CustomPanelAPI panel, CustomDialogCallback callback) {
        this.mainElem = panel.createUIElement(WIDTH, HEIGHT, false);

        // Credits elem START
        TooltipMakerAPI creditsElement = panel.createUIElement(WIDTH / 4f, 20f, false);
        creditsElement.setParaSmallInsignia();
        LabelAPI creditsLabel = creditsElement.addPara("Credits: %s", 0f, Misc.getGrayColor(), Misc.getHighlightColor(),
                Misc.getWithDGS(Global.getSector().getPlayerFleet().getCargo().getCredits().get()));
        creditsLabel.setHighlightOnMouseover(true);
        creditsElement.addTooltipToPrevious(new TextTooltip("Credits available"), TooltipMakerAPI.TooltipLocation.ABOVE);
        creditsElement.getPosition().setYAlignOffset(-33f);
        panel.addUIElement(creditsElement);
        // Credits elem END

        // Terraforming options elem START
        float columnOneWidth = WIDTH / 3f + 100f;
        float columnWidth = (WIDTH - columnOneWidth) / 2f;
        String nameTooltipText = "Name of the condition to terraform on a planet";
        String timeTooltipText = "Build time, in days. Until the terraforming project finishes.";
        String costTooltipText = "One-time cost to begin terraforming project, in credits";
        if (Objects.equals(this.industry.getId(), TMEIds.PLANETARY_HOLOGRAM)) {
            nameTooltipText = "Name of planet type to change into";
            timeTooltipText = "Build time, in days. Until a planet's visual changes.";
            costTooltipText = "One-time cost to change a planet's visual, in credits";
        }
        // noinspection RedundantArrayCreation
        this.mainElem.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
                0f, false, true,
                new Object[]{"Name", columnOneWidth, "Build time", columnWidth, "Cost", columnWidth - 6f});
        this.mainElem.addTableHeaderTooltip(0, nameTooltipText);
        this.mainElem.addTableHeaderTooltip(1, timeTooltipText);
        this.mainElem.addTableHeaderTooltip(2, costTooltipText);
        this.mainElem.addTable("", 0, 0f);
        this.projectListPlugin = new TerraformingOptionsPlugin(this.mainElem, this.industry, WIDTH, 350f);
        this.mainElem.addCustom(this.projectListPlugin.getMainPanel(), 0f);
        // Terraforming options elem END

        // Mirror inputs elem START
        if (this.industry.getId().equals(TMEIds.STELLAR_MANUFACTORY)) {
            this.mainElem.addSectionHeading("Stellar Mirror Settings", Alignment.MID, 10f).getPosition().setXAlignOffset(0f);

            CustomPanelAPI inputsPanel = panel.createCustomPanel(WIDTH, 43f, null);

            // Mirror options elem START
            TooltipMakerAPI mirrorOptionsElem = inputsPanel.createUIElement(190f, 43f, false);
            mirrorOptionsElem.addPara("Stellar Mirror Options", 0f).setAlignment(Alignment.MID);
            Map<String, Object> mirrorOptions = new LinkedHashMap<>();
            mirrorOptions.put("Do Nothing", StellarManufactory.StellarMirrorOptions.NONE);
            mirrorOptions.put("Add Three", StellarManufactory.StellarMirrorOptions.ADD_THREE);
            mirrorOptions.put("Add Five", StellarManufactory.StellarMirrorOptions.ADD_FIVE);
            mirrorOptions.put("Remove All", StellarManufactory.StellarMirrorOptions.REMOVE_ALL);
            this.mirrorDropdownPlugin = new DropdownPlugin(mirrorOptionsElem, mirrorOptions, 190f);
            mirrorOptionsElem.addCustom(this.mirrorDropdownPlugin.getMainPanel(), 3f);
            mirrorOptionsElem.addTooltipTo(new TextTooltip("How many stellar mirrors will be created or removed in orbit"), this.mirrorDropdownPlugin.getMainPanel(), TooltipMakerAPI.TooltipLocation.BELOW);
            inputsPanel.addUIElement(mirrorOptionsElem).setXAlignOffset(-5f);
            // Mirror options elem END

            // Shade options elem START
            TooltipMakerAPI shadeOptionsElem = inputsPanel.createUIElement(190f, 43f, false);
            shadeOptionsElem.addPara("Stellar Shade Options", 0f).setAlignment(Alignment.MID);
            Map<String, Object> shadeOptions = new LinkedHashMap<>();
            shadeOptions.put("Do Nothing", StellarManufactory.StellarMirrorOptions.NONE);
            shadeOptions.put("Add One", StellarManufactory.StellarMirrorOptions.ADD_ONE);
            shadeOptions.put("Add Three", StellarManufactory.StellarMirrorOptions.ADD_THREE);
            shadeOptions.put("Remove All", StellarManufactory.StellarMirrorOptions.REMOVE_ALL);
            this.shadeDropdownPlugin = new DropdownPlugin(shadeOptionsElem, shadeOptions, 190f);
            shadeOptionsElem.addCustom(this.shadeDropdownPlugin.getMainPanel(), 3f);
            shadeOptionsElem.addTooltipTo(new TextTooltip("How many stellar shades will be created or removed in orbit"), this.shadeDropdownPlugin.getMainPanel(), TooltipMakerAPI.TooltipLocation.BELOW);
            inputsPanel.addUIElement(shadeOptionsElem).rightOfTop(mirrorOptionsElem, 20f);
            // Shade options elem END

            this.mainElem.addCustom(inputsPanel, 8f);
        }
        // Mirror inputs elem END

        panel.addUIElement(this.mainElem).setXAlignOffset(-5f);
    }

    @Override
    public void customDialogConfirm() {
        TerraformingProjectSpec projectSpec = this.projectListPlugin.getSelectedProject();
        if (projectSpec == null) {
            return;
        }

        if (TMEIds.STELLAR_MANUFACTORY.equals(this.industry.getId())) {
            StellarManufactory.StellarMirrorOptions mirrorData = StellarManufactory.StellarMirrorOptions.NONE;
            StellarManufactory.StellarMirrorOptions shadeData = StellarManufactory.StellarMirrorOptions.NONE;

            if (this.mirrorDropdownPlugin.getSelectedOption() instanceof StellarManufactory.StellarMirrorOptions option) {
                mirrorData = option;
            }
            if (this.shadeDropdownPlugin.getSelectedOption() instanceof StellarManufactory.StellarMirrorOptions option) {
                shadeData = option;
            }

            ((StellarManufactory) this.industry).stellarMirrorData = mirrorData;
            ((StellarManufactory) this.industry).stellarShadeData = shadeData;
        }

        this.industry.setProject(projectSpec);
        this.industry.startUpgrading();

        float baseCost = projectSpec.getCost();
        boolean isConditionForRemoval = this.industry.getMarket().hasCondition(projectSpec.getConditionId());
        float conditionRemovalMult = !isConditionForRemoval ? 1f : Settings.REMOVAL_COST_MULTIPLIER;
        float totalCost = Math.round(baseCost * conditionRemovalMult * Settings.BUILD_COST_MULTIPLIER);

        Global.getSector().getPlayerFleet().getCargo().getCredits().subtract(totalCost);
        Global.getSoundPlayer().playSound("ui_upgrade_industry", 1f, 1f, Global.getSoundPlayer().getListenerPos(), new Vector2f());
    }

    @Override
    public String getConfirmText() {
        if (TMEIds.PLANETARY_HOLOGRAM.equals(this.industry.getId())) {
            return "Mask";
        }
        return "Start";
    }

    @Override
    public boolean hasCancelButton() {
        return true;
    }

    @Override
    public String getCancelText() {
        return "Cancel";
    }
}
