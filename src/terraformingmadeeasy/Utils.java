package terraformingmadeeasy;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomEntitySpecAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetSpecAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MarketConditionSpecAPI;
import com.fs.starfarer.api.impl.campaign.procgen.PlanetGenDataSpec;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.util.Misc;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static final String TERRAFORMING_OPTIONS_FILE = "data/campaign/tme_terraforming_options.csv";
    public static final String MEGASTRUCTURE_OPTIONS_FILE = "data/campaign/tme_megastructure_options.csv";
    public static List<Utils.ProjectData> AGRICULTURAL_LABORATORY_OPTIONS = new ArrayList<>();
    public static List<Utils.ProjectData> ATMOSPHERE_REGULATOR_OPTIONS = new ArrayList<>();
    public static List<Utils.ProjectData> CONSTRUCTION_GRID_OPTIONS = new ArrayList<>();
    public static List<Utils.ProjectData> ELEMENT_SYNTHESIZER_OPTIONS = new ArrayList<>();
    public static List<Utils.ProjectData> GEOMORPHOLOGY_STATION_OPTIONS = new ArrayList<>();
    public static List<Utils.ProjectData> MINERAL_REPLICATOR_OPTIONS = new ArrayList<>();
    public static List<Utils.ProjectData> PLANETARY_HOLOGRAM_OPTIONS = new ArrayList<>();
    public static List<Utils.ProjectData> STELLAR_MANUFACTORY_OPTIONS = new ArrayList<>();
    public static List<Utils.ProjectData> TERRESTRIAL_ENGINE_OPTIONS = new ArrayList<>();
    public static List<Utils.ProjectData> UNIFICATION_CENTER_OPTIONS = new ArrayList<>();

    public static boolean isAllTrue(boolean[] array) {
        for (boolean b : array) {
            if (!b) {
                return false;
            }
        }
        return true;
    }

    public static Set<String> getUniqueIds(String text) {
        StringBuilder tempText = new StringBuilder();
        String[] expressions = text.split(",");

        for (String s : expressions) {
            if (s.contains("needAll")) {
                tempText.append(s.replaceAll("needAll:", ""));
            } else if (s.contains("needOne")) {
                tempText.append(s.replaceAll("needOne:", ""));
            }
            tempText.append("|");
        }

        String[] ids = tempText.toString().split("\\|");
        return new HashSet<>(Arrays.asList(ids));
    }

    public static boolean evaluateExpression(String text, Map<String, Boolean> values) {
        // Resolve parentheses recursively
        String expression = text;
        Pattern pattern = Pattern.compile("\\([^()]*\\)");
        Matcher matcher = pattern.matcher(expression);
        while (matcher.find()) {
            String subExpression = matcher.group();
            boolean result = evaluateExpression(subExpression.substring(1, subExpression.length() - 1), values);
            expression = expression.substring(0, matcher.start()) + result + expression.substring(matcher.end());
            matcher = pattern.matcher(expression); // Reset matcher after modification
        }

        // Evaluate AND (&&)
        if (expression.contains("&&")) {
            for (String part : expression.split("&&")) {
                if (!evaluateExpression(part, values)) {
                    return false;
                }
            }
            return true;
        }

        // Evaluate OR (||)
        if (expression.contains("||")) {
            for (String part : expression.split("\\|\\|")) {
                if (evaluateExpression(part, values)) {
                    return true;
                }
            }
            return false;
        }

        // Return the value of the term (true/false or variable)
        if (expression.equals("true")) {
            return true;
        }
        if (expression.equals("false")) {
            return false;
        }
        Boolean value = values.get(expression);
        if (value == null) {
            throw new IllegalArgumentException("Unknown variable: " + expression);
        }
        return value;
    }

    public static List<Utils.ProjectData> getPlanetaryHologramOptions() {
        List<Utils.ProjectData> options = new ArrayList<>();
        for (PlanetGenDataSpec pDataSpec : Global.getSettings().getAllSpecs(PlanetGenDataSpec.class)) {
            for (PlanetSpecAPI pSpec : Global.getSettings().getAllPlanetSpecs()) {
                if (Objects.equals(pDataSpec.getId(), pSpec.getPlanetType())) {
                    options.add(new Utils.ProjectData(
                            pDataSpec.getId(),
                            pSpec.getName(),
                            pSpec.getTexture(),
                            100000f,
                            30f,
                            true,
                            null,
                            null,
                            null,
                            pDataSpec.getId()
                    ));
                    break;
                }
            }
        }
        return options;
    }

    public static List<Utils.ProjectData> getMegastructureOptions() {
        try {
            List<Utils.ProjectData> projects = new ArrayList<>();
            JSONArray data = Global.getSettings().loadCSV(Utils.MEGASTRUCTURE_OPTIONS_FILE);
            for (int i = 0; i < data.length(); i++) {
                JSONObject row = data.getJSONObject(i);
                if (row.getString("structureId").isEmpty()) {
                    continue;
                }
                if (row.getString("structureId").contains("#")) {
                    continue;
                }

                String structureId = row.getString("structureId").replaceAll("\\s", "").trim();
                float cost = row.getInt("cost");
                float buildTime = row.getInt("buildTime");

                projects.add(new Utils.ProjectData(
                        Global.getSettings().getCustomEntitySpec(structureId),
                        cost,
                        buildTime));
            }
            return projects;
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Utils.ProjectData> getTerraformingOptions(String industryId) {
        try {
            List<Utils.ProjectData> projects = new ArrayList<>();
            JSONArray data = Global.getSettings().loadCSV(Utils.TERRAFORMING_OPTIONS_FILE);
            for (int i = 0; i < data.length(); i++) {
                JSONObject row = data.getJSONObject(i);
                if (!Objects.equals(row.getString("structureId"), industryId)) {
                    continue;
                }
                if (row.getString("structureId").isEmpty()) {
                    continue;
                }
                if (row.getString("structureId").contains("#")) {
                    continue;
                }
                if (Global.getSettings().getMarketConditionSpec(row.getString("conditionId")) == null) {
                    continue;
                }

                String conditionId = row.getString("conditionId").replaceAll("\\s", "").trim();
                float buildTime = row.getInt("buildTime");
                float cost = row.getInt("cost");
                boolean canChangeGasGiants = row.getBoolean("canChangeGasGiants");

                String likedConditions = row.getString("likedConditions").replaceAll("\\s", "").trim();
                String likedIndustries = row.getString("likedIndustries").replaceAll("\\s", "").trim();
                String hatedConditions = row.getString("hatedConditions").replaceAll("\\s", "").trim();
                String planetSpecOverride = row.getString("planetSpecOverride").replaceAll("\\s", "").trim();

                projects.add(new Utils.ProjectData(
                        Global.getSettings().getMarketConditionSpec(conditionId),
                        cost,
                        buildTime,
                        canChangeGasGiants,
                        likedConditions,
                        likedIndustries,
                        hatedConditions,
                        planetSpecOverride
                ));
            }
            return projects;
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> castList(Object obj, Class<T> clazz) {
        List<T> result = new ArrayList<>();
        if (obj instanceof List<?>) {
            for (Object o : (List<?>) obj) {
                result.add(clazz.cast(o));
            }
            return result;
        }
        return null;
    }

    public static void setButtonEnabledOrHighlighted(ButtonAPI button, Boolean isEnabled, Boolean isHighlighted) {
        button.setButtonPressedSound(isEnabled ? "ui_button_pressed" : "ui_button_disabled_pressed");
        button.setGlowBrightness(isEnabled ? 0.56f : 1.2f);
        button.setHighlightBrightness(0.6f);
        button.setQuickMode(isEnabled);

        if (isHighlighted) {
            button.highlight();
        } else {
            button.unhighlight();
        }
    }

    public static MarketAPI getLargestMarketWithCondition(LocationAPI location, String factionId, String conditionId) {
        MarketAPI result = null;
        int max = 0;

        for (MarketAPI market : Misc.getMarketsInLocation(location, factionId)) {
            if (!market.hasCondition(conditionId)) {
                continue;
            }
            if (market.getSize() > max) {
                max = market.getSize();
                result = market;
            }
        }

        return result;
    }

    public record OrbitData(SectorEntityToken entity, float orbitAngle, float orbitRadius, float orbitDays) {
    }

    public static class ProjectData {
        public final String id;
        public final String name;
        public final float cost;
        public final float buildTime;
        public String icon;
        public boolean canChangeGasGiants;
        public String likedConditions;
        public String likedIndustries;
        public String hatedConditions;
        public String planetSpecOverride;

        public ProjectData(String id, String name, String icon, float cost, float buildTime) {
            this.id = id;
            this.name = name;
            this.icon = icon;
            this.cost = cost;
            this.buildTime = buildTime;
        }

        public ProjectData(CustomEntitySpecAPI spec, float cost, float buildTime) {
            this.id = spec.getId();
            this.name = spec.getDefaultName();
            this.icon = "graphics/illustrations/bombardment_saturation.jpg";
            if (spec.getInteractionImage() != null) {
                this.icon = spec.getInteractionImage();
            }
            this.cost = cost;
            this.buildTime = buildTime;
        }

        public ProjectData(MarketConditionSpecAPI spec, float cost, float buildTime, boolean canChangeGasGiants, String likedConditions, String likedIndustries, String hatedConditions, String planetSpecOverride) {
            this.id = spec.getId();
            this.name = spec.getName();
            this.icon = spec.getIcon();
            this.cost = cost;
            this.buildTime = buildTime;
            this.canChangeGasGiants = canChangeGasGiants;
            this.likedConditions = likedConditions;
            this.likedIndustries = likedIndustries;
            this.hatedConditions = hatedConditions;
            this.planetSpecOverride = planetSpecOverride;
        }

        public ProjectData(String id, String name, String icon, float cost, float buildTime, boolean canChangeGasGiants, String likedConditions, String likedIndustries, String hatedConditions, String planetSpecOverride) {
            this.id = id;
            this.name = name;
            this.icon = icon;
            this.cost = cost;
            this.buildTime = buildTime;
            this.canChangeGasGiants = canChangeGasGiants;
            this.likedConditions = likedConditions;
            this.likedIndustries = likedIndustries;
            this.hatedConditions = hatedConditions;
            this.planetSpecOverride = planetSpecOverride;
        }
    }
}
