package terraformingmadeeasy.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.listeners.BaseIndustryOptionProvider;
import com.fs.starfarer.api.campaign.listeners.DialogCreatorUI;
import com.fs.starfarer.api.campaign.listeners.IndustryOptionProvider;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import terraformingmadeeasy.dialogue.TMEConfirmDialogueDelegate;
import terraformingmadeeasy.dialogue.TMEIndustryDialogueDelegate;
import terraformingmadeeasy.industry.TMEBaseIndustry;

import java.util.ArrayList;
import java.util.List;

public class TMEIndustryOptionProvider extends BaseIndustryOptionProvider {
    public static List<String> tmeIndustries = new ArrayList<>();

    static {
        tmeIndustries.add("TMEAtmosphereRegulators");
        tmeIndustries.add("TMEStellarFactoryRelays");
        tmeIndustries.add("TMETerrestrialCoreEngines");
        tmeIndustries.add("TMEScientificMilitaryBases");
    }

    public static Object CUSTOM_PLUGIN = new Object();

    public static void register() {
        ListenerManagerAPI listeners = Global.getSector().getListenerManager();
        if (!listeners.hasListenerOfClass(TMEIndustryOptionProvider.class))
            listeners.addListener(new TMEIndustryOptionProvider(), true);
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
            IndustryOptionProvider.IndustryOptionData opt = new IndustryOptionProvider.IndustryOptionData(
                    "Terraform planet...", CUSTOM_PLUGIN, ind, this);
            result.add(opt);
        } else {
            IndustryOptionProvider.IndustryOptionData opt = new IndustryOptionProvider.IndustryOptionData(
                    "Cancel project...", CUSTOM_PLUGIN, ind, this);
            result.add(opt);
        }

        return result;
    }

    public void createTooltip(IndustryOptionProvider.IndustryOptionData opt, TooltipMakerAPI tooltip, float width) {
        if (opt.id == CUSTOM_PLUGIN && !opt.ind.isUpgrading()) {
            tooltip.addPara("A specialized industry capable of removing and adding hazard conditions of a planet.", 0f);
        } else if (opt.id == CUSTOM_PLUGIN && opt.ind.isUpgrading()) {
            tooltip.addPara("Cancel the terraforming project for a %s refund.", 0f, Misc.getHighlightColor(),
                    Misc.getDGSCredits(((TMEBaseIndustry) opt.ind).modifiableCondition.cost));
        }
    }

    public void optionSelected(IndustryOptionProvider.IndustryOptionData opt, DialogCreatorUI ui) {
        if (opt.id == CUSTOM_PLUGIN && !opt.ind.isUpgrading()) {
            TMEIndustryDialogueDelegate tmeIndustryDialogueDelegate = new TMEIndustryDialogueDelegate(opt.ind);
            ui.showDialog(TMEIndustryDialogueDelegate.WIDTH, TMEIndustryDialogueDelegate.HEIGHT, tmeIndustryDialogueDelegate);
        } else if (opt.id == CUSTOM_PLUGIN && opt.ind.isUpgrading()) {
            TMEConfirmDialogueDelegate tmeConfirmDialgueDelegate = new TMEConfirmDialogueDelegate(opt.ind);
            ui.showDialog(TMEConfirmDialogueDelegate.WIDTH, TMEConfirmDialogueDelegate.HEIGHT, tmeConfirmDialgueDelegate);
        }
    }
}
