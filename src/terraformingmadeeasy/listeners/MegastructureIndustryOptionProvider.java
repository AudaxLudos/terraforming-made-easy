package terraformingmadeeasy.listeners;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.listeners.BaseIndustryOptionProvider;
import com.fs.starfarer.api.campaign.listeners.DialogCreatorUI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import terraformingmadeeasy.Settings;
import terraformingmadeeasy.ids.TMEIds;
import terraformingmadeeasy.industries.MegastructureIndustry;
import terraformingmadeeasy.loading.specs.MegastructureProjectSpec;
import terraformingmadeeasy.ui.dialogs.ConfirmDialogDelegate;
import terraformingmadeeasy.ui.dialogs.MegastructureDialogDelegate;

import java.util.ArrayList;
import java.util.List;

public class MegastructureIndustryOptionProvider extends BaseIndustryOptionProvider {
    public static final Object CUSTOM_PLUGIN = new Object();

    @Override
    public List<IndustryOptionData> getIndustryOptions(Industry ind) {
        if (isUnsuitable(ind, false)) {
            return null;
        }
        List<IndustryOptionData> result = new ArrayList<>();

        String isNotUpgradingText = "Build megastructure...";
        String isUpgradingText = "Cancel megastructure project...";

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
        boolean isTMEIndustry = TMEIds.CONSTRUCTION_GRID.equals(ind.getId());
        boolean isPlayerOwned = ind.getMarket().isPlayerOwned();
        return isTMEIndustry && isPlayerOwned;
    }

    @Override
    public void createTooltip(IndustryOptionData opt, TooltipMakerAPI tooltip, float width) {
        String description = "A large structural grid for constructing Megastructures.";
        String refundText = "Cancel the Megastructure project for a %s refund.";
        float refundCost = 0;

        String text = description;
        if (opt.ind.isUpgrading()) {
            MegastructureProjectSpec project = ((MegastructureIndustry) opt.ind).getProject();
            refundCost = Math.round(project.getCost() * Settings.BUILD_COST_MULTIPLIER);
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
                MegastructureDialogDelegate dialogDelegate = new MegastructureDialogDelegate((MegastructureIndustry) opt.ind);
                ui.showDialog(MegastructureDialogDelegate.WIDTH, MegastructureDialogDelegate.HEIGHT, dialogDelegate);
            } else {
                MegastructureProjectSpec project = ((MegastructureIndustry) opt.ind).getProject();
                float totalCost = Math.round(project.getCost() * Settings.BUILD_COST_MULTIPLIER);

                ConfirmDialogDelegate dialogDelegate = new ConfirmDialogDelegate(opt.ind, totalCost);
                ui.showDialog(ConfirmDialogDelegate.WIDTH, ConfirmDialogDelegate.HEIGHT, dialogDelegate);
            }
        }
    }
}
