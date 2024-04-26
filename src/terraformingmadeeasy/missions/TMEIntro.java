package terraformingmadeeasy.missions;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import terraformingmadeeasy.ids.TMEPeople;

import java.awt.*;
import java.util.List;
import java.util.Map;

public class TMEIntro extends HubMissionWithBarEvent {
    public MarketAPI createdAt;
    public MarketAPI market;
    public SectorEntityToken planet;
    public StarSystemAPI system;
    public PersonAPI inadire;

    @Override
    public boolean shouldShowAtMarket(MarketAPI market) {
        return market.getFaction().getId().equals(Factions.INDEPENDENT);
    }

    @Override
    protected boolean create(MarketAPI createdAt, boolean barEvent) {
        /* find or set quest giver */
        if (barEvent) {
            setPersonOverride(getImportantPerson(TMEPeople.INADIRE));
            this.inadire = getPerson();
        }

        if (this.inadire == null) return false;
        if (!setPersonMissionRef(this.inadire, "$tmeIntro_ref")) return false;
        if (!setGlobalReference("$tmeIntro_ref")) return false; // Stops the mission from being repeatable

        /* Find and pick a planet to use for quest */
        requireMarketFaction(Factions.INDEPENDENT);
        requireMarketSizeAtLeast(3);
        requireMarketHasSpaceport();
        requireMarketStabilityAtLeast(6);

        this.market = pickMarket();

        if (market == null || market == createdAt)
            return false;

        this.planet = this.market.getPrimaryEntity();
        this.system = this.market.getStarSystem();

        /* set up starting and end stages */
        setStoryMission();
        setStartingStage(Stage.ESCORT_CONTACT);
        addSuccessStages(Stage.COMPLETED);

        /* Make these locations important */
        makeImportant(this.market, "$tmeIntro_escortContact", Stage.ESCORT_CONTACT);

        /* Flags that can be used to enter the next stage */
        setStageOnGlobalFlag(Stage.COMPLETED, "$tmeIntro_completed");

        setCreditReward(CreditReward.AVERAGE);

        /* Create a fleet near entity after completing survey planet */
        beginStageTrigger(Stage.ESCORT_CONTACT);
        triggerCreateFleet(FleetSize.MEDIUM, FleetQuality.DEFAULT, Factions.HEGEMONY, FleetTypes.PATROL_MEDIUM, createdAt.getStarSystem());
        triggerSetFleetOfficers(OfficerNum.DEFAULT, OfficerQuality.DEFAULT);
        triggerFleetAllowLongPursuit();
        triggerSetFleetAlwaysPursue();
        triggerPickLocationAroundPlayer(1000f);
        triggerSpawnFleetAtPickedLocation();
        triggerOrderFleetInterceptPlayer();
        triggerOrderFleetEBurn(1f);
        triggerSetFleetFlag("$tmeIntro_hegeFleet", Stage.ESCORT_CONTACT);
        endTrigger();

        this.createdAt = createdAt;

        setPersonIsPotentialContactOnSuccess(inadire);

        return true;
    }

    @Override
    protected void updateInteractionDataImpl() {
        set("$tmeIntro_distance", getDistanceLY(this.system));
        set("$tmeIntro_systemName", this.system.getNameWithLowercaseTypeShort());
        set("$tmeIntro_planetName", this.planet.getFullName());
        set("$tmeIntro_reward", Misc.getWithDGS(getCreditsReward()));
    }

    @Override
    protected boolean callAction(String action, String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (action.equals("rejectContact")) {
            if (this.inadire.getMarket() != null) {
                this.inadire.getMarket().getCommDirectory().removePerson(this.inadire);
                this.inadire.getMarket().removePerson(this.inadire);
            }
            this.createdAt.getCommDirectory().addPerson(this.inadire);
            this.market.addPerson(this.inadire);
            return true;
        } else if (action.equals("moveContact")) {
            if (this.inadire.getMarket() != null) {
                this.inadire.getMarket().getCommDirectory().removePerson(this.inadire);
                this.inadire.getMarket().removePerson(this.inadire);
            }
            this.market.getCommDirectory().addPerson(this.inadire);
            this.market.addPerson(this.inadire);
            return true;
        }
        return false;
    }

    @Override
    public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
        float oPad = 10f;
        Color h = Misc.getHighlightColor();

        if (currentStage == Stage.ESCORT_CONTACT) {
            info.addPara("Escort %s to %s in the %s", oPad, h, inadire.getName().getFullName(), planet.getFullName(), system.getNameWithLowercaseTypeShort());
        }
    }

    @Override
    public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
        if (currentStage == Stage.ESCORT_CONTACT) {
            info.addPara("Escort %s to a planet within the %s", pad, tc, inadire.getName().getFullName(), system.getNameWithLowercaseTypeShort());
            return true;
        }

        return false;
    }

    @Override
    public String getBaseName() {
        return "Inadire's Requests";
    }

    public enum Stage {
        ESCORT_CONTACT,
        COMPLETED,
    }
}
