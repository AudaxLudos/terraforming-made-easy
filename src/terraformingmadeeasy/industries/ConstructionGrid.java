package terraformingmadeeasy.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.CoronalTapParticleScript;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.ids.TMEIds;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ConstructionGrid extends BaseIndustry {
    public static final String MEGASTRUCTURE_OPTIONS_FILE = "data/config/megastructure_options.csv";
    public static final float GAMMA_BUILD_TIME_MULT = 0.20f;
    public static final float BETA_BUILD_TIME_MULT = 0.30f;
    public static final float ALPHA_BUILD_TIME_MULT = 0.50f;
    public List<Utils.BuildableMegastructure> buildableMegastructures = new ArrayList<>();
    public Utils.BuildableMegastructure buildableMegastructure = null;
    public Utils.OrbitData megastructureOrbitData = null;
    public Boolean isAICoreBuildTimeMultApplied = false;
    public float aiCoreCurrentBuildTimeMult = 0f;
    public boolean firstTick = false;
    public String prevAICoreId = null;

    public ConstructionGrid() {
        try {
            JSONArray data = Global.getSettings().loadCSV(MEGASTRUCTURE_OPTIONS_FILE);
            for (int i = 0; i < data.length(); i++) {
                JSONObject row = data.getJSONObject(i);
                if (row.getString("structureId").isEmpty()) continue;
                if (row.getString("structureId").contains("#")) continue;

                String structureId = row.getString("structureId");
                float cost = row.getInt("cost");
                float buildTime = row.getInt("buildTime");

                this.buildableMegastructures.add(new Utils.BuildableMegastructure(
                        Global.getSettings().getCustomEntitySpec(structureId),
                        cost,
                        buildTime));
            }
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void apply() {
        apply(true);
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);
        if (firstTick) {
            if (Objects.equals(aiCoreId, Commodities.ALPHA_CORE)) {
                aiCoreCurrentBuildTimeMult = ALPHA_BUILD_TIME_MULT;
            } else if (Objects.equals(aiCoreId, Commodities.BETA_CORE)) {
                aiCoreCurrentBuildTimeMult = BETA_BUILD_TIME_MULT;
            } else if (Objects.equals(aiCoreId, Commodities.GAMMA_CORE)) {
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
    public boolean isUpgrading() {
        return building && buildableMegastructure != null;
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

        return "Building: " + left + " " + days + " left";
    }

    @Override
    public void finishBuildingOrUpgrading() {
        building = false;
        buildProgress = 0;
        buildTime = 1f;
        isAICoreBuildTimeMultApplied = false;
        if (buildableMegastructure != null) {
            sendCompletedMessage();
            completeMegastructure();
            buildableMegastructure = null;
            megastructureOrbitData = null;
            market.removeIndustry(getId(), null, false);
        } else {
            buildingFinished();
            reapply();
        }
    }

    @Override
    public void startUpgrading() {
        /* Will be called from MegastructureDialogDelegate to start building megastructure */
        if (buildableMegastructure != null && megastructureOrbitData != null) {
            building = true;
            buildProgress = 0;
            buildTime = buildableMegastructure.buildTime;
            firstTick = true;
        }
    }

    @Override
    public void cancelUpgrade() {
        /* Will be called from ConfirmDialogDelegate to cancel megastructure project */
        building = false;
        buildProgress = 0;
        buildableMegastructure = null;
        isAICoreBuildTimeMultApplied = false;
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
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(aiCoreId);
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
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(aiCoreId);
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
    protected void addPostUpkeepSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode) {
        float oPad = 10f;
        float pad = 3f;

        if (mode == IndustryTooltipMode.NORMAL || isUpgrading()) {
            tooltip.addSpacer(10f);
            tooltip.addSectionHeading("Megastructure Project", Alignment.MID, 0f);
            if (isUpgrading()) {
                TooltipMakerAPI imageWithText = tooltip.beginImageWithText(buildableMegastructure.icon, 40f);
                imageWithText.addPara("Status: %s", oPad, Misc.getHighlightColor(), "Ongoing");
                imageWithText.addPara("Action: %s", pad, Misc.getHighlightColor(), "Add");
                imageWithText.addPara("Megastructure: %s", pad, Misc.getHighlightColor(), buildableMegastructure.name);
                imageWithText.addPara("Days Left: %s", pad, Misc.getHighlightColor(), Math.round(buildTime - buildProgress) + "");
                tooltip.addImageWithText(0f);
            } else {
                TooltipMakerAPI imageWithText = tooltip.beginImageWithText("graphics/icons/stable_location.png", 40f);
                imageWithText.addPara("Status: %s", oPad, Misc.getHighlightColor(), "Idle");
                imageWithText.addPara("Action: %s", pad, Misc.getHighlightColor(), "-");
                imageWithText.addPara("Megastructure: %s", pad, Misc.getHighlightColor(), "-");
                imageWithText.addPara("Days Left: %s", pad, Misc.getHighlightColor(), "-");
                tooltip.addImageWithText(0f);
            }
        }
    }

    public void sendCompletedMessage() {
        if (market.isPlayerOwned()) {
            MessageIntel intel = new MessageIntel(buildableMegastructure.name + " megastructure completed", Misc.getBasePlayerColor());
            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
        }
    }

    public void completeMegastructure() {
        StarSystemAPI system = getMarket().getStarSystem();
        String customEntityId = buildableMegastructure.id;
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
        } else if (Objects.equals(customEntityId, "tme_station")) {
            /* Pick a random station spec */
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
            for (MarketConditionAPI condition : market.getConditions())
                condition.setSurveyed(true);
            ((StoragePlugin) market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getPlugin()).setPlayerPaidToUnlock(true);
            station.setMarket(market);
            station.setFaction(market.getFactionId());
            market.getConnectedEntities().add(station);
            station.getMemoryWithoutUpdate().set("$hasStation", true);
            Global.getSector().getEconomy().addMarket(market, true);
            /* Needed to fix bug where market size instantly raises to max */
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
