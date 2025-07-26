package terraformingmadeeasy.industries;

import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.Misc;
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.ids.TMEIds;

import java.util.List;

public class StellarManufactory extends BaseTerraformingIndustry {
    @Override
    public String getAOTDVOKTechId() {
        return TMEIds.STELLAR_MANUFACTORY_TECH;
    }

    @Override
    public List<Utils.ProjectData> getProjects() {
        return Utils.STELLAR_MANUFACTORY_OPTIONS;
    }

    @Override
    public void completeProject() {
        super.completeProject();
        float randomAngle = Misc.random.nextFloat() * 360f;
        addSolarMirrors(randomAngle);
        addSolarShades(randomAngle);
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
