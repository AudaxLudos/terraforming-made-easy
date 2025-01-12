package terraformingmadeeasy.ui.dialogs;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.industries.TMEBaseIndustry;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TerraformDialogDelegate extends TMEBaseDialogDelegate {
    public TMEBaseIndustry industry;

    public TerraformDialogDelegate(float width, float height, Industry industry) {
        WIDTH = width;
        HEIGHT = height;
        this.industry = (TMEBaseIndustry) industry;
    }

    @SuppressWarnings("RedundantArrayCreation")
    @Override
    public void createCustomDialog(CustomPanelAPI panel, CustomDialogCallback callback) {
        this.buttons.clear();

        TooltipMakerAPI mElement = panel.createUIElement(WIDTH, HEIGHT, false);
        panel.addUIElement(mElement).setXAlignOffset(-5f);

        float columnOneWidth = WIDTH / 3f + 100f;
        float columnWidth = (WIDTH - columnOneWidth) / 2f;
        float rowHeight = 440f;

        // Terraforming options selection area
        CustomPanelAPI conditionsPanel = panel.createCustomPanel(WIDTH, rowHeight, null);
        TooltipMakerAPI conditionsHeader = conditionsPanel.createUIElement(WIDTH, rowHeight, false);
        conditionsHeader.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
                0f, false, true,
                new Object[]{"Name", columnOneWidth, "Build time", columnWidth, "Cost", columnWidth - 6f});
        conditionsHeader.addTableHeaderTooltip(0, "Name of the condition to terraform on a planet");
        conditionsHeader.addTableHeaderTooltip(1, "Build time, in days. Until the terraforming project finishes.");
        conditionsHeader.addTableHeaderTooltip(2, "One-time cost to begin terraforming project, in credits");
        conditionsHeader.addTable("", 0, 0f);
        conditionsHeader.getPrev().getPosition().setXAlignOffset(0f);
        conditionsPanel.addUIElement(conditionsHeader);

        TooltipMakerAPI conditionsBody = conditionsPanel.createUIElement(WIDTH, rowHeight - 22f, true);
        List<Utils.ModifiableCondition> conditions = this.industry.getModifiableConditions();
        Collections.sort(conditions, new SortCanAffordAndBuild(this.industry));
        for (Utils.ModifiableCondition condition : conditions) {
            CustomPanelAPI conditionPanel = Utils.addCustomButton(panel, condition, this.industry, this.buttons, WIDTH, this);
            conditionsBody.addCustom(conditionPanel, 0f);
        }
        conditionsPanel.addUIElement(conditionsBody);
        mElement.addCustom(conditionsPanel, 0f);

        // Show player credits
        TooltipMakerAPI creditsElement = panel.createUIElement(WIDTH, 0f, false);
        creditsElement.setParaSmallInsignia();
        creditsElement.addPara("Credits: %s", 0f, Misc.getGrayColor(), Misc.getHighlightColor(),
                Misc.getWithDGS(Global.getSector().getPlayerFleet().getCargo().getCredits().get()));
        panel.addUIElement(creditsElement).inBL(0f, -32f);
    }

    @Override
    public String getConfirmText() {
        return "Terraform";
    }

    @Override
    public void customDialogConfirm() {
        if (this.selected == null) {
            return;
        }
        Utils.ModifiableCondition selectedCondition = (Utils.ModifiableCondition) this.selected;
        this.industry.setModifiableCondition(selectedCondition);
        this.industry.startUpgrading();
        Global.getSector().getPlayerFleet().getCargo().getCredits().subtract(selectedCondition.cost);
        Global.getSoundPlayer().playSound("ui_upgrade_industry", 1f, 1f, Global.getSoundPlayer().getListenerPos(), new Vector2f());
    }

    public static class SortCanAffordAndBuild implements Comparator<Utils.ModifiableCondition> {
        TMEBaseIndustry industry;

        public SortCanAffordAndBuild(TMEBaseIndustry industry) {
            this.industry = industry;
        }

        @Override
        public int compare(Utils.ModifiableCondition o1, Utils.ModifiableCondition o2) {
            return Boolean.compare(canAffordAndBuild(o1), canAffordAndBuild(o2));
        }

        public boolean canAffordAndBuild(Utils.ModifiableCondition condition) {
            boolean canAfford = Global.getSector().getPlayerFleet().getCargo().getCredits().get() >= condition.cost;
            boolean canBeRemoved = this.industry.getMarket().hasCondition(condition.id);
            boolean canBuild = this.industry.canTerraformCondition(condition) || canBeRemoved;
            if (this.industry.getMarket().getPlanetEntity().isGasGiant()) {
                canBuild = canBuild && condition.canChangeGasGiants;
            }
            return !(canBuild && canAfford);
        }
    }
}
