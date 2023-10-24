package terraformingmadeeasy.industry;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetSpecAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MarketConditionSpecAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.loading.specs.PlanetSpec;

import java.util.ArrayList;
import java.util.List;

public class TMEBaseIndustry extends BaseIndustry {
    public static class ModifiableCondition {
        public MarketConditionSpecAPI spec;
        public float cost;
        public float buildTime;
        public List<String> restrictions = new ArrayList<>();
        public List<String> requirements = new ArrayList<>();

        public ModifiableCondition(String conditionSpecId, float cost, float buildTime, List<String> restrictions, List<String> requirements) {
            this.spec = Global.getSettings().getMarketConditionSpec(conditionSpecId);
            this.cost = cost;
            this.buildTime = buildTime;
            if (restrictions != null) this.restrictions = restrictions;
            if (requirements != null) this.requirements = requirements;
        }
    }

    public List<ModifiableCondition> modifiableConditions = new ArrayList<>();
    public ModifiableCondition modifiableCondition = null;

    @Override
    public void apply() {
        apply(true);
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
        if (modifiableCondition != null) {
            market.removeIndustry(getId(), null, true);
            market.addIndustry(getId());
            BaseIndustry industry = (BaseIndustry) market.getIndustry(getId());
            industry.setAICoreId(getAICoreId());
            industry.setImproved(isImproved());
            sendTerraformingMessage();
            setSpecialItem(industry.getSpecialItem());
            changePlanetConditions(modifiableCondition);
            changePlanetClass();
            industry.reapply();
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
    }

    public void cancelUpgrade() {
        building = false;
        buildProgress = 0;
        modifiableCondition = null;
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

    public void changePlanetConditions(ModifiableCondition condition) {
        if (getMarket().hasCondition(condition.spec.getId())) {
            getMarket().removeCondition(condition.spec.getId());
        } else {
            getMarket().addCondition(condition.spec.getId());
            for (String restriction : condition.restrictions) {
                if (getMarket().hasCondition(restriction))
                    getMarket().removeCondition(restriction);
            }
        }
    }

    public void changePlanetClass() {
        MarketAPI m = getMarket();
        if (!m.getPlanetEntity().isStar() && !m.getPlanetEntity().isGasGiant()
                && !m.hasCondition(Conditions.HABITABLE) && !m.hasCondition(Conditions.VERY_COLD)
                && !m.hasCondition(Conditions.VERY_HOT) && !m.hasCondition(Conditions.NO_ATMOSPHERE)
                && !m.hasCondition(Conditions.THIN_ATMOSPHERE) && !m.hasCondition(Conditions.DENSE_ATMOSPHERE)
                && !m.hasCondition(Conditions.TOXIC_ATMOSPHERE) && !m.hasCondition(Conditions.IRRADIATED)
                && !m.hasCondition(Conditions.DARK)) {
            m.addCondition(Conditions.HABITABLE);
        }
        if (m.hasCondition(Conditions.NO_ATMOSPHERE)) {
            // change planet to barren
            // remove farming and organics
            changePlanetVisuals("barren");
            System.out.println("terraform to barren world");
        }
        if (m.hasCondition(Conditions.THIN_ATMOSPHERE)) {
            // change planet to barren-dessert
            changePlanetVisuals("barren-desert");
            System.out.println("terraform to barren-dessert world");
        }
        if (m.hasCondition(Conditions.TOXIC_ATMOSPHERE)) {
            // change planet to toxic
            // lower organics down to organics common
            // remove farming
            changePlanetVisuals("toxic");
            System.out.println("terraform to toxic world");
        }
        if (m.hasCondition(Conditions.IRRADIATED)) {
            // change planet to irradiated
            // remove farming and organics
            changePlanetVisuals("irradiated");
            System.out.println("terraform to irradiated world");
        }
        if (m.hasCondition(Conditions.TECTONIC_ACTIVITY) || m.hasCondition(Conditions.EXTREME_TECTONIC_ACTIVITY)) {
            if ((m.hasCondition(Conditions.VERY_HOT) || m.hasCondition(Conditions.HOT))
                    && (m.hasCondition(Conditions.TOXIC_ATMOSPHERE) || m.hasCondition(Conditions.THIN_ATMOSPHERE) || m.hasCondition(Conditions.DENSE_ATMOSPHERE))
                    && (m.hasCondition(Conditions.ORE_ABUNDANT) || m.hasCondition(Conditions.ORE_RICH) || m.hasCondition(Conditions.ORE_ULTRARICH))
                    && (m.hasCondition(Conditions.RARE_ORE_ABUNDANT) || m.hasCondition(Conditions.RARE_ORE_RICH) || m.hasCondition(Conditions.RARE_ORE_ULTRARICH))) {
                // Change planet to lava
                // remove farming and organics
                changePlanetVisuals("lava");
                System.out.println("terraform to lava or lava-minor world");
            }
            if (m.hasCondition(Conditions.VERY_COLD)
                    && (m.hasCondition(Conditions.VOLATILES_TRACE) || m.hasCondition(Conditions.VOLATILES_DIFFUSE) ||
                    m.hasCondition(Conditions.VOLATILES_ABUNDANT) || m.hasCondition(Conditions.VOLATILES_PLENTIFUL))) {
                // change planet cryovolcanic
                // remove farming and organics
                changePlanetVisuals("cryovolcanic");
                System.out.println("terraform to cryovolanic world");
            }
        }
        if (m.hasCondition(Conditions.WATER_SURFACE)) {
            if (m.hasCondition(Conditions.VERY_COLD)
                    && (m.hasCondition(Conditions.VOLATILES_TRACE) || m.hasCondition(Conditions.VOLATILES_DIFFUSE)
                    || m.hasCondition(Conditions.VOLATILES_ABUNDANT) || m.hasCondition(Conditions.VOLATILES_PLENTIFUL))) {
                // change planet frozen
                // remove farming and organics
                // remove water surface
                changePlanetVisuals("frozen");
                System.out.println("terraform to frozen world");
            }
            if ((m.hasCondition(Conditions.COLD) || m.hasCondition(Conditions.VERY_COLD))
                    && m.hasCondition(Conditions.NO_ATMOSPHERE)) {
                // change planet to rocky ice
                // remove farming and organics
                // remove water surface
                changePlanetVisuals("rocky_ice");
                System.out.println("terraform to rocky ice world");
            }
        }
        if (m.hasCondition(Conditions.HABITABLE)) {
            if (m.hasCondition(Conditions.HOT)) {
                // change planet to jungle or arid or desert
                // add or improve farming or organics
                changePlanetVisuals("jungle");
                System.out.println("terraform to jungle or arid or desert world");
            }
            if (m.hasCondition(Conditions.COLD)) {
                // change planet to tundra
                // add or improve farming or organics
                changePlanetVisuals("tundra");
                System.out.println("terraform to tundra world");
            }
            if (!m.hasCondition(Conditions.HOT) && !m.hasCondition(Conditions.COLD)) {
                // change planet to terran or terran-eccentric
                // add or improve farming or organics
                changePlanetVisuals("terran");
                System.out.println("terraform to terran or terran-eccentric world");
            }
        }
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

    public Boolean canTerraformCondition(ModifiableCondition condition) {
        for (String requirement : condition.requirements)
            if (!getMarket().hasCondition(requirement))
                return false;
        return true;
    }
}
