package terraformingmadeeasy.listeners;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.listeners.BaseIndustryOptionProvider;
import com.fs.starfarer.api.campaign.listeners.DialogCreatorUI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import terraformingmadeeasy.ids.TMEIds;
import terraformingmadeeasy.industries.TerraformingIndustry;
import terraformingmadeeasy.ui.dialogs.ConfirmDialogDelegate;
import terraformingmadeeasy.ui.dialogs.TerraformingDialogDelegate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TerraformingIndustryOptionProvider extends BaseIndustryOptionProvider {
    public static final List<String> terraformingIndustryIds = new ArrayList<>();
    public static final Object CUSTOM_PLUGIN = new Object();

    static {
        terraformingIndustryIds.add(TMEIds.AGRICULTURAL_LABORATORY);
        terraformingIndustryIds.add(TMEIds.ATMOSPHERE_REGULATOR);
        terraformingIndustryIds.add(TMEIds.ELEMENT_SYNTHESIZER);
        terraformingIndustryIds.add(TMEIds.GEOMORPHOLOGY_STATION);
        terraformingIndustryIds.add(TMEIds.MINERAL_REPLICATOR);
        terraformingIndustryIds.add(TMEIds.PLANETARY_HOLOGRAM);
        terraformingIndustryIds.add(TMEIds.STELLAR_MANUFACTORY);
        terraformingIndustryIds.add(TMEIds.TERRESTRIAL_ENGINE);
        terraformingIndustryIds.add(TMEIds.UNIFICATION_CENTER);
    }

    @Override
    public List<IndustryOptionData> getIndustryOptions(Industry ind) {
        if (isUnsuitable(ind, false)) {
            return null;
        }
        List<IndustryOptionData> result = new ArrayList<>();

        String isNotUpgradingText = "Terraform planet...";
        String isUpgradingText = "Cancel terraforming project...";
        if (Objects.equals(ind.getId(), TMEIds.PLANETARY_HOLOGRAM)) {
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
        boolean isTMEIndustry = terraformingIndustryIds.contains(ind.getId());
        boolean isPlayerOwned = ind.getMarket().isPlayerOwned();
        return isTMEIndustry && isPlayerOwned;
    }

    @Override
    public void createTooltip(IndustryOptionData opt, TooltipMakerAPI tooltip, float width) {
        String description = "A specialized industry capable of removing and adding planetary conditions of a planet.";
        String refundText = "Cancel the terraforming project for a %s refund.";
        float refundCost = 0;
        if (Objects.equals(opt.ind.getId(), TMEIds.PLANETARY_HOLOGRAM)) {
            description = "A specialized structure that can change a planet's visual to a different planet type.";
            refundText = "Cancel the planet visual change for a %s refund.";
        }

        String text = description;
        if (opt.ind.isUpgrading()) {
            refundCost = ((TerraformingIndustry) opt.ind).getProject().getCost(opt.ind.getMarket());
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
                TerraformingDialogDelegate dialogDelegate = new TerraformingDialogDelegate((TerraformingIndustry) opt.ind);
                ui.showDialog(TerraformingDialogDelegate.WIDTH, TerraformingDialogDelegate.HEIGHT, dialogDelegate);
            } else {
                float totalCost = ((TerraformingIndustry) opt.ind).getProject().getCost(opt.ind.getMarket());
                ConfirmDialogDelegate dialogDelegate = new ConfirmDialogDelegate(opt.ind, totalCost);
                ui.showDialog(ConfirmDialogDelegate.WIDTH, ConfirmDialogDelegate.HEIGHT, dialogDelegate);
            }
        }
    }
}
