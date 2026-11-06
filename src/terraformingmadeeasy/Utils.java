package terraformingmadeeasy;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.opengl.GL11;
import terraformingmadeeasy.loading.specs.TerraformingProjectSpec;

import java.awt.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
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

    public static boolean canAfford(float cost) {
        return Global.getSettings().isInCampaignState() && Global.getSector().getPlayerFleet().getCargo().getCredits().get() >= cost;
    }

    public static boolean canPerformProject(MarketAPI market, TerraformingProjectSpec project) {
        if (market == null) {
            return false;
        }
        return canTerraformCondition(market, project);
    }

    public static Boolean canTerraformCondition(MarketAPI market, TerraformingProjectSpec project) {
        return hasRequiredConditions(market, project) && hasRequiredIndustries(market, project);
    }

    public static boolean hasRequiredConditions(MarketAPI market, TerraformingProjectSpec project) {
        String text = project.getLikedConditions();

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
                values.put(id, market.hasCondition(id));
            }
            expressionsResult[i] = evaluateExpression(expression, values);
        }

        return isAllTrue(expressionsResult);
    }

    public static boolean hasRequiredIndustries(MarketAPI market, TerraformingProjectSpec condition) {
        String text = condition.getLikedIndustries();

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
                values.put(id, market.hasIndustry(id));
            }
            expressionsResult[i] = evaluateExpression(expression, values);
        }

        return isAllTrue(expressionsResult);
    }

    public static void renderQuadBorder(float x, float y, float width, float height, Color color, float alphaMult, float thickness) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ZERO);

        GL11.glColor4f(
                color.getRed() / 255f,
                color.getGreen() / 255f,
                color.getBlue() / 255f,
                (color.getAlpha() / 255f) * alphaMult
        );
        GL11.glLineWidth(thickness);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        {
            float offset = thickness / 2f;
            GL11.glVertex2f(x + offset, y + offset);
            GL11.glVertex2f(x + offset, y + height - offset);
            GL11.glVertex2f(x + width - offset, y + height - offset);
            GL11.glVertex2f(x + width - offset, y + offset);
        }
        GL11.glEnd();
    }
}
