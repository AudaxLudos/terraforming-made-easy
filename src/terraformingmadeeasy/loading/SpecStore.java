package terraformingmadeeasy.loading;

import com.fs.starfarer.api.Global;
import org.json.JSONArray;
import org.json.JSONObject;
import terraformingmadeeasy.loading.specs.TerraformingProjectSpec;

public class SpecStore {
    public static void loadTerraformingProjectSpecs() {
        try {
            JSONArray jsonArray = Global.getSettings().getMergedSpreadsheetDataForMod("id", "data/campaign/terraformingmadeeasy/terraforming_options.csv", "terraformingmadeeasy");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject rowData = jsonArray.getJSONObject(i);
                TerraformingProjectSpec spec = new TerraformingProjectSpec();
                if (rowData.getString("id").isEmpty() || rowData.getString("id").contains("#")) {
                    continue;
                }
                if (rowData.getString("structureId").isEmpty() || rowData.getString("structureId").contains("#")) {
                    continue;
                }
                if (rowData.getString("conditionId").isEmpty() || rowData.getString("conditionId").contains("#")) {
                    continue;
                }
                spec.setId(rowData.getString("id"));
                spec.setStructureId(rowData.getString("structureId"));
                spec.setConditionId(rowData.getString("conditionId"));
                spec.setPlanetSpecOverride(rowData.optString("planetSpecOverride"));

                spec.setUnlocked(rowData.optBoolean("unlocked"));
                spec.setChangeGasGiants(rowData.optBoolean("changeGasGiants"));

                spec.setCost((float) rowData.optDouble("cost", 1337f)); // Elite
                spec.setBuildTime((float) rowData.optDouble("buildTime", 42f)); // Life

                spec.setLikedConditions(rowData.optString("likedConditions"));
                spec.setHatedConditions(rowData.optString("hatedConditions"));
                spec.setLikedIndustries(rowData.optString("likedIndustries"));
                spec.setHatedIndustries(rowData.optString("hatedIndustries"));

                Global.getSettings().putSpec(TerraformingProjectSpec.class, spec.getId(), spec);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
