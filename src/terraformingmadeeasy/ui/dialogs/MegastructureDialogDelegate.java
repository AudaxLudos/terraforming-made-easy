package terraformingmadeeasy.ui.dialogs;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomDialogDelegate;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;
import terraformingmadeeasy.Settings;
import terraformingmadeeasy.ids.TMEIds;
import terraformingmadeeasy.industries.MegastructureIndustry;
import terraformingmadeeasy.loading.specs.MegastructureProjectSpec;
import terraformingmadeeasy.ui.plugins.DropdownPlugin;
import terraformingmadeeasy.ui.plugins.MegastructureOptionsPlugin;
import terraformingmadeeasy.ui.plugins.TextFieldPlugin;
import terraformingmadeeasy.ui.tooltips.*;

import java.util.LinkedHashMap;
import java.util.Map;

public class MegastructureDialogDelegate extends BaseCustomDialogDelegate {
    public static final float WIDTH = 800f;
    public static final float HEIGHT = 464f;
    public MegastructureIndustry industry;
    public TooltipMakerAPI mainElem;
    public MegastructureOptionsPlugin projectListPlugin;
    public DropdownPlugin entityDropdownPlugin;
    public TextFieldPlugin angleTextFieldPlugin;
    public TextFieldPlugin radiusTextFieldPlugin;
    public TextFieldPlugin daysTextFieldPlugin;

    public MegastructureDialogDelegate(MegastructureIndustry industry) {
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

        // Megastructure options elem START
        float columnOneWidth = WIDTH / 3f + 100f;
        float columnWidth = (WIDTH - columnOneWidth) / 2f;
        // noinspection RedundantArrayCreation
        this.mainElem.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
                0f, false, true,
                new Object[]{"Name", columnOneWidth, "Build time", columnWidth, "Cost", columnWidth - 6f});
        this.mainElem.addTableHeaderTooltip(0, "Name of megastructure to build");
        this.mainElem.addTableHeaderTooltip(1, "Build time, in days. Until the megastructure project finishes.");
        this.mainElem.addTableHeaderTooltip(2, "One-time cost to begin megastructure project, in credits");
        this.mainElem.addTable("", 0, 0f);
        this.projectListPlugin = new MegastructureOptionsPlugin(this.mainElem, this.industry, WIDTH, 350f);
        this.mainElem.addCustom(this.projectListPlugin.getMainPanel(), 0f);
        // Megastructure options elem END

        // Orbit inputs elem START
        this.mainElem.addSectionHeading("Orbit Settings", Alignment.MID, 10f).getPosition().setXAlignOffset(0f);

        CustomPanelAPI inputsPanel = panel.createCustomPanel(WIDTH, 43f, null);

        float colInputWidth = WIDTH / 4f - 20f;

        // Entity options elem START
        TooltipMakerAPI entityOptionsElem = inputsPanel.createUIElement(colInputWidth, 43f, false);
        entityOptionsElem.addPara("Orbit Entity", 0f).setAlignment(Alignment.MID);
        Map<String, Object> entityOptions = new LinkedHashMap<>();
        for (PlanetAPI planet : this.industry.getMarket().getStarSystem().getPlanets()) {
            entityOptions.put(planet.getName(), planet);
        }
        this.entityDropdownPlugin = new DropdownPlugin(entityOptionsElem, entityOptions, colInputWidth);
        entityOptionsElem.addCustom(this.entityDropdownPlugin.getMainPanel(), 3f);
        entityOptionsElem.addTooltipTo(new OrbitFocusFieldTooltip(), this.entityDropdownPlugin.getMainPanel(), TooltipMakerAPI.TooltipLocation.BELOW);
        inputsPanel.addUIElement(entityOptionsElem).setXAlignOffset(-5f);
        // Entity options elem START

