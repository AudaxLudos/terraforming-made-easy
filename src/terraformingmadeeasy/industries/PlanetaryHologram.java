package terraformingmadeeasy.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetSpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.procgen.PlanetGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.loading.specs.PlanetSpec;
import terraformingmadeeasy.Utils;

import java.util.Objects;

public class PlanetaryHologram extends TMEBaseIndustry {
    String originalPlanetType = null;
    String originalPlanetName = null;
    boolean originalIsGasGiant = false;
    String fakePlanetType = null;

    public PlanetaryHologram() {
        for (PlanetGenDataSpec pDataSpec : Global.getSettings().getAllSpecs(PlanetGenDataSpec.class)) {
            for (PlanetSpecAPI pSpec : Global.getSettings().getAllPlanetSpecs()) {
                if (Objects.equals(pDataSpec.getId(), pSpec.getPlanetType())) {
                    this.modifiableConditions.add(new Utils.ModifiableCondition(
                            pDataSpec.getId(),
                            pSpec.getName(),
                            pSpec.getTexture(),
                            100000f,
                            30f,
                            true,
                            null,
                            null,
                            null,
                            null,
                            pDataSpec.getId()
                    ));
                    break;
                }
            }
        }
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);

        if (this.fakePlanetType != null && !Objects.equals(this.market.getPlanetEntity().getTypeId(), this.fakePlanetType)) {
            changePlanetVisuals(this.fakePlanetType);
        }
    }

    @Override
    public void notifyBeingRemoved(MarketAPI.MarketInteractionMode mode, boolean forUpgrade) {
        super.notifyBeingRemoved(mode, forUpgrade);
        if (this.originalPlanetType != null) {
            changePlanetVisuals(this.originalPlanetType);
        }
    }

    @Override
    protected void addPostUpkeepSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode) {
        float oPad = 10f;
        float pad = 3f;

        if (mode == IndustryTooltipMode.NORMAL) {
            tooltip.addSpacer(10f);
            tooltip.addSectionHeading("Projected Hologram", Alignment.MID, 0f);
            tooltip.addPara("Original Planet Type: %s", oPad, Misc.getHighlightColor(), this.market.getPlanetEntity().getTypeId());
            tooltip.addPara("Fake Planet Type: %s", pad, Misc.getHighlightColor(), this.fakePlanetType != null ? this.fakePlanetType : "-");
        }
    }

    @Override
    public void changePlanetVisuals(String planetTypeId) {
        this.originalPlanetType = this.market.getPlanetEntity().getSpec().getPlanetType();
        this.originalPlanetName = this.market.getPlanetEntity().getSpec().getName();
        this.originalIsGasGiant = this.market.getPlanetEntity().getSpec().isGasGiant();
        this.fakePlanetType = planetTypeId;
        if (this.modifiableCondition != null && this.modifiableCondition.planetSpecOverride != null) {
            for (PlanetSpecAPI spec : Global.getSettings().getAllPlanetSpecs()) {
                if (spec.isStar()) {
                    continue;
                }
                if (Objects.equals(spec.getPlanetType(), this.modifiableCondition.planetSpecOverride)) {
                    this.fakePlanetType = spec.getPlanetType();
                    break;
                }
            }
        }
        // Change the planet type but make sure certain info are still its based info
        this.market.getPlanetEntity().changeType(this.fakePlanetType, StarSystemGenerator.random);
        ((PlanetSpec) this.market.getPlanetEntity().getSpec()).planetType = this.originalPlanetType;
        ((PlanetSpec) this.market.getPlanetEntity().getSpec()).name = this.originalPlanetName;
        ((PlanetSpec) this.market.getPlanetEntity().getSpec()).setGasGiant(this.originalIsGasGiant);
        this.market.getPlanetEntity().applySpecChanges();
    }
}
