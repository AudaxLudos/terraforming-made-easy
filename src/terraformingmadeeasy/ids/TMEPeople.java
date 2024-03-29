package terraformingmadeeasy.ids;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;

public class TMEPeople {
    public static String INADIRE = "INADIRE";

    public static void register() {
        addTMEPeople();
    }

    public static void addTMEPeople() {
        ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
        if (ip.getPerson(INADIRE) == null) {
            PersonAPI person = Global.getFactory().createPerson();
            person.setId(INADIRE);
            person.setFaction(Factions.INDEPENDENT);
            person.setGender(FullName.Gender.FEMALE);
            person.setRankId(Ranks.CITIZEN);
            person.setPostId(Ranks.POST_SCIENTIST);
            person.setImportance(PersonImportance.VERY_HIGH);
            person.getName().setFirst("Asirav");
            person.getName().setLast("Inadire");
            person.addTag(Tags.CONTACT_SCIENCE);
            person.setVoice(Voices.SCIENTIST);
            person.setPortraitSprite("graphics/portraits/portrait_corporate04.png");

            MarketAPI market = Global.getSector().getImportantPeople().getData(People.BAIRD).getLocation().getMarket();
            if(market != null) {
                market.getCommDirectory().addPerson(person);
                market.addPerson(person);
            }

            Global.getSector().getImportantPeople().addPerson(person);
        }
    }
}
