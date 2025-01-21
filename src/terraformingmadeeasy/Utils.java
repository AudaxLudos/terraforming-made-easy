package terraformingmadeeasy;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomEntitySpecAPI;
import com.fs.starfarer.api.campaign.PlanetSpecAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.characters.MarketConditionSpecAPI;
import com.fs.starfarer.api.impl.campaign.procgen.PlanetGenDataSpec;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import lunalib.lunaSettings.LunaSettings;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import terraformingmadeeasy.ids.TMEIds;
import terraformingmadeeasy.industries.ConstructionGrid;
import terraformingmadeeasy.industries.TMEBaseIndustry;
import terraformingmadeeasy.ui.dialogs.TMEBaseDialogDelegate;
import terraformingmadeeasy.ui.plugins.SelectButtonPlugin;
import terraformingmadeeasy.ui.tooltips.MegastructureTooltip;
import terraformingmadeeasy.ui.tooltips.TerraformTooltip;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static final String TERRAFORMING_OPTIONS_FILE = "data/campaign/terraforming_options.csv";
    public static final String MEGASTRUCTURE_OPTIONS_FILE = "data/campaign/megastructure_options.csv";
    public static List<Utils.ModifiableCondition> AGRICULTURAL_LABORATORY_OPTIONS = new ArrayList<>();
    public static List<Utils.ModifiableCondition> ATMOSPHERE_REGULATOR_OPTIONS = new ArrayList<>();
    public static List<Utils.BuildableMegastructure> CONSTRUCTION_GRID_OPTIONS = new ArrayList<>();
    public static List<Utils.ModifiableCondition> ELEMENT_SYNTHESIZER_OPTIONS = new ArrayList<>();
    public static List<Utils.ModifiableCondition> GEOMORPHOLOGY_STATION_OPTIONS = new ArrayList<>();
    public static List<Utils.ModifiableCondition> MINERAL_REPLICATOR_OPTIONS = new ArrayList<>();
    public static List<Utils.ModifiableCondition> PLANETARY_HOLOGRAM_OPTIONS = new ArrayList<>();
    public static List<Utils.ModifiableCondition> STELLAR_MANUFACTORY_OPTIONS = new ArrayList<>();
    public static List<Utils.ModifiableCondition> TERRESTRIAL_ENGINE_OPTIONS = new ArrayList<>();
    public static List<Utils.ModifiableCondition> UNIFICATION_CENTER_OPTIONS = new ArrayList<>();
    public static float BUILD_TIME_MULTIPLIER = 1.0f;
    public static float BUILD_COST_MULTIPLIER = 1.0f;

    public static void loadLunaSettings() {
        BUILD_TIME_MULTIPLIER = getBuildCostSettingValue(getSettingsString("tme_build_time_settings"), "tme_custom_build_time_settings");
        BUILD_COST_MULTIPLIER = getBuildCostSettingValue(getSettingsString("tme_build_cost_settings"), "tme_custom_build_cost_settings");
    }

    public static float getBuildCostSettingValue(String setting, String customFieldId) {
        float value = 1f;
        switch (setting) {
            case "Fast":
                value = 0.5f;
                break;
            case "Normal":
                value = 1.0f;
                break;
            case "Slow":
                value = 2.0f;
                break;
            case "Custom":
                value = getSettingsFloat(customFieldId);
                break;
        }
        return value;
    }

    public static float getSettingsFloat(String fieldId) {
        Float val = LunaSettings.getFloat(TMEIds.MOD_ID, fieldId);
        if (val == null)
            return 1f;
        return val;
    }

    public static String getSettingsString(String fieldId) {
        String val = LunaSettings.getString(TMEIds.MOD_ID, fieldId);
        if (val == null || val.isEmpty()) {
            return "Normal";
        }
        return val;
    }

    public static boolean isLunaLibEnabled() {
        return Global.getSettings().getModManager().isModEnabled("lunalib");
    }

    public static boolean isAOTDVOKEnabled() {
        return Global.getSettings().getModManager().isModEnabled("aotd_vok");
    }

    public static boolean isAllTrue(boolean[] array) {
        for (boolean b : array) {
            if (!b) {
                return false;
            }
        }
        return true;
    }

    public static String[] getUniqueIds(String text) {
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
        Set<String> setIds = new HashSet<>(Arrays.asList(ids));
        return setIds.toArray(new String[0]);
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

    public static List<Utils.ModifiableCondition> getPlanetaryHologramOptions() {
        List<Utils.ModifiableCondition> options = new ArrayList<>();
        for (PlanetGenDataSpec pDataSpec : Global.getSettings().getAllSpecs(PlanetGenDataSpec.class)) {
            for (PlanetSpecAPI pSpec : Global.getSettings().getAllPlanetSpecs()) {
                if (Objects.equals(pDataSpec.getId(), pSpec.getPlanetType())) {
                    options.add(new Utils.ModifiableCondition(
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

    public static List<Utils.BuildableMegastructure> getMegastructureOptions() {
        try {
            List<Utils.BuildableMegastructure> buildableMegastructures = new ArrayList<>();
            JSONArray data = Global.getSettings().loadCSV(Utils.MEGASTRUCTURE_OPTIONS_FILE);
            for (int i = 0; i < data.length(); i++) {
                JSONObject row = data.getJSONObject(i);
                if (row.getString("structureId").isEmpty()) {
                    continue;
                }
                if (row.getString("structureId").contains("#")) {
                    continue;
                }

                String structureId = row.getString("structureId").replaceAll("\\s","").trim();
                float cost = row.getInt("cost");
                float buildTime = row.getInt("buildTime");

                buildableMegastructures.add(new Utils.BuildableMegastructure(
                        Global.getSettings().getCustomEntitySpec(structureId),
                        cost,
                        buildTime));
            }
            return buildableMegastructures;
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Utils.ModifiableCondition> getTerraformingOptions(String industryId) {
        try {
            List<Utils.ModifiableCondition> modifiableConditions = new ArrayList<>();
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

                String conditionId = row.getString("conditionId").replaceAll("\\s","").trim();
                float buildTime = row.getInt("buildTime");
                float cost = row.getInt("cost");
                boolean canChangeGasGiants = row.getBoolean("canChangeGasGiants");

                String likedConditions = row.getString("likedConditions").replaceAll("\\s","").trim();
                String likedIndustries = row.getString("likedIndustries").replaceAll("\\s","").trim();
                String hatedConditions = row.getString("hatedConditions").replaceAll("\\s","").trim();
                String planetSpecOverride = row.getString("planetSpecOverride").replaceAll("\\s","").trim();

                modifiableConditions.add(new Utils.ModifiableCondition(
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
            return modifiableConditions;
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
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

    public static CustomPanelAPI addCustomButton(CustomPanelAPI panel, Object data, Object industry, List<ButtonAPI> buttons, float width, TMEBaseDialogDelegate delegate) {
        float columnOneWidth = width / 3f + 100f;
        float columnWidth = (width - columnOneWidth) / 2f;
        float cost = 0;
        int buildTime = 0;
        boolean canAfford = true;
        boolean canBuild = true;
        boolean canBeRemoved = false;
        boolean canAffordAndBuild = true;
        String icon = "";
        String name = "";
        TooltipMakerAPI.TooltipCreator tooltip = null;

        if (data instanceof Utils.ModifiableCondition) {
            Utils.ModifiableCondition cond = (ModifiableCondition) data;
            TMEBaseIndustry ind = (TMEBaseIndustry) industry;
            cost = cond.cost;
            buildTime = Math.round(cond.buildTime);
            canAfford = Global.getSector().getPlayerFleet().getCargo().getCredits().get() >= cost;
            canBeRemoved = ind.getMarket().hasCondition(cond.id);
            canBuild = ind.canTerraformCondition(cond) || canBeRemoved;
            if (ind.getMarket().getPlanetEntity().isGasGiant()) {
                canBuild = canBuild && cond.canChangeGasGiants;
            }
            canAffordAndBuild = canBuild && canAfford;
            icon = cond.icon;
            name = cond.name;
            tooltip = new TerraformTooltip(cond, ind);
        } else if (data instanceof Utils.BuildableMegastructure) {
            Utils.BuildableMegastructure struct = (BuildableMegastructure) data;
            ConstructionGrid ind = (ConstructionGrid) industry;
            cost = struct.cost;
            buildTime = Math.round(struct.buildTime);
            canAfford = Global.getSector().getPlayerFleet().getCargo().getCredits().get() >= cost;
            canBuild = ind.canBuildMegastructure(struct.id);
            canAffordAndBuild = canBuild && canAfford;
            icon = struct.icon;
            name = struct.name;
            tooltip = new MegastructureTooltip(struct);
        }

        CustomPanelAPI optionPanel = panel.createCustomPanel(width, 44f, new SelectButtonPlugin(delegate));

        TooltipMakerAPI optionButtonElement = optionPanel.createUIElement(width, 44f, false);
        ButtonAPI optionButton = optionButtonElement.addButton("", data, new Color(0, 195, 255, 190), new Color(0, 0, 0, 255), Alignment.MID, CutStyle.NONE, width, 44f, 0f);
        Utils.setButtonEnabledOrHighlighted(optionButton, canAffordAndBuild, !canAffordAndBuild);
        optionButtonElement.addTooltipTo(tooltip, optionButton, TooltipMakerAPI.TooltipLocation.RIGHT);
        optionButtonElement.getPosition().setXAlignOffset(-10f);
        optionPanel.addUIElement(optionButtonElement);

        TooltipMakerAPI optionNameElement = optionPanel.createUIElement(columnOneWidth, 40f, false);
        TooltipMakerAPI optionImage = optionNameElement.beginImageWithText(icon, 40f);
        String addOrRemoveText = canBeRemoved ? "Remove " : "Add ";
        optionImage.addPara(addOrRemoveText + name, canAffordAndBuild ? Misc.getBasePlayerColor() : Misc.getNegativeHighlightColor(), 0f);
        optionNameElement.addImageWithText(0f);
        optionNameElement.getPosition().setXAlignOffset(-8f).setYAlignOffset(2f);
        optionPanel.addUIElement(optionNameElement);

        TooltipMakerAPI optionBuildTimeElement = optionPanel.createUIElement(columnWidth, 40f, false);
        optionBuildTimeElement.addPara(buildTime + "", Misc.getHighlightColor(), 12f).setAlignment(Alignment.MID);
        optionBuildTimeElement.getPosition().rightOfMid(optionNameElement, 0f);
        optionPanel.addUIElement(optionBuildTimeElement);

        TooltipMakerAPI optionCostElement = optionPanel.createUIElement(columnWidth, 40f, false);
        optionCostElement.addPara(Misc.getDGSCredits(cost), canAfford ? Misc.getHighlightColor() : Misc.getNegativeHighlightColor(), 12f).setAlignment(Alignment.MID);
        optionCostElement.getPosition().rightOfMid(optionBuildTimeElement, 0f);
        optionPanel.addUIElement(optionCostElement);

        if (canAffordAndBuild) {
            buttons.add(optionButton);
        }

        return optionPanel;
    }

    public static class BuildableMegastructure {
        public String id;
        public String name;
        public String icon;
        public float cost;
        public float buildTime;

        public BuildableMegastructure(CustomEntitySpecAPI spec, float cost, float buildTime) {
            this.id = spec.getId();
            this.name = spec.getDefaultName();
            this.icon = "graphics/illustrations/bombardment_saturation.jpg";
            if (spec.getInteractionImage() != null) {
                this.icon = spec.getInteractionImage();
            }
            this.cost = cost * BUILD_COST_MULTIPLIER;
            this.buildTime = buildTime * BUILD_TIME_MULTIPLIER;
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

    public static class ModifiableCondition {
        public String id;
        public String name;
        public String icon;
        public float cost;
        public float buildTime;
        public boolean canChangeGasGiants;
        public String likedConditions;
        public String likedIndustries;
        public String hatedConditions;
        public String planetSpecOverride;

        public ModifiableCondition(MarketConditionSpecAPI spec, float cost, float buildTime, boolean canChangeGasGiants, String likedConditions, String likedIndustries, String hatedConditions, String planetSpecOverride) {
            this.id = spec.getId();
            this.name = spec.getName();
            this.icon = spec.getIcon();
            this.cost = cost * BUILD_COST_MULTIPLIER;
            this.buildTime = buildTime * BUILD_TIME_MULTIPLIER;
            this.canChangeGasGiants = canChangeGasGiants;
            this.likedConditions = likedConditions;
            this.likedIndustries = likedIndustries;
            this.hatedConditions = hatedConditions;
            this.planetSpecOverride = planetSpecOverride;
        }

        public ModifiableCondition(String id, String name, String icon, float cost, float buildTime, boolean canChangeGasGiants, String likedConditions, String likedIndustries, String hatedConditions, String planetSpecOverride) {
            this.id = id;
            this.name = name;
            this.icon = icon;
            this.cost = cost * BUILD_COST_MULTIPLIER;
            this.buildTime = buildTime * BUILD_TIME_MULTIPLIER;
            this.canChangeGasGiants = canChangeGasGiants;
            this.likedConditions = likedConditions;
            this.likedIndustries = likedIndustries;
            this.hatedConditions = hatedConditions;
            this.planetSpecOverride = planetSpecOverride;
        }
    }
}
