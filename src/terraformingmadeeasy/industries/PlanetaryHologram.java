package terraformingmadeeasy.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.loading.specs.PlanetSpec;
import terraformingmadeeasy.Utils;
import terraformingmadeeasy.ids.TMEIds;

import java.util.Objects;

public class PlanetaryHologram extends TMEBaseIndustry {
    PlanetSpec fakePlanetSpec = null;
    PlanetSpec originalPlanetSpec = null;
    boolean originalIsGasGiant = false;

    public PlanetaryHologram() {
        setModifiableConditions(Utils.PLANETARY_HOLOGRAM_OPTIONS);
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);
        if (this.fakePlanetSpec != null && !Objects.equals(this.market.getPlanetEntity().getTypeId(), this.fakePlanetSpec.getPlanetType()) && !isUpgrading()) {
            updatePlanetVisuals(this.fakePlanetSpec.getPlanetType());
        }
    }

    @Override
    public void notifyBeingRemoved(MarketAPI.MarketInteractionMode mode, boolean forUpgrade) {
        super.notifyBeingRemoved(mode, forUpgrade);
        if (this.originalPlanetSpec != null) {
            this.setModifiableCondition(null);
            updatePlanetVisuals(this.originalPlanetSpec.getPlanetType());
        }
    }

    @Override
    protected void addPostUpkeepSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode) {
        float oPad = 10f;
        float pad = 3f;

        if (mode == IndustryTooltipMode.NORMAL && this.market.getPlanetEntity() != null) {
            tooltip.addSpacer(10f);
            tooltip.addSectionHeading("Projected Hologram", Alignment.MID, 0f);
            String[] text = this.market.getPlanetEntity().getSpec().getName().split("[()]");
            tooltip.addPara("Original Planet Type: %s World", oPad, Misc.getHighlightColor(), text.length >= 2 ? text[1] : text[0]);
            tooltip.addPara("Fake Planet Type: %s World", pad, Misc.getHighlightColor(), this.fakePlanetSpec != null ? this.fakePlanetSpec.getName() : "-");
        }
    }

    @Override
    public void sendCompletedMessage() {
        if (this.market.isPlayerOwned()) {
            MessageIntel intel = new MessageIntel("Changed the visuals of the planet %s", Misc.getBasePlayerColor(), new String[]{this.market.getName()}, Misc.getBasePlayerColor());
            intel.addLine(BaseIntelPlugin.BULLET + "The planet's visuals are now a %s-type world", Misc.getTextColor(), new String[]{this.getModifiableCondition().name.toLowerCase()}, Misc.getTextColor());
            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, this.market);
        }
    }

    @Override
    public void terraformPlanet() {
    }

    @Override
    public void updatePlanetVisuals(String planetTypeId) {
        this.originalPlanetSpec = (PlanetSpec) Global.getSettings().getSpec(PlanetSpec.class, this.market.getPlanetEntity().getSpec().getPlanetType(), false);
        this.originalIsGasGiant = this.market.getPlanetEntity().getSpec().isGasGiant();
        this.fakePlanetSpec = (PlanetSpec) Global.getSettings().getSpec(PlanetSpec.class, planetTypeId, false);
        if (this.getModifiableCondition() != null && this.getModifiableCondition().planetSpecOverride != null) {
            this.fakePlanetSpec = (PlanetSpec) Global.getSettings().getSpec(PlanetSpec.class, this.getModifiableCondition().planetSpecOverride, false);
        }
        String name = this.fakePlanetSpec.getName();
        if (Objects.equals(this.originalPlanetSpec.getPlanetType(), this.fakePlanetSpec.getPlanetType())) {
            name = this.originalPlanetSpec.getName();
        }

        // Change the planet type but make sure certain info are still its based info
        this.market.getPlanetEntity().changeType(this.fakePlanetSpec.getPlanetType(), StarSystemGenerator.random);
        ((PlanetSpec) this.market.getPlanetEntity().getSpec()).planetType = this.originalPlanetSpec.getPlanetType();
        ((PlanetSpec) this.market.getPlanetEntity().getSpec()).name = name;
        ((PlanetSpec) this.market.getPlanetEntity().getSpec()).setGasGiant(this.originalIsGasGiant);
        this.market.getPlanetEntity().applySpecChanges();

        if (Objects.equals(this.originalPlanetSpec.getPlanetType(), this.fakePlanetSpec.getPlanetType())) {
            this.fakePlanetSpec = null;
        }
    }

    @Override
    public String getAOTDVOKTechId() {
        return TMEIds.PLANETARY_HOLOGRAM_TECH;
    }
}
