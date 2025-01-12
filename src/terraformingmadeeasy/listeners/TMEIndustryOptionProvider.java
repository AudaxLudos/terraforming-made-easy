package terraformingmadeeasy.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.listeners.BaseIndustryOptionProvider;
import com.fs.starfarer.api.campaign.listeners.DialogCreatorUI;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import terraformingmadeeasy.ids.TMEIds;
import terraformingmadeeasy.industries.ConstructionGrid;
import terraformingmadeeasy.industries.TMEBaseIndustry;
import terraformingmadeeasy.ui.dialogs.ConfirmDialogDelegate;
import terraformingmadeeasy.ui.dialogs.MegastructureDialogDelegate;
import terraformingmadeeasy.ui.dialogs.TerraformDialogDelegate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TMEIndustryOptionProvider extends BaseIndustryOptionProvider {
    public static List<String> tmeIndustries = new ArrayList<>();
    public static Object CUSTOM_PLUGIN = new Object();

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
        if (!listeners.hasListenerOfClass(TMEIndustryOptionProvider.class)) {
            listeners.addListener(new TMEIndustryOptionProvider(), true);
        }
    }

    public boolean isUnsuitable(Industry ind, boolean allowUnderConstruction) {
        return (super.isUnsuitable(ind, allowUnderConstruction) || !isSuitable(ind));
    }

    public boolean isSuitable(Industry ind) {
        boolean isTMEIndustry = tmeIndustries.contains(ind.getId());
        boolean isPlayerOwned = ind.getMarket().isPlayerOwned();
        return isTMEIndustry && isPlayerOwned;
    }

    public List<IndustryOptionData> getIndustryOptions(Industry ind) {
        if (isUnsuitable(ind, false)) {
            return null;
        }
        List<IndustryOptionData> result = new ArrayList<>();

        String isNotUpgradingText = "Terraform planet...";
        String isUpgradingText = "Cancel project...";
        if (Objects.equals(ind.getId(), TMEIds.CONSTRUCTION_GRID)) {
            isNotUpgradingText = "Build a megastructure...";
            isUpgradingText = "Cancel the megastructure project...";
        } else if (Objects.equals(ind.getId(), TMEIds.PLANETARY_HOLOGRAM)) {
            isNotUpgradingText = "Change planet visual...";
            isUpgradingText = "Cancel the planet visual change...";
        }

        String text = ind.isUpgrading() ? isUpgradingText : isNotUpgradingText;
        IndustryOptionData opt = new IndustryOptionData(text, CUSTOM_PLUGIN, ind, this);
        result.add(opt);

        return result;
    }

    public void createTooltip(IndustryOptionData opt, TooltipMakerAPI tooltip, float width) {
        String description = "Cancel the terraforming project for a %s refund.";
        String refundText = "A specialized industry capable of removing and adding hazard conditions of a planet.";
        float refundCost = 0;
        if (Objects.equals(opt.ind.getId(), TMEIds.CONSTRUCTION_GRID)) {
            description = "A large structural grid for constructing Megastructures.";
            refundText = "Cancel the Megastructure project for a %s refund.";
            if (opt.ind.isUpgrading()) {
                refundCost = ((ConstructionGrid) opt.ind).buildableMegastructure.cost;
            }
        } else if (Objects.equals(opt.ind.getId(), TMEIds.PLANETARY_HOLOGRAM)) {
            description = "A specialized structure that can change a planet's visual to a different planet type.";
            refundText = "Cancel the planet visual change for a %s refund.";
            if (opt.ind.isUpgrading()) {
                refundCost = ((TMEBaseIndustry) opt.ind).getModifiableCondition().cost;
            }
        } else {
            if (opt.ind.isUpgrading()) {
                refundCost = ((TMEBaseIndustry) opt.ind).getModifiableCondition().cost;
            }
        }

        if (opt.id == CUSTOM_PLUGIN) {
            String text = opt.ind.isUpgrading() ? refundText : description;
            tooltip.addPara(text, 0f, Misc.getHighlightColor(), Misc.getDGSCredits(refundCost));
        }
    }

    public void optionSelected(IndustryOptionData opt, DialogCreatorUI ui) {
        if (opt.id == CUSTOM_PLUGIN && Objects.equals(opt.ind.getId(), TMEIds.CONSTRUCTION_GRID)) {
            if (!opt.ind.isUpgrading()) {
                MegastructureDialogDelegate dialogueDelegate = new MegastructureDialogDelegate(800f, 464f, (ConstructionGrid) opt.ind);
                ui.showDialog(800f, 464f, dialogueDelegate);
            } else {
                ConfirmDialogDelegate dialogueDelegate = new ConfirmDialogDelegate(opt.ind, ((ConstructionGrid) opt.ind).buildableMegastructure.cost);
                ui.showDialog(ConfirmDialogDelegate.WIDTH, ConfirmDialogDelegate.HEIGHT, dialogueDelegate);
            }
        } else {
            if (!opt.ind.isUpgrading()) {
                TerraformDialogDelegate dialogueDelegate = new TerraformDialogDelegate(800f, 464f, opt.ind);
                ui.showDialog(TerraformDialogDelegate.WIDTH, TerraformDialogDelegate.HEIGHT, dialogueDelegate);
            } else {
                ConfirmDialogDelegate tmeConfirmDialogueDelegate = new ConfirmDialogDelegate(opt.ind, ((TMEBaseIndustry) opt.ind).getModifiableCondition().cost);
                ui.showDialog(ConfirmDialogDelegate.WIDTH, ConfirmDialogDelegate.HEIGHT, tmeConfirmDialogueDelegate);
            }
        }
    }
}
