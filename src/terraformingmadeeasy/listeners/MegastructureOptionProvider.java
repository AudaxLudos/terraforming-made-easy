package terraformingmadeeasy.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.listeners.BaseIndustryOptionProvider;
import com.fs.starfarer.api.campaign.listeners.DialogCreatorUI;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import terraformingmadeeasy.dialogue.ConfirmDialogueDelegate;
import terraformingmadeeasy.dialogue.MegastructureDialogueDelegate;
import terraformingmadeeasy.industry.BaseIndustry;
import terraformingmadeeasy.industry.ConstructionGrid;

import java.util.ArrayList;
import java.util.List;

public class MegastructureOptionProvider extends BaseIndustryOptionProvider {
    public static List<String> tmeIndustries = new ArrayList<>();
    public static Object CUSTOM_PLUGIN = new Object();

    static {
        tmeIndustries.add("tme_construction_grid");
    }

    public static void register() {
        ListenerManagerAPI listeners = Global.getSector().getListenerManager();
        if (!listeners.hasListenerOfClass(MegastructureOptionProvider.class))
            listeners.addListener(new MegastructureOptionProvider(), true);
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
                    "Build a Megastructure...", CUSTOM_PLUGIN, ind, this);
            result.add(opt);
        } else {
            com.fs.starfarer.api.campaign.listeners.IndustryOptionProvider.IndustryOptionData opt = new com.fs.starfarer.api.campaign.listeners.IndustryOptionProvider.IndustryOptionData(
                    "Cancel the Megastructure project...", CUSTOM_PLUGIN, ind, this);
            result.add(opt);
        }

        return result;
    }

    public void createTooltip(com.fs.starfarer.api.campaign.listeners.IndustryOptionProvider.IndustryOptionData opt, TooltipMakerAPI tooltip, float width) {
        if (opt.id == CUSTOM_PLUGIN && !opt.ind.isUpgrading()) {
            tooltip.addPara("A large structural grid for constructing Megastructures.", 0f);
        } else if (opt.id == CUSTOM_PLUGIN && opt.ind.isUpgrading()) {
            tooltip.addPara("Cancel the Megastructure project for a %s refund.", 0f, Misc.getHighlightColor(),
                    Misc.getDGSCredits(((BaseIndustry) opt.ind).modifiableCondition.cost));
        }
    }

    public void optionSelected(com.fs.starfarer.api.campaign.listeners.IndustryOptionProvider.IndustryOptionData opt, DialogCreatorUI ui) {
        if (opt.id == CUSTOM_PLUGIN && !opt.ind.isUpgrading()) {
            MegastructureDialogueDelegate dialogueDelegate = new MegastructureDialogueDelegate((ConstructionGrid) opt.ind);
            ui.showDialog(800f, 400f, dialogueDelegate);
        } else if (opt.id == CUSTOM_PLUGIN && opt.ind.isUpgrading()) {
            ConfirmDialogueDelegate tmeConfirmDialogueDelegate = new ConfirmDialogueDelegate(opt.ind);
            ui.showDialog(ConfirmDialogueDelegate.WIDTH, ConfirmDialogueDelegate.HEIGHT, tmeConfirmDialogueDelegate);
        }
    }
}
