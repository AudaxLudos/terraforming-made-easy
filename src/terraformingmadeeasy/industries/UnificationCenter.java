package terraformingmadeeasy.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.ids.TMEIds;

import java.awt.*;
import java.util.List;
import java.util.*;

public class UnificationCenter extends TMEBaseIndustry {
    public UnificationCenter() {
        setModifiableConditions(Utils.UNIFICATION_CENTER_OPTIONS);

        if (Utils.isAOTDVOKEnabled()) {
            List<Utils.ModifiableCondition> modifiableConditionsCopy = new ArrayList<>(getModifiableConditions());

            for (Utils.ModifiableCondition condition : modifiableConditionsCopy) {
                StringBuilder needOne = new StringBuilder();
                StringBuilder needAll = new StringBuilder("needAll:");
                String[] ids = getUniqueIds(condition.likedIndustries);

                for (String id : ids) {
                    switch (id) {
                        case Industries.MINING: {
                            String newExpression = String.format(
                                    "needOne:%s|%s|%s, ",
                                    "mining",
                                    "fracking",
                                    "mining_megaplex");
                            needOne.append(newExpression);
                            break;
                        }
                        case Industries.REFINING: {
                            String newExpression = String.format(
                                    "needOne:%s|%s|%s|%s|%s, ",
                                    "refining",
                                    "crystalizator",
                                    "isotope_separator",
                                    "policrystalizator",
                                    "cascade_reprocesor");
                            needOne.append(newExpression);
                            break;
                        }
                        case Industries.LIGHTINDUSTRY: {
                            String newExpression = String.format(
                                    "needOne:%s|%s|%s|%s, ",
                                    "lightindustry",
                                    "hightech",
                                    "druglight",
                                    "consumerindustry");
                            needOne.append(newExpression);
                            break;
                        }
                        case Industries.ORBITALWORKS:
                        case Industries.HEAVYINDUSTRY: {
                            String newExpression = String.format(
                                    "needOne:%s|%s|%s|%s|%s|%s|%s, ",
                                    "orbitalworks",
                                    "supplyheavy",
                                    "weaponheavy",
                                    "triheavy",
                                    "hegeheavy",
                                    "orbitalheavy",
                                    "stella_manufactorium");
                            needOne.append(newExpression);
                            break;
                        }
                        case Industries.FARMING: {
                            String newExpression = String.format(
                                    "needOne:%s|%s|%s, ",
                                    "farming",
                                    "artifarming",
                                    "subfarming");
                            needOne.append(newExpression);
                            break;
                        }
                        case Industries.FUELPROD: {
                            String newExpression = String.format(
                                    "needOne:%s|%s, ",
                                    "fuelprod",
                                    "blast_processing");
                            needOne.append(newExpression);
                            break;
                        }
                        case Industries.HIGHCOMMAND:
                        case Industries.COMMERCE:
                        case Industries.MEGAPORT: {
                            needAll.append(id).append("|");
                        }
                    }
                }

                condition.likedIndustries = needOne.append(needAll.toString().replaceFirst(".$", "")).toString();
            }
            setModifiableConditions(modifiableConditionsCopy);
        }
    }

    @Override
    public void apply() {
        super.apply();

        modifyStabilityWithBaseMod();
        int size = this.market.getSize();
        demand(Commodities.MARINES, size);
        demand(Commodities.SUPPLIES, size);
    }

    @Override
    public void unapply() {
        super.unapply();

        unmodifyStabilityWithBaseMod();
    }

    @Override
    protected void addAlphaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        float oPad = 10f;
        Color highlight = Misc.getHighlightColor();

        String pre = "Alpha-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = "Alpha-level AI core. ";
        }
        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP || mode == AICoreDescriptionMode.MANAGE_CORE_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48);
            text.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. Reduces terraforming time by %s.", oPad, highlight,
                    (int) ((1f - UPKEEP_MULT) * 100f) + "%", "" + DEMAND_REDUCTION, (int) (ALPHA_BUILD_TIME_MULT * 100f) + "%");
            tooltip.addImageWithText(oPad);
            return;
        }

        tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. Reduces terraforming time by %s.", oPad, highlight,
                (int) ((1f - UPKEEP_MULT) * 100f) + "%", "" + DEMAND_REDUCTION, (int) (ALPHA_BUILD_TIME_MULT * 100f) + "%");
    }

    @Override
    protected void addBetaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        float oPad = 10f;
        Color highlight = Misc.getHighlightColor();

        String pre = "Beta-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = "Beta-level AI core. ";
        }
        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP || mode == AICoreDescriptionMode.MANAGE_CORE_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48);
            text.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. Reduces terraforming time by %s.", oPad, highlight,
                    (int) ((1f - UPKEEP_MULT) * 100f) + "%", "" + DEMAND_REDUCTION, (int) (BETA_BUILD_TIME_MULT * 100f) + "%");
            tooltip.addImageWithText(oPad);
            return;
        }

        tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. Reduces terraforming time by %s.", oPad, highlight,
                (int) ((1f - UPKEEP_MULT) * 100f) + "%", "" + DEMAND_REDUCTION, (int) (BETA_BUILD_TIME_MULT * 100f) + "%");
    }

    @Override
    protected void addGammaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        float oPad = 10f;
        Color highlight = Misc.getHighlightColor();

        String pre = "Gamma-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = "Gamma-level AI core. ";
        }
        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP || mode == AICoreDescriptionMode.MANAGE_CORE_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48);
            text.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. Reduces terraforming time by %s.", oPad, highlight,
                    (int) ((1f - UPKEEP_MULT) * 100f) + "%", "" + DEMAND_REDUCTION, (int) (GAMMA_BUILD_TIME_MULT * 100f) + "%");
            tooltip.addImageWithText(oPad);
            return;
        }

        tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. Reduces terraforming time by %s.", oPad, highlight,
                (int) ((1f - UPKEEP_MULT) * 100f) + "%", "" + DEMAND_REDUCTION, (int) (GAMMA_BUILD_TIME_MULT * 100f) + "%");
    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
        addStabilityPostDemandSection(tooltip, hasDemand, mode);
    }

    @Override
    protected int getBaseStabilityMod() {
        return 1;
    }

    @Override
    public String getAOTDVOKTechId() {
        return TMEIds.UNIFICATION_CENTER_TECH;
    }

    protected String[] getUniqueIds(String text) {
        StringBuilder tempText = new StringBuilder();
        String[] expressions = text.replaceAll(" ", "").split(",");

        for (String s : expressions) {
            if (s.contains("needAll")) {
                tempText.append(s.replaceAll("needAll:", ""));
            } else if (s.contains("needOne")) {
                tempText.append(s.replaceAll("needOne:", ""));
            }
            tempText.append("|");
        }

        String[] ids = tempText.toString().split("\\|");
        Set<String> setIds = new HashSet<>(Arrays.asList(ids));
        return setIds.toArray(new String[0]);
    }
}
