package terraformingmadeeasy.industries;

import com.fs.starfarer.api.Global;
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
import org.apache.log4j.Logger;
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.ids.TMEIds;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ConstructionGrid extends BaseIndustry {
    public static final Logger log = Global.getLogger(ConstructionGrid.class);
    public static final float GAMMA_BUILD_TIME_MULT = 0.20f;
    public static final float BETA_BUILD_TIME_MULT = 0.30f;
    public static final float ALPHA_BUILD_TIME_MULT = 0.50f;
    public List<Utils.BuildableMegastructure> buildableMegastructures = new ArrayList<>();
    public Utils.BuildableMegastructure buildableMegastructure = null;
    public Utils.OrbitData megastructureOrbitData = null;
    public Boolean isAICoreBuildTimeMultApplied = false;
    public float aiCoreCurrentBuildTimeMult = 0f;
    public float aiCoreBuildProgressRemoved = 0f;
    public String prevAICoreId = null;
    public boolean isExpanded = false;

    public ConstructionGrid() {
        setBuildableMegastructures(Utils.CONSTRUCTION_GRID_OPTIONS);
    }

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
        return this.building && this.buildableMegastructure != null;
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

        return "Building: " + left + " " + days + " left";
    }

    @Override
    public void finishBuildingOrUpgrading() {
        this.building = false;
        this.buildProgress = 0;
        this.buildTime = 1f;
        this.isAICoreBuildTimeMultApplied = false;
        if (this.buildableMegastructure != null) {
            log.info(String.format("Completion of %s megastructure in %s by %s", this.buildableMegastructure.name, getMarket().getStarSystem().getName(), getCurrentName()));
            sendCompletedMessage();
            completeMegastructure();
            this.buildableMegastructure = null;
            this.megastructureOrbitData = null;
            this.market.removeIndustry(getId(), null, false);
        } else {
            buildingFinished();
            reapply();
        }
    }

    @Override
    public void startUpgrading() {
        // Will be called from MegastructureDialogDelegate to start building megastructure
        if (this.buildableMegastructure != null && this.megastructureOrbitData != null) {
            log.info(String.format("Construction of %s megastructure in %s by %s", this.buildableMegastructure.name, getMarket().getStarSystem().getName(), getCurrentName()));
            this.building = true;
            this.buildProgress = 0;
            this.aiCoreBuildProgressRemoved = 0f;
            this.isAICoreBuildTimeMultApplied = false;
            this.buildTime = this.buildableMegastructure.buildTime;
        }
    }

    @Override
    public void cancelUpgrade() {
        // Will be called from ConfirmDialogDelegate to cancel megastructure project
        log.info(String.format("Deconstruction of %s megastructure in %s by %s", this.buildableMegastructure.name, getMarket().getStarSystem().getName(), getCurrentName()));
        this.building = false;
        this.buildProgress = 0;
        this.aiCoreBuildProgressRemoved = 0f;
        this.isAICoreBuildTimeMultApplied = false;
        this.buildableMegastructure = null;
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
            text.addPara(pre + "Reduces upkeep cost by %s. Reduces megastructure build time by %s.", oPad, highlight,
                    (int) ((1f - UPKEEP_MULT) * 100f) + "%", (int) (ALPHA_BUILD_TIME_MULT * 100f) + "%");
            tooltip.addImageWithText(oPad);
            return;
        }

        tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces megastructure build time by %s.", oPad, highlight,
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
            text.addPara(pre + "Reduces upkeep cost by %s. Reduces megastructure build time by %s.", oPad, highlight,
                    (int) ((1f - UPKEEP_MULT) * 100f) + "%", (int) (BETA_BUILD_TIME_MULT * 100f) + "%");
            tooltip.addImageWithText(oPad);
            return;
        }

        tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces megastructure build time by %s.", oPad, highlight,
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
            text.addPara(pre + "Reduces upkeep cost by %s. Reduces megastructure build time by %s.", oPad, highlight,
                    (int) ((1f - UPKEEP_MULT) * 100f) + "%", (int) (GAMMA_BUILD_TIME_MULT * 100f) + "%");
            tooltip.addImageWithText(oPad);
            return;
        }

        tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces megastructure build time by %s.", oPad, highlight,
                (int) ((1f - UPKEEP_MULT) * 100f) + "%", (int) (GAMMA_BUILD_TIME_MULT * 100f) + "%");
    }

    @Override
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode) {
        if (this.isExpanded) {
            int rowLimit = 6;
            int andMore = this.buildableMegastructures.size() - rowLimit;
            if (this.buildableMegastructures.size() < rowLimit) {
                rowLimit = this.buildableMegastructures.size();
                andMore = 0;
            }

            tooltip.addPara("Buildable Megastructures:", 10f);
            for (int i = 0; i < rowLimit; i++) {
                float pad = 1f;
                if (i == 0) {
                    pad = 3f;
                }
                tooltip.addPara("    " + this.buildableMegastructures.get(i).name, Misc.getHighlightColor(), pad);
            }
            if (andMore > 0) {
                tooltip.addPara("    ...and %s more", 0f, Misc.getTextColor(), Misc.getHighlightColor(), andMore + "");
            }
        } else {
            tooltip.addPara("Press %s to see megastructures you can build", 10f, Misc.getGrayColor(), Misc.getHighlightColor(), "F1");
        }
    }

    @Override
    protected void addPostUpkeepSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode) {
        float pad = 3f;

        if (mode == IndustryTooltipMode.NORMAL || isUpgrading()) {
            tooltip.addSectionHeading("Megastructure Project", Alignment.MID, 10f);
            if (isUpgrading()) {
                tooltip.addSectionHeading("Megastructure project", Alignment.MID, 10f);
                TooltipMakerAPI imageWithText = tooltip.beginImageWithText(this.buildableMegastructure.icon, 40f);
                imageWithText.addPara("Status: %s", 0f, Misc.getHighlightColor(), "Ongoing");
                imageWithText.addPara("Action: %s", pad, Misc.getHighlightColor(), "Add");
                imageWithText.addPara("Megastructure: %s", pad, Misc.getHighlightColor(), this.buildableMegastructure.name);
                imageWithText.addPara("Days Left: %s", pad, Misc.getHighlightColor(), Math.round(this.buildTime - this.buildProgress) + "");
                tooltip.addImageWithText(10f);
            } else {
                tooltip.addSectionHeading("No Projects started", Alignment.MID, 10f);
            }
        }
    }

    public void setBuildableMegastructures(List<Utils.BuildableMegastructure> options) {
        this.buildableMegastructures = this.buildableMegastructures == null ? options : this.buildableMegastructures;
    }

    public void sendCompletedMessage() {
        if (this.market.isPlayerOwned()) {
            MessageIntel intel = new MessageIntel(this.buildableMegastructure.name + " megastructure completed", Misc.getBasePlayerColor());
            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, this.market);
        }
    }

    public void completeMegastructure() {
        StarSystemAPI system = getMarket().getStarSystem();
        String customEntityId = this.buildableMegastructure.id;
        SectorEntityToken orbitEntity = this.megastructureOrbitData.entity;
        float orbitAngle = this.megastructureOrbitData.orbitAngle;
        float orbitRadius = this.megastructureOrbitData.orbitRadius;
        float orbitDays = this.megastructureOrbitData.orbitDays;
        if (Objects.equals(customEntityId, Entities.CORONAL_TAP)) {
            SectorEntityToken coronalTap = system.addCustomEntity(null, null, Entities.CORONAL_TAP, Factions.NEUTRAL);
            coronalTap.setCircularOrbit(orbitEntity, orbitAngle, orbitRadius, orbitDays);
            system.addScript(new MiscellaneousThemeGenerator.MakeCoronalTapFaceNearestStar(coronalTap));
            system.addScript(new CoronalTapParticleScript(coronalTap));
            coronalTap.getMemoryWithoutUpdate().removeAllRequired("$defenderFleet");
            coronalTap.getMemoryWithoutUpdate().removeAllRequired("$hasNonStation");
            coronalTap.getMemoryWithoutUpdate().set("$beingRepaired", true);
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

    public boolean canBuildMegastructure(String customEntityId) {
        StarSystemAPI system = getMarket().getStarSystem();
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

    public String generateProceduralName(String tag, String parent) {
        Random random = StarSystemGenerator.random;
        int randomLagRangePointType = random.nextInt(StarSystemGenerator.LagrangePointType.values().length);
        StarSystemGenerator.LagrangePointType lagrangePointType = StarSystemGenerator.LagrangePointType.values()[randomLagRangePointType];
        ProcgenUsedNames.NamePick namePick = ProcgenUsedNames.pickName(tag, parent, lagrangePointType);
        return namePick.spec.getName();
    }
}
