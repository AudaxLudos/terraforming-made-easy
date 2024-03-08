package terraformingmadeeasy;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomEntitySpecAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.characters.MarketConditionSpecAPI;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static String capitalizeString(String givenString) {
        String text = givenString.replace("_", " ");
        text = Character.toUpperCase(text.charAt(0)) + text.substring(1);
        return text;
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

    public static class ModifiableCondition {
        public String id;
        public String name;
        public String icon;
        public float cost;
        public float buildTime;
        public boolean canChangeGasGiants;
        public List<String> likesConditions = new ArrayList<>();
        public List<String> hatesConditions = new ArrayList<>();

        public ModifiableCondition(String conditionSpecId, float cost, float buildTime, boolean canChangeGasGiants, List<String> likesConditions, List<String> hatesConditions) {
            MarketConditionSpecAPI spec = Global.getSettings().getMarketConditionSpec(conditionSpecId);

            this.id = spec.getId();
            this.name = spec.getName();
            this.icon = spec.getIcon();
            this.cost = cost;
            this.buildTime = buildTime;
            this.canChangeGasGiants = canChangeGasGiants;
            if (likesConditions != null) this.likesConditions = likesConditions;
            if (hatesConditions != null) this.hatesConditions = hatesConditions;
        }
    }
}
