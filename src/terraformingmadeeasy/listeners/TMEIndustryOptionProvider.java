package terraformingmadeeasy.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomDialogDelegate;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.listeners.BaseIndustryOptionProvider;
import com.fs.starfarer.api.campaign.listeners.DialogCreatorUI;
import com.fs.starfarer.api.campaign.listeners.IndustryOptionProvider;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import terraformingmadeeasy.dialogue.TMEIndustryDialogueDelegate;
import terraformingmadeeasy.industry.TMEBaseIndustry;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TMEIndustryOptionProvider extends BaseIndustryOptionProvider {
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
        List<String> tmeIndustries = new ArrayList<>();
        tmeIndustries.add("TMEAtmosphereRegulators");
        tmeIndustries.add("TMEStellarFactoryRelays");
        tmeIndustries.add("TMETerrestrialCoreEngine");
        tmeIndustries.add("TMEScientificMilitaryBases");
        boolean isTMEIndustry = tmeIndustries.contains(ind.getId());
        boolean isPlayerOwned = ind.getMarket().isPlayerOwned();
        return isTMEIndustry && isPlayerOwned;
    }

    public List<IndustryOptionData> getIndustryOptions(Industry ind) {
        if (isUnsuitable(ind, false))
            return null;
        List<IndustryOptionData> result = new ArrayList<>();
        IndustryOptionProvider.IndustryOptionData opt = new IndustryOptionProvider.IndustryOptionData(
                "Choose terraforming options", CUSTOM_PLUGIN, ind, (IndustryOptionProvider) this);
        result.add(opt);
        return result;
    }

    public void createTooltip(IndustryOptionProvider.IndustryOptionData opt, TooltipMakerAPI tooltip, float width) {
        if (opt.id == CUSTOM_PLUGIN) {
                tooltip.addPara("A specialized industry capable of removing and adding hazard conditions of a planet.", 0f);
        }
    }

    public void optionSelected(IndustryOptionProvider.IndustryOptionData opt, DialogCreatorUI ui) {
        if (opt.id == CUSTOM_PLUGIN) {
            TMEIndustryDialogueDelegate tmeIndustryDialogueDelegate = new TMEIndustryDialogueDelegate(opt.ind);
            ui.showDialog(TMEIndustryDialogueDelegate.WIDTH, TMEIndustryDialogueDelegate.HEIGHT, (CustomDialogDelegate) tmeIndustryDialogueDelegate);
        }
    }
}
