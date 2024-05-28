package terraformingmadeeasy.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.listeners.ColonyInteractionListener;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.impl.PlayerFleetPersonnelTracker;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.util.Misc;
import terraformingmadeeasy.conditions.DeathWorld;
import terraformingmadeeasy.ids.TMEIds;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class TMEDeathWorldScript implements EconomyTickListener, ColonyInteractionListener {
    @Override
    public void reportPlayerOpenedMarket(MarketAPI market) {
    }

    @Override
    public void reportPlayerClosedMarket(MarketAPI market) {
    }

    @Override
    public void reportPlayerOpenedMarketAndCargoUpdated(MarketAPI market) {
    }

    @Override
    public void reportPlayerMarketTransaction(PlayerMarketTransaction transaction) {
        if (transaction.getMarket() != null ||
                transaction.getMarket().getPrimaryEntity() != null ||
                transaction.getSubmarket() != null ||
                transaction.getSubmarket().getSpecId().equals(Submarkets.LOCAL_RESOURCES) ||
                systemHasDeathWorld(transaction.getMarket().getStarSystem())) {
            processTransaction(transaction, transaction.getMarket().getPrimaryEntity());
        }
    }

    public Boolean systemHasDeathWorld(StarSystemAPI system) {
        for (MarketAPI m : Misc.getMarketsInLocation(system, Factions.PLAYER)) {
            if (m.hasCondition(TMEIds.DEATH_WORLD)) {
                return true;
            }
        }
        return false;
    }

    public void processTransaction(PlayerMarketTransaction transaction, SectorEntityToken entity) {
        SubmarketAPI sub = transaction.getSubmarket();
        PlayerFleetPersonnelTracker tracker = PlayerFleetPersonnelTracker.getInstance();
        PlayerFleetPersonnelTracker.PersonnelData marineData = tracker.getMarineData();
        PlayerFleetPersonnelTracker.PersonnelAtEntity at = tracker.getDroppedOffAt(Commodities.MARINES, entity, sub, true);

        for (CargoStackAPI stack : transaction.getSold().getStacksCopy()) {
            if (stack.isPersonnelStack() && stack.isMarineStack()) {
                int num = (int) stack.getSize();
                PlayerFleetPersonnelTracker.transferPersonnel(marineData, at.data, num, marineData);
            }
        }

        for (CargoStackAPI stack : transaction.getBought().getStacksCopy()) {
            if (stack.isPersonnelStack() && stack.isMarineStack()) {
                int num = (int) stack.getSize();
                PlayerFleetPersonnelTracker.transferPersonnel(at.data, marineData, num, marineData);
            }
        }

        doCleanup(true);
        tracker.update(false, true, null);
        at.data.numMayHaveChanged(transaction.getSubmarket().getCargo().getMarines(), true);
    }

    public void doCleanup(Boolean withDroppedOff) {
        PlayerFleetPersonnelTracker tracker = PlayerFleetPersonnelTracker.getInstance();
        PlayerFleetPersonnelTracker.PersonnelData marineData = tracker.getMarineData();
        marineData.savedNum = marineData.num;
        marineData.savedXP = marineData.xp;

        if (withDroppedOff) {
            Iterator<PlayerFleetPersonnelTracker.PersonnelAtEntity> itr = tracker.getDroppedOff().iterator();
            while (itr.hasNext()) {
                PlayerFleetPersonnelTracker.PersonnelAtEntity pae = itr.next();
                if (!pae.entity.isAlive() || pae.data.num <= 0 || pae.data.xp <= 0) {
                    itr.remove();
                }
            }
        }
    }

    @Override
    public void reportEconomyTick(int itrIndex) {
    }

    @Override
    public void reportEconomyMonthEnd() {
        for (MarketAPI m : Misc.getPlayerMarkets(true)) {
            if (!m.hasCondition(TMEIds.DEATH_WORLD)) {
                continue;
            }

            SubmarketAPI subMarket = Misc.getLocalResources(m).getSubmarket();
            int marineCount = subMarket.getCargo().getMarines();
            PlayerFleetPersonnelTracker.PersonnelAtEntity tracker =
                    PlayerFleetPersonnelTracker.getInstance().getDroppedOffAt(
                            Commodities.MARINES,
                            m.getPrimaryEntity(),
                            subMarket,
                            true);
            tracker.data.num = marineCount;
            tracker.data.addXP(marineCount * DeathWorld.MARINES_TO_TRAIN_MULT);

            if (m.getCondition(TMEIds.DEATH_WORLD).getPlugin() instanceof DeathWorld) {
                DeathWorld conditionPlugin = (DeathWorld) m.getCondition(TMEIds.DEATH_WORLD).getPlugin();

                ++conditionPlugin.monthsActive;
                if (conditionPlugin.monthsActive >= DeathWorld.SUPPRESS_CONDITION_PER_MONTH_MOD) {
                    List<MarketConditionAPI> conditions = new ArrayList<>();
                    for (MarketConditionAPI c : m.getConditions()) {
                        if (c.getGenSpec() != null && c.getGenSpec().getHazard() > 0) {
                            conditions.add(c);
                        }
                    }
                    conditions.removeAll(conditionPlugin.suppressedConditions);
                    if (!conditions.isEmpty()) {
                        MarketConditionAPI condition = conditions.get(new Random().nextInt(conditions.size()));
                        conditionPlugin.suppressedConditions.add(condition);
                        sendConditionSuppressedMessage(m, condition);
                    }
                    conditionPlugin.monthsActive = 0;
                }
            }
        }
    }

    public void sendConditionSuppressedMessage(MarketAPI market, MarketConditionAPI condition) {
        MessageIntel intel = new MessageIntel("Citizens of " + market.getName() + " has adapted to a condition", Misc.getBasePlayerColor());
        intel.addLine(BaseIntelPlugin.BULLET + "The " + condition.getName() + " planetary condition has been suppressed");
        intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
        intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
        Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
    }
}
