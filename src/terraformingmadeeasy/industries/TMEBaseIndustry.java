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
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.loading.specs.PlanetSpec;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import terraformingmadeeasy.Utils;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class TMEBaseIndustry extends BaseIndustry {
    public static final String TERRAFORMING_OPTIONS_FILE = "data/config/terraforming_options.csv";
    public static final float GAMMA_BUILD_TIME_MULT = 0.20f;
    public static final float BETA_BUILD_TIME_MULT = 0.30f;
    public static final float ALPHA_BUILD_TIME_MULT = 0.50f;
    public List<Utils.ModifiableCondition> modifiableConditions = new ArrayList<>();
    public Utils.ModifiableCondition modifiableCondition = null;
    public Boolean isAICoreBuildTimeMultApplied = false;
    public float aiCoreCurrentBuildTimeMult = 0f;
    public boolean firstTick = false;
    public String prevAICoreId = null;
    public boolean hasAtLeastOneLikedCondition = false;

    @Override
    public void apply() {
        apply(true);
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);
        if (firstTick) {
            boolean alpha = Objects.equals(aiCoreId, Commodities.ALPHA_CORE);
            boolean beta = Objects.equals(aiCoreId, Commodities.BETA_CORE);
            boolean gamma = Objects.equals(aiCoreId, Commodities.GAMMA_CORE);

            if (alpha) {
                aiCoreCurrentBuildTimeMult = ALPHA_BUILD_TIME_MULT;
            } else if (beta) {
                aiCoreCurrentBuildTimeMult = BETA_BUILD_TIME_MULT;
            } else if (gamma) {
                aiCoreCurrentBuildTimeMult = GAMMA_BUILD_TIME_MULT;
            }

            if (aiCoreId != null && !isAICoreBuildTimeMultApplied) {
                buildTime = buildTime * (1f - aiCoreCurrentBuildTimeMult);
                isAICoreBuildTimeMultApplied = true;
            } else {
                aiCoreCurrentBuildTimeMult = 0f;
                isAICoreBuildTimeMultApplied = false;
            }

            prevAICoreId = getAICoreId();
            firstTick = false;
        }

        if (!Objects.equals(getAICoreId(), prevAICoreId)) {
            buildTime = buildTime / (1f - aiCoreCurrentBuildTimeMult);
            isAICoreBuildTimeMultApplied = false;
            firstTick = true;
        }
    }

    @Override
    public String getBuildOrUpgradeProgressText() {
        if (isDisrupted()) {
            int left = (int) getDisruptedDays();
            if (left < 1) left = 1;
            String days = "days";
            if (left == 1) days = "day";

            return "Disrupted: " + left + " " + days + " left";
        }

        int left = (int) (buildTime - buildProgress);
        if (left < 1) left = 1;
        String days = "days";
        if (left == 1) days = "day";

        if (isUpgrading()) {
            return "Terraforming: " + left + " " + days + " left";
        } else {
            return "Building: " + left + " " + days + " left";
        }
    }

    @Override
    public void reapply() {
        super.reapply();
        for (Industry ind : market.getIndustries()) {
            ind.doPreSaveCleanup();
            ind.doPostSaveRestore();
        }
    }

    @Override
    public boolean isAvailableToBuild() {
        if (!super.isAvailableToBuild()) return false;
        return market.getPlanetEntity() != null;
    }

    @Override
    public String getUnavailableReason() {
        if (!super.isAvailableToBuild()) return super.getUnavailableReason();
        return "Requires a planet";
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
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(aiCoreId);
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
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(aiCoreId);
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
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(aiCoreId);
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
    protected void updateAICoreToSupplyAndDemandModifiers() {
        // TME industries don't supply or demand commodities for now
    }

    public void getTerraformingOptions(String industryId) {
        try {
            JSONArray data = Global.getSettings().loadCSV(TERRAFORMING_OPTIONS_FILE);
            for (int i = 0; i < data.length(); i++) {
                JSONObject row = data.getJSONObject(i);
                if (!Objects.equals(row.getString("structureId"), industryId)) continue;
                if (row.getString("structureId").isEmpty()) continue;
                if (row.getString("structureId").contains("#")) continue;
                if (Global.getSettings().getMarketConditionSpec(row.getString("conditionId")) == null) continue;

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
                if (id.isEmpty() || Global.getSettings().getIndustrySpec(id) == null)
                    itr.remove();
            } else {
                if (id.isEmpty() || Global.getSettings().getMarketConditionSpec(id) == null)
                    itr.remove();
            }
        }

        return ids;
    }

    public boolean isUpgrading() {
        return building && modifiableCondition != null;
    }

    public void finishBuildingOrUpgrading() {
        building = false;
        buildProgress = 0;
        buildTime = 1f;
        isAICoreBuildTimeMultApplied = false;
        if (modifiableCondition != null) {
            sendCompletedMessage();
            changePlanetConditions();
            changePlanetClass();
            reapply();
            modifiableCondition = null;
        } else {
            buildingFinished();
            reapply();
        }
    }

    public void startUpgrading(Utils.ModifiableCondition condition) {
        // Will be called from TerraformDialogDelegate to start terraforming
        building = true;
        buildProgress = 0;
        modifiableCondition = condition;
        buildTime = condition.buildTime;
        firstTick = true;
    }

    public void cancelUpgrade() {
        // Will be called from ConfirmDialogDelegate to cancel terraforming
        building = false;
        buildProgress = 0;
        modifiableCondition = null;
        isAICoreBuildTimeMultApplied = false;
    }

    public void sendCompletedMessage() {
        if (market.isPlayerOwned()) {
            String addOrRemoveText = !market.hasCondition(modifiableCondition.id) ? "Added " : "Removed ";
            MessageIntel intel = new MessageIntel("Terraforming completed at " + market.getName(), Misc.getBasePlayerColor());
            intel.addLine(BaseIntelPlugin.BULLET + addOrRemoveText + modifiableCondition.name.toLowerCase() + " planet condition");
            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
        }
    }

    public void changePlanetConditions() {
        if (market.hasCondition(modifiableCondition.id)) {
            market.removeCondition(modifiableCondition.id);
        } else {
            market.addCondition(modifiableCondition.id);
            market.getFirstCondition(modifiableCondition.id).setSurveyed(true);
            for (String restriction : modifiableCondition.hatedConditions) {
                if (market.hasCondition(restriction))
                    market.removeCondition(restriction);
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
        if (!market.getPlanetEntity().isStar() && !market.getPlanetEntity().isGasGiant()
                && !market.hasCondition(Conditions.HABITABLE) && !market.hasCondition(Conditions.VERY_COLD)
                && !market.hasCondition(Conditions.VERY_HOT) && !market.hasCondition(Conditions.NO_ATMOSPHERE)
                && !market.hasCondition(Conditions.THIN_ATMOSPHERE) && !market.hasCondition(Conditions.DENSE_ATMOSPHERE)
                && !market.hasCondition(Conditions.TOXIC_ATMOSPHERE) && !market.hasCondition(Conditions.IRRADIATED)
                && !market.hasCondition(Conditions.DARK)) {
            addOrImproveFarming();
            addOrImproveOrganics();
            market.addCondition(Conditions.HABITABLE);
        }
        if (market.hasCondition(Conditions.HABITABLE)) {
            String[] richHabitableTypes = {"terran", "terran-eccentric"};
            planetTypeId = richHabitableTypes[Misc.random.nextInt(richHabitableTypes.length)];
            if (market.hasCondition(Conditions.HOT)) {
                String[] poorHabitableTypes = {"jungle", "arid", "desert", "desert1"};
                planetTypeId = poorHabitableTypes[Misc.random.nextInt(poorHabitableTypes.length)];
            }
            if (market.hasCondition(Conditions.COLD)) {
                planetTypeId = "tundra";
            }
        }
        if (market.hasCondition(Conditions.NO_ATMOSPHERE) || market.hasCondition(Conditions.VERY_HOT) || market.hasCondition(Conditions.VERY_COLD)) {
            String[] poorBarrenTypes = {"barren", "barren2", "barren3", "barren_castiron", "barren_venuslike", "rocky_metallic", "barren-bombarded"};
            planetTypeId = poorBarrenTypes[Misc.random.nextInt(poorBarrenTypes.length)];
            removeFarming = true;
            removeOrganics = true;
            if (market.hasCondition(Conditions.TECTONIC_ACTIVITY) || market.hasCondition(Conditions.EXTREME_TECTONIC_ACTIVITY))
                planetTypeId = "rocky_unstable";
        }
        if (market.hasCondition(Conditions.WATER_SURFACE)) {
            planetTypeId = "water";
            removeFarming = true;
            removeLobsters = false;
            removeWaterSurface = false;
            if (market.hasCondition(Conditions.VERY_COLD)) {
                String[] frozenTypes = {"frozen", "frozen1", "frozen2", "frozen3"};
                planetTypeId = frozenTypes[Misc.random.nextInt(frozenTypes.length)];
                removeWaterSurface = true;
                removeOrganics = true;
                removeLobsters = true;
            }
            if ((market.hasCondition(Conditions.COLD) || market.hasCondition(Conditions.VERY_COLD)) && market.hasCondition(Conditions.NO_ATMOSPHERE)) {
                planetTypeId = "rocky_ice";
                removeWaterSurface = true;
                removeOrganics = true;
                removeLobsters = true;
            }
        }
        if (market.hasCondition(Conditions.THIN_ATMOSPHERE)) {
            planetTypeId = "barren-desert";
            removeFarming = true;
            reduceOrganics = true;
            removeWaterSurface = true;
        }
        if (market.hasCondition(Conditions.TOXIC_ATMOSPHERE)) {
            planetTypeId = "toxic";
            removeFarming = true;
            reduceOrganics = true;
            removeWaterSurface = true;
        }
        if (market.hasCondition(Conditions.IRRADIATED)) {
            planetTypeId = "irradiated";
            removeFarming = true;
            removeOrganics = true;
            reduceOrganics = false;
        }
        if (market.hasCondition(Conditions.TECTONIC_ACTIVITY) || market.hasCondition(Conditions.EXTREME_TECTONIC_ACTIVITY)) {
            if ((market.hasCondition(Conditions.VERY_HOT) || market.hasCondition(Conditions.HOT))
                    && (market.hasCondition(Conditions.TOXIC_ATMOSPHERE) || market.hasCondition(Conditions.THIN_ATMOSPHERE) || market.hasCondition(Conditions.DENSE_ATMOSPHERE))
                    && (market.hasCondition(Conditions.ORE_ABUNDANT) || market.hasCondition(Conditions.ORE_RICH) || market.hasCondition(Conditions.ORE_ULTRARICH))
                    && (market.hasCondition(Conditions.RARE_ORE_ABUNDANT) || market.hasCondition(Conditions.RARE_ORE_RICH) || market.hasCondition(Conditions.RARE_ORE_ULTRARICH))) {
                planetTypeId = "lava";
                removeFarming = true;
                removeOrganics = true;
                reduceOrganics = false;
            }
            if (market.hasCondition(Conditions.VERY_COLD)
                    && (market.hasCondition(Conditions.VOLATILES_TRACE) || market.hasCondition(Conditions.VOLATILES_DIFFUSE) ||
                    market.hasCondition(Conditions.VOLATILES_ABUNDANT) || market.hasCondition(Conditions.VOLATILES_PLENTIFUL))) {
                planetTypeId = "cryovolcanic";
                removeFarming = true;
                removeOrganics = true;
                reduceOrganics = false;
            }
        }
        if (market.getPlanetEntity().isGasGiant()) {
            removeFarming = true;
            removeOrganics = true;
            reduceOrganics = false;
            if (market.hasCondition(Conditions.VERY_COLD))
                planetTypeId = "ice_giant";
            if (market.hasCondition(Conditions.VERY_HOT))
                planetTypeId = "gas_giant";
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
        market.removeCondition(Conditions.WATER_SURFACE);
    }

    public void updateFarmingOrAquaculture() {
        if (!market.hasCondition(Conditions.WATER_SURFACE) && market.hasIndustry(Industries.AQUACULTURE)) {
            market.removeIndustry(Industries.AQUACULTURE, null, false);
            market.addIndustry(Industries.FARMING);
        } else if (market.hasCondition(Conditions.WATER_SURFACE) && market.hasIndustry(Industries.FARMING)) {
            market.removeIndustry(Industries.FARMING, null, false);
            market.addIndustry(Industries.AQUACULTURE);
        }
    }

    public void changePlanetVisuals(String planetTypeId) {
        String planetType = planetTypeId;
        PlanetSpecAPI marketSpec = market.getPlanetEntity().getSpec();
        if (modifiableCondition.planetSpecOverride != null) {
            for (PlanetSpecAPI spec : Global.getSettings().getAllPlanetSpecs()) {
                if (spec.isStar()) continue;
                if (Objects.equals(spec.getPlanetType(), modifiableCondition.planetSpecOverride)) {
                    marketSpec = spec;
                    planetType = spec.getPlanetType();
                    break;
                }
            }
        }
        for (PlanetSpecAPI spec : Global.getSettings().getAllPlanetSpecs()) {
            if (spec.getPlanetType().equals(planetType)) {
                marketSpec.setAtmosphereColor(spec.getAtmosphereColor());
                marketSpec.setAtmosphereThickness(spec.getAtmosphereThickness());
                marketSpec.setAtmosphereThicknessMin(spec.getAtmosphereThicknessMin());
                marketSpec.setCloudColor(spec.getCloudColor());
                marketSpec.setCloudRotation(spec.getCloudRotation());
                marketSpec.setCloudTexture(spec.getCloudTexture());
                marketSpec.setGlowColor(spec.getGlowColor());
                marketSpec.setGlowTexture(spec.getGlowTexture());
                marketSpec.setIconColor(spec.getIconColor());
                marketSpec.setPlanetColor(spec.getPlanetColor());
                marketSpec.setStarscapeIcon(spec.getStarscapeIcon());
                marketSpec.setTexture(spec.getTexture());
                marketSpec.setUseReverseLightForGlow(spec.isUseReverseLightForGlow());
                ((PlanetSpec) marketSpec).planetType = planetType;
                ((PlanetSpec) marketSpec).name = spec.getName();
                ((PlanetSpec) marketSpec).descriptionId = ((PlanetSpec) spec).descriptionId;
                break;
            }
        }
        market.getPlanetEntity().changeType(planetType, StarSystemGenerator.random);
        market.getPlanetEntity().applySpecChanges();
    }

    public void removeFarming() {
        market.removeCondition(Conditions.FARMLAND_POOR);
        market.removeCondition(Conditions.FARMLAND_ADEQUATE);
        market.removeCondition(Conditions.FARMLAND_RICH);
        market.removeCondition(Conditions.FARMLAND_BOUNTIFUL);
    }

    public void removeOrganics() {
        market.removeCondition(Conditions.ORGANICS_TRACE);
        market.removeCondition(Conditions.ORGANICS_COMMON);
        market.removeCondition(Conditions.ORGANICS_ABUNDANT);
        market.removeCondition(Conditions.ORGANICS_PLENTIFUL);
    }

    public void removeLobsters() {
        market.removeCondition(Conditions.VOLTURNIAN_LOBSTER_PENS);
    }

    public void addOrImproveFarming() {
        if (market.hasCondition(Conditions.FARMLAND_POOR)) {
            market.removeCondition(Conditions.FARMLAND_POOR);
            market.addCondition(Conditions.FARMLAND_ADEQUATE);
            market.getFirstCondition(Conditions.FARMLAND_ADEQUATE).setSurveyed(true);
        } else if (market.hasCondition(Conditions.FARMLAND_ADEQUATE)) {
            market.removeCondition(Conditions.FARMLAND_ADEQUATE);
            market.addCondition(Conditions.FARMLAND_RICH);
            market.getFirstCondition(Conditions.FARMLAND_RICH).setSurveyed(true);
        } else if (market.hasCondition(Conditions.FARMLAND_RICH)) {
            market.removeCondition(Conditions.FARMLAND_RICH);
            market.addCondition(Conditions.FARMLAND_BOUNTIFUL);
            market.getFirstCondition(Conditions.FARMLAND_BOUNTIFUL).setSurveyed(true);
        } else if (!market.hasCondition(Conditions.FARMLAND_BOUNTIFUL)) {
            market.addCondition(Conditions.FARMLAND_POOR);
            market.getFirstCondition(Conditions.FARMLAND_POOR).setSurveyed(true);
        }
    }

    public void addOrImproveOrganics() {
        if (market.hasCondition(Conditions.ORGANICS_TRACE)) {
            market.removeCondition(Conditions.ORGANICS_TRACE);
            market.addCondition(Conditions.ORGANICS_COMMON);
            market.getFirstCondition(Conditions.ORGANICS_COMMON).setSurveyed(true);
        } else if (market.hasCondition(Conditions.ORGANICS_COMMON)) {
            market.removeCondition(Conditions.ORGANICS_COMMON);
            market.addCondition(Conditions.ORGANICS_ABUNDANT);
            market.getFirstCondition(Conditions.ORGANICS_ABUNDANT).setSurveyed(true);
        } else if (market.hasCondition(Conditions.ORGANICS_ABUNDANT)) {
            market.removeCondition(Conditions.ORGANICS_ABUNDANT);
            market.addCondition(Conditions.ORGANICS_PLENTIFUL);
            market.getFirstCondition(Conditions.ORGANICS_PLENTIFUL).setSurveyed(true);
        } else if (!market.hasCondition(Conditions.ORGANICS_PLENTIFUL)) {
            market.addCondition(Conditions.ORGANICS_TRACE);
            market.getFirstCondition(Conditions.ORGANICS_TRACE).setSurveyed(true);
        }
    }

    public void reduceOrganicsToCommon() {
        if (market.hasCondition(Conditions.ORGANICS_ABUNDANT) || market.hasCondition(Conditions.ORGANICS_PLENTIFUL)) {
            market.removeCondition(Conditions.ORGANICS_ABUNDANT);
            market.removeCondition(Conditions.ORGANICS_PLENTIFUL);
            market.addCondition(Conditions.ORGANICS_COMMON);
            market.getFirstCondition(Conditions.ORGANICS_COMMON).setSurveyed(true);
        }
    }

    public Boolean canTerraformCondition(Utils.ModifiableCondition condition) {
        return hasLikedConditions(condition) && hasLikedIndustries(condition);
    }

    public boolean hasLikedConditions(Utils.ModifiableCondition condition) {
        // Checks if market has at least one of these condition
        hasAtLeastOneLikedCondition = true;
        if (!condition.likedConditions.isEmpty()) {
            boolean hasOneLikedCondition = false;
            for (String conditionId : condition.likedConditions) {
                hasOneLikedCondition = hasOneLikedCondition || market.hasCondition(conditionId);
            }
            return hasOneLikedCondition;
        }
        return true;
    }

    public boolean hasLikedIndustries(Utils.ModifiableCondition condition) {
        // Checks if market has all industries
        if (!condition.likedIndustries.isEmpty()) {
            for (String industryId : condition.likedIndustries) {
                if (!market.hasIndustry(industryId)) {
                    return false;
                }
            }
        }

        return true;
    }
}
