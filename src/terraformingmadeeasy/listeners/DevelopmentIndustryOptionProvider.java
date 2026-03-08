package terraformingmadeeasy.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.listeners.BaseIndustryOptionProvider;
import com.fs.starfarer.api.campaign.listeners.DialogCreatorUI;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import terraformingmadeeasy.Settings;
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.ids.TMEIds;
import terraformingmadeeasy.industries.BaseDevelopmentIndustry;
import terraformingmadeeasy.ui.dialogs.ConfirmDialogDelegate;
import terraformingmadeeasy.ui.dialogs.DevelopmentDialogDelegate;
import terraformingmadeeasy.ui.dialogs.TerraformingDialogDelegate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DevelopmentIndustryOptionProvider extends BaseIndustryOptionProvider {
    public static final List<String> tmeIndustries = new ArrayList<>();
    public static final Object CUSTOM_PLUGIN = new Object();

    static {
        tmeIndustries.add(TMEIds.AGRICULTURAL_LABORATORY);
        tmeIndustries.add(TMEIds.ATMOSPHERE_REGULATOR);
        tmeIndustries.add(TMEIds.CONSTRUCTION_GRID);
        tmeIndustries.add(TMEIds.ELEMENT_SYNTHESIZER);
        tmeIndustries.add(TMEIds.GEOMORPHOLOGY_STATION);
        tmeIndustries.add(TMEIds.MINERAL_REPLICATOR);
        tmeIndustries.add(TMEIds.PLANETARY_HOLOGRAM);
        tmeIndustries.add(TMEIds.STELLAR_MANUFACTORY);
        tmeIndustries.add(TMEIds.TERRESTRIAL_ENGINE);
        tmeIndustries.add(TMEIds.UNIFICATION_CENTER);
    }

    public static void register() {
        ListenerManagerAPI listeners = Global.getSector().getListenerManager();
        if (!listeners.hasListenerOfClass(DevelopmentIndustryOptionProvider.class)) {
            listeners.addListener(new DevelopmentIndustryOptionProvider(), true);
        }
    }

    @Override
    public List<IndustryOptionData> getIndustryOptions(Industry ind) {
        if (isUnsuitable(ind, false)) {
            return null;
        }
        List<IndustryOptionData> result = new ArrayList<>();

        String isNotUpgradingText = "Terraform planet...";
        String isUpgradingText = "Cancel terraforming project...";
        if (Objects.equals(ind.getId(), TMEIds.CONSTRUCTION_GRID)) {
            isNotUpgradingText = "Build megastructure...";
            isUpgradingText = "Cancel megastructure project...";
        } else if (Objects.equals(ind.getId(), TMEIds.PLANETARY_HOLOGRAM)) {
            isNotUpgradingText = "Change planet visual...";
            isUpgradingText = "Cancel planet visual change...";
        }

        String text = ind.isUpgrading() ? isUpgradingText : isNotUpgradingText;
        IndustryOptionData opt = new IndustryOptionData(text, CUSTOM_PLUGIN, ind, this);
        result.add(opt);

        return result;
    }

    @Override
    public boolean isUnsuitable(Industry ind, boolean allowUnderConstruction) {
        return (super.isUnsuitable(ind, allowUnderConstruction) || !isSuitable(ind));
    }

    public boolean isSuitable(Industry ind) {
        boolean isTMEIndustry = tmeIndustries.contains(ind.getId());
        boolean isPlayerOwned = ind.getMarket().isPlayerOwned();
        return isTMEIndustry && isPlayerOwned;
    }

    @Override
    public void createTooltip(IndustryOptionData opt, TooltipMakerAPI tooltip, float width) {
        String description = "A specialized industry capable of removing and adding planetary conditions of a planet.";
        String refundText = "Cancel the terraforming project for a %s refund.";
        float refundCost = 0;
        if (Objects.equals(opt.ind.getId(), TMEIds.CONSTRUCTION_GRID)) {
            description = "A large structural grid for constructing Megastructures.";
            refundText = "Cancel the Megastructure project for a %s refund.";
        } else if (Objects.equals(opt.ind.getId(), TMEIds.PLANETARY_HOLOGRAM)) {
            description = "A specialized structure that can change a planet's visual to a different planet type.";
            refundText = "Cancel the planet visual change for a %s refund.";
        }
        String text = description;
        if (opt.ind.isUpgrading()) {
            Utils.ProjectData project = ((BaseDevelopmentIndustry) opt.ind).getProject();
            float baseCost = project.cost;
            boolean isConditionForRemoval = opt.ind != null && opt.ind.getMarket().hasCondition(project.id);
            float conditionRemovalMult = !isConditionForRemoval ? 1f : Settings.REMOVAL_COST_MULTIPLIER;
            refundCost = Math.round(baseCost * conditionRemovalMult * Settings.BUILD_COST_MULTIPLIER);
            text = refundText;
        }

        if (opt.id == CUSTOM_PLUGIN) {
            tooltip.addPara(text, 0f, Misc.getHighlightColor(), Misc.getDGSCredits(refundCost));
        }
    }

    @Override
    public void optionSelected(IndustryOptionData opt, DialogCreatorUI ui) {
        if (opt.id == CUSTOM_PLUGIN) {
            if (!opt.ind.isUpgrading()) {
                if (Objects.equals(opt.ind.getId(), TMEIds.CONSTRUCTION_GRID)) {
                    DevelopmentDialogDelegate dialogDelegate = new DevelopmentDialogDelegate(800f, 464f, true, (BaseDevelopmentIndustry) opt.ind);
                    ui.showDialog(dialogDelegate.width, dialogDelegate.height, dialogDelegate);
                } else {
                    TerraformingDialogDelegate dialogDelegate = new TerraformingDialogDelegate(800f, 464f, false, (BaseDevelopmentIndustry) opt.ind);
                    ui.showDialog(dialogDelegate.width, dialogDelegate.height, dialogDelegate);
                }
            } else {
                Utils.ProjectData project = ((BaseDevelopmentIndustry) opt.ind).getProject();
                float baseCost = project.cost;
                boolean isConditionForRemoval = opt.ind != null && opt.ind.getMarket().hasCondition(project.id);
                float conditionRemovalMult = !isConditionForRemoval ? 1f : Settings.REMOVAL_COST_MULTIPLIER;
                float totalCost = Math.round(baseCost * conditionRemovalMult * Settings.BUILD_COST_MULTIPLIER);

                ConfirmDialogDelegate dialogDelegate = new ConfirmDialogDelegate(opt.ind, totalCost);
                ui.showDialog(ConfirmDialogDelegate.WIDTH, ConfirmDialogDelegate.HEIGHT, dialogDelegate);
            }
        }
    }
}
