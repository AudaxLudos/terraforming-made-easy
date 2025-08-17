package terraformingmadeeasy.ui.dialogs;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.ids.TMEIds;
import terraformingmadeeasy.industries.BaseDevelopmentIndustry;
import terraformingmadeeasy.industries.PlanetaryHologram;
import terraformingmadeeasy.industries.StellarManufactory;
import terraformingmadeeasy.ui.plugins.DropdownPluginV2;
import terraformingmadeeasy.ui.plugins.ProjectListPlugin;
import terraformingmadeeasy.ui.tooltips.TextTooltip;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class TerraformingDialogDelegate extends DevelopmentDialogDelegate {
    public TerraformingDialogDelegate(float width, float height, boolean hasCustomInputs, BaseDevelopmentIndustry industry) {
        super(width, height, hasCustomInputs, industry);

        if (Objects.equals(this.industry.getId(), TMEIds.STELLAR_MANUFACTORY)) {
            this.hasCustomInputs = true;
        }
    }

    @SuppressWarnings("RedundantArrayCreation")
    @Override
    public void addCustomDataInputs(CustomPanelAPI panel, TooltipMakerAPI tooltip) {
        // Inputs
        float orbitInputsPanelHeight = 100f;
        CustomPanelAPI inputsPanel = panel.createCustomPanel(this.width, orbitInputsPanelHeight, null);

        // Inputs body
        TooltipMakerAPI inputsElement = inputsPanel.createUIElement(this.width, orbitInputsPanelHeight, false);
        inputsElement.getPosition().inTL(0f, 3f);
        inputsElement.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
                0f, false, true,
                new Object[]{"Stellar Mirror Data", this.width});
        inputsElement.addTableHeaderTooltip(0, "Determines the amount of stellar mirrors that will be created or removed in orbit");
        inputsElement.addTable("", 0, 0f);
        inputsElement.getPrev().getPosition().setXAlignOffset(0f);

        // Orbit focus input
        CustomPanelAPI mirrorOptionsPanel = inputsPanel.createCustomPanel(this.width / 4f, 40f, null);
        TooltipMakerAPI mirrorOptionsElement = mirrorOptionsPanel.createUIElement(this.width / 4f, 40f, false);
        mirrorOptionsElement.addPara("Stellar Mirror Options", 0f).setAlignment(Alignment.MID);
        mirrorOptionsElement.setParaSmallInsignia();
        mirrorOptionsElement.addSpacer(3f);
        // Orbit focus input dropdown
        Map<String, Object> mirrorOptions = new LinkedHashMap<>();
        mirrorOptions.put("Do Nothing", StellarManufactory.StellarMirrorOptions.NONE);
        mirrorOptions.put("Add Three", StellarManufactory.StellarMirrorOptions.ADD_THREE);
        mirrorOptions.put("Add Five", StellarManufactory.StellarMirrorOptions.ADD_FIVE);
        mirrorOptions.put("Remove All", StellarManufactory.StellarMirrorOptions.REMOVE_ALL);
        this.data2 = new DropdownPluginV2(mirrorOptionsPanel, 190f, 25f, mirrorOptions);
        ((DropdownPluginV2) this.data2).setSelected(StellarManufactory.StellarMirrorOptions.NONE);
        mirrorOptionsElement.addCustom(((DropdownPluginV2) this.data2).panel, 0f);
        mirrorOptionsPanel.addUIElement(mirrorOptionsElement);
        inputsElement.addCustom(mirrorOptionsPanel, 0f);
        inputsElement.addTooltipTo(new TextTooltip("How many stellar mirrors will be created or removed in orbit"), mirrorOptionsPanel, TooltipMakerAPI.TooltipLocation.BELOW);

        // Orbit focus input
        CustomPanelAPI shadeOptionsPanel = inputsPanel.createCustomPanel(this.width / 4f, 40f, null);
        TooltipMakerAPI shadeOptionsElement = shadeOptionsPanel.createUIElement(this.width / 4f, 40f, false);
        shadeOptionsElement.addPara("Stellar Shade Options", 0f).setAlignment(Alignment.MID);
        shadeOptionsElement.setParaSmallInsignia();
        shadeOptionsElement.addSpacer(3f);
        // Orbit focus input dropdown
        Map<String, Object> shadeOptions = new LinkedHashMap<>();
        shadeOptions.put("Do Nothing", StellarManufactory.StellarMirrorOptions.NONE);
        shadeOptions.put("Add One", StellarManufactory.StellarMirrorOptions.ADD_ONE);
        shadeOptions.put("Add Three", StellarManufactory.StellarMirrorOptions.ADD_THREE);
        shadeOptions.put("Remove All", StellarManufactory.StellarMirrorOptions.REMOVE_ALL);
        this.data3 = new DropdownPluginV2(shadeOptionsPanel, 190f, 25f, shadeOptions);
        ((DropdownPluginV2) this.data3).setSelected(StellarManufactory.StellarMirrorOptions.NONE);
        shadeOptionsElement.addCustom(((DropdownPluginV2) this.data3).panel, 0f);
        shadeOptionsPanel.addUIElement(shadeOptionsElement);
        inputsElement.addCustom(shadeOptionsPanel, 0f).getPosition().rightOfMid(mirrorOptionsPanel, 0f);
        inputsElement.addTooltipTo(new TextTooltip("How many stellar mirrors will be created or removed in orbit"), shadeOptionsPanel, TooltipMakerAPI.TooltipLocation.BELOW);

        inputsPanel.addUIElement(inputsElement);
        tooltip.addCustom(inputsPanel, 0f);
    }

    @Override
    public void customDialogConfirm() {
        if (!(this.data instanceof ProjectListPlugin)) {
            return;
        }

        Utils.ProjectData project = ((ProjectListPlugin) this.data).selected;

        if (project == null) {
            return;
        }

        if (Objects.equals(this.industry.getId(), TMEIds.STELLAR_MANUFACTORY)) {
            if (!(this.data2 instanceof DropdownPluginV2) || !(this.data3 instanceof DropdownPluginV2)) {
                return;
            }

            StellarManufactory.StellarMirrorOptions mirrorData = (StellarManufactory.StellarMirrorOptions) ((DropdownPluginV2) this.data2).getSelected();
            StellarManufactory.StellarMirrorOptions shadeData = (StellarManufactory.StellarMirrorOptions) ((DropdownPluginV2) this.data3).getSelected();

            if (mirrorData == null || shadeData == null) {
                return;
            }

            ((StellarManufactory) this.industry).stellarMirrorData = mirrorData;
            ((StellarManufactory) this.industry).stellarShadeData = shadeData;
        }

        this.industry.setProject(project);
        this.industry.startUpgrading();
        Global.getSector().getPlayerFleet().getCargo().getCredits().subtract(project.cost * Utils.BUILD_COST_MULTIPLIER);
        Global.getSoundPlayer().playSound("ui_upgrade_industry", 1f, 1f, Global.getSoundPlayer().getListenerPos(), new Vector2f());
    }

    @Override
    public String getConfirmText() {
        if (this.industry instanceof PlanetaryHologram) {
            return "Mask";
        } else {
            return "Terraform";
        }
    }
}
