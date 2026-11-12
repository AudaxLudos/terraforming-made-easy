package terraformingmadeeasy.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomEntitySpecAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.CoronalTapParticleScript;
import com.fs.starfarer.api.impl.campaign.GateEntityPlugin;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.procgen.ProcgenUsedNames;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.MiscellaneousThemeGenerator;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.kaysaar.aotd.vok.scripts.research.AoTDMainResearchManager;
import org.apache.log4j.Logger;
import terraformingmadeeasy.Settings;
import terraformingmadeeasy.TMEIds;
import terraformingmadeeasy.loading.specs.MegastructureProjectSpec;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class MegastructureIndustry extends BaseIndustry {
    public static final Logger log = Global.getLogger(MegastructureIndustry.class);
    public static final float GAMMA_BUILD_TIME_MULT = 0.20f;
    public static final float BETA_BUILD_TIME_MULT = 0.30f;
    public static final float ALPHA_BUILD_TIME_MULT = 0.50f;
    public SectorEntityToken orbitEntity;
    public float orbitAngle;
    public float orbitRadius;
    public float orbitDays;
    protected MegastructureProjectSpec project = null;
    protected Boolean isAICoreBuildTimeMultApplied = false;
    protected float aiCoreBuildProgressRemoved = 0f;
    protected String prevAICoreId = null;

    public static boolean canBuildMegastructure(MarketAPI market, String customEntityId) {
        if (market == null) {
            return true;
        }
        StarSystemAPI system = market.getStarSystem();
        if (Objects.equals(customEntityId, Entities.CORONAL_TAP)) {
            return system.getEntitiesWithTag(Tags.CORONAL_TAP).isEmpty() && Objects.equals(system.getStar().getTypeId(), StarTypes.BLUE_SUPERGIANT);
        } else if (Objects.equals(customEntityId, Entities.DERELICT_CRYOSLEEPER)) {
            return system.getEntitiesWithTag(Tags.CRYOSLEEPER).isEmpty();
        } else if (Objects.equals(customEntityId, Entities.INACTIVE_GATE)) {
            return system.getEntitiesWithTag(Tags.GATE).isEmpty();
        } else if (Objects.equals(customEntityId, TMEIds.TME_STATION)) {
            return system.getEntitiesWithTag(TMEIds.TME_STATION).size() < 3;
        }

        return system.getEntitiesWithTag(customEntityId).isEmpty();
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
            this.aiCoreBuildProgressRemoved = 0f;
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
        if (mode == IndustryTooltipMode.NORMAL || isUpgrading()) {
            float oPad = 10f;
            float pad = 3f;

            if (isUpgrading()) {
                CustomEntitySpecAPI entitySpec = Global.getSettings().getCustomEntitySpec(this.project.getCustomEntityId());
                tooltip.addSectionHeading("Megastructure project", Alignment.MID, oPad);
                TooltipMakerAPI imageWithText = tooltip.beginImageWithText(entitySpec.getIconName(), 40f);
                imageWithText.addPara("Status: %s", 0f, Misc.getHighlightColor(), "Ongoing");
                imageWithText.addPara("Action: %s", pad, Misc.getHighlightColor(), "Add");
                imageWithText.addPara("Megastructure: %s", pad, Misc.getHighlightColor(), entitySpec.getDefaultName());
                imageWithText.addPara("Days Left: %s", pad, Misc.getHighlightColor(), Math.round(this.buildTime - this.buildProgress) + "");
                tooltip.addImageWithText(oPad);
            } else {
                tooltip.addSectionHeading("No Projects started", Alignment.MID, oPad);
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
        return TMEIds.CONSTRUCTION_GRID_TECH;
    }

    public List<MegastructureProjectSpec> getProjects() {
        return Settings.getAllMegastructureProjectSpecs();
    }

    public MegastructureProjectSpec getProject() {
        return this.project;
    }

    public void setProject(MegastructureProjectSpec option) {
        this.project = option;
    }

    public void completeProject() {
        CustomEntitySpecAPI entitySpec = Global.getSettings().getCustomEntitySpec(this.project.getCustomEntityId());
        log.info(String.format("Completion of %s megastructure in %s by %s", entitySpec.getDefaultName(), getMarket().getStarSystem().getName(), getCurrentName()));

        sendCompletedMessage();
        buildMegastructure();
        this.project = null;
        this.orbitEntity = null;
        this.orbitAngle = 0f;
        this.orbitRadius = 1000f;
        this.orbitDays = 100f;
        notifyBeingRemoved(MarketAPI.MarketInteractionMode.REMOTE, false);
        this.market.removeIndustry(getId(), null, false);
    }

    public void buildMegastructure() {
        StarSystemAPI system = getMarket().getStarSystem();
        String customEntityId = this.project.getCustomEntityId();
        SectorEntityToken orbitEntity = this.orbitEntity;
        float orbitAngle = this.orbitAngle;
        float orbitRadius = this.orbitRadius;
        float orbitDays = this.orbitDays;
        if (Objects.equals(customEntityId, Entities.CORONAL_TAP)) {
            SectorEntityToken coronalTap = system.addCustomEntity(null, null, Entities.CORONAL_TAP, Factions.NEUTRAL);
            coronalTap.setCircularOrbit(orbitEntity, orbitAngle, orbitRadius, orbitDays);
            system.addScript(new MiscellaneousThemeGenerator.MakeCoronalTapFaceNearestStar(coronalTap));
            system.addScript(new CoronalTapParticleScript(coronalTap));
            coronalTap.getMemoryWithoutUpdate().removeAllRequired("$defenderFleet");
            coronalTap.getMemoryWithoutUpdate().removeAllRequired("$hasNonStation");
            coronalTap.getMemoryWithoutUpdate().set("$beingRepaired", true, 5f);
            coronalTap.getMemoryWithoutUpdate().set("$defenderFleetDefeated", true);
            coronalTap.getMemoryWithoutUpdate().set("$hasDefenders", false);
            coronalTap.getMemoryWithoutUpdate().set("$usable", true);
        } else if (Objects.equals(customEntityId, Entities.DERELICT_CRYOSLEEPER)) {
            SectorEntityToken cryoSleeper = system.addCustomEntity(null, null, Entities.DERELICT_CRYOSLEEPER, Factions.NEUTRAL);
            cryoSleeper.setCircularOrbit(orbitEntity, orbitAngle, orbitRadius, orbitDays);
            cryoSleeper.getMemoryWithoutUpdate().removeAllRequired("$defenderFleet");
            cryoSleeper.getMemoryWithoutUpdate().removeAllRequired("$hasDefenders");
            cryoSleeper.getMemoryWithoutUpdate().set("$usable", true);
            cryoSleeper.getMemoryWithoutUpdate().set("$defenderFleetDefeated", true);
            cryoSleeper.getMemoryWithoutUpdate().set("$option", "salBeatDefendersContinue");
        } else if (Objects.equals(customEntityId, Entities.INACTIVE_GATE)) {
            SectorEntityToken inactiveGate = system.addCustomEntity(null, null, Entities.INACTIVE_GATE, Factions.NEUTRAL);
            inactiveGate.setCircularOrbit(orbitEntity, orbitAngle, orbitRadius, orbitDays);
            inactiveGate.getMemoryWithoutUpdate().set("$gateScanned", true);
            inactiveGate.getMemoryWithoutUpdate().set("$fullName", "Active Gate");
            GateEntityPlugin.addGateScanned();
            GateEntityPlugin.getGateData().scanned.add(inactiveGate);
            inactiveGate.getCustomPlugin().advance(0f);
        } else if (Objects.equals(customEntityId, "tme_station")) {
            // Pick a random station spec
            WeightedRandomPicker<String> stations = new WeightedRandomPicker<>(StarSystemGenerator.random);
            stations.add("station_side00");
            stations.add("station_side02");
            stations.add("station_side03");
            stations.add("station_side04");
            stations.add("station_side05");
            stations.add("station_side07");
            stations.add("station_side06");
            String parentName = orbitEntity.getName();
            if (orbitEntity.getStarSystem().getConstellation() != null) {
                parentName = orbitEntity.getStarSystem().getConstellation().getName();
            }
            SectorEntityToken station = system.addCustomEntity(null, generateProceduralName(Tags.STATION, parentName), stations.pick(), Factions.PLAYER);
            station.setCircularOrbit(orbitEntity, orbitAngle, orbitRadius, orbitDays);
            station.setId("system_" + station.getId() + ":tme_station_" + station.getStarSystem().getEntitiesWithTag("tme_station").size());
            station.addTag(TMEIds.TME_STATION);
            MarketAPI market = Global.getFactory().createMarket("market_" + station.getId(), station.getName(), 3);
            market.setPrimaryEntity(station);
            market.setFactionId(Global.getSector().getPlayerFleet().getFaction().getId());
            market.getMemoryWithoutUpdate().set("$startingFactionId", market.getFaction().getId());
            market.addCondition(Conditions.POPULATION_3);
            market.addIndustry(Industries.POPULATION);
            market.getConstructionQueue().addToEnd(Industries.SPACEPORT, 0);
            market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
            market.addSubmarket(Submarkets.LOCAL_RESOURCES);
            market.setPlayerOwned(true);
            market.addTag("tme_station");
            market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
            for (MarketConditionAPI condition : market.getConditions()) {
                condition.setSurveyed(true);
            }
            ((StoragePlugin) market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getPlugin()).setPlayerPaidToUnlock(true);
            station.setMarket(market);
            station.setFaction(market.getFactionId());
            market.getConnectedEntities().add(station);
            station.getMemoryWithoutUpdate().set("$hasStation", true);
            Global.getSector().getEconomy().addMarket(market, true);
            // Needed to fix bug where market size instantly raises to max
            Global.getSector().getCampaignUI().showInteractionDialog(station);
            Global.getSector().getCampaignUI().getCurrentInteractionDialog().getTextPanel().clear();
            Global.getSector().getCampaignUI().getCurrentInteractionDialog().getTextPanel().addPara("Sorry, I have to force open a dialog to fix a bug that instantly increases the population size of a recently finished Orbital Station Megastructure to 6 (haven't found any other solutions)", Misc.getHighlightColor());
        } else {
            SectorEntityToken entity = system.addCustomEntity(null, null, customEntityId, Factions.NEUTRAL);
            entity.setCircularOrbit(orbitEntity, orbitAngle, orbitRadius, orbitDays);
        }
    }

    public void sendCompletedMessage() {
        if (this.market.isPlayerOwned()) {
            CustomEntitySpecAPI entitySpec = Global.getSettings().getCustomEntitySpec(this.project.getCustomEntityId());
            MessageIntel intel = new MessageIntel(entitySpec.getDefaultName() + " megastructure completed", Misc.getBasePlayerColor());
            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, this.market);
        }
    }

    public String generateProceduralName(String tag, String parent) {
        Random random = StarSystemGenerator.random;
        int randomLagRangePointType = random.nextInt(StarSystemGenerator.LagrangePointType.values().length);
        StarSystemGenerator.LagrangePointType lagrangePointType = StarSystemGenerator.LagrangePointType.values()[randomLagRangePointType];
        ProcgenUsedNames.NamePick namePick = ProcgenUsedNames.pickName(tag, parent, lagrangePointType);
        return namePick.spec.getName();
    }
}
