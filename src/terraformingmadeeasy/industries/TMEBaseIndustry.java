package terraformingmadeeasy.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetSpecAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
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
import terraformingmadeeasy.Utils;

import java.awt.*;
import java.util.List;
import java.util.*;

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
    protected void applyNoAICoreModifiers() {
        if (this.isAICoreBuildTimeMultApplied && !Objects.equals(getAICoreId(), this.prevAICoreId)) {
            this.buildProgress = this.buildProgress - this.aiCoreBuildProgressRemoved;
            this.aiCoreBuildProgressRemoved = 0f;
            this.isAICoreBuildTimeMultApplied = false;
        }
    }

    @Override
    protected void applyGammaCoreModifiers() {
        if (!this.isAICoreBuildTimeMultApplied) {
            applyNoAICoreModifiers();
            applyBuildTimeMultiplier(GAMMA_BUILD_TIME_MULT);
        }
    }

    @Override
    protected void applyBetaCoreModifiers() {
        if (!this.isAICoreBuildTimeMultApplied) {
            applyNoAICoreModifiers();
            applyBuildTimeMultiplier(BETA_BUILD_TIME_MULT);
        }
    }

    @Override
    protected void applyAlphaCoreModifiers() {
        if (!this.isAICoreBuildTimeMultApplied) {
            applyNoAICoreModifiers();
            applyBuildTimeMultiplier(ALPHA_BUILD_TIME_MULT);
        }
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
            terraformPlanet();
            updatePlanetConditions();
            String category = evaluatePlanetCategory();
            String type = evaluatePlanetType(category);
            updatePlanetVisuals(type);
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
            this.buildTime = this.modifiableCondition.buildTime * Utils.BUILD_TIME_MULTIPLIER;
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

    @Override
    protected void applyAICoreToIncomeAndUpkeep() {
        if (this.aiCoreId == null) {
            getUpkeep().unmodifyMult("ind_core");
            return;
        }

        float mult = UPKEEP_MULT;
        String name = "AI Core assigned";
        if (Objects.equals(this.aiCoreId, Commodities.ALPHA_CORE)) {
            name = "Alpha Core assigned";
        } else if (Objects.equals(this.aiCoreId, Commodities.BETA_CORE)) {
            name = "Beta Core assigned";
        } else if (Objects.equals(this.aiCoreId, Commodities.GAMMA_CORE)) {
            name = "Gamma Core assigned";
        }

        getUpkeep().modifyMult("ind_core", mult, name);
    }

    public void addTerraformingOptionList(TooltipMakerAPI tooltip, IndustryTooltipMode mode) {
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

    public void terraformPlanet() {
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

    public void updatePlanetConditions() {
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
                && !this.market.hasCondition(Conditions.DARK) && !this.market.hasCondition(Conditions.EXTREME_TECTONIC_ACTIVITY)) {
            addOrImproveFarming();
            addOrImproveOrganics();
            this.market.addCondition(Conditions.HABITABLE);
        }
        if (this.market.hasCondition(Conditions.WATER_SURFACE)) {
            removeFarming = true;
            removeLobsters = false;
            removeWaterSurface = false;
        }
        if (this.market.hasCondition(Conditions.NO_ATMOSPHERE) || this.market.hasCondition(Conditions.VERY_HOT) ||
                this.market.hasCondition(Conditions.VERY_COLD) || this.market.hasCondition(Conditions.EXTREME_TECTONIC_ACTIVITY)) {
            removeFarming = true;
            removeOrganics = true;
            removeLobsters = true;
            removeWaterSurface = true;
        }
        if (this.market.hasCondition(Conditions.THIN_ATMOSPHERE)) {
            removeFarming = true;
            reduceOrganics = true;
            removeLobsters = true;
            removeWaterSurface = true;
        }
        if (this.market.hasCondition(Conditions.TOXIC_ATMOSPHERE)) {
            removeFarming = true;
            reduceOrganics = true;
            removeLobsters = true;
            removeWaterSurface = true;
        }
        if (this.market.hasCondition(Conditions.IRRADIATED)) {
            removeFarming = true;
            removeOrganics = true;
            reduceOrganics = false;
            removeLobsters = true;
            removeWaterSurface = true;
        }
        if (!this.market.hasCondition(Conditions.HABITABLE) && !this.market.hasCondition(Conditions.NO_ATMOSPHERE)) {
            if (this.market.hasCondition(Conditions.TECTONIC_ACTIVITY) || this.market.hasCondition(Conditions.EXTREME_TECTONIC_ACTIVITY)) {
                if (this.market.hasCondition(Conditions.VERY_HOT) || this.market.hasCondition(Conditions.HOT)) {
                    removeFarming = true;
                    removeOrganics = true;
                    reduceOrganics = false;
                    removeLobsters = true;
                    removeWaterSurface = true;
                }
                if (this.market.hasCondition((Conditions.VERY_COLD))) {
                    removeFarming = true;
                    removeOrganics = true;
                    reduceOrganics = false;
                    removeLobsters = true;
                    removeWaterSurface = true;
                }
            }
        }
        if (this.market.getPlanetEntity().isGasGiant()) {
            removeFarming = true;
            removeOrganics = true;
            reduceOrganics = false;
            removeLobsters = true;
            removeWaterSurface = true;
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
    }

    public String evaluatePlanetCategory() {
        PlanetGenDataSpec spec = (PlanetGenDataSpec) Global.getSettings()
                .getSpec(PlanetGenDataSpec.class, this.market.getPlanetEntity().getTypeId(), false);
        String result = spec.getCategory();

        if (this.market.hasCondition(Conditions.HABITABLE)) {
            result = "cat_hab4";

            if (this.market.hasCondition(Conditions.HOT) || this.market.hasCondition(Conditions.WATER_SURFACE) || this.market.hasCondition(Conditions.COLD)) {
                result = "cat_hab3";
            }
            if (!this.market.hasCondition(Conditions.WATER_SURFACE) && this.market.hasCondition(Conditions.HOT) && this.market.hasCondition(Conditions.EXTREME_WEATHER)) {
                result = "cat_hab2";
            }
        }
        if (this.market.hasCondition(Conditions.THIN_ATMOSPHERE)) {
            result = "cat_hab1";
        }
        if (this.market.hasCondition(Conditions.VERY_HOT) || this.market.hasCondition(Conditions.EXTREME_TECTONIC_ACTIVITY)) {
            result = "cat_lava";
        }
        if (this.market.hasCondition(Conditions.NO_ATMOSPHERE)) {
            result = "cat_barren";
        }
        if (this.market.hasCondition(Conditions.VERY_COLD)) {
            result = "cat_frozen";
        }
        if (this.market.hasCondition(Conditions.TOXIC_ATMOSPHERE)) {
            result = "cat_toxic";
        }
        if (this.market.hasCondition(Conditions.IRRADIATED)) {
            result = "cat_irradiated";
        }
        if (!this.market.hasCondition(Conditions.HABITABLE) && !this.market.hasCondition(Conditions.NO_ATMOSPHERE)) {
            if (this.market.hasCondition(Conditions.TECTONIC_ACTIVITY) || this.market.hasCondition(Conditions.EXTREME_TECTONIC_ACTIVITY)) {
                if (this.market.hasCondition(Conditions.VERY_HOT) || this.market.hasCondition(Conditions.HOT)) {
                    result = "cat_lava";
                }
                if (this.market.hasCondition((Conditions.VERY_COLD))) {
                    result = "cat_cryovolcanic";
                }
            }
        }
        if (this.market.getPlanetEntity().isGasGiant()) {
            result = "cat_giant";
        }

        return result;
    }

    public String evaluatePlanetType(String category) {
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
                if (this.market.hasCondition(Conditions.WATER_SURFACE)) {
                    picker.setWeight(index, 100f);
                } else {
                    picker.remove("water");
                }
            }
            if (items.contains("tundra")) {
                int index = items.indexOf("tundra");
                if (this.market.hasCondition(Conditions.COLD)) {
                    picker.setWeight(index, 100f);
                } else {
                    picker.remove("tundra");
                }
            }
        } else if (Objects.equals(category, "cat_barren")) {
            if (items.contains("rocky_unstable")) {
                int index = items.indexOf("rocky_unstable");
                if (this.market.hasCondition(Conditions.TECTONIC_ACTIVITY) || this.market.hasCondition(Conditions.EXTREME_TECTONIC_ACTIVITY)) {
                    picker.setWeight(index, 100f);
                } else {
                    picker.remove("rocky_unstable");
                }
            }
            if (items.contains("rocky_ice")) {
                int index = items.indexOf("rocky_ice");
                if (this.market.hasCondition(Conditions.COLD)) {
                    picker.setWeight(index, 100f);
                } else {
                    picker.remove("rocky_ice");
                }
            }
        } else if (Objects.equals(category, "cat_giant")) {
            if (items.contains("ice_giant")) {
                int index = items.indexOf("ice_giant");
                if (this.market.hasCondition(Conditions.COLD) || this.market.hasCondition(Conditions.VERY_COLD)) {
                    picker.setWeight(index, 100f);
                } else {
                    picker.remove("ice_giant");
                }
            }
        }

        return picker.pick();
    }

    public void removeWaterSurface() {
        this.market.removeCondition(Conditions.WATER_SURFACE);
    }

    public void updateFarmingOrAquaculture() {
        if (Utils.isAOTDVOKEnabled()) {
            if (!this.market.hasCondition(Conditions.WATER_SURFACE)) {
                if (this.market.hasIndustry(Industries.AQUACULTURE)) {
                    this.market.removeIndustry(Industries.AQUACULTURE, null, false);
                    this.market.addIndustry(Industries.FARMING);
                } else if (this.market.hasIndustry("fishery")) {
                    this.market.removeIndustry("fishery", null, false);
                    this.market.addIndustry("subfarming");
                }
            } else if (this.market.hasCondition(Conditions.WATER_SURFACE)) {
                if (this.market.hasIndustry(Industries.FARMING)) {
                    this.market.removeIndustry(Industries.FARMING, null, false);
                    this.market.addIndustry(Industries.AQUACULTURE);
                } else if (this.market.hasIndustry("subfarming")) {
                    this.market.removeIndustry("subfarming", null, false);
                    this.market.addIndustry("fishery");
                } else if (this.market.hasIndustry("artifarming")) {
                    this.market.removeIndustry("artifarming", null, false);
                    this.market.addIndustry("fishery");
                }
            }
        } else {
            if (!this.market.hasCondition(Conditions.WATER_SURFACE) && this.market.hasIndustry(Industries.AQUACULTURE)) {
                this.market.removeIndustry(Industries.AQUACULTURE, null, false);
                this.market.addIndustry(Industries.FARMING);
            } else if (this.market.hasCondition(Conditions.WATER_SURFACE) && this.market.hasIndustry(Industries.FARMING)) {
                this.market.removeIndustry(Industries.FARMING, null, false);
                this.market.addIndustry(Industries.AQUACULTURE);
            }
        }
    }

    public void updatePlanetVisuals(String planetTypeId) {
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
