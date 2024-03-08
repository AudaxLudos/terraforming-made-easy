package terraformingmadeeasy.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomEntitySpecAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.impl.campaign.CoronalTapParticleScript;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.procgen.themes.MiscellaneousThemeGenerator;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConstructionGrid extends BaseIndustry {
    public static final float GAMMA_BUILD_TIME_MULT = 0.20f;
    public static final float BETA_BUILD_TIME_MULT = 0.30f;
    public static final float ALPHA_BUILD_TIME_MULT = 0.50f;
    public List<BuildableMegastructure> buildableMegastructures = new ArrayList<>();
    public BuildableMegastructure buildableMegastructure = null;
    public OrbitData megastructureOrbitData = null;
    public Boolean isAICoreBuildTimeMultApplied = false;
    public float aiCoreCurrentBuildTimeMult = 0f;
    public boolean firstTick = false;
    public String prevAICoreId = null;

    public ConstructionGrid() {
        this.buildableMegastructures.add(new BuildableMegastructure(Entities.DERELICT_CRYOSLEEPER, 20000000, 1080f));
        this.buildableMegastructures.add(new BuildableMegastructure(Entities.CORONAL_TAP, 20000000, 1440));
        this.buildableMegastructures.add(new BuildableMegastructure(Entities.INACTIVE_GATE, 12000000, 1080f));
    }

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

        if (isUpgrading()) {
            return "Building: " + left + " " + days + " left";
        } else {
            return "Building: " + left + " " + days + " left";
        }
    }

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

    public void startUpgrading(BuildableMegastructure megastructure, OrbitData orbitData) {
        // Will be called from MegastructureDialogDelegate to start building megastructure
        building = true;
        buildProgress = 0;
        buildableMegastructure = megastructure;
        megastructureOrbitData = orbitData;
        buildTime = megastructure.buildTime;
        firstTick = true;
    }

    public void cancelUpgrade() {
        // Will be called from ConfirmDialogDelegate to cancel megastructure project
        building = false;
        buildProgress = 0;
        buildableMegastructure = null;
        isAICoreBuildTimeMultApplied = false;
    }

    public void completeMegastructure() {
        StarSystemAPI system = getMarket().getStarSystem();
        String customEntityId = buildableMegastructure.id;
        SectorEntityToken entity = this.megastructureOrbitData.entity;
        float orbitAngle = this.megastructureOrbitData.orbitAngle;
        float orbitRadius = this.megastructureOrbitData.orbitRadius;
        float orbitDays = this.megastructureOrbitData.orbitDays;
        if (Objects.equals(customEntityId, Entities.CORONAL_TAP)) {
            SectorEntityToken coronalTap = system.addCustomEntity(null, null, Entities.CORONAL_TAP, Factions.NEUTRAL);
            coronalTap.setCircularOrbit(entity, orbitAngle, orbitRadius, orbitDays);
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
            cryoSleeper.setCircularOrbit(entity, orbitAngle, orbitRadius, orbitDays);
            cryoSleeper.getMemoryWithoutUpdate().removeAllRequired("$defenderFleet");
            cryoSleeper.getMemoryWithoutUpdate().removeAllRequired("$hasDefenders");
            cryoSleeper.getMemoryWithoutUpdate().set("$usable", true);
            cryoSleeper.getMemoryWithoutUpdate().set("$defenderFleetDefeated", true);
            cryoSleeper.getMemoryWithoutUpdate().set("$option", "salBeatDefendersContinue");
        } else if (Objects.equals(customEntityId, Entities.INACTIVE_GATE)) {
            SectorEntityToken inactiveGate = system.addCustomEntity(null, null, Entities.INACTIVE_GATE, Factions.NEUTRAL);
            inactiveGate.setCircularOrbit(entity, orbitAngle, orbitRadius, orbitDays);
            inactiveGate.getMemoryWithoutUpdate().set("$gateScanned", true);
            inactiveGate.getMemoryWithoutUpdate().set("$fullName", "Active Gate");
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

    public void sendCompletedMessage() {
        if (market.isPlayerOwned()) {
            MessageIntel intel = new MessageIntel(buildableMegastructure.name + " megastructure completed", Misc.getBasePlayerColor());
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
    protected void updateAICoreToSupplyAndDemandModifiers() {
        // TME industries don't supply or demand commodities for now
    }

    public boolean canBuildMegastructure(String customEntityId) {
        StarSystemAPI system = getMarket().getStarSystem();
        if (Objects.equals(customEntityId, Entities.CORONAL_TAP)) {
            return system.getEntitiesWithTag(Tags.CORONAL_TAP).isEmpty() && Objects.equals(system.getStar().getTypeId(), StarTypes.BLUE_SUPERGIANT);
        } else if (Objects.equals(customEntityId, Entities.DERELICT_CRYOSLEEPER)) {
            return system.getEntitiesWithTag(Tags.CRYOSLEEPER).isEmpty();
        } else if (Objects.equals(customEntityId, Entities.INACTIVE_GATE)) {
            return system.getEntitiesWithTag(Tags.GATE).isEmpty();
        }

        return true;
    }

    public static class BuildableMegastructure {
        public String id;
        public String name;
        public String icon;
        public float cost;
        public float buildTime;

        public BuildableMegastructure(String customEntityId, float cost, float buildTime) {
            CustomEntitySpecAPI spec = Global.getSettings().getCustomEntitySpec(customEntityId);

            this.id = spec.getId();
            this.name = spec.getDefaultName();
            this.icon = spec.getInteractionImage();
            this.cost = cost;
            this.buildTime = buildTime;
        }
    }

    public static class OrbitData {
        public SectorEntityToken entity;
        public float orbitAngle;
        public float orbitRadius;
        public float orbitDays;

        public OrbitData(SectorEntityToken entity, float orbitAngle, float orbitRadius, float orbitDays) {
            this.entity = entity;
            this.orbitAngle = orbitAngle;
            this.orbitRadius = orbitRadius;
            this.orbitDays = orbitDays;
        }
    }
}
