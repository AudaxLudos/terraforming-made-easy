package terraformingmadeeasy.industry;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetSpecAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.characters.MarketConditionSpecAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
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
            addOrImproveOrganicsAndFarming();
            m.addCondition(Conditions.HABITABLE);
        }
        if (m.hasCondition(Conditions.NO_ATMOSPHERE)) {
            removeOrganicsAndFarming();
            String[] poorBarrenTypes = {"barren", "barren2", "barren3", "barren_castiron", "barren_venuslike", "rocky_metallic", "barren-bombarded"};
            changePlanetVisuals(poorBarrenTypes[Misc.random.nextInt(poorBarrenTypes.length)]);
            if (m.hasCondition(Conditions.TECTONIC_ACTIVITY) || m.hasCondition(Conditions.EXTREME_TECTONIC_ACTIVITY)) {
                changePlanetVisuals("rocky_unstable");
            }
        }
        if (m.hasCondition(Conditions.THIN_ATMOSPHERE)) {
            removeFarming();
            reduceOrganicsToCommon();
            changePlanetVisuals("barren-desert");
        }
        if (m.hasCondition(Conditions.TOXIC_ATMOSPHERE)) {
            removeFarming();
            reduceOrganicsToCommon();
            changePlanetVisuals("toxic");
        }
        if (m.hasCondition(Conditions.IRRADIATED)) {
            removeOrganicsAndFarming();
            changePlanetVisuals("irradiated");
        }
        if (m.hasCondition(Conditions.TECTONIC_ACTIVITY) || m.hasCondition(Conditions.EXTREME_TECTONIC_ACTIVITY)) {
            if ((m.hasCondition(Conditions.VERY_HOT) || m.hasCondition(Conditions.HOT))
                    && (m.hasCondition(Conditions.TOXIC_ATMOSPHERE) || m.hasCondition(Conditions.THIN_ATMOSPHERE) || m.hasCondition(Conditions.DENSE_ATMOSPHERE))
                    && (m.hasCondition(Conditions.ORE_ABUNDANT) || m.hasCondition(Conditions.ORE_RICH) || m.hasCondition(Conditions.ORE_ULTRARICH))
                    && (m.hasCondition(Conditions.RARE_ORE_ABUNDANT) || m.hasCondition(Conditions.RARE_ORE_RICH) || m.hasCondition(Conditions.RARE_ORE_ULTRARICH))) {
                removeOrganicsAndFarming();
                changePlanetVisuals("lava");
            }
            if (m.hasCondition(Conditions.VERY_COLD)
                    && (m.hasCondition(Conditions.VOLATILES_TRACE) || m.hasCondition(Conditions.VOLATILES_DIFFUSE) ||
                    m.hasCondition(Conditions.VOLATILES_ABUNDANT) || m.hasCondition(Conditions.VOLATILES_PLENTIFUL))) {
                removeOrganicsAndFarming();
                changePlanetVisuals("cryovolcanic");
            }
        }
        if (m.hasCondition(Conditions.WATER_SURFACE)) {
            if (m.hasCondition(Conditions.VERY_COLD)
                    && (m.hasCondition(Conditions.VOLATILES_TRACE) || m.hasCondition(Conditions.VOLATILES_DIFFUSE)
                    || m.hasCondition(Conditions.VOLATILES_ABUNDANT) || m.hasCondition(Conditions.VOLATILES_PLENTIFUL))) {
                m.removeCondition(Conditions.WATER_SURFACE);
                removeOrganicsAndFarming();
                String[] frozenTypes = {"frozen", "frozen1", "frozen2", "frozen3"};
                changePlanetVisuals(frozenTypes[Misc.random.nextInt(frozenTypes.length)]);
            }
            if ((m.hasCondition(Conditions.COLD) || m.hasCondition(Conditions.VERY_COLD))
                    && m.hasCondition(Conditions.NO_ATMOSPHERE)) {
                m.removeCondition(Conditions.WATER_SURFACE);
                removeOrganicsAndFarming();
                changePlanetVisuals("rocky_ice");
            }
        }
        if (m.hasCondition(Conditions.HABITABLE)) {
            if (m.hasCondition(Conditions.HOT)) {
                String[] poorHabitableTypes = {"jungle", "arid", "desert", "desert1"};
                changePlanetVisuals(poorHabitableTypes[Misc.random.nextInt(poorHabitableTypes.length)]);
            }
            if (m.hasCondition(Conditions.COLD)) {
                changePlanetVisuals("tundra");
            }
            if (!m.hasCondition(Conditions.HOT) && !m.hasCondition(Conditions.COLD)) {
                String[] richHabitableTypes = {"terran", "terran-eccentric"};
                changePlanetVisuals(richHabitableTypes[Misc.random.nextInt(richHabitableTypes.length)]);
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

    public void removeOrganicsAndFarming() {
        removeFarming();
        removeOrganics();
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

        for (MutableCommodityQuantity SupplyStat : getMarket().getIndustry(Industries.FARMING).getAllSupply())
            SupplyStat.getQuantity().unmodify();
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

        for (MutableCommodityQuantity SupplyStat : getMarket().getIndustry(Industries.FARMING).getAllSupply())
            SupplyStat.getQuantity().unmodify();
    }

    public void addOrImproveOrganicsAndFarming() {
        addOrImproveFarming();
        addOrImproveOrganics();
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
        } else {
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
        } else {
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

    public Boolean canTerraformCondition(ModifiableCondition condition) {
        for (String requirement : condition.requirements)
            if (!getMarket().hasCondition(requirement))
                return false;
        return true;
    }
}
