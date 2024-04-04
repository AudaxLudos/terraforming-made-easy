package terraformingmadeeasy.ids;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PersonImportance;
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
            person.getName().setFirst("Inadire");
            person.getName().setLast("Asirav");
            person.setGender(FullName.Gender.FEMALE);
            person.setFaction(Factions.INDEPENDENT);
            person.setRankId(Ranks.CITIZEN);
            person.setPostId(Ranks.POST_SCIENTIST);
            person.setPortraitSprite("graphics/portraits/portrait_corporate04.png");
            person.setPersonality(Personalities.STEADY);
            person.setImportance(PersonImportance.VERY_HIGH);
            person.addTag(Tags.CONTACT_SCIENCE);
            person.setVoice(Voices.SCIENTIST);
            ip.addPerson(person);
        }
    }
}
