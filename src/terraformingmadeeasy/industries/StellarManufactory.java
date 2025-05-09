package terraformingmadeeasy.industries;

import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.Misc;
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.ids.TMEIds;

import java.util.List;

public class StellarManufactory extends TMEBaseIndustry {
    public StellarManufactory() {
        setModifiableConditions(Utils.STELLAR_MANUFACTORY_OPTIONS);
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
            float randomAngle = Misc.random.nextFloat() * 360f;
            addSolarMirrors(randomAngle);
            addSolarShades(randomAngle);
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
    public String getAOTDVOKTechId() {
        return TMEIds.STELLAR_MANUFACTORY_TECH;
    }

    public void addSolarMirrors(float randAngle) {
        if (hasSolarMirrors()) {
            return;
        }

        PlanetAPI planet = getMarket().getPlanetEntity();
        float period = 100f;
        float angle = randAngle;
        float radius = planet.getRadius() + 270f;
        if (!planet.getStarSystem().isNebula() || planet.getCircularOrbitPeriod() > 0f) {
            period = planet.getCircularOrbitPeriod();
            angle = planet.getCircularOrbitAngle() - 60f;
        }

        for (int i = 0; i < 5; i++) {
            SectorEntityToken mirror = getMarket().getStarSystem().addCustomEntity(null, null, Entities.STELLAR_MIRROR, Factions.NEUTRAL);
            mirror.setCircularOrbitPointingDown(getMarket().getPlanetEntity(), angle, radius, period);
            angle += 30f;
        }

        log.info(String.format("Added 5 Stellar Mirrors to %s", getMarket().getName()));
    }

    public void addSolarShades(float randAngle) {
        if (hasSolarShades()) {
            return;
        }

        PlanetAPI planet = getMarket().getPlanetEntity();
        float period = 100f;
        float angle = randAngle + 180f + 25f;
        float radius = planet.getRadius() + 270f;
        if (!planet.getStarSystem().isNebula() || planet.getCircularOrbitPeriod() > 0f) {
            period = planet.getCircularOrbitPeriod();
            angle = planet.getCircularOrbitAngle() + 180f - 25f;
        }

        for (int i = 0; i < 3; i++) {
            SectorEntityToken mirror = getMarket().getStarSystem().addCustomEntity(null, null, Entities.STELLAR_SHADE, Factions.NEUTRAL);
            mirror.setCircularOrbitPointingDown(getMarket().getPlanetEntity(), angle, radius, period);
            angle += 25f;
        }

        log.info(String.format("Added 3 Stellar Shades to %s", getMarket().getName()));
    }

    public boolean hasSolarShades() {
        List<SectorEntityToken> shades = getMarket().getStarSystem().getEntitiesWithTag("stellar_shade");
        for (SectorEntityToken shade : shades) {
            if (shade.getOrbitFocus() == getMarket().getPlanetEntity()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSolarMirrors() {
        List<SectorEntityToken> mirrors = getMarket().getStarSystem().getEntitiesWithTag("stellar_mirror");
        for (SectorEntityToken mirror : mirrors) {
            if (mirror.getOrbitFocus() == getMarket().getPlanetEntity()) {
                return true;
            }
        }
        return false;
    }
}