        // Angle input elem START
        TooltipMakerAPI angleInputElem = inputsPanel.createUIElement(colInputWidth, 43f, false);
        angleInputElem.addPara("Orbit Angle", 0f).setAlignment(Alignment.MID);
        this.angleTextFieldPlugin = new TextFieldPlugin(angleInputElem, colInputWidth);
        this.angleTextFieldPlugin.setTextFieldText("0");
        this.angleTextFieldPlugin.setTextFieldMaxChar(5);
        this.angleTextFieldPlugin.setAllowLetters(false);
        this.angleTextFieldPlugin.setAllowSymbols(false);
        angleInputElem.addCustom(this.angleTextFieldPlugin.getMainPanel(), 3f);
        angleInputElem.addTooltipTo(new StartingAngleFieldTooltip(), this.angleTextFieldPlugin.getMainPanel(), TooltipMakerAPI.TooltipLocation.BELOW);
        inputsPanel.addUIElement(angleInputElem).rightOfTop(entityOptionsElem, 20f);
        // Angle input elem START

        // Radius input elem START
        TooltipMakerAPI radiusInputElem = inputsPanel.createUIElement(colInputWidth, 43f, false);
        radiusInputElem.addPara("Orbit Radius", 0f).setAlignment(Alignment.MID);
        this.radiusTextFieldPlugin = new TextFieldPlugin(radiusInputElem, colInputWidth);
        this.radiusTextFieldPlugin.setTextFieldText("1000");
        this.radiusTextFieldPlugin.setTextFieldMaxChar(5);
        this.radiusTextFieldPlugin.setAllowLetters(false);
        this.radiusTextFieldPlugin.setAllowSymbols(false);
        radiusInputElem.addCustom(this.radiusTextFieldPlugin.getMainPanel(), 3f);
        radiusInputElem.addTooltipTo(new OrbitRadiusFieldTooltip(), this.radiusTextFieldPlugin.getMainPanel(), TooltipMakerAPI.TooltipLocation.BELOW);
        inputsPanel.addUIElement(radiusInputElem).rightOfTop(angleInputElem, 20f);
        // Radius input elem START

        // Days input elem START
        TooltipMakerAPI daysInputElem = inputsPanel.createUIElement(colInputWidth, 43f, false);
        daysInputElem.addPara("Orbit Days", 0f).setAlignment(Alignment.MID);
        this.daysTextFieldPlugin = new TextFieldPlugin(daysInputElem, colInputWidth);
        this.daysTextFieldPlugin.setTextFieldText("100");
        this.daysTextFieldPlugin.setTextFieldMaxChar(5);
        this.daysTextFieldPlugin.setAllowLetters(false);
        this.daysTextFieldPlugin.setAllowSymbols(false);
        daysInputElem.addCustom(this.daysTextFieldPlugin.getMainPanel(), 3f);
        daysInputElem.addTooltipTo(new OrbitDaysFieldTooltip(), this.daysTextFieldPlugin.getMainPanel(), TooltipMakerAPI.TooltipLocation.BELOW);
        inputsPanel.addUIElement(daysInputElem).rightOfTop(radiusInputElem, 20f);
        // Days input elem START

        this.mainElem.addCustom(inputsPanel, 8f);
        // Orbit inputs elem END

        panel.addUIElement(this.mainElem).setXAlignOffset(-5f);
    }

    @Override
    public void customDialogConfirm() {
        MegastructureProjectSpec projectSpec = this.projectListPlugin.getSelectedProject();
        if (projectSpec == null) {
            return;
        }

        this.industry.setProject(projectSpec);
        this.industry.startUpgrading();
        this.industry.orbitEntity = (SectorEntityToken) this.entityDropdownPlugin.getSelectedOption();
        this.industry.orbitAngle = Float.parseFloat(this.angleTextFieldPlugin.getTextFieldText());
        this.industry.orbitDays = Float.parseFloat(this.daysTextFieldPlugin.getTextFieldText());
        this.industry.orbitRadius = Float.parseFloat(this.radiusTextFieldPlugin.getTextFieldText());
        this.industry.startUpgrading();

        float totalCost = Math.round(projectSpec.getCost() * Settings.BUILD_COST_MULTIPLIER);
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
