package terraformingmadeeasy.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.PlanetSpecAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MarketConditionSpecAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.procgen.PlanetGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.kaysaar.aotd.vok.scripts.research.AoTDMainResearchManager;
import org.apache.log4j.Logger;
import terraformingmadeeasy.Settings;
import terraformingmadeeasy.ids.TMEIds;
import terraformingmadeeasy.loading.specs.TerraformingProjectSpec;

import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class TerraformingIndustry extends BaseIndustry {
    public static final Logger log = Global.getLogger(TerraformingIndustry.class);
    public static final float GAMMA_BUILD_TIME_MULT = 0.20f;
    public static final float BETA_BUILD_TIME_MULT = 0.30f;
    public static final float ALPHA_BUILD_TIME_MULT = 0.50f;
    protected TerraformingProjectSpec project = null;
    protected Boolean isAICoreBuildTimeMultApplied = false;
    protected float aiCoreBuildProgressRemoved = 0f;
    protected String prevAICoreId = null;

    public static void sendCompletedMessage(MarketAPI market, TerraformingProjectSpec project) {
        if (market.isPlayerOwned()) {
            MarketConditionSpecAPI spec = Global.getSettings().getMarketConditionSpec(project.getConditionId());
            String addOrRemoveText = !market.hasCondition(spec.getId()) ? "Added " : "Removed ";
            MessageIntel intel = new MessageIntel("Terraforming completed at " + market.getName(), Misc.getBasePlayerColor());
            intel.addLine(BaseIntelPlugin.BULLET + addOrRemoveText + spec.getName().toLowerCase() + " planet condition");
            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
        }
    }

    public static void terraformPlanet(MarketAPI market, TerraformingProjectSpec project) {
        MarketConditionSpecAPI spec = Global.getSettings().getMarketConditionSpec(project.getConditionId());
        if (Global.getSettings().getMarketConditionSpec(spec.getId()) == null) {
            return;
        }

        if (market.hasCondition(spec.getId())) {
            market.removeCondition(spec.getId());
        } else {
            market.addCondition(spec.getId());
            market.getFirstCondition(spec.getId()).setSurveyed(true);

            // remove all hated conditions
            if (!project.getHatedConditions().isEmpty()) {
                String textExpression = project.getHatedConditions();
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
                        if (market.hasCondition(id)) {
                            market.removeCondition(id);
                        }
                    }
                }
            }
        }
    }

    public static void updatePlanetConditions(MarketAPI market) {
        boolean removeFarming = false;
        boolean removeOrganics = false;
        boolean reduceOrganics = false;
        boolean removeLobsters = true;
        boolean removeWaterSurface = true;
        if (!market.getPlanetEntity().isStar() && !market.getPlanetEntity().isGasGiant()
                && !market.hasCondition(Conditions.HABITABLE) && !market.hasCondition(Conditions.VERY_COLD)
                && !market.hasCondition(Conditions.VERY_HOT) && !market.hasCondition(Conditions.NO_ATMOSPHERE)
                && !market.hasCondition(Conditions.THIN_ATMOSPHERE) && !market.hasCondition(Conditions.DENSE_ATMOSPHERE)
                && !market.hasCondition(Conditions.TOXIC_ATMOSPHERE) && !market.hasCondition(Conditions.IRRADIATED)
                && !market.hasCondition(Conditions.DARK) && !market.hasCondition(Conditions.EXTREME_TECTONIC_ACTIVITY)) {
            addOrImproveFarming(market);
            addOrImproveOrganics(market);
            market.addCondition(Conditions.HABITABLE);
        }
        if (market.hasCondition(Conditions.WATER_SURFACE)) {
            removeFarming = true;
            removeLobsters = false;
            removeWaterSurface = false;
        }
        if (market.hasCondition(Conditions.NO_ATMOSPHERE) || market.hasCondition(Conditions.VERY_HOT) ||
                market.hasCondition(Conditions.VERY_COLD) || market.hasCondition(Conditions.EXTREME_TECTONIC_ACTIVITY)) {
            removeFarming = true;
            removeOrganics = true;
            removeLobsters = true;
            removeWaterSurface = true;
        }
        if (market.hasCondition(Conditions.THIN_ATMOSPHERE)) {
            removeFarming = true;
            reduceOrganics = true;
            removeLobsters = true;
            removeWaterSurface = true;
        }
        if (market.hasCondition(Conditions.TOXIC_ATMOSPHERE)) {
            removeFarming = true;
            reduceOrganics = true;
            removeLobsters = true;
            removeWaterSurface = true;
        }
        if (market.hasCondition(Conditions.IRRADIATED)) {
            removeFarming = true;
            removeOrganics = true;
            reduceOrganics = false;
            removeLobsters = true;
            removeWaterSurface = true;
        }
        if (!market.hasCondition(Conditions.HABITABLE) && !market.hasCondition(Conditions.NO_ATMOSPHERE)) {
            if (market.hasCondition(Conditions.TECTONIC_ACTIVITY) || market.hasCondition(Conditions.EXTREME_TECTONIC_ACTIVITY)) {
                if (market.hasCondition(Conditions.VERY_HOT) || market.hasCondition(Conditions.HOT)) {
                    removeFarming = true;
                    removeOrganics = true;
                    reduceOrganics = false;
                    removeLobsters = true;
                    removeWaterSurface = true;
                }
                if (market.hasCondition((Conditions.VERY_COLD))) {
                    removeFarming = true;
                    removeOrganics = true;
                    reduceOrganics = false;
                    removeLobsters = true;
                    removeWaterSurface = true;
                }
            }
        }
        if (market.getPlanetEntity().isGasGiant()) {
            removeFarming = true;
            removeOrganics = true;
            reduceOrganics = false;
            removeLobsters = true;
            removeWaterSurface = true;
        }

        if (removeFarming) {
            removeFarming(market);
        }
        if (removeLobsters) {
            removeLobsters(market);
        }
        if (removeWaterSurface) {
            removeWaterSurface(market);
        }
        if (reduceOrganics) {
            reduceOrganicsToCommon(market);
        } else if (removeOrganics) {
            removeOrganics(market);
        }

        updateFarmingOrAquaculture(market);
    }

    public static String evaluatePlanetCategory(MarketAPI market) {
        PlanetGenDataSpec spec = (PlanetGenDataSpec) Global.getSettings()
                .getSpec(PlanetGenDataSpec.class, market.getPlanetEntity().getTypeId(), true);
        String result = "";
        if (spec != null) {
            result = spec.getCategory();
        }

        if (market.hasCondition(Conditions.HABITABLE)) {
            result = "cat_hab4";

            if (market.hasCondition(Conditions.HOT) || market.hasCondition(Conditions.WATER_SURFACE) || market.hasCondition(Conditions.COLD)) {
                result = "cat_hab3";
            }
            if (!market.hasCondition(Conditions.WATER_SURFACE) && market.hasCondition(Conditions.HOT) && market.hasCondition(Conditions.EXTREME_WEATHER)) {
                result = "cat_hab2";
            }
        }
        if (market.hasCondition(Conditions.THIN_ATMOSPHERE)) {
            result = "cat_hab1";
        }
        if (market.hasCondition(Conditions.VERY_HOT) || market.hasCondition(Conditions.EXTREME_TECTONIC_ACTIVITY)) {
            result = "cat_lava";
        }
        if (market.hasCondition(Conditions.NO_ATMOSPHERE)) {
            result = "cat_barren";
        }
        if (market.hasCondition(Conditions.VERY_COLD)) {
            result = "cat_frozen";
        }
        if (market.hasCondition(Conditions.TOXIC_ATMOSPHERE)) {
            result = "cat_toxic";
        }
        if (market.hasCondition(Conditions.IRRADIATED)) {
            result = "cat_irradiated";
        }
        if (!market.hasCondition(Conditions.HABITABLE) && !market.hasCondition(Conditions.NO_ATMOSPHERE)) {
            if (market.hasCondition(Conditions.TECTONIC_ACTIVITY) || market.hasCondition(Conditions.EXTREME_TECTONIC_ACTIVITY)) {
                if (market.hasCondition(Conditions.VERY_HOT) || market.hasCondition(Conditions.HOT)) {
                    result = "cat_lava";
                }
                if (market.hasCondition((Conditions.VERY_COLD))) {
                    result = "cat_cryovolcanic";
                }
            }
        }
        if (market.getPlanetEntity().isGasGiant()) {
            result = "cat_giant";
        }

        return result;
    }

    public static String evaluatePlanetType(String category, MarketAPI market) {
        WeightedRandomPicker<String> picker = new WeightedRandomPicker<>(Misc.random);
        Collection<PlanetGenDataSpec> specs = Global.getSettings().getAllSpecs(PlanetGenDataSpec.class);
        for (PlanetGenDataSpec spec : specs) {
            if (Objects.equals(spec.getCategory(), category)) {
                if (spec.getFrequency() > 0) {
                    picker.add(spec.getId(), 1);
                }
            }
        }

        List<String> items = picker.getItems();
        if (Objects.equals(category, "cat_hab3")) {
            if (items.contains("water")) {
                int index = items.indexOf("water");
                if (market.hasCondition(Conditions.WATER_SURFACE)) {
                    picker.setWeight(index, 100f);
                } else {
                    picker.remove("water");
                }
            }
            if (items.contains("tundra")) {
                int index = items.indexOf("tundra");
                if (market.hasCondition(Conditions.COLD)) {
                    picker.setWeight(index, 100f);
                } else {
                    picker.remove("tundra");
                }
            }
        } else if (Objects.equals(category, "cat_barren")) {
            if (items.contains("rocky_unstable")) {
                int index = items.indexOf("rocky_unstable");
                if (market.hasCondition(Conditions.TECTONIC_ACTIVITY) || market.hasCondition(Conditions.EXTREME_TECTONIC_ACTIVITY)) {
                    picker.setWeight(index, 100f);
                } else {
                    picker.remove("rocky_unstable");
                }
            }
            if (items.contains("rocky_ice")) {
                int index = items.indexOf("rocky_ice");
                if (market.hasCondition(Conditions.COLD)) {
                    picker.setWeight(index, 100f);
                } else {
                    picker.remove("rocky_ice");
                }
            }
        } else if (Objects.equals(category, "cat_giant")) {
            if (items.contains("ice_giant")) {
                int index = items.indexOf("ice_giant");
                if (market.hasCondition(Conditions.COLD) || market.hasCondition(Conditions.VERY_COLD)) {
                    picker.setWeight(index, 100f);
                } else {
                    picker.remove("ice_giant");
                }
            }
        }

        return picker.pick();
    }

    public static void removeWaterSurface(MarketAPI market) {
        market.removeCondition(Conditions.WATER_SURFACE);
    }

    public static void updateFarmingOrAquaculture(MarketAPI market) {
        if (Settings.isAoTDVoKEnabled()) {
            if (!market.hasCondition(Conditions.WATER_SURFACE)) {
                if (market.hasIndustry(Industries.AQUACULTURE)) {
                    market.removeIndustry(Industries.AQUACULTURE, null, false);
                    market.addIndustry(Industries.FARMING);
                } else if (market.hasIndustry("fishery")) {
                    market.removeIndustry("fishery", null, false);
                    market.addIndustry("subfarming");
                }
            } else if (market.hasCondition(Conditions.WATER_SURFACE)) {
                if (market.hasIndustry(Industries.FARMING)) {
                    market.removeIndustry(Industries.FARMING, null, false);
                    market.addIndustry(Industries.AQUACULTURE);
                } else if (market.hasIndustry("subfarming")) {
                    market.removeIndustry("subfarming", null, false);
                    market.addIndustry("fishery");
                } else if (market.hasIndustry("artifarming")) {
                    market.removeIndustry("artifarming", null, false);
                    market.addIndustry("fishery");
                }
            }
        } else {
            if (!market.hasCondition(Conditions.WATER_SURFACE) && market.hasIndustry(Industries.AQUACULTURE)) {
                market.removeIndustry(Industries.AQUACULTURE, null, false);
                market.addIndustry(Industries.FARMING);
            } else if (market.hasCondition(Conditions.WATER_SURFACE) && market.hasIndustry(Industries.FARMING)) {
                market.removeIndustry(Industries.FARMING, null, false);
                market.addIndustry(Industries.AQUACULTURE);
            }
        }
    }

    public static void updatePlanetVisuals(String planetTypeId, MarketAPI market, TerraformingProjectSpec project) {
        String planetType = planetTypeId;
        if (!project.getPlanetSpecOverride().isEmpty()) {
            for (PlanetSpecAPI spec : Global.getSettings().getAllPlanetSpecs()) {
                if (spec.isStar()) {
                    continue;
                }
                if (Objects.equals(spec.getPlanetType(), project.getPlanetSpecOverride())) {
                    planetType = spec.getPlanetType();
                    break;
                }
            }
        }
        market.getPlanetEntity().changeType(planetType, StarSystemGenerator.random);
        market.getPlanetEntity().applySpecChanges();
    }

    public static void removeFarming(MarketAPI market) {
        market.removeCondition(Conditions.FARMLAND_POOR);
        market.removeCondition(Conditions.FARMLAND_ADEQUATE);
        market.removeCondition(Conditions.FARMLAND_RICH);
        market.removeCondition(Conditions.FARMLAND_BOUNTIFUL);
    }

    public static void removeOrganics(MarketAPI market) {
        market.removeCondition(Conditions.ORGANICS_TRACE);
        market.removeCondition(Conditions.ORGANICS_COMMON);
        market.removeCondition(Conditions.ORGANICS_ABUNDANT);
        market.removeCondition(Conditions.ORGANICS_PLENTIFUL);
    }

    public static void removeLobsters(MarketAPI market) {
        market.removeCondition(Conditions.VOLTURNIAN_LOBSTER_PENS);
    }

    public static void addOrImproveFarming(MarketAPI market) {
        String improvedFarmingId = Conditions.FARMLAND_POOR;
        if (market.hasCondition(Conditions.FARMLAND_POOR)) {
            improvedFarmingId = Conditions.FARMLAND_ADEQUATE;
        } else if (market.hasCondition(Conditions.FARMLAND_ADEQUATE)) {
            improvedFarmingId = Conditions.FARMLAND_RICH;
        } else if (market.hasCondition(Conditions.FARMLAND_RICH)) {
            improvedFarmingId = Conditions.FARMLAND_BOUNTIFUL;
        } else if (market.hasCondition(Conditions.FARMLAND_BOUNTIFUL)) {
            return;
        }
        removeFarming(market);
        market.addCondition(improvedFarmingId);
        market.getFirstCondition(improvedFarmingId).setSurveyed(true);
    }

    public static void addOrImproveOrganics(MarketAPI market) {
        String improvedOrganicsId = Conditions.ORGANICS_TRACE;
        if (market.hasCondition(Conditions.ORGANICS_TRACE)) {
            improvedOrganicsId = Conditions.ORGANICS_COMMON;
        } else if (market.hasCondition(Conditions.ORGANICS_COMMON)) {
            improvedOrganicsId = Conditions.ORGANICS_ABUNDANT;
        } else if (market.hasCondition(Conditions.ORGANICS_ABUNDANT)) {
            improvedOrganicsId = Conditions.ORGANICS_PLENTIFUL;
        } else if (market.hasCondition(Conditions.ORGANICS_PLENTIFUL)) {
            return;
        }
        removeOrganics(market);
        market.addCondition(improvedOrganicsId);
        market.getFirstCondition(improvedOrganicsId).setSurveyed(true);
    }

    public static void reduceOrganicsToCommon(MarketAPI market) {
        if (market.hasCondition(Conditions.ORGANICS_ABUNDANT) || market.hasCondition(Conditions.ORGANICS_PLENTIFUL)) {
            market.removeCondition(Conditions.ORGANICS_ABUNDANT);
            market.removeCondition(Conditions.ORGANICS_PLENTIFUL);
            market.addCondition(Conditions.ORGANICS_COMMON);
            market.getFirstCondition(Conditions.ORGANICS_COMMON).setSurveyed(true);
        }
    }

    @Override
    public void apply() {
        super.apply(true);
    }

    @Override
    protected void applyNoAICoreModifiers() {
        if (this.isAICoreBuildTimeMultApplied && !Objects.equals(getAICoreId(), this.prevAICoreId)) {
            this.buildProgress = this.buildProgress - this.aiCoreBuildProgressRemoved;
            this.aiCoreBuildProgressRemoved = 0f;
            this.isAICoreBuildTimeMultApplied = false;
        }
    }

    @Override
    protected void applyGammaCoreModifiers() {
        applyBuildTimeMultiplier(GAMMA_BUILD_TIME_MULT);
    }

    @Override
    protected void applyBetaCoreModifiers() {
        applyBuildTimeMultiplier(BETA_BUILD_TIME_MULT);
    }

    @Override
    protected void applyAlphaCoreModifiers() {
        applyBuildTimeMultiplier(ALPHA_BUILD_TIME_MULT);
    }

    protected void applyBuildTimeMultiplier(float mult) {
        if (!this.isAICoreBuildTimeMultApplied) {
            applyNoAICoreModifiers();
            float daysLeft = this.buildTime - this.buildProgress;
            this.aiCoreBuildProgressRemoved = daysLeft * mult;
            this.buildProgress = this.buildTime - (daysLeft - this.aiCoreBuildProgressRemoved);
            this.isAICoreBuildTimeMultApplied = true;
            this.prevAICoreId = getAICoreId();
        }
    }

    public boolean hasOngoingProject() {
        return this.project != null;
    }

    @Override
    public boolean isUpgrading() {
        return isBuilding() && hasOngoingProject();
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

        return getProgressText() + ": " + left + " " + days + " left";
    }

    protected String getProgressText() {
        return "building";
    }

    @Override
    public void finishBuildingOrUpgrading() {
        this.building = false;
        this.buildProgress = 0;
        this.buildTime = 1f;
        this.isAICoreBuildTimeMultApplied = false;
        if (hasOngoingProject()) {
            completeProject();
        } else {
            buildingFinished();
            reapply();
        }
    }

    @Override
    public void startUpgrading() {
        if (hasOngoingProject()) {
            this.building = true;
            this.buildProgress = 0;
            this.isAICoreBuildTimeMultApplied = false;
            this.buildTime = this.project.getBuildTime() * Settings.BUILD_TIME_MULTIPLIER;
        }
    }

    @Override
    public void cancelUpgrade() {
        this.building = false;
        this.buildProgress = 0;
        this.aiCoreBuildProgressRemoved = 0f;
        this.isAICoreBuildTimeMultApplied = false;
        this.project = null;
    }

    @Override
    public boolean isAvailableToBuild() {
        if (Settings.isAoTDVoKEnabled()) {
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
        if (Settings.isAoTDVoKEnabled()) {
            return AoTDMainResearchManager.getInstance().isAvailableForThisMarket(getAOTDVOKTechId(), this.market) && this.market.getPlanetEntity() != null && super.showWhenUnavailable();
        }
        return super.showWhenUnavailable();
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
            text.addPara(pre + "Reduces upkeep cost by %s. Reduces " + getProgressText() + " time by %s.", oPad, highlight,
                    (int) ((1f - UPKEEP_MULT) * 100f) + "%", (int) (ALPHA_BUILD_TIME_MULT * 100f) + "%");
            tooltip.addImageWithText(oPad);
            return;
        }

        tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces " + getProgressText() + " time by %s.", oPad, highlight,
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
            text.addPara(pre + "Reduces upkeep cost by %s. Reduces " + getProgressText() + " time by %s.", oPad, highlight,
                    (int) ((1f - UPKEEP_MULT) * 100f) + "%", (int) (BETA_BUILD_TIME_MULT * 100f) + "%");
            tooltip.addImageWithText(oPad);
            return;
        }

        tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces " + getProgressText() + " time by %s.", oPad, highlight,
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
            text.addPara(pre + "Reduces upkeep cost by %s. Reduces " + getProgressText() + " time by %s.", oPad, highlight,
                    (int) ((1f - UPKEEP_MULT) * 100f) + "%", (int) (GAMMA_BUILD_TIME_MULT * 100f) + "%");
            tooltip.addImageWithText(oPad);
            return;
        }

        tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces " + getProgressText() + " time by %s.", oPad, highlight,
                (int) ((1f - UPKEEP_MULT) * 100f) + "%", (int) (GAMMA_BUILD_TIME_MULT * 100f) + "%");
    }

    @Override
    protected void addPostUpkeepSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode) {
        addProjectSection(tooltip, mode);
    }

    protected void addProjectSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode) {
        float oPad = 10f;
        float pad = 3f;

        if (mode == IndustryTooltipMode.NORMAL || isUpgrading()) {
            if (isUpgrading()) {
                MarketConditionSpecAPI conditionSpec = Global.getSettings().getMarketConditionSpec(this.project.getConditionId());
                tooltip.addSectionHeading("Terraforming project", Alignment.MID, oPad);
                TooltipMakerAPI imageWithText = tooltip.beginImageWithText(conditionSpec.getIcon(), 40f, getTooltipWidth(), false);
                imageWithText.addPara("Status: %s", 0f, Misc.getHighlightColor(), "Ongoing");
                imageWithText.addPara("Action: %s", pad, Misc.getHighlightColor(), !this.market.hasCondition(conditionSpec.getId()) ? "Add" : "Remove");
                imageWithText.addPara("Condition: %s", pad, Misc.getHighlightColor(), conditionSpec.getName());
                imageWithText.addPara("Days Left: %s", pad, Misc.getHighlightColor(), Math.round(this.buildTime - this.buildProgress) + "");
                tooltip.addImageWithText(oPad);
            } else {
                tooltip.addSectionHeading("No projects started", Alignment.MID, oPad);
            }
        }
    }

    @Override
    protected void applyAICoreToIncomeAndUpkeep() {
        if (this.aiCoreId == null) {
            getUpkeep().unmodifyMult("ind_core");
            return;
        }

        float mult = UPKEEP_MULT;
        String name = switch (this.aiCoreId) {
            case Commodities.ALPHA_CORE -> "Alpha Core assigned";
            case Commodities.BETA_CORE -> "Beta Core assigned";
            case Commodities.GAMMA_CORE -> "Gamma Core assigned";
            default -> "AI Core assigned";
        };

        getUpkeep().modifyMult("ind_core", mult, name);
    }

    public String getAOTDVOKTechId() {
        return switch (getId()) {
            case TMEIds.AGRICULTURAL_LABORATORY -> TMEIds.AGRICULTURAL_LABORATORY_TECH;
            case TMEIds.ATMOSPHERE_REGULATOR -> TMEIds.ATMOSPHERE_REGULATOR_TECH;
            case TMEIds.ELEMENT_SYNTHESIZER -> TMEIds.ELEMENT_SYNTHESIZER_TECH;
            case TMEIds.GEOMORPHOLOGY_STATION -> TMEIds.GEOMORPHOLOGY_STATION_TECH;
            case TMEIds.MINERAL_REPLICATOR -> TMEIds.MINERAL_REPLICATOR_TECH;
            case TMEIds.PLANETARY_HOLOGRAM -> TMEIds.PLANETARY_HOLOGRAM_TECH;
            case TMEIds.STELLAR_MANUFACTORY -> TMEIds.STELLAR_MANUFACTORY_TECH;
            case TMEIds.TERRESTRIAL_ENGINE -> TMEIds.TERRESTRIAL_ENGINE_TECH;
            case TMEIds.UNIFICATION_CENTER -> TMEIds.UNIFICATION_CENTER_TECH;
            default -> null;
        };
    }

    public List<TerraformingProjectSpec> getProjects() {
        return Settings.getAllTerraformingProjectSpecs().stream().filter(p -> p.getStructureId().equals(getId())).toList();
    }

    public TerraformingProjectSpec getProject() {
        return this.project;
    }

    public void setProject(TerraformingProjectSpec option) {
        this.project = option;
    }

    public void completeProject() {
        MarketConditionSpecAPI conditionSpec = Global.getSettings().getMarketConditionSpec(this.project.getConditionId());
        log.info(String.format(
                "Completed %s %s condition in %s by %s",
                !this.market.hasCondition(conditionSpec.getId()) ? "Adding" : "Removing",
                conditionSpec.getName(), getMarket().getName(), getCurrentName()));

        sendCompletedMessage();
        terraformPlanet();
        updatePlanetVisuals();
        reapply();
        for (Industry ind : this.market.getIndustries()) { // Force reapply industries demands and supply
            ind.doPreSaveCleanup();
            ind.doPostSaveRestore();
        }
        this.project = null;
    }

    public void sendCompletedMessage() {
        sendCompletedMessage(getMarket(), this.project);
    }

    public void terraformPlanet() {
        terraformPlanet(getMarket(), this.project);
    }

    public void updatePlanetVisuals() {
        if (this.market.getPrimaryEntity() instanceof PlanetAPI) {
            updatePlanetConditions(getMarket());
            String category = evaluatePlanetCategory(getMarket());
            String type = evaluatePlanetType(category, getMarket());
            updatePlanetVisuals(type, getMarket(), this.project);
        }
    }
}
