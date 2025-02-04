package terraformingmadeeasy.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.PlanetSpecAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.campaign.listeners.ListenerUtil;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.loading.IndustrySpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.IconRenderMode;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.kaysaar.aotd.vok.scripts.research.AoTDMainResearchManager;
import org.apache.log4j.Logger;
import terraformingmadeeasy.Utils;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TMEBaseIndustry extends BaseIndustry {
    public static final Logger log = Global.getLogger(TMEBaseIndustry.class);
    public static final float GAMMA_BUILD_TIME_MULT = 0.20f;
    public static final float BETA_BUILD_TIME_MULT = 0.30f;
    public static final float ALPHA_BUILD_TIME_MULT = 0.50f;
    protected List<Utils.ModifiableCondition> modifiableConditions = null;
    protected Utils.ModifiableCondition modifiableCondition = null;
    protected Boolean isAICoreBuildTimeMultApplied = false;
    protected float aiCoreBuildProgressRemoved = 0f;
    protected String prevAICoreId = null;

    @Override
    public void apply() {
        super.apply(true);
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);

        if (!this.isAICoreBuildTimeMultApplied) {
            float aiCoreCurrentBuildTimeMult = 0f;
            if (Objects.equals(this.aiCoreId, Commodities.ALPHA_CORE)) {
                aiCoreCurrentBuildTimeMult = ALPHA_BUILD_TIME_MULT;
            } else if (Objects.equals(this.aiCoreId, Commodities.BETA_CORE)) {
                aiCoreCurrentBuildTimeMult = BETA_BUILD_TIME_MULT;
            } else if (Objects.equals(this.aiCoreId, Commodities.GAMMA_CORE)) {
                aiCoreCurrentBuildTimeMult = GAMMA_BUILD_TIME_MULT;
            }

            float daysLeft = this.buildTime - this.buildProgress;
            this.aiCoreBuildProgressRemoved = daysLeft * aiCoreCurrentBuildTimeMult;
            this.buildProgress = this.buildTime - (daysLeft - this.aiCoreBuildProgressRemoved);
            this.isAICoreBuildTimeMultApplied = true;
            this.prevAICoreId = getAICoreId();
        }

        if (this.isAICoreBuildTimeMultApplied && !Objects.equals(getAICoreId(), this.prevAICoreId)) {
            this.buildProgress = this.buildProgress - this.aiCoreBuildProgressRemoved;
            this.aiCoreBuildProgressRemoved = 0f;
            this.isAICoreBuildTimeMultApplied = false;
        }
    }

    @Override
    public boolean isUpgrading() {
        return this.building && this.modifiableCondition != null;
    }

    @Override
    public String getBuildOrUpgradeProgressText() {
        if (isDisrupted()) {
            int left = (int) getDisruptedDays();
            if (left < 1) {
                left = 1;
            }
            String days = "days";
            if (left == 1) {
                days = "day";
            }

            return "Disrupted: " + left + " " + days + " left";
        }

        int left = (int) (this.buildTime - this.buildProgress);
        if (left < 1) {
            left = 1;
        }
        String days = "days";
        if (left == 1) {
            days = "day";
        }

        if (isUpgrading()) {
            return "Terraforming: " + left + " " + days + " left";
        } else {
            return "Building: " + left + " " + days + " left";
        }
    }

    @Override
    public void finishBuildingOrUpgrading() {
        this.building = false;
        this.buildProgress = 0;
        this.buildTime = 1f;
        this.aiCoreBuildProgressRemoved = 0f;
        this.isAICoreBuildTimeMultApplied = false;
        if (this.modifiableCondition != null) {
            log.info(String.format("Completed %s %s condition in %s by %s", !this.market.hasCondition(this.modifiableCondition.id) ? "Adding" : "Removing", this.modifiableCondition.name, getMarket().getName(), getCurrentName()));
            sendCompletedMessage();
            changePlanetConditions();
            changePlanetClass();
            reapply();
            // Force reapply demands and supply
            for (Industry ind : this.market.getIndustries()) {
                ind.doPreSaveCleanup();
                ind.doPostSaveRestore();
            }
            this.modifiableCondition = null;
        } else {
            buildingFinished();
            reapply();
        }
    }

    @Override
    public void startUpgrading() {
        // Will be called from TerraformDialogDelegate to start terraforming
        if (this.modifiableCondition != null) {
            log.info(String.format("Started %s %s condition in %s by %s", !this.market.hasCondition(this.modifiableCondition.id) ? "Adding" : "Removing", this.modifiableCondition.name, getMarket().getName(), getCurrentName()));
            this.building = true;
            this.buildProgress = 0;
            this.aiCoreBuildProgressRemoved = 0f;
            this.isAICoreBuildTimeMultApplied = false;
            this.buildTime = this.modifiableCondition.buildTime;
        }
    }

    @Override
    public void cancelUpgrade() {
        // Will be called from ConfirmDialogDelegate to cancel terraforming
        log.info(String.format("Cancelled %s %s condition in %s by %s", !this.market.hasCondition(this.modifiableCondition.id) ? "Adding" : "Removing", this.modifiableCondition.name, getMarket().getName(), getCurrentName()));
        this.building = false;
        this.buildProgress = 0;
        this.aiCoreBuildProgressRemoved = 0f;
        this.isAICoreBuildTimeMultApplied = false;
        this.modifiableCondition = null;
    }

    @Override
    public boolean isAvailableToBuild() {
        if (Utils.isAOTDVOKEnabled()) {
            return AoTDMainResearchManager.getInstance().isAvailableForThisMarket(getAOTDVOKTechId(), this.market) && this.market.getPlanetEntity() != null && super.isAvailableToBuild();
        }
        return this.market.getPlanetEntity() != null && super.isAvailableToBuild();
    }

    @Override
    public String getUnavailableReason() {
        if (!super.isAvailableToBuild()) {
            return super.getUnavailableReason();
        }
        return "Requires a planet";
    }

    @Override
    public boolean showWhenUnavailable() {
        if (Utils.isAOTDVOKEnabled()) {
            return AoTDMainResearchManager.getInstance().isAvailableForThisMarket(getAOTDVOKTechId(), this.market) && this.market.getPlanetEntity() != null && super.showWhenUnavailable();
        }
        return super.showWhenUnavailable();
    }

    @Override
    public boolean isTooltipExpandable() {
        return true;
    }

    @Override
    public void createTooltip(IndustryTooltipMode mode, TooltipMakerAPI tooltip, boolean expanded) {
        this.currTooltipMode = mode;
        float pad = 3f;
        float oPad = 10f;
        FactionAPI faction = this.market.getFaction();
        Color color = faction.getBaseUIColor();
        Color dark = faction.getDarkUIColor();
        Color gray = Misc.getGrayColor();
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        MarketAPI copy = this.market.clone();
        copy.setSuppressedConditions(this.market.getSuppressedConditions());
        copy.setRetainSuppressedConditionsSetWhenEmpty(true);
        this.market.setRetainSuppressedConditionsSetWhenEmpty(true);
        MarketAPI orig = this.market;
        this.market = copy;
        boolean needToAddIndustry = !this.market.hasIndustry(getId());

        if (needToAddIndustry) {
            this.market.getIndustries().add(this);
        }

        if (mode != IndustryTooltipMode.NORMAL) {
            this.market.clearCommodities();
            for (CommodityOnMarketAPI curr : this.market.getAllCommodities()) {
                curr.getAvailableStat().setBaseValue(100);
            }
        }

        this.market.reapplyConditions();
        reapply();

        String type = "";
        if (isIndustry()) type = " - Industry";
        if (isStructure()) type = " - Structure";

        tooltip.addTitle(getCurrentName() + type, color);

        String desc = this.spec.getDesc();
        String override = getDescriptionOverride();
        if (override != null) {
            desc = override;
        }
        desc = Global.getSector().getRules().performTokenReplacement(null, desc, this.market.getPrimaryEntity(), null);

        tooltip.addPara(desc, oPad);

        if (isIndustry() && (mode == IndustryTooltipMode.ADD_INDUSTRY || mode == IndustryTooltipMode.UPGRADE || mode == IndustryTooltipMode.DOWNGRADE)) {
            int num = Misc.getNumIndustries(this.market);
            int max = Misc.getMaxIndustries(this.market);

            if (isIndustry()) {
                if (mode == IndustryTooltipMode.UPGRADE) {
                    for (Industry curr : this.market.getIndustries()) {
                        if (getSpec().getId().equals(curr.getSpec().getUpgrade())) {
                            if (curr.isIndustry()) {
                                num--;
                            }
                            break;
                        }
                    }
                } else if (mode == IndustryTooltipMode.DOWNGRADE) {
                    for (Industry curr : this.market.getIndustries()) {
                        if (getSpec().getId().equals(curr.getSpec().getDowngrade())) {
                            if (curr.isIndustry()) {
                                num--;
                            }
                            break;
                        }
                    }
                }
            }

            if (num > max) {
                tooltip.addPara("Maximum number of industries reached", bad, oPad);
            }
        }

        addRightAfterDescriptionSection(tooltip, mode);
        addTerraformingOptionList(tooltip, mode, expanded);

        if (isDisrupted()) {
            int left = (int) getDisruptedDays();
            if (left < 1) left = 1;
            String days = "days";
            if (left == 1) days = "day";

            tooltip.addPara("Operations disrupted! %s " + days + " until return to normal function.",
                    oPad, Misc.getNegativeHighlightColor(), highlight, "" + left);
        }

        if (DebugFlags.COLONY_DEBUG || this.market.isPlayerOwned()) {
            if (mode == IndustryTooltipMode.NORMAL) {
                if (getSpec().getUpgrade() != null && !isBuilding()) {
                    tooltip.addPara("Click to manage or upgrade", Misc.getPositiveHighlightColor(), oPad);
                } else {
                    tooltip.addPara("Click to manage", Misc.getPositiveHighlightColor(), oPad);
                }
            }
        }

        if (mode == IndustryTooltipMode.QUEUED) {
            tooltip.addPara("Click to remove or adjust position in queue", Misc.getPositiveHighlightColor(), oPad);
            tooltip.addPara("Currently queued for construction. Does not have any impact on the colony.", oPad);

            int left = (int) (getSpec().getBuildTime());
            if (left < 1) {
                left = 1;
            }

            String days = "days";
            if (left == 1) {
                days = "day";
            }

            tooltip.addPara("Requires %s " + days + " to build.", oPad, highlight, "" + left);
        } else if (!isFunctional() && mode == IndustryTooltipMode.NORMAL && !isDisrupted()) {
            tooltip.addPara("Currently under construction and not producing anything or providing other benefits.", oPad);

            int left = (int) (this.buildTime - this.buildProgress);
            if (left < 1) {
                left = 1;
            }

            String days = "days";
            if (left == 1) {
                days = "day";
            }

            tooltip.addPara("Requires %s more " + days + " to finish building.", oPad, highlight, "" + left);
        }

        if (!isAvailableToBuild() &&
                (mode == IndustryTooltipMode.ADD_INDUSTRY ||
                        mode == IndustryTooltipMode.UPGRADE ||
                        mode == IndustryTooltipMode.DOWNGRADE)) {
            String reason = getUnavailableReason();
            if (reason != null) {
                tooltip.addPara(reason, bad, oPad);
            }
        }

        boolean category = getSpec().hasTag(Industries.TAG_PARENT);
        if (!category) {
            int credits = (int) Global.getSector().getPlayerFleet().getCargo().getCredits().get();
            String creditsStr = Misc.getDGSCredits(credits);
            if (mode == IndustryTooltipMode.UPGRADE || mode == IndustryTooltipMode.ADD_INDUSTRY) {
                int cost = (int) getBuildCost();
                String costStr = Misc.getDGSCredits(cost);

                int days = (int) getBuildTime();
                String daysStr = "days";
                if (days == 1) {
                    daysStr = "day";
                }

                LabelAPI label;
                if (mode == IndustryTooltipMode.UPGRADE) {
                    label = tooltip.addPara("%s and %s " + daysStr + " to upgrade. You have %s.", oPad, highlight, costStr, "" + days, creditsStr);
                } else {
                    label = tooltip.addPara("%s and %s " + daysStr + " to build. You have %s.", oPad, highlight, costStr, "" + days, creditsStr);
                }
                label.setHighlight(costStr, "" + days, creditsStr);
                if (credits >= cost) {
                    label.setHighlightColors(highlight, highlight, highlight);
                } else {
                    label.setHighlightColors(bad, highlight, highlight);
                }
            } else if (mode == IndustryTooltipMode.DOWNGRADE) {
                if (getSpec().getUpgrade() != null) {
                    float refundFraction = Global.getSettings().getFloat("industryRefundFraction");
                    IndustrySpecAPI spec = Global.getSettings().getIndustrySpec(getSpec().getUpgrade());
                    int cost = (int) (spec.getCost() * refundFraction);
                    String refundStr = Misc.getDGSCredits(cost);

                    tooltip.addPara("%s refunded for downgrade.", oPad, highlight, refundStr);
                }
            }

            addPostDescriptionSection(tooltip, mode);

            if (!getIncome().isUnmodified()) {
                int income = getIncome().getModifiedInt();
                tooltip.addPara("Monthly income: %s", oPad, highlight, Misc.getDGSCredits(income));
                tooltip.addStatModGrid(300, 65, 10, pad, getIncome(), true, new TooltipMakerAPI.StatModValueGetter() {
                    public String getPercentValue(MutableStat.StatMod mod) {
                        return null;
                    }

                    public String getMultValue(MutableStat.StatMod mod) {
                        return null;
                    }

                    public Color getModColor(MutableStat.StatMod mod) {
                        return null;
                    }

                    public String getFlatValue(MutableStat.StatMod mod) {
                        return Misc.getWithDGS(mod.value) + Strings.C;
                    }
                });
            }

            if (!getUpkeep().isUnmodified()) {
                int upkeep = getUpkeep().getModifiedInt();
                tooltip.addPara("Monthly upkeep: %s", oPad, highlight, Misc.getDGSCredits(upkeep));
                tooltip.addStatModGrid(300, 65, 10, pad, getUpkeep(), true, new TooltipMakerAPI.StatModValueGetter() {
                    public String getPercentValue(MutableStat.StatMod mod) {
                        return null;
                    }

                    public String getMultValue(MutableStat.StatMod mod) {
                        return null;
                    }

                    public Color getModColor(MutableStat.StatMod mod) {
                        return null;
                    }

                    public String getFlatValue(MutableStat.StatMod mod) {
                        return Misc.getWithDGS(mod.value) + Strings.C;
                    }
                });
            }

            addPostUpkeepSection(tooltip, mode);

            boolean hasSupply = false;
            for (MutableCommodityQuantity curr : this.supply.values()) {
                int qty = curr.getQuantity().getModifiedInt();
                if (qty <= 0) continue;
                hasSupply = true;
                break;
            }
            boolean hasDemand = false;
            for (MutableCommodityQuantity curr : this.demand.values()) {
                int qty = curr.getQuantity().getModifiedInt();
                if (qty <= 0) continue;
                hasDemand = true;
                break;
            }

            if (hasSupply) {
                tooltip.addSectionHeading("Production", color, dark, Alignment.MID, oPad);
                tooltip.beginIconGroup();
                tooltip.setIconSpacingMedium();
                for (MutableCommodityQuantity curr : this.supply.values()) {
                    int normal = curr.getQuantity().getModifiedInt();
                    if (normal > 0) {
                        tooltip.addIcons(this.market.getCommodityData(curr.getCommodityId()), normal, IconRenderMode.NORMAL);
                    }

                    int plus = 0;
                    int minus = 0;
                    for (MutableStat.StatMod mod : curr.getQuantity().getFlatMods().values()) {
                        if (mod.value > 0) {
                            plus += (int) mod.value;
                        } else if (mod.desc != null && mod.desc.contains("shortage")) {
                            minus += (int) Math.abs(mod.value);
                        }
                    }
                    minus = Math.min(minus, plus);
                    if (minus > 0 && mode == IndustryTooltipMode.NORMAL) {
                        tooltip.addIcons(this.market.getCommodityData(curr.getCommodityId()), minus, IconRenderMode.DIM_RED);
                    }
                }
                int rows = 3;
                tooltip.addIconGroup(32, rows, oPad);
            }

            addPostSupplySection(tooltip, hasSupply, mode);

            if (hasDemand || hasPostDemandSection(hasDemand, mode)) {
                tooltip.addSectionHeading("Demand & effects", color, dark, Alignment.MID, oPad);
            }

            if (hasDemand) {
                tooltip.beginIconGroup();
                tooltip.setIconSpacingMedium();
                for (MutableCommodityQuantity curr : this.demand.values()) {
                    int qty = curr.getQuantity().getModifiedInt();
                    if (qty <= 0) continue;

                    CommodityOnMarketAPI com = orig.getCommodityData(curr.getCommodityId());
                    int available = com.getAvailable();

                    int normal = Math.min(available, qty);
                    int red = Math.max(0, qty - available);

                    if (mode != IndustryTooltipMode.NORMAL) {
                        normal = qty;
                        red = 0;
                    }
                    if (normal > 0) {
                        tooltip.addIcons(com, normal, IconRenderMode.NORMAL);
                    }
                    if (red > 0) {
                        tooltip.addIcons(com, red, IconRenderMode.DIM_RED);
                    }
                }
                int rows = 1;
                tooltip.addIconGroup(32, rows, oPad);
            }

            addPostDemandSection(tooltip, hasDemand, mode);

            if (!needToAddIndustry) {
                addInstalledItemsSection(mode, tooltip, expanded);
                addImprovedSection(mode, tooltip, expanded);

                ListenerUtil.addToIndustryTooltip(this, mode, tooltip, getTooltipWidth(), expanded);
            }

            tooltip.addPara("*Shown production and demand values are already adjusted based on current market size and local conditions.", gray, oPad);
        }

        if (needToAddIndustry) {
            unapply();
            this.market.getIndustries().remove(this);
        }

        this.market = orig;
        this.market.setRetainSuppressedConditionsSetWhenEmpty(null);
        if (!needToAddIndustry) {
            reapply();
        }
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
            text.addPara(pre + "Reduces upkeep cost by %s. Reduces terraforming time by %s.", oPad, highlight,
                    (int) ((1f - UPKEEP_MULT) * 100f) + "%", (int) (ALPHA_BUILD_TIME_MULT * 100f) + "%");
            tooltip.addImageWithText(oPad);
            return;
        }

        tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces terraforming time by %s.", oPad, highlight,
                (int) ((1f - UPKEEP_MULT) * 100f) + "%", (int) (ALPHA_BUILD_TIME_MULT * 100f) + "%");
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
            text.addPara(pre + "Reduces upkeep cost by %s. Reduces terraforming time by %s.", oPad, highlight,
                    (int) ((1f - UPKEEP_MULT) * 100f) + "%", (int) (BETA_BUILD_TIME_MULT * 100f) + "%");
            tooltip.addImageWithText(oPad);
            return;
        }

        tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces terraforming time by %s.", oPad, highlight,
                (int) ((1f - UPKEEP_MULT) * 100f) + "%", (int) (BETA_BUILD_TIME_MULT * 100f) + "%");
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
            text.addPara(pre + "Reduces upkeep cost by %s. Reduces terraforming time by %s.", oPad, highlight,
                    (int) ((1f - UPKEEP_MULT) * 100f) + "%", (int) (GAMMA_BUILD_TIME_MULT * 100f) + "%");
            tooltip.addImageWithText(oPad);
            return;
        }

        tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces terraforming time by %s.", oPad, highlight,
                (int) ((1f - UPKEEP_MULT) * 100f) + "%", (int) (GAMMA_BUILD_TIME_MULT * 100f) + "%");
    }

    public void addTerraformingOptionList(TooltipMakerAPI tooltip, IndustryTooltipMode mode, boolean expanded) {
        if (expanded) {
            int rowLimit = 6;
            int andMore = this.modifiableConditions.size() - rowLimit;
            if (this.modifiableConditions.size() < rowLimit) {
                rowLimit = this.modifiableConditions.size();
                andMore = 0;
            }

            tooltip.addPara("Modifiable Conditions:", 10f);
            for (int i = 0; i < rowLimit; i++) {
                float pad = 1f;
                if (i == 0) {
                    pad = 3f;
                }
                tooltip.addPara("    " + this.modifiableConditions.get(i).name, Misc.getHighlightColor(), pad);
            }
            if (andMore > 0) {
                tooltip.addPara("    ...and %s more", 0f, Misc.getTextColor(), Misc.getHighlightColor(), andMore + "");
            }
        } else {
            tooltip.addPara("Press %s to see conditions you can alter", 10f, Misc.getGrayColor(), Misc.getHighlightColor(), "F1");
        }
    }

    @Override
    protected void addPostUpkeepSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode) {
        float pad = 3f;

        if (mode == IndustryTooltipMode.NORMAL || isUpgrading()) {
            if (isUpgrading()) {
                tooltip.addSectionHeading("Terraforming project", Alignment.MID, 10f);
                TooltipMakerAPI imageWithText = tooltip.beginImageWithText(this.modifiableCondition.icon, 40f, getTooltipWidth(), false);
                imageWithText.addPara("Status: %s", 0f, Misc.getHighlightColor(), "Ongoing");
                imageWithText.addPara("Action: %s", pad, Misc.getHighlightColor(), !this.market.hasCondition(this.modifiableCondition.id) ? "Add" : "Remove");
                imageWithText.addPara("Condition: %s", pad, Misc.getHighlightColor(), this.modifiableCondition.name);
                imageWithText.addPara("Days Left: %s", pad, Misc.getHighlightColor(), Math.round(this.buildTime - this.buildProgress) + "");
                tooltip.addImageWithText(10f);
            } else {
                tooltip.addSectionHeading("No projects started", Alignment.MID, 10f);
            }
        }
    }

    public String getAOTDVOKTechId() {
        return "";
    }

    public List<Utils.ModifiableCondition> getModifiableConditions() {
        return this.modifiableConditions;
    }

    public void setModifiableConditions(List<Utils.ModifiableCondition> options) {
        this.modifiableConditions = options;
    }

    public Utils.ModifiableCondition getModifiableCondition() {
        return this.modifiableCondition;
    }

    public void setModifiableCondition(Utils.ModifiableCondition option) {
        this.modifiableCondition = option;
    }

    public void sendCompletedMessage() {
        if (this.market.isPlayerOwned()) {
            String addOrRemoveText = !this.market.hasCondition(this.modifiableCondition.id) ? "Added " : "Removed ";
            MessageIntel intel = new MessageIntel("Terraforming completed at " + this.market.getName(), Misc.getBasePlayerColor());
            intel.addLine(BaseIntelPlugin.BULLET + addOrRemoveText + this.modifiableCondition.name.toLowerCase() + " planet condition");
            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, this.market);
        }
    }

    public void changePlanetConditions() {
        if (Global.getSettings().getMarketConditionSpec(this.modifiableCondition.id) == null) {
            return;
        }

        if (this.market.hasCondition(this.modifiableCondition.id)) {
            this.market.removeCondition(this.modifiableCondition.id);
        } else {
            this.market.addCondition(this.modifiableCondition.id);
            this.market.getFirstCondition(this.modifiableCondition.id).setSurveyed(true);

            // remove all hated conditions
            String textExpression = this.modifiableCondition.hatedConditions;
            String[] expressions = textExpression.split(",");
            for (String s : expressions) {
                String expression = s;
                if (expression.contains("needAll")) {
                    expression = expression.replaceAll("needAll:", "");
                } else if (expression.contains("needOne")) {
                    expression = expression.replaceAll("needOne:", "");
                }
                String[] ids = expression.split("\\|");
                for (String id : ids) {
                    if (this.market.hasCondition(id)) {
                        this.market.removeCondition(id);
                    }
                }
            }
        }
    }

    public void changePlanetClass() {
        String planetTypeId = null;
        boolean removeFarming = false;
        boolean removeOrganics = false;
        boolean reduceOrganics = false;
        boolean removeLobsters = true;
        boolean removeWaterSurface = true;
        if (!this.market.getPlanetEntity().isStar() && !this.market.getPlanetEntity().isGasGiant()
                && !this.market.hasCondition(Conditions.HABITABLE) && !this.market.hasCondition(Conditions.VERY_COLD)
                && !this.market.hasCondition(Conditions.VERY_HOT) && !this.market.hasCondition(Conditions.NO_ATMOSPHERE)
                && !this.market.hasCondition(Conditions.THIN_ATMOSPHERE) && !this.market.hasCondition(Conditions.DENSE_ATMOSPHERE)
                && !this.market.hasCondition(Conditions.TOXIC_ATMOSPHERE) && !this.market.hasCondition(Conditions.IRRADIATED)
                && !this.market.hasCondition(Conditions.DARK)) {
            addOrImproveFarming();
            addOrImproveOrganics();
            this.market.addCondition(Conditions.HABITABLE);
        }
        if (this.market.hasCondition(Conditions.HABITABLE)) {
            String[] richHabitableTypes = {"terran", "terran-eccentric"};
            planetTypeId = richHabitableTypes[Misc.random.nextInt(richHabitableTypes.length)];
        }
        if (this.market.hasCondition(Conditions.HOT)) {
            String[] poorHabitableTypes = {"jungle", "arid", "desert", "desert1"};
            planetTypeId = poorHabitableTypes[Misc.random.nextInt(poorHabitableTypes.length)];
        }
        if (this.market.hasCondition(Conditions.COLD)) {
            planetTypeId = "tundra";
        }
        if (this.market.hasCondition(Conditions.NO_ATMOSPHERE) || this.market.hasCondition(Conditions.VERY_HOT) || this.market.hasCondition(Conditions.VERY_COLD)) {
            String[] poorBarrenTypes = {"barren", "barren2", "barren3", "barren_castiron", "barren_venuslike", "rocky_metallic", "barren-bombarded"};
            planetTypeId = poorBarrenTypes[Misc.random.nextInt(poorBarrenTypes.length)];
            removeFarming = true;
            removeOrganics = true;
            if (this.market.hasCondition(Conditions.TECTONIC_ACTIVITY) || this.market.hasCondition(Conditions.EXTREME_TECTONIC_ACTIVITY)) {
                planetTypeId = "rocky_unstable";
            }
        }
        if (this.market.hasCondition(Conditions.WATER_SURFACE)) {
            planetTypeId = "water";
            removeFarming = true;
            removeLobsters = false;
            removeWaterSurface = false;
            if (this.market.hasCondition(Conditions.VERY_COLD)) {
                String[] frozenTypes = {"frozen", "frozen1", "frozen2", "frozen3"};
                planetTypeId = frozenTypes[Misc.random.nextInt(frozenTypes.length)];
                removeWaterSurface = true;
                removeOrganics = true;
                removeLobsters = true;
            }
            if ((this.market.hasCondition(Conditions.COLD) || this.market.hasCondition(Conditions.VERY_COLD)) && this.market.hasCondition(Conditions.NO_ATMOSPHERE)) {
                planetTypeId = "rocky_ice";
                removeWaterSurface = true;
                removeOrganics = true;
                removeLobsters = true;
            }
        }
        if (this.market.hasCondition(Conditions.THIN_ATMOSPHERE)) {
            planetTypeId = "barren-desert";
            removeFarming = true;
            reduceOrganics = true;
            removeWaterSurface = true;
        }
        if (this.market.hasCondition(Conditions.TOXIC_ATMOSPHERE)) {
            planetTypeId = "toxic";
            removeFarming = true;
            reduceOrganics = true;
            removeWaterSurface = true;
        }
        if (this.market.hasCondition(Conditions.IRRADIATED)) {
            planetTypeId = "irradiated";
            removeFarming = true;
            removeOrganics = true;
            reduceOrganics = false;
        }
        if (this.market.hasCondition(Conditions.TECTONIC_ACTIVITY) || this.market.hasCondition(Conditions.EXTREME_TECTONIC_ACTIVITY)) {
            if ((this.market.hasCondition(Conditions.VERY_HOT) || this.market.hasCondition(Conditions.HOT))
                    && (this.market.hasCondition(Conditions.TOXIC_ATMOSPHERE) || this.market.hasCondition(Conditions.THIN_ATMOSPHERE) || this.market.hasCondition(Conditions.DENSE_ATMOSPHERE))
                    && (this.market.hasCondition(Conditions.ORE_ABUNDANT) || this.market.hasCondition(Conditions.ORE_RICH) || this.market.hasCondition(Conditions.ORE_ULTRARICH))
                    && (this.market.hasCondition(Conditions.RARE_ORE_ABUNDANT) || this.market.hasCondition(Conditions.RARE_ORE_RICH) || this.market.hasCondition(Conditions.RARE_ORE_ULTRARICH))) {
                planetTypeId = "lava";
                removeFarming = true;
                removeOrganics = true;
                reduceOrganics = false;
            }
            if (this.market.hasCondition(Conditions.VERY_COLD)
                    && (this.market.hasCondition(Conditions.VOLATILES_TRACE) || this.market.hasCondition(Conditions.VOLATILES_DIFFUSE) ||
                    this.market.hasCondition(Conditions.VOLATILES_ABUNDANT) || this.market.hasCondition(Conditions.VOLATILES_PLENTIFUL))) {
                planetTypeId = "cryovolcanic";
                removeFarming = true;
                removeOrganics = true;
                reduceOrganics = false;
            }
        }
        if (this.market.getPlanetEntity().isGasGiant()) {
            removeFarming = true;
            removeOrganics = true;
            reduceOrganics = false;
            if (this.market.hasCondition(Conditions.VERY_COLD)) {
                planetTypeId = "ice_giant";
            }
            if (this.market.hasCondition(Conditions.VERY_HOT)) {
                planetTypeId = "gas_giant";
            }
        }

        if (removeFarming) {
            removeFarming();
        }
        if (removeLobsters) {
            removeLobsters();
        }
        if (removeWaterSurface) {
            removeWaterSurface();
        }
        if (reduceOrganics) {
            reduceOrganicsToCommon();
        } else if (removeOrganics) {
            removeOrganics();
        }

        updateFarmingOrAquaculture();
        changePlanetVisuals(planetTypeId);
    }

    public void removeWaterSurface() {
        this.market.removeCondition(Conditions.WATER_SURFACE);
    }

    public void updateFarmingOrAquaculture() {
        if (!this.market.hasCondition(Conditions.WATER_SURFACE) && this.market.hasIndustry(Industries.AQUACULTURE)) {
            this.market.removeIndustry(Industries.AQUACULTURE, null, false);
            this.market.addIndustry(Industries.FARMING);
        } else if (this.market.hasCondition(Conditions.WATER_SURFACE) && this.market.hasIndustry(Industries.FARMING)) {
            this.market.removeIndustry(Industries.FARMING, null, false);
            this.market.addIndustry(Industries.AQUACULTURE);
        }
    }

    public void changePlanetVisuals(String planetTypeId) {
        String planetType = planetTypeId;
        if (this.modifiableCondition.planetSpecOverride != null) {
            for (PlanetSpecAPI spec : Global.getSettings().getAllPlanetSpecs()) {
                if (spec.isStar()) {
                    continue;
                }
                if (Objects.equals(spec.getPlanetType(), this.modifiableCondition.planetSpecOverride)) {
                    planetType = spec.getPlanetType();
                    break;
                }
            }
        }
        this.market.getPlanetEntity().changeType(planetType, StarSystemGenerator.random);
        this.market.getPlanetEntity().applySpecChanges();
    }

    public void removeFarming() {
        this.market.removeCondition(Conditions.FARMLAND_POOR);
        this.market.removeCondition(Conditions.FARMLAND_ADEQUATE);
        this.market.removeCondition(Conditions.FARMLAND_RICH);
        this.market.removeCondition(Conditions.FARMLAND_BOUNTIFUL);
    }

    public void removeOrganics() {
        this.market.removeCondition(Conditions.ORGANICS_TRACE);
        this.market.removeCondition(Conditions.ORGANICS_COMMON);
        this.market.removeCondition(Conditions.ORGANICS_ABUNDANT);
        this.market.removeCondition(Conditions.ORGANICS_PLENTIFUL);
    }

    public void removeLobsters() {
        this.market.removeCondition(Conditions.VOLTURNIAN_LOBSTER_PENS);
    }

    public void addOrImproveFarming() {
        if (this.market.hasCondition(Conditions.FARMLAND_POOR)) {
            this.market.removeCondition(Conditions.FARMLAND_POOR);
            this.market.addCondition(Conditions.FARMLAND_ADEQUATE);
            this.market.getFirstCondition(Conditions.FARMLAND_ADEQUATE).setSurveyed(true);
        } else if (this.market.hasCondition(Conditions.FARMLAND_ADEQUATE)) {
            this.market.removeCondition(Conditions.FARMLAND_ADEQUATE);
            this.market.addCondition(Conditions.FARMLAND_RICH);
            this.market.getFirstCondition(Conditions.FARMLAND_RICH).setSurveyed(true);
        } else if (this.market.hasCondition(Conditions.FARMLAND_RICH)) {
            this.market.removeCondition(Conditions.FARMLAND_RICH);
            this.market.addCondition(Conditions.FARMLAND_BOUNTIFUL);
            this.market.getFirstCondition(Conditions.FARMLAND_BOUNTIFUL).setSurveyed(true);
        } else if (!this.market.hasCondition(Conditions.FARMLAND_BOUNTIFUL)) {
            this.market.addCondition(Conditions.FARMLAND_POOR);
            this.market.getFirstCondition(Conditions.FARMLAND_POOR).setSurveyed(true);
        }
    }

    public void addOrImproveOrganics() {
        if (this.market.hasCondition(Conditions.ORGANICS_TRACE)) {
            this.market.removeCondition(Conditions.ORGANICS_TRACE);
            this.market.addCondition(Conditions.ORGANICS_COMMON);
            this.market.getFirstCondition(Conditions.ORGANICS_COMMON).setSurveyed(true);
        } else if (this.market.hasCondition(Conditions.ORGANICS_COMMON)) {
            this.market.removeCondition(Conditions.ORGANICS_COMMON);
            this.market.addCondition(Conditions.ORGANICS_ABUNDANT);
            this.market.getFirstCondition(Conditions.ORGANICS_ABUNDANT).setSurveyed(true);
        } else if (this.market.hasCondition(Conditions.ORGANICS_ABUNDANT)) {
            this.market.removeCondition(Conditions.ORGANICS_ABUNDANT);
            this.market.addCondition(Conditions.ORGANICS_PLENTIFUL);
            this.market.getFirstCondition(Conditions.ORGANICS_PLENTIFUL).setSurveyed(true);
        } else if (!this.market.hasCondition(Conditions.ORGANICS_PLENTIFUL)) {
            this.market.addCondition(Conditions.ORGANICS_TRACE);
            this.market.getFirstCondition(Conditions.ORGANICS_TRACE).setSurveyed(true);
        }
    }

    public void reduceOrganicsToCommon() {
        if (this.market.hasCondition(Conditions.ORGANICS_ABUNDANT) || this.market.hasCondition(Conditions.ORGANICS_PLENTIFUL)) {
            this.market.removeCondition(Conditions.ORGANICS_ABUNDANT);
            this.market.removeCondition(Conditions.ORGANICS_PLENTIFUL);
            this.market.addCondition(Conditions.ORGANICS_COMMON);
            this.market.getFirstCondition(Conditions.ORGANICS_COMMON).setSurveyed(true);
        }
    }

    public Boolean canTerraformCondition(Utils.ModifiableCondition condition) {
        return hasRequiredConditions(condition) && hasRequiredIndustries(condition);
    }

    public boolean hasRequiredConditions(Utils.ModifiableCondition condition) {
        String text = condition.likedConditions;

        if (text == null || text.isEmpty()) {
            return true;
        }

        String[] expressions = text.split(",");
        boolean[] expressionsResult = new boolean[expressions.length];

        for (int i = 0; i < expressions.length; i++) {
            String expression = expressions[i];
            if (expression.contains("needAll")) {
                expression = expression.replaceAll("needAll:", "").replaceAll("\\|", "&&");
            } else if (expression.contains("needOne")) {
                expression = expression.replaceAll("needOne:", "").replaceAll("\\|", "||");
            }

            String[] ids = expression.split("&&|\\|\\|");
            Map<String, Boolean> values = new HashMap<>();
            for (String id : ids) {
                values.put(id, this.market.hasCondition(id));
            }
            expressionsResult[i] = Utils.evaluateExpression(expression, values);
        }

        return Utils.isAllTrue(expressionsResult);
    }

    public boolean hasRequiredIndustries(Utils.ModifiableCondition condition) {
        String text = condition.likedIndustries;

        if (text == null || text.isEmpty()) {
            return true;
        }

        String[] expressions = text.split(",");
        boolean[] expressionsResult = new boolean[expressions.length];

        for (int i = 0; i < expressions.length; i++) {
            String expression = expressions[i];
            if (expression.contains("needAll")) {
                expression = expression.replaceAll("needAll:", "").replaceAll("\\|", "&&");
            } else if (expression.contains("needOne")) {
                expression = expression.replaceAll("needOne:", "").replaceAll("\\|", "||");
            }

            String[] ids = expression.split("&&|\\|\\|");
            Map<String, Boolean> values = new HashMap<>();
            for (String id : ids) {
                values.put(id, this.market.hasIndustry(id));
            }
            expressionsResult[i] = Utils.evaluateExpression(expression, values);
        }

        return Utils.isAllTrue(expressionsResult);
    }
}
