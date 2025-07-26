package terraformingmadeeasy.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.CoronalTapParticleScript;
import com.fs.starfarer.api.impl.campaign.GateEntityPlugin;
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
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.ids.TMEIds;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ConstructionGrid extends TMEBaseIndustryTest {
    public Utils.OrbitData orbitData = null;

    @Override
    public void finishBuildingOrUpgrading() {
        this.building = false;
        this.buildProgress = 0;
        this.buildTime = 1f;
        this.isAICoreBuildTimeMultApplied = false;
        if (this.project != null) {
            log.info(String.format("Completion of %s megastructure in %s by %s", this.project.name, getMarket().getStarSystem().getName(), getCurrentName()));
            completeProject();
        } else {
            buildingFinished();
            reapply();
        }
    }

    @Override
    public void startUpgrading() {
        if (this.project != null && this.orbitData != null) {
            log.info(String.format("Construction of %s megastructure in %s by %s", this.project.name, getMarket().getStarSystem().getName(), getCurrentName()));
            this.building = true;
            this.buildProgress = 0;
            this.aiCoreBuildProgressRemoved = 0f;
            this.isAICoreBuildTimeMultApplied = false;
            this.buildTime = this.project.buildTime * Utils.BUILD_TIME_MULTIPLIER;
        }
    }

    @Override
    protected void addProjectSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode) {
        if (mode == IndustryTooltipMode.NORMAL || isUpgrading()) {
            float oPad = 10f;
            float pad = 3f;

            if (isUpgrading()) {
                tooltip.addSectionHeading("Megastructure project", Alignment.MID, oPad);
                TooltipMakerAPI imageWithText = tooltip.beginImageWithText(this.project.icon, 40f);
                imageWithText.addPara("Status: %s", 0f, Misc.getHighlightColor(), "Ongoing");
                imageWithText.addPara("Action: %s", pad, Misc.getHighlightColor(), "Add");
                imageWithText.addPara("Megastructure: %s", pad, Misc.getHighlightColor(), this.project.name);
                imageWithText.addPara("Days Left: %s", pad, Misc.getHighlightColor(), Math.round(this.buildTime - this.buildProgress) + "");
                tooltip.addImageWithText(oPad);
            } else {
                tooltip.addSectionHeading("No Projects started", Alignment.MID, oPad);
            }
        }
    }

    @Override
    public String getAOTDVOKTechId() {
        return TMEIds.CONSTRUCTION_GRID_TECH;
    }

    @Override
    public List<Utils.ProjectData> getProjects() {
        return Utils.CONSTRUCTION_GRID_OPTIONS;
    }

    @Override
    public void completeProject() {
        StarSystemAPI system = getMarket().getStarSystem();
        String customEntityId = this.project.id;
        SectorEntityToken orbitEntity = this.orbitData.entity;
        float orbitAngle = this.orbitData.orbitAngle;
        float orbitRadius = this.orbitData.orbitRadius;
        float orbitDays = this.orbitData.orbitDays;
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

        sendCompletedMessage();
        this.project = null;
        this.orbitData = null;
        notifyBeingRemoved(MarketAPI.MarketInteractionMode.REMOTE, false);
        this.market.removeIndustry(getId(), null, false);
    }

    @Override
    public void sendCompletedMessage() {
        if (this.market.isPlayerOwned()) {
            MessageIntel intel = new MessageIntel(this.project.name + " megastructure completed", Misc.getBasePlayerColor());
            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, this.market);
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
