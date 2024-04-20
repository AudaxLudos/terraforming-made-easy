package terraformingmadeeasy.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.listeners.BaseIndustryOptionProvider;
import com.fs.starfarer.api.campaign.listeners.DialogCreatorUI;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import terraformingmadeeasy.ids.TMEIndustries;
import terraformingmadeeasy.industries.ConstructionGrid;
import terraformingmadeeasy.ui.dialogs.ConfirmDialogDelegate;
import terraformingmadeeasy.ui.dialogs.MegastructureDialogDelegate;

import java.util.ArrayList;
import java.util.List;

public class MegastructureOptionProvider extends BaseIndustryOptionProvider {
    public static List<String> tmeIndustries = new ArrayList<>();
    public static Object CUSTOM_PLUGIN = new Object();

    static {
        tmeIndustries.add(TMEIndustries.CONSTRUCTION_GRID);
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
            IndustryOptionData opt = new IndustryOptionData("Build a Megastructure...", CUSTOM_PLUGIN, ind, this);
            result.add(opt);
        } else {
            IndustryOptionData opt = new IndustryOptionData("Cancel the Megastructure project...", CUSTOM_PLUGIN, ind, this);
            result.add(opt);
        }

        return result;
    }

    public void createTooltip(IndustryOptionData opt, TooltipMakerAPI tooltip, float width) {
        if (opt.id == CUSTOM_PLUGIN && !opt.ind.isUpgrading()) {
            tooltip.addPara("A large structural grid for constructing Megastructures.", 0f);
        } else if (opt.id == CUSTOM_PLUGIN && opt.ind.isUpgrading()) {
            tooltip.addPara("Cancel the Megastructure project for a %s refund.", 0f, Misc.getHighlightColor(),
                    Misc.getDGSCredits(((ConstructionGrid) opt.ind).buildableMegastructure.cost));
        }
    }

    public void optionSelected(IndustryOptionData opt, DialogCreatorUI ui) {
        if (opt.id == CUSTOM_PLUGIN && !opt.ind.isUpgrading()) {
            MegastructureDialogDelegate dialogueDelegate = new MegastructureDialogDelegate(800f, 464f, (ConstructionGrid) opt.ind);
            ui.showDialog(800f, 464f, dialogueDelegate);
        } else if (opt.id == CUSTOM_PLUGIN && opt.ind.isUpgrading()) {
            ConfirmDialogDelegate dialogueDelegate = new ConfirmDialogDelegate(opt.ind, ((ConstructionGrid) opt.ind).buildableMegastructure.cost);
            ui.showDialog(ConfirmDialogDelegate.WIDTH, ConfirmDialogDelegate.HEIGHT, dialogueDelegate);
        }
    }
}
