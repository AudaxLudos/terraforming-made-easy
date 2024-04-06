package terraformingmadeeasy;

import com.fs.starfarer.api.campaign.CustomEntitySpecAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.characters.MarketConditionSpecAPI;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static class BuildableMegastructure {
        public String id;
        public String name;
        public String icon;
        public float cost;
        public float buildTime;

        public BuildableMegastructure(CustomEntitySpecAPI spec, float cost, float buildTime) {
            this.id = spec.getId();
            this.name = spec.getDefaultName();
            this.icon = spec.getInteractionImage();
            this.cost = cost;
            this.buildTime = buildTime;
        }

        public BuildableMegastructure(CustomEntitySpecAPI spec, String name, float cost, float buildTime) {
            this.id = spec.getId();
            this.name = name;
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
        public List<String> likedConditions = new ArrayList<>();
        public List<String> hatedConditions = new ArrayList<>();
        public List<String> likedIndustries = new ArrayList<>();
        public List<String> hatedIndustries = new ArrayList<>();

        public ModifiableCondition(MarketConditionSpecAPI spec, float cost, float buildTime, boolean canChangeGasGiants, List<String> likedConditions, List<String> hatedConditions) {
            this.id = spec.getId();
            this.name = spec.getName();
            this.icon = spec.getIcon();
            this.cost = cost;
            this.buildTime = buildTime;
            this.canChangeGasGiants = canChangeGasGiants;
            if (likedConditions != null) this.likedConditions = likedConditions;
            if (hatedConditions != null) this.hatedConditions = hatedConditions;
        }

        public ModifiableCondition(MarketConditionSpecAPI spec, float cost, float buildTime, boolean canChangeGasGiants, List<String> likedConditions, List<String> hatedConditions, List<String> likedIndustries, List<String> hatedIndustries) {
            this.id = spec.getId();
            this.name = spec.getName();
            this.icon = spec.getIcon();
            this.cost = cost;
            this.buildTime = buildTime;
            this.canChangeGasGiants = canChangeGasGiants;
            if (likedConditions != null) this.likedConditions = likedConditions;
            if (hatedConditions != null) this.hatedConditions = hatedConditions;
            if (likedIndustries != null) this.likedIndustries = likedIndustries;
            if (hatedIndustries != null) this.hatedIndustries = hatedIndustries;
        }
    }
}
