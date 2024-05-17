package terraformingmadeeasy.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetSpecAPI;
import com.fs.starfarer.api.impl.campaign.procgen.PlanetGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.loading.specs.PlanetSpec;
import terraformingmadeeasy.Utils;

import java.util.Objects;

public class PlanetaryHologram extends TMEBaseIndustry {
    public PlanetaryHologram() {
        for (PlanetGenDataSpec pDataSpec : Global.getSettings().getAllSpecs(PlanetGenDataSpec.class)) {
            for (PlanetSpecAPI pSpec : Global.getSettings().getAllPlanetSpecs()) {
                if (Objects.equals(pDataSpec.getId(), pSpec.getPlanetType())) {
                    this.modifiableConditions.add(new Utils.ModifiableCondition(
                            pDataSpec.getId(),
                            pSpec.getName(),
                            pSpec.getTexture(),
                            200000f,
                            60f,
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
    public void changePlanetVisuals(String planetTypeId) {
        String prevPlanetType = this.market.getPlanetEntity().getSpec().getPlanetType();
        String prevPlanetName = this.market.getPlanetEntity().getSpec().getName();
        boolean prevIsGasGiant = this.market.getPlanetEntity().getSpec().isGasGiant();
        String newPlanetType = planetTypeId;
        if (this.modifiableCondition.planetSpecOverride != null) {
            for (PlanetSpecAPI spec : Global.getSettings().getAllPlanetSpecs()) {
                if (spec.isStar()) {
                    continue;
                }
                if (Objects.equals(spec.getPlanetType(), this.modifiableCondition.planetSpecOverride)) {
                    newPlanetType = spec.getPlanetType();
                    break;
                }
            }
        }
        // Change the planet type but make sure certain info are still its based info
        this.market.getPlanetEntity().changeType(newPlanetType, StarSystemGenerator.random);
        ((PlanetSpec) this.market.getPlanetEntity().getSpec()).planetType = prevPlanetType;
        ((PlanetSpec) this.market.getPlanetEntity().getSpec()).name = prevPlanetName;
        ((PlanetSpec) this.market.getPlanetEntity().getSpec()).setGasGiant(prevIsGasGiant);
        this.market.getPlanetEntity().applySpecChanges();
    }
}
