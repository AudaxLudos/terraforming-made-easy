package terraformingmadeeasy.industry;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetSpecAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MarketConditionSpecAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.loading.specs.PlanetSpec;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TMEBaseIndustry extends BaseIndustry {
    public static class ModifiableCondition {
        public String id;
        public String name;
        public String icon;
        public float cost;
        public float buildTime;
        public List<String> restrictions = new ArrayList<>();
        public List<String> requirements = new ArrayList<>();

        public ModifiableCondition(String conditionSpecId, float cost, float buildTime, List<String> restrictions, List<String> requirements) {
            MarketConditionSpecAPI spec = Global.getSettings().getMarketConditionSpec(conditionSpecId);

            this.id = spec.getId();
            this.name = spec.getName();
            this.icon = spec.getIcon();
            this.cost = cost;
            this.buildTime = buildTime;
            if (restrictions != null) this.restrictions = restrictions;
            if (requirements != null) this.requirements = requirements;
        }
    }

    public static final float GAMMA_BUILD_TIME_MULT = 0.20f;
    public static final float BETA_BUILD_TIME_MULT = 0.30f;
    public static final float ALPHA_BUILD_TIME_MULT = 0.50f;

    public List<ModifiableCondition> modifiableConditions = new ArrayList<>();
    public ModifiableCondition modifiableCondition = null;
    public Boolean isAICoreBuildTimeMultApplied = false;
    public float aiCoreCurrentBuildTimeMult = 0f;
    public boolean firstTick = false;
    public String prevAICoreId = null;

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

    public boolean isUpgrading() {
        return building && modifiableCondition != null;
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

    public void finishBuildingOrUpgrading() {
        building = false;
        buildProgress = 0;
        buildTime = 1f;
        isAICoreBuildTimeMultApplied = false;
        if (modifiableCondition != null) {
            sendTerraformingMessage();
            changePlanetConditions();
            changePlanetClass();
            reapplySupplyAndDemand();
            reapply();
            modifiableCondition = null;
        } else {
            buildingFinished();
            reapply();
        }
    }

    public void startUpgrading(ModifiableCondition condition) {
        // Will be called from TMEIndustryDialogueDelegate to start terraforming
        building = true;
        buildProgress = 0;
        modifiableCondition = condition;
        buildTime = condition.buildTime;
        firstTick = true;
    }

    public void cancelUpgrade() {
        // Will be called from TMEConfirmDialogueDelegate to cancel terraforming
        building = false;
        buildProgress = 0;
        modifiableCondition = null;
        isAICoreBuildTimeMultApplied = false;
    }

    @Override
    public boolean isAvailableToBuild() {
        if (!super.isAvailableToBuild()) return false;
        if (getMarket().getPlanetEntity() == null) return false;
        return !getMarket().getPlanetEntity().isGasGiant();
    }

    @Override
    public String getUnavailableReason() {
        if (!super.isAvailableToBuild()) return super.getUnavailableReason();
        if (getMarket().getPlanetEntity() == null) return "Requires a planet";
        return "Can not be built on gas giants";
    }

    public void sendTerraformingMessage() {
        if (market.isPlayerOwned()) {
            MessageIntel intel = new MessageIntel(getCurrentName() + " at " + market.getName(), Misc.getBasePlayerColor());
            intel.addLine(BaseIntelPlugin.BULLET + "Terraforming completed");
            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
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
    }

    public void changePlanetConditions() {
        if (getMarket().hasCondition(modifiableCondition.id)) {
            getMarket().removeCondition(modifiableCondition.id);
        } else {
            getMarket().addCondition(modifiableCondition.id);
            getMarket().getFirstCondition(modifiableCondition.id).setSurveyed(true);
            for (String restriction : modifiableCondition.restrictions) {
                if (getMarket().hasCondition(restriction))
                    getMarket().removeCondition(restriction);
            }
        }
    }

    public void changePlanetClass() {
        MarketAPI m = getMarket();
        String planetTypeId = null;
        boolean removeFarming = false;
        boolean removeOrganics = false;
        boolean reduceOrganics = false;
        if (!m.getPlanetEntity().isStar() && !m.getPlanetEntity().isGasGiant()
                && !m.hasCondition(Conditions.HABITABLE) && !m.hasCondition(Conditions.VERY_COLD)
                && !m.hasCondition(Conditions.VERY_HOT) && !m.hasCondition(Conditions.NO_ATMOSPHERE)
                && !m.hasCondition(Conditions.THIN_ATMOSPHERE) && !m.hasCondition(Conditions.DENSE_ATMOSPHERE)
                && !m.hasCondition(Conditions.TOXIC_ATMOSPHERE) && !m.hasCondition(Conditions.IRRADIATED)
                && !m.hasCondition(Conditions.DARK)) {
            addOrImproveFarming();
            addOrImproveOrganics();
            m.addCondition(Conditions.HABITABLE);
        }
        if (m.hasCondition(Conditions.HABITABLE)) {
            String[] richHabitableTypes = {"terran", "terran-eccentric"};
            planetTypeId = richHabitableTypes[Misc.random.nextInt(richHabitableTypes.length)];
            if (m.hasCondition(Conditions.HOT)) {
                String[] poorHabitableTypes = {"jungle", "arid", "desert", "desert1"};
                planetTypeId = poorHabitableTypes[Misc.random.nextInt(poorHabitableTypes.length)];
            }
            if (m.hasCondition(Conditions.COLD)) {
                planetTypeId = "tundra";
            }
        }
        if (m.hasCondition(Conditions.WATER_SURFACE)) {
            removeFarming = true;
            planetTypeId = "water";
            if (m.hasCondition(Conditions.VERY_COLD)) {
                m.removeCondition(Conditions.WATER_SURFACE);
                String[] frozenTypes = {"frozen", "frozen1", "frozen2", "frozen3"};
                planetTypeId = frozenTypes[Misc.random.nextInt(frozenTypes.length)];
                removeOrganics = true;
            }
            if ((m.hasCondition(Conditions.COLD) || m.hasCondition(Conditions.VERY_COLD)) && m.hasCondition(Conditions.NO_ATMOSPHERE)) {
                m.removeCondition(Conditions.WATER_SURFACE);
                planetTypeId = "rocky_ice";
                removeOrganics = true;
            }
        }
        if (m.hasCondition(Conditions.THIN_ATMOSPHERE)) {
            planetTypeId = "barren-desert";
            removeFarming = true;
            reduceOrganics = true;
        }
        if (m.hasCondition(Conditions.NO_ATMOSPHERE) || m.hasCondition(Conditions.VERY_HOT) || m.hasCondition(Conditions.VERY_COLD)) {
            String[] poorBarrenTypes = {"barren", "barren2", "barren3", "barren_castiron", "barren_venuslike", "rocky_metallic", "barren-bombarded"};
            planetTypeId = poorBarrenTypes[Misc.random.nextInt(poorBarrenTypes.length)];
            removeFarming = true;
            removeOrganics = true;
            if (m.hasCondition(Conditions.TECTONIC_ACTIVITY) || m.hasCondition(Conditions.EXTREME_TECTONIC_ACTIVITY))
                planetTypeId = "rocky_unstable";
        }
        if (m.hasCondition(Conditions.TOXIC_ATMOSPHERE)) {
            planetTypeId = "toxic";
            removeFarming = true;
            reduceOrganics = true;
        }
        if (m.hasCondition(Conditions.IRRADIATED)) {
            planetTypeId = "irradiated";
            removeFarming = true;
            removeOrganics = true;
        }
        if (m.hasCondition(Conditions.TECTONIC_ACTIVITY) || m.hasCondition(Conditions.EXTREME_TECTONIC_ACTIVITY)) {
            if ((m.hasCondition(Conditions.VERY_HOT) || m.hasCondition(Conditions.HOT))
                    && (m.hasCondition(Conditions.TOXIC_ATMOSPHERE) || m.hasCondition(Conditions.THIN_ATMOSPHERE) || m.hasCondition(Conditions.DENSE_ATMOSPHERE))
                    && (m.hasCondition(Conditions.ORE_ABUNDANT) || m.hasCondition(Conditions.ORE_RICH) || m.hasCondition(Conditions.ORE_ULTRARICH))
                    && (m.hasCondition(Conditions.RARE_ORE_ABUNDANT) || m.hasCondition(Conditions.RARE_ORE_RICH) || m.hasCondition(Conditions.RARE_ORE_ULTRARICH))) {
                planetTypeId = "lava";
                removeFarming = true;
                removeOrganics = true;
            }
            if (m.hasCondition(Conditions.VERY_COLD)
                    && (m.hasCondition(Conditions.VOLATILES_TRACE) || m.hasCondition(Conditions.VOLATILES_DIFFUSE) ||
                    m.hasCondition(Conditions.VOLATILES_ABUNDANT) || m.hasCondition(Conditions.VOLATILES_PLENTIFUL))) {
                planetTypeId = "cryovolcanic";
                removeFarming = true;
                removeOrganics = true;
            }
        }

        if (removeFarming) removeFarming();
        if (reduceOrganics) reduceOrganicsToCommon();
        else if (removeOrganics) removeOrganics();
        changePlanetVisuals(planetTypeId);
    }

    public void changePlanetVisuals(String planetTypeId) {
        PlanetSpecAPI marketSpec = getMarket().getPlanetEntity().getSpec();
        for (PlanetSpecAPI spec : Global.getSettings().getAllPlanetSpecs()) {
            if (spec.getPlanetType().equals(planetTypeId)) {
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
                ((PlanetSpec) marketSpec).planetType = planetTypeId;
                ((PlanetSpec) marketSpec).name = spec.getName();
                ((PlanetSpec) marketSpec).descriptionId = ((PlanetSpec) spec).descriptionId;
                break;
            }
        }
        getMarket().getPlanetEntity().applySpecChanges();
    }

    public void removeFarming() {
        if (getMarket().hasCondition(Conditions.FARMLAND_POOR))
            getMarket().removeCondition(Conditions.FARMLAND_POOR);
        else if (getMarket().hasCondition(Conditions.FARMLAND_ADEQUATE))
            getMarket().removeCondition(Conditions.FARMLAND_ADEQUATE);
        else if (getMarket().hasCondition(Conditions.FARMLAND_RICH))
            getMarket().removeCondition(Conditions.FARMLAND_RICH);
        else if (getMarket().hasCondition(Conditions.FARMLAND_BOUNTIFUL))
            getMarket().removeCondition(Conditions.FARMLAND_BOUNTIFUL);
    }

    public void removeOrganics() {
        if (getMarket().hasCondition(Conditions.ORGANICS_TRACE))
            getMarket().removeCondition(Conditions.ORGANICS_TRACE);
        else if (getMarket().hasCondition(Conditions.ORGANICS_COMMON))
            getMarket().removeCondition(Conditions.ORGANICS_COMMON);
        else if (getMarket().hasCondition(Conditions.ORGANICS_ABUNDANT))
            getMarket().removeCondition(Conditions.ORGANICS_ABUNDANT);
        else if (getMarket().hasCondition(Conditions.ORGANICS_PLENTIFUL))
            getMarket().removeCondition(Conditions.ORGANICS_PLENTIFUL);
    }

    public void addOrImproveFarming() {
        if (getMarket().hasCondition(Conditions.FARMLAND_POOR)) {
            getMarket().removeCondition(Conditions.FARMLAND_POOR);
            getMarket().addCondition(Conditions.FARMLAND_ADEQUATE);
            getMarket().getFirstCondition(Conditions.FARMLAND_ADEQUATE).setSurveyed(true);
        } else if (getMarket().hasCondition(Conditions.FARMLAND_ADEQUATE)) {
            getMarket().removeCondition(Conditions.FARMLAND_ADEQUATE);
            getMarket().addCondition(Conditions.FARMLAND_RICH);
            getMarket().getFirstCondition(Conditions.FARMLAND_RICH).setSurveyed(true);
        } else if (getMarket().hasCondition(Conditions.FARMLAND_RICH)) {
            getMarket().removeCondition(Conditions.FARMLAND_RICH);
            getMarket().addCondition(Conditions.FARMLAND_BOUNTIFUL);
            getMarket().getFirstCondition(Conditions.FARMLAND_BOUNTIFUL).setSurveyed(true);
        } else if (!getMarket().hasCondition(Conditions.FARMLAND_BOUNTIFUL)) {
            getMarket().addCondition(Conditions.FARMLAND_POOR);
            getMarket().getFirstCondition(Conditions.FARMLAND_POOR).setSurveyed(true);
        }
    }

    public void addOrImproveOrganics() {
        if (getMarket().hasCondition(Conditions.ORGANICS_TRACE)) {
            getMarket().removeCondition(Conditions.ORGANICS_TRACE);
            getMarket().addCondition(Conditions.ORGANICS_COMMON);
            getMarket().getFirstCondition(Conditions.ORGANICS_COMMON).setSurveyed(true);
        } else if (getMarket().hasCondition(Conditions.ORGANICS_COMMON)) {
            getMarket().removeCondition(Conditions.ORGANICS_COMMON);
            getMarket().addCondition(Conditions.ORGANICS_ABUNDANT);
            getMarket().getFirstCondition(Conditions.ORGANICS_ABUNDANT).setSurveyed(true);
        } else if (getMarket().hasCondition(Conditions.ORGANICS_ABUNDANT)) {
            getMarket().removeCondition(Conditions.ORGANICS_ABUNDANT);
            getMarket().addCondition(Conditions.ORGANICS_PLENTIFUL);
            getMarket().getFirstCondition(Conditions.ORGANICS_PLENTIFUL).setSurveyed(true);
        } else if (!getMarket().hasCondition(Conditions.ORGANICS_PLENTIFUL)) {
            getMarket().addCondition(Conditions.ORGANICS_TRACE);
            getMarket().getFirstCondition(Conditions.ORGANICS_TRACE).setSurveyed(true);
        }
    }

    public void reduceOrganicsToCommon() {
        if (getMarket().hasCondition(Conditions.ORGANICS_ABUNDANT) || getMarket().hasCondition(Conditions.ORGANICS_PLENTIFUL)) {
            getMarket().removeCondition(Conditions.ORGANICS_ABUNDANT);
            getMarket().removeCondition(Conditions.ORGANICS_PLENTIFUL);
            getMarket().addCondition(Conditions.ORGANICS_COMMON);
            getMarket().getFirstCondition(Conditions.ORGANICS_COMMON).setSurveyed(true);
        }
    }

    public void reapplySupplyAndDemand() {
        for (Industry ind : getMarket().getIndustries()) {
            ind.doPreSaveCleanup();
            ind.doPostSaveRestore();
        }
    }

    public Boolean canTerraformCondition(ModifiableCondition condition) {
        for (String requirement : condition.requirements)
            if (!getMarket().hasCondition(requirement))
                return false;
        return true;
    }
}
