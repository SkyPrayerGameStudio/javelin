package javelin.model.spell.necromancy.wounds;

import javelin.controller.challenge.ChallengeRatingCalculator;

/**
 * See the d20 SRD for more info.
 */
public class InflictLightWounds extends InflictModerateWounds {

	public InflictLightWounds(String name, float incrementcost) {
		super(name, incrementcost, new int[] { 1, 8, 1 }, 1);
	}

	public InflictLightWounds() {
		this("Inflict light wounds", ChallengeRatingCalculator.ratespelllikeability(1));
	}

}
