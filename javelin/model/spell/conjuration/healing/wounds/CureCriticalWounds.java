package javelin.model.spell.conjuration.healing.wounds;

import javelin.controller.challenge.ChallengeRatingCalculator;

/**
 * See the d20 SRD for more info.
 */
public class CureCriticalWounds extends CureModerateWounds {

	public CureCriticalWounds(String name, float incrementcost) {
		super(name, incrementcost, new int[] { 4, 8, 8 }, 4);
	}

	public CureCriticalWounds() {
		this("Cure critical wounds", ChallengeRatingCalculator.ratespelllikeability(4));
	}

}
