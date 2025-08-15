package terraformingmadeeasy.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.PlanetSpecAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.procgen.PlanetGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import terraformingmadeeasy.Utils;

import java.util.*;

public class BaseTerraformingIndustry extends BaseDevelopmentIndustry {
    @Override
    protected String getBuildingText() {
        return "terraforming";
    }

    @Override
    protected void addProjectSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode) {
        float oPad = 10f;
        float pad = 3f;

        if (mode == IndustryTooltipMode.NORMAL || isUpgrading()) {
            if (isUpgrading()) {
                tooltip.addSectionHeading("Terraforming project", Alignment.MID, oPad);
                TooltipMakerAPI imageWithText = tooltip.beginImageWithText(this.project.icon, 40f, getTooltipWidth(), false);
                imageWithText.addPara("Status: %s", 0f, Misc.getHighlightColor(), "Ongoing");
                imageWithText.addPara("Action: %s", pad, Misc.getHighlightColor(), !this.market.hasCondition(this.project.id) ? "Add" : "Remove");
                imageWithText.addPara("Condition: %s", pad, Misc.getHighlightColor(), this.project.name);
                imageWithText.addPara("Days Left: %s", pad, Misc.getHighlightColor(), Math.round(this.buildTime - this.buildProgress) + "");
                tooltip.addImageWithText(oPad);
            } else {
                tooltip.addSectionHeading("No projects started", Alignment.MID, oPad);
            }
        }
    }

    @Override
    public void completeProject() {
        log.info(String.format("Completed %s %s condition in %s by %s", !this.market.hasCondition(this.project.id) ? "Adding" : "Removing", this.project.name, getMarket().getName(), getCurrentName()));

        sendCompletedMessage();
        terraformPlanet();
        if (this.market.getPrimaryEntity() instanceof PlanetAPI) {
            updatePlanetConditions();
            String category = evaluatePlanetCategory();
            String type = evaluatePlanetType(category);
            updatePlanetVisuals(type);
        }
        reapply();
        // Force reapply demands and supply
        for (Industry ind : this.market.getIndustries()) {
            ind.doPreSaveCleanup();
            ind.doPostSaveRestore();
        }
        this.project = null;
    }

    public void terraformPlanet() {
        if (Global.getSettings().getMarketConditionSpec(this.project.id) == null) {
            return;
        }

        if (this.market.hasCondition(this.project.id)) {
            this.market.removeCondition(this.project.id);
        } else {
            this.market.addCondition(this.project.id);
            this.market.getFirstCondition(this.project.id).setSurveyed(true);

            // remove all hated conditions
            if (!this.project.hatedConditions.isEmpty()) {
                String textExpression = this.project.hatedConditions;
                String[] expressions = textExpression.split(",");
                for (String s : expressions) {
                    String expression = s;
                    if (expression.contains("needAll")) {
                        expression = expression.replaceAll("needAll:", "");
                    } else if (expression.contains("needOne")) {
                        expression = expression.replaceAll("needOne:", "");
                    }
                    String[] ids = expression.split("\\|");
                    for (String id : ids) {
                        if (this.market.hasCondition(id)) {
                            this.market.removeCondition(id);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void sendCompletedMessage() {
        if (this.market.isPlayerOwned()) {
            String addOrRemoveText = !this.market.hasCondition(this.project.id) ? "Added " : "Removed ";
            MessageIntel intel = new MessageIntel("Terraforming completed at " + this.market.getName(), Misc.getBasePlayerColor());
            intel.addLine(BaseIntelPlugin.BULLET + addOrRemoveText + this.project.name.toLowerCase() + " planet condition");
            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, this.market);
        }
    }

    public void updatePlanetConditions() {
        boolean removeFarming = false;
        boolean removeOrganics = false;
        boolean reduceOrganics = false;
        boolean removeLobsters = true;
        boolean removeWaterSurface = true;
        if (!this.market.getPlanetEntity().isStar() && !this.market.getPlanetEntity().isGasGiant()
                && !this.market.hasCondition(Conditions.HABITABLE) && !this.market.hasCondition(Conditions.VERY_COLD)
                && !this.market.hasCondition(Conditions.VERY_HOT) && !this.market.hasCondition(Conditions.NO_ATMOSPHERE)
                && !this.market.hasCondition(Conditions.THIN_ATMOSPHERE) && !this.market.hasCondition(Conditions.DENSE_ATMOSPHERE)
                && !this.market.hasCondition(Conditions.TOXIC_ATMOSPHERE) && !this.market.hasCondition(Conditions.IRRADIATED)
                && !this.market.hasCondition(Conditions.DARK) && !this.market.hasCondition(Conditions.EXTREME_TECTONIC_ACTIVITY)) {
            addOrImproveFarming();
            addOrImproveOrganics();
            this.market.addCondition(Conditions.HABITABLE);
        }
        if (this.market.hasCondition(Conditions.WATER_SURFACE)) {
            removeFarming = true;
            removeLobsters = false;
            removeWaterSurface = false;
        }
        if (this.market.hasCondition(Conditions.NO_ATMOSPHERE) || this.market.hasCondition(Conditions.VERY_HOT) ||
                this.market.hasCondition(Conditions.VERY_COLD) || this.market.hasCondition(Conditions.EXTREME_TECTONIC_ACTIVITY)) {
            removeFarming = true;
            removeOrganics = true;
            removeLobsters = true;
            removeWaterSurface = true;
        }
        if (this.market.hasCondition(Conditions.THIN_ATMOSPHERE)) {
            removeFarming = true;
            reduceOrganics = true;
            removeLobsters = true;
            removeWaterSurface = true;
        }
        if (this.market.hasCondition(Conditions.TOXIC_ATMOSPHERE)) {
            removeFarming = true;
            reduceOrganics = true;
            removeLobsters = true;
            removeWaterSurface = true;
        }
        if (this.market.hasCondition(Conditions.IRRADIATED)) {
            removeFarming = true;
            removeOrganics = true;
            reduceOrganics = false;
            removeLobsters = true;
            removeWaterSurface = true;
        }
        if (!this.market.hasCondition(Conditions.HABITABLE) && !this.market.hasCondition(Conditions.NO_ATMOSPHERE)) {
            if (this.market.hasCondition(Conditions.TECTONIC_ACTIVITY) || this.market.hasCondition(Conditions.EXTREME_TECTONIC_ACTIVITY)) {
                if (this.market.hasCondition(Conditions.VERY_HOT) || this.market.hasCondition(Conditions.HOT)) {
                    removeFarming = true;
                    removeOrganics = true;
                    reduceOrganics = false;
                    removeLobsters = true;
                    removeWaterSurface = true;
                }
                if (this.market.hasCondition((Conditions.VERY_COLD))) {
                    removeFarming = true;
                    removeOrganics = true;
                    reduceOrganics = false;
                    removeLobsters = true;
                    removeWaterSurface = true;
                }
            }
        }
        if (this.market.getPlanetEntity().isGasGiant()) {
            removeFarming = true;
            removeOrganics = true;
            reduceOrganics = false;
            removeLobsters = true;
            removeWaterSurface = true;
        }

        if (removeFarming) {
            removeFarming();
        }
        if (removeLobsters) {
            removeLobsters();
        }
        if (removeWaterSurface) {
            removeWaterSurface();
        }
        if (reduceOrganics) {
            reduceOrganicsToCommon();
        } else if (removeOrganics) {
            removeOrganics();
        }

        updateFarmingOrAquaculture();
    }

    public String evaluatePlanetCategory() {
        PlanetGenDataSpec spec = (PlanetGenDataSpec) Global.getSettings()
                .getSpec(PlanetGenDataSpec.class, this.market.getPlanetEntity().getTypeId(), false);
        String result = spec.getCategory();

        if (this.market.hasCondition(Conditions.HABITABLE)) {
            result = "cat_hab4";

            if (this.market.hasCondition(Conditions.HOT) || this.market.hasCondition(Conditions.WATER_SURFACE) || this.market.hasCondition(Conditions.COLD)) {
                result = "cat_hab3";
            }
            if (!this.market.hasCondition(Conditions.WATER_SURFACE) && this.market.hasCondition(Conditions.HOT) && this.market.hasCondition(Conditions.EXTREME_WEATHER)) {
                result = "cat_hab2";
            }
        }
        if (this.market.hasCondition(Conditions.THIN_ATMOSPHERE)) {
            result = "cat_hab1";
        }
        if (this.market.hasCondition(Conditions.VERY_HOT) || this.market.hasCondition(Conditions.EXTREME_TECTONIC_ACTIVITY)) {
            result = "cat_lava";
        }
        if (this.market.hasCondition(Conditions.NO_ATMOSPHERE)) {
            result = "cat_barren";
        }
        if (this.market.hasCondition(Conditions.VERY_COLD)) {
            result = "cat_frozen";
        }
        if (this.market.hasCondition(Conditions.TOXIC_ATMOSPHERE)) {
            result = "cat_toxic";
        }
        if (this.market.hasCondition(Conditions.IRRADIATED)) {
            result = "cat_irradiated";
        }
        if (!this.market.hasCondition(Conditions.HABITABLE) && !this.market.hasCondition(Conditions.NO_ATMOSPHERE)) {
            if (this.market.hasCondition(Conditions.TECTONIC_ACTIVITY) || this.market.hasCondition(Conditions.EXTREME_TECTONIC_ACTIVITY)) {
                if (this.market.hasCondition(Conditions.VERY_HOT) || this.market.hasCondition(Conditions.HOT)) {
                    result = "cat_lava";
                }
                if (this.market.hasCondition((Conditions.VERY_COLD))) {
                    result = "cat_cryovolcanic";
                }
            }
        }
        if (this.market.getPlanetEntity().isGasGiant()) {
            result = "cat_giant";
        }

        return result;
    }

    public String evaluatePlanetType(String category) {
        WeightedRandomPicker<String> picker = new WeightedRandomPicker<>(Misc.random);
        Collection<PlanetGenDataSpec> specs = Global.getSettings().getAllSpecs(PlanetGenDataSpec.class);
        for (PlanetGenDataSpec spec : specs) {
            if (Objects.equals(spec.getCategory(), category)) {
                if (spec.getFrequency() > 0) {
                    picker.add(spec.getId(), 1);
                }
            }
        }

        List<String> items = picker.getItems();
        if (Objects.equals(category, "cat_hab3")) {
            if (items.contains("water")) {
                int index = items.indexOf("water");
                if (this.market.hasCondition(Conditions.WATER_SURFACE)) {
                    picker.setWeight(index, 100f);
                } else {
                    picker.remove("water");
                }
            }
            if (items.contains("tundra")) {
                int index = items.indexOf("tundra");
                if (this.market.hasCondition(Conditions.COLD)) {
                    picker.setWeight(index, 100f);
                } else {
                    picker.remove("tundra");
                }
            }
        } else if (Objects.equals(category, "cat_barren")) {
            if (items.contains("rocky_unstable")) {
                int index = items.indexOf("rocky_unstable");
                if (this.market.hasCondition(Conditions.TECTONIC_ACTIVITY) || this.market.hasCondition(Conditions.EXTREME_TECTONIC_ACTIVITY)) {
                    picker.setWeight(index, 100f);
                } else {
                    picker.remove("rocky_unstable");
                }
            }
            if (items.contains("rocky_ice")) {
                int index = items.indexOf("rocky_ice");
                if (this.market.hasCondition(Conditions.COLD)) {
                    picker.setWeight(index, 100f);
                } else {
                    picker.remove("rocky_ice");
                }
            }
        } else if (Objects.equals(category, "cat_giant")) {
            if (items.contains("ice_giant")) {
                int index = items.indexOf("ice_giant");
                if (this.market.hasCondition(Conditions.COLD) || this.market.hasCondition(Conditions.VERY_COLD)) {
                    picker.setWeight(index, 100f);
                } else {
                    picker.remove("ice_giant");
                }
            }
        }

        return picker.pick();
    }

    public void removeWaterSurface() {
        this.market.removeCondition(Conditions.WATER_SURFACE);
    }

    public void updateFarmingOrAquaculture() {
        if (Utils.isAOTDVOKEnabled()) {
            if (!this.market.hasCondition(Conditions.WATER_SURFACE)) {
                if (this.market.hasIndustry(Industries.AQUACULTURE)) {
                    this.market.removeIndustry(Industries.AQUACULTURE, null, false);
                    this.market.addIndustry(Industries.FARMING);
                } else if (this.market.hasIndustry("fishery")) {
                    this.market.removeIndustry("fishery", null, false);
                    this.market.addIndustry("subfarming");
                }
            } else if (this.market.hasCondition(Conditions.WATER_SURFACE)) {
                if (this.market.hasIndustry(Industries.FARMING)) {
                    this.market.removeIndustry(Industries.FARMING, null, false);
                    this.market.addIndustry(Industries.AQUACULTURE);
                } else if (this.market.hasIndustry("subfarming")) {
                    this.market.removeIndustry("subfarming", null, false);
                    this.market.addIndustry("fishery");
                } else if (this.market.hasIndustry("artifarming")) {
                    this.market.removeIndustry("artifarming", null, false);
                    this.market.addIndustry("fishery");
                }
            }
        } else {
            if (!this.market.hasCondition(Conditions.WATER_SURFACE) && this.market.hasIndustry(Industries.AQUACULTURE)) {
                this.market.removeIndustry(Industries.AQUACULTURE, null, false);
                this.market.addIndustry(Industries.FARMING);
            } else if (this.market.hasCondition(Conditions.WATER_SURFACE) && this.market.hasIndustry(Industries.FARMING)) {
                this.market.removeIndustry(Industries.FARMING, null, false);
                this.market.addIndustry(Industries.AQUACULTURE);
            }
        }
    }

    public void updatePlanetVisuals(String planetTypeId) {
        String planetType = planetTypeId;
        if (!this.project.planetSpecOverride.isEmpty()) {
            for (PlanetSpecAPI spec : Global.getSettings().getAllPlanetSpecs()) {
                if (spec.isStar()) {
                    continue;
                }
                if (Objects.equals(spec.getPlanetType(), this.project.planetSpecOverride)) {
                    planetType = spec.getPlanetType();
                    break;
                }
            }
        }
        this.market.getPlanetEntity().changeType(planetType, StarSystemGenerator.random);
        this.market.getPlanetEntity().applySpecChanges();
    }

    public void removeFarming() {
        this.market.removeCondition(Conditions.FARMLAND_POOR);
        this.market.removeCondition(Conditions.FARMLAND_ADEQUATE);
        this.market.removeCondition(Conditions.FARMLAND_RICH);
        this.market.removeCondition(Conditions.FARMLAND_BOUNTIFUL);
    }

    public void removeOrganics() {
        this.market.removeCondition(Conditions.ORGANICS_TRACE);
        this.market.removeCondition(Conditions.ORGANICS_COMMON);
        this.market.removeCondition(Conditions.ORGANICS_ABUNDANT);
        this.market.removeCondition(Conditions.ORGANICS_PLENTIFUL);
    }

    public void removeLobsters() {
        this.market.removeCondition(Conditions.VOLTURNIAN_LOBSTER_PENS);
    }

    public void addOrImproveFarming() {
        if (this.market.hasCondition(Conditions.FARMLAND_POOR)) {
            this.market.removeCondition(Conditions.FARMLAND_POOR);
            this.market.addCondition(Conditions.FARMLAND_ADEQUATE);
            this.market.getFirstCondition(Conditions.FARMLAND_ADEQUATE).setSurveyed(true);
        } else if (this.market.hasCondition(Conditions.FARMLAND_ADEQUATE)) {
            this.market.removeCondition(Conditions.FARMLAND_ADEQUATE);
            this.market.addCondition(Conditions.FARMLAND_RICH);
            this.market.getFirstCondition(Conditions.FARMLAND_RICH).setSurveyed(true);
        } else if (this.market.hasCondition(Conditions.FARMLAND_RICH)) {
            this.market.removeCondition(Conditions.FARMLAND_RICH);
            this.market.addCondition(Conditions.FARMLAND_BOUNTIFUL);
            this.market.getFirstCondition(Conditions.FARMLAND_BOUNTIFUL).setSurveyed(true);
        } else if (!this.market.hasCondition(Conditions.FARMLAND_BOUNTIFUL)) {
            this.market.addCondition(Conditions.FARMLAND_POOR);
            this.market.getFirstCondition(Conditions.FARMLAND_POOR).setSurveyed(true);
        }
    }

    public void addOrImproveOrganics() {
        if (this.market.hasCondition(Conditions.ORGANICS_TRACE)) {
            this.market.removeCondition(Conditions.ORGANICS_TRACE);
            this.market.addCondition(Conditions.ORGANICS_COMMON);
            this.market.getFirstCondition(Conditions.ORGANICS_COMMON).setSurveyed(true);
        } else if (this.market.hasCondition(Conditions.ORGANICS_COMMON)) {
            this.market.removeCondition(Conditions.ORGANICS_COMMON);
            this.market.addCondition(Conditions.ORGANICS_ABUNDANT);
            this.market.getFirstCondition(Conditions.ORGANICS_ABUNDANT).setSurveyed(true);
        } else if (this.market.hasCondition(Conditions.ORGANICS_ABUNDANT)) {
            this.market.removeCondition(Conditions.ORGANICS_ABUNDANT);
            this.market.addCondition(Conditions.ORGANICS_PLENTIFUL);
            this.market.getFirstCondition(Conditions.ORGANICS_PLENTIFUL).setSurveyed(true);
        } else if (!this.market.hasCondition(Conditions.ORGANICS_PLENTIFUL)) {
            this.market.addCondition(Conditions.ORGANICS_TRACE);
            this.market.getFirstCondition(Conditions.ORGANICS_TRACE).setSurveyed(true);
        }
    }

    public void reduceOrganicsToCommon() {
        if (this.market.hasCondition(Conditions.ORGANICS_ABUNDANT) || this.market.hasCondition(Conditions.ORGANICS_PLENTIFUL)) {
            this.market.removeCondition(Conditions.ORGANICS_ABUNDANT);
            this.market.removeCondition(Conditions.ORGANICS_PLENTIFUL);
            this.market.addCondition(Conditions.ORGANICS_COMMON);
            this.market.getFirstCondition(Conditions.ORGANICS_COMMON).setSurveyed(true);
        }
    }

    public Boolean canTerraformCondition(Utils.ProjectData condition) {
        return hasRequiredConditions(condition) && hasRequiredIndustries(condition);
    }

    public boolean hasRequiredConditions(Utils.ProjectData condition) {
        String text = condition.likedConditions;

        if (text == null || text.isEmpty()) {
            return true;
        }

        String[] expressions = text.split(",");
        boolean[] expressionsResult = new boolean[expressions.length];

        for (int i = 0; i < expressions.length; i++) {
            String expression = expressions[i];
            if (expression.contains("needAll")) {
                expression = expression.replaceAll("needAll:", "").replaceAll("\\|", "&&");
            } else if (expression.contains("needOne")) {
                expression = expression.replaceAll("needOne:", "").replaceAll("\\|", "||");
            }

            String[] ids = expression.split("&&|\\|\\|");
            Map<String, Boolean> values = new HashMap<>();
            for (String id : ids) {
                values.put(id, this.market.hasCondition(id));
            }
            expressionsResult[i] = Utils.evaluateExpression(expression, values);
        }

        return Utils.isAllTrue(expressionsResult);
    }

    public boolean hasRequiredIndustries(Utils.ProjectData condition) {
        String text = condition.likedIndustries;

        if (text == null || text.isEmpty()) {
            return true;
        }

        String[] expressions = text.split(",");
        boolean[] expressionsResult = new boolean[expressions.length];

        for (int i = 0; i < expressions.length; i++) {
            String expression = expressions[i];
            if (expression.contains("needAll")) {
                expression = expression.replaceAll("needAll:", "").replaceAll("\\|", "&&");
            } else if (expression.contains("needOne")) {
                expression = expression.replaceAll("needOne:", "").replaceAll("\\|", "||");
            }

            String[] ids = expression.split("&&|\\|\\|");
            Map<String, Boolean> values = new HashMap<>();
            for (String id : ids) {
                values.put(id, this.market.hasIndustry(id));
            }
            expressionsResult[i] = Utils.evaluateExpression(expression, values);
        }

        return Utils.isAllTrue(expressionsResult);
    }
}
