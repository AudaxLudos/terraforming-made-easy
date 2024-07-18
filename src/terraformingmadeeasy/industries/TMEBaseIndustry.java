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
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import terraformingmadeeasy.Utils;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class TMEBaseIndustry extends BaseIndustry {
    public static final Logger log = Global.getLogger(TMEBaseIndustry.class);
    public static final String TERRAFORMING_OPTIONS_FILE = "data/config/terraforming_options.csv";
    public static final float GAMMA_BUILD_TIME_MULT = 0.20f;
    public static final float BETA_BUILD_TIME_MULT = 0.30f;
    public static final float ALPHA_BUILD_TIME_MULT = 0.50f;
    public List<Utils.ModifiableCondition> modifiableConditions = new ArrayList<>();
    public Utils.ModifiableCondition modifiableCondition = null;
    public Boolean isAICoreBuildTimeMultApplied = false;
    public float aiCoreCurrentBuildTimeMult = 0f;
    public float aiCoreBuildProgressRemoved = 0f;
    public String prevAICoreId = null;
    public boolean hasAtLeastOneLikedCondition = false;
    public boolean isExpanded = false;

    @Override
    public void apply() {
        apply(true);
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);

        if (!this.isAICoreBuildTimeMultApplied) {
            if (Objects.equals(this.aiCoreId, Commodities.ALPHA_CORE)) {
                this.aiCoreCurrentBuildTimeMult = ALPHA_BUILD_TIME_MULT;
            } else if (Objects.equals(this.aiCoreId, Commodities.BETA_CORE)) {
                this.aiCoreCurrentBuildTimeMult = BETA_BUILD_TIME_MULT;
            } else if (Objects.equals(this.aiCoreId, Commodities.GAMMA_CORE)) {
                this.aiCoreCurrentBuildTimeMult = GAMMA_BUILD_TIME_MULT;
            } else {
                this.aiCoreCurrentBuildTimeMult = 0f;
            }

            float daysLeft = this.buildTime - this.buildProgress;
            this.aiCoreBuildProgressRemoved = daysLeft * this.aiCoreCurrentBuildTimeMult;
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
            log.info(String.format("Completed %s of %s condition in %s by %s", this.modifiableCondition.name, !this.market.hasCondition(this.modifiableCondition.id) ? "Adding" : "Removing", getMarket().getName(), getCurrentName()));
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
            log.info(String.format("Start %s of %s condition in %s by %s", this.modifiableCondition.name, !this.market.hasCondition(this.modifiableCondition.id) ? "Adding" : "Removing", getMarket().getName(), getCurrentName()));
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
        log.info(String.format("Cancel %s of %s condition in %s by %s", this.modifiableCondition.name, !this.market.hasCondition(this.modifiableCondition.id) ? "Adding" : "Removing", getMarket().getName(), getCurrentName()));
        this.building = false;
        this.buildProgress = 0;
        this.aiCoreBuildProgressRemoved = 0f;
        this.isAICoreBuildTimeMultApplied = false;
        this.modifiableCondition = null;
    }

    @Override
    public boolean isAvailableToBuild() {
        if (!super.isAvailableToBuild()) {
            return false;
        }
        return this.market.getPlanetEntity() != null;
    }

    @Override
    public String getUnavailableReason() {
        if (!super.isAvailableToBuild()) {
            return super.getUnavailableReason();
        }
        return "Requires a planet";
    }

    @Override
    public boolean isTooltipExpandable() {
        return true;
    }

    @Override
    public void createTooltip(IndustryTooltipMode mode, TooltipMakerAPI tooltip, boolean expanded) {
        this.isExpanded = expanded;
        super.createTooltip(mode, tooltip, expanded);
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
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode) {
        if (this.isExpanded) {
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

    public void getTerraformingOptions(String industryId) {
        try {
            JSONArray data = Global.getSettings().loadCSV(TERRAFORMING_OPTIONS_FILE);
            for (int i = 0; i < data.length(); i++) {
                JSONObject row = data.getJSONObject(i);
                if (!Objects.equals(row.getString("structureId"), industryId)) {
                    continue;
                }
                if (row.getString("structureId").isEmpty()) {
                    continue;
                }
                if (row.getString("structureId").contains("#")) {
                    continue;
                }
                if (Global.getSettings().getMarketConditionSpec(row.getString("conditionId")) == null) {
                    continue;
                }

                String conditionId = row.getString("conditionId");
                float buildTime = row.getInt("buildTime");
                float cost = row.getInt("cost");
                boolean canChangeGasGiants = row.getBoolean("canChangeGasGiants");

                List<String> likedConditions = new ArrayList<>();
                List<String> hatedConditions = new ArrayList<>();
                List<String> likedIndustries = new ArrayList<>();
                List<String> hatedIndustries = new ArrayList<>();
                if (!row.getString("likedConditions").isEmpty()) {
                    likedConditions.addAll(Arrays.asList(row.getString("likedConditions").replace(" ", "").split(",")));
                    likedConditions = filterUnknownSpecs(likedConditions, false);
                }
                if (!row.getString("hatedConditions").isEmpty()) {
                    hatedConditions.addAll(Arrays.asList(row.getString("hatedConditions").replace(" ", "").split(",")));
                    hatedConditions = filterUnknownSpecs(hatedConditions, false);
                }
                if (!row.getString("likedIndustries").isEmpty()) {
                    likedIndustries.addAll(Arrays.asList(row.getString("likedIndustries").replace(" ", "").split(",")));
                    likedIndustries = filterUnknownSpecs(likedIndustries, true);
                }
                if (!row.getString("hatedIndustries").isEmpty()) {
                    hatedIndustries.addAll(Arrays.asList(row.getString("hatedIndustries").replace(" ", "").split(",")));
                    hatedIndustries = filterUnknownSpecs(hatedIndustries, true);
                }
                String planetSpecOverride = row.getString("planetSpecOverride");

                this.modifiableConditions.add(new Utils.ModifiableCondition(
                        Global.getSettings().getMarketConditionSpec(conditionId),
                        cost,
                        buildTime,
                        canChangeGasGiants,
                        likedConditions,
                        hatedConditions,
                        likedIndustries,
                        hatedIndustries,
                        planetSpecOverride
                ));
            }
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> filterUnknownSpecs(List<String> ids, boolean isIndustry) {
        for (Iterator<String> itr = ids.iterator(); itr.hasNext(); ) {
            String id = itr.next();

            if (isIndustry) {
                if (id.isEmpty() || Global.getSettings().getIndustrySpec(id) == null) {
                    itr.remove();
                }
            } else {
                if (id.isEmpty() || Global.getSettings().getMarketConditionSpec(id) == null) {
                    itr.remove();
                }
            }
        }

        return ids;
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
            for (String restriction : this.modifiableCondition.hatedConditions) {
                if (this.market.hasCondition(restriction)) {
                    this.market.removeCondition(restriction);
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
        return hasLikedConditions(condition) && hasLikedIndustries(condition);
    }

    public boolean hasLikedConditions(Utils.ModifiableCondition condition) {
        // Checks if market has at least one of these condition
        this.hasAtLeastOneLikedCondition = true;
        if (!condition.likedConditions.isEmpty()) {
            boolean hasOneLikedCondition = false;
            for (String conditionId : condition.likedConditions) {
                hasOneLikedCondition = hasOneLikedCondition || this.market.hasCondition(conditionId);
            }
            return hasOneLikedCondition;
        }
        return true;
    }

    public boolean hasLikedIndustries(Utils.ModifiableCondition condition) {
        // Checks if market has all industries
        if (!condition.likedIndustries.isEmpty()) {
            for (String industryId : condition.likedIndustries) {
                if (!this.market.hasIndustry(industryId)) {
                    return false;
                }
            }
        }

        return true;
    }
}
