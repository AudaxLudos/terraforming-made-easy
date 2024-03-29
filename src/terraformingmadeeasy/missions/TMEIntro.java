package terraformingmadeeasy.missions;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.RelationshipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import terraformingmadeeasy.ids.TMEPeople;

import java.awt.*;

public class TMEIntro extends HubMissionWithBarEvent {
    public enum Stage {
        MEET_PERSON,
        SURVEY_PLANET,
        RETURN,
        COMPLETED,
    }

    public PlanetAPI planet;
    public StarSystemAPI system;
    public PersonAPI inadire;

    @Override
    protected boolean create(MarketAPI createdAt, boolean barEvent) {
        // find or set quest giver
        if (barEvent) {
            setPersonOverride(getImportantPerson(TMEPeople.INADIRE));
            inadire = getPerson();
        }
        if (inadire == null) return false;
        if (!setPersonMissionRef(inadire, "$tmeIntro_ref")) return false;
        if (!setGlobalReference("$tmeIntro_ref")) return false;

        // Find and pick a planet to use for quest
        requireSystemTags(ReqMode.ANY, new String[] { Tags.THEME_REMNANT, Tags.THEME_REMNANT_MAIN, Tags.THEME_REMNANT_RESURGENT });
        requireSystemTags(ReqMode.NOT_ANY, new String[] { Tags.THEME_REMNANT_NO_FLEETS, Tags.THEME_REMNANT_SECONDARY, Tags.THEME_REMNANT_SUPPRESSED });
        preferSystemUnexplored();
        requirePlanetNotStar();
        requirePlanetNotGasGiant();
        requirePlanetWithRuins();
        preferPlanetNotFullySurveyed();

        planet = pickPlanet();
        if (planet == null)
            return false;
        system = planet.getStarSystem();

        // set up starting and end stages
        setStoryMission();
        setStartingStage(Stage.MEET_PERSON);
        addSuccessStages(Stage.COMPLETED);

        // Make these locations important
        makeImportant(inadire, "$tmeIntro_meetPerson", Stage.MEET_PERSON);
        makeImportant(planet, "$tmeIntro_surveyPlanet", Stage.SURVEY_PLANET);
        makeImportant(inadire, "$tmeIntro_return", Stage.RETURN);

        // Flags that can be used to enter the next stage
        connectWithGlobalFlag(Stage.MEET_PERSON, Stage.SURVEY_PLANET, "$tmeIntro_surveyPlanet");
        connectWithGlobalFlag(Stage.SURVEY_PLANET, Stage.RETURN, "$tmeIntro_return");
        setStageOnGlobalFlag(Stage.COMPLETED, "$tmeIntro_completed");

        setCreditReward(CreditReward.AVERAGE);

        return true;
    }

    @Override
    protected void updateInteractionDataImpl() {
        set("$tmeIntro_distance", getDistanceLY(system));
        set("$tmeIntro_systemName", system.getNameWithLowercaseTypeShort());
        set("$tmeIntro_planetName", planet.getFullName());
        set("$tmeIntro_reward", Misc.getWithDGS(getCreditsReward()));
    }

    @Override
    public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
        float oPad = 10f;

        if (currentStage == Stage.MEET_PERSON) {
            info.addPara("Go to the " + system.getNameWithLowercaseTypeShort() + " and investigate the planet " + planet.getName() + ".", oPad);
        } else if (currentStage == Stage.SURVEY_PLANET) {
            info.addPara("Return to " + inadire.getName().getFullName() + " and tell " + inadire.getHimOrHer() + " about what you found", oPad);
            addStandardMarketDesc(inadire.getNameString() + " is located " + inadire.getMarket().getOnOrAt(), inadire.getMarket(), info, oPad);
        } else if (currentStage == Stage.RETURN) {
            info.addPara("Return to " + inadire.getName().getFullName() + " and tell " + inadire.getHimOrHer() + " about what you found", oPad);
        }
    }

    @Override
    public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
        if (currentStage == Stage.MEET_PERSON) {
            info.addPara("Meet the contractor at " + inadire.getMarket().getStarSystem().getNameWithLowercaseTypeShort(), tc, pad);
            return true;
        } else if (currentStage == Stage.SURVEY_PLANET) {
            info.addPara("Survey the planet marked at " + system.getNameWithLowercaseTypeShort(), tc, pad);
            return true;
        } else if (currentStage == Stage.RETURN) {
            info.addPara("Return back to the contractor at " + inadire.getMarket().getStarSystem().getNameWithLowercaseTypeShort(), tc, pad);
            return true;
        }

        return false;
    }

    @Override
    public String getBaseName() {
        return "Inadire's Requests";
    }
}
