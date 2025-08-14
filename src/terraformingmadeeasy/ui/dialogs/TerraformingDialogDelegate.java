package terraformingmadeeasy.ui.dialogs;

import com.fs.starfarer.api.Global;
import org.lwjgl.util.vector.Vector2f;
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.industries.BaseDevelopmentIndustry;
import terraformingmadeeasy.industries.PlanetaryHologram;
import terraformingmadeeasy.ui.plugins.ProjectListPlugin;

public class TerraformingDialogDelegate extends DevelopmentDialogDelegate {
    public TerraformingDialogDelegate(float width, float height, boolean hasCustomInputs, BaseDevelopmentIndustry industry) {
        super(width, height, hasCustomInputs, industry);
    }

    @Override
    public void customDialogConfirm() {
        if (!(this.data instanceof ProjectListPlugin)) {
            return;
        }

        Utils.ProjectData project = ((ProjectListPlugin) this.data).selected;

        if (project == null) {
            return;
        }

        this.industry.setProject(project);
        this.industry.startUpgrading();
        Global.getSector().getPlayerFleet().getCargo().getCredits().subtract(project.cost * Utils.BUILD_COST_MULTIPLIER);
        Global.getSoundPlayer().playSound("ui_upgrade_industry", 1f, 1f, Global.getSoundPlayer().getListenerPos(), new Vector2f());
    }

    @Override
    public String getConfirmText() {
        if (this.industry instanceof PlanetaryHologram) {
            return "Mask";
        } else {
            return "Terraform";
        }
    }
}
