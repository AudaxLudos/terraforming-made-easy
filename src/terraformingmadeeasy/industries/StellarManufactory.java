package terraformingmadeeasy.industries;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.Misc;
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.ids.TMEIds;

import java.util.List;
import java.util.Objects;

public class StellarManufactory extends BaseTerraformingIndustry {
    public enum StellarMirrorOptions {
        NONE,
        ADD_ONE,
        ADD_THREE,
        ADD_FIVE,
        REMOVE_ALL
    }
    public StellarMirrorOptions stellarMirrorData = StellarMirrorOptions.NONE;
    public StellarMirrorOptions stellarShadeData = StellarMirrorOptions.NONE;

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
        addSolarMirrors(Entities.STELLAR_MIRROR, this.stellarMirrorData, randomAngle);
        this.stellarMirrorData = StellarMirrorOptions.NONE;
        addSolarMirrors(Entities.STELLAR_SHADE, this.stellarShadeData, randomAngle);
        this.stellarShadeData = StellarMirrorOptions.NONE;
    }

    public void addSolarMirrors(String mirrorType, StellarMirrorOptions data, float randAngle) {
        int numOfMirrors = 0;
        float angleOffset = 0f;
        switch (data) {
            case NONE:
                return;
            case ADD_ONE:
                numOfMirrors = 1;
                break;
            case ADD_THREE:
                numOfMirrors = 3;
                angleOffset = 25f;
                break;
            case ADD_FIVE:
                numOfMirrors = 5;
                angleOffset = 50f;
                break;
            case REMOVE_ALL:
                removeSolarMirrors(mirrorType);
                return;
        }

        removeSolarMirrors(mirrorType);

        float startAngle = 0f;
        if (Objects.equals(mirrorType, Entities.STELLAR_SHADE)) {
            startAngle = 180f;
        }

        SectorEntityToken entity = getMarket().getPrimaryEntity();
        float period = 100f;
        float angle = randAngle + startAngle - angleOffset;
        float radius = entity.getRadius() + 270f;
        if (!entity.getStarSystem().isNebula() || entity.getCircularOrbitPeriod() > 0f) {
            period = entity.getCircularOrbitPeriod();
            angle = entity.getCircularOrbitAngle() + startAngle - angleOffset;
        }

        for (int i = 0; i < numOfMirrors; i++) {
            SectorEntityToken mirror = getMarket().getStarSystem().addCustomEntity(null, null, mirrorType, Factions.NEUTRAL);
            mirror.setCircularOrbitPointingDown(entity, angle, radius, period);
            angle += 25f;
        }

        log.info(String.format("Added %s %s to %s", numOfMirrors, mirrorType.replaceAll("_", " "), getMarket().getName().toLowerCase()));
    }

    public void removeSolarMirrors(String mirrorType) {
        StarSystemAPI system = getMarket().getStarSystem();
        List<SectorEntityToken> mirrors = system.getEntitiesWithTag(mirrorType);
        for (SectorEntityToken mirror : mirrors) {
            if (mirror.getOrbitFocus() == getMarket().getPrimaryEntity()) {
                system.removeEntity(mirror);
            }
        }
    }
}
