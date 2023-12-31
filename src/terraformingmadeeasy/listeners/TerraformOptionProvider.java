package terraformingmadeeasy.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.listeners.BaseIndustryOptionProvider;
import com.fs.starfarer.api.campaign.listeners.DialogCreatorUI;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import terraformingmadeeasy.dialogue.ConfirmDialogueDelegate;
import terraformingmadeeasy.dialogue.TerraformDialogueDelegate;
import terraformingmadeeasy.industry.BaseIndustry;

import java.util.ArrayList;
import java.util.List;

public class TerraformOptionProvider extends BaseIndustryOptionProvider {
    public static List<String> tmeIndustries = new ArrayList<>();

    static {
        tmeIndustries.add("tme_agricultural_laboratory");
        tmeIndustries.add("tme_atmosphere_regulator");
        tmeIndustries.add("tme_element_synthesizer");
        tmeIndustries.add("tme_geomorphology_station");
        tmeIndustries.add("tme_mineral_replicator");
        tmeIndustries.add("tme_stellar_manufactory");
        tmeIndustries.add("tme_terrestrial_engine");
    }

    public static Object CUSTOM_PLUGIN = new Object();

    public static void register() {
        ListenerManagerAPI listeners = Global.getSector().getListenerManager();
        if (!listeners.hasListenerOfClass(TerraformOptionProvider.class))
            listeners.addListener(new TerraformOptionProvider(), true);
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
        if (isUnsuitable(ind, false))
            return null;
        List<IndustryOptionData> result = new ArrayList<>();

        if (!ind.isUpgrading()) {
            com.fs.starfarer.api.campaign.listeners.IndustryOptionProvider.IndustryOptionData opt = new com.fs.starfarer.api.campaign.listeners.IndustryOptionProvider.IndustryOptionData(
                    "Terraform planet...", CUSTOM_PLUGIN, ind, this);
            result.add(opt);
        } else {
            com.fs.starfarer.api.campaign.listeners.IndustryOptionProvider.IndustryOptionData opt = new com.fs.starfarer.api.campaign.listeners.IndustryOptionProvider.IndustryOptionData(
                    "Cancel project...", CUSTOM_PLUGIN, ind, this);
            result.add(opt);
        }

        return result;
    }

    public void createTooltip(com.fs.starfarer.api.campaign.listeners.IndustryOptionProvider.IndustryOptionData opt, TooltipMakerAPI tooltip, float width) {
        if (opt.id == CUSTOM_PLUGIN && !opt.ind.isUpgrading()) {
            tooltip.addPara("A specialized industry capable of removing and adding hazard conditions of a planet.", 0f);
        } else if (opt.id == CUSTOM_PLUGIN && opt.ind.isUpgrading()) {
            tooltip.addPara("Cancel the terraforming project for a %s refund.", 0f, Misc.getHighlightColor(),
                    Misc.getDGSCredits(((BaseIndustry) opt.ind).modifiableCondition.cost));
        }
    }

    public void optionSelected(com.fs.starfarer.api.campaign.listeners.IndustryOptionProvider.IndustryOptionData opt, DialogCreatorUI ui) {
        if (opt.id == CUSTOM_PLUGIN && !opt.ind.isUpgrading()) {
            TerraformDialogueDelegate tmeIndustryDialogueDelegate = new TerraformDialogueDelegate(opt.ind);
            ui.showDialog(TerraformDialogueDelegate.WIDTH, TerraformDialogueDelegate.HEIGHT, tmeIndustryDialogueDelegate);
        } else if (opt.id == CUSTOM_PLUGIN && opt.ind.isUpgrading()) {
            ConfirmDialogueDelegate tmeConfirmDialogueDelegate = new ConfirmDialogueDelegate(opt.ind);
            ui.showDialog(ConfirmDialogueDelegate.WIDTH, ConfirmDialogueDelegate.HEIGHT, tmeConfirmDialogueDelegate);
        }
    }
}
