package terraformingmadeeasy.industries;

import com.fs.starfarer.api.Global;
import org.json.JSONException;
import org.json.JSONObject;
import terraformingmadeeasy.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class AgriculturalLaboratory extends TMEBaseIndustry {
    public AgriculturalLaboratory() throws JSONException {
        for (int i = 0; i < Utils.TERRAFORMING_OPTIONS_DATA.length(); i++) {
            JSONObject row = Utils.TERRAFORMING_OPTIONS_DATA.getJSONObject(i);
            if (!Objects.equals(row.getString("structureID"), getId())) continue;

            String conditionId = row.getString("conditionID");
            float buildTime = row.getInt("buildTime");
            float cost = row.getInt("cost");
            boolean canChangeGasGiants = row.getBoolean("canChangeGasGiants");
            List<String> likedConditions = new ArrayList<>();
            List<String> hatedConditions = new ArrayList<>();
            List<String> likedIndustries = new ArrayList<>();
            List<String> hatedIndustries = new ArrayList<>();
            if (!row.getString("likedConditions").isEmpty())
                likedConditions.addAll(Arrays.asList(row.getString("likedConditions").replace(" ", "").split(",")));
            if (!row.getString("hatedConditions").isEmpty())
                hatedConditions.addAll(Arrays.asList(row.getString("hatedConditions").replace(" ", "").split(",")));
            if (!row.getString("likedIndustries").isEmpty())
                likedIndustries.addAll(Arrays.asList(row.getString("likedIndustries").replace(" ", "").split(",")));
            if (!row.getString("hatedIndustries").isEmpty())
                hatedIndustries.addAll(Arrays.asList(row.getString("hatedIndustries").replace(" ", "").split(",")));

            this.modifiableConditions.add(new Utils.ModifiableCondition(
                    Global.getSettings().getMarketConditionSpec(conditionId),
                    buildTime,
                    cost,
                    canChangeGasGiants,
                    likedConditions,
                    hatedConditions,
                    likedIndustries,
                    hatedIndustries
            ));
        }
    }
}
