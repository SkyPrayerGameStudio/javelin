package javelin.model.unit.abilities.spell.abjuration;

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.Touch;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.Resistant;

/**
 * See the d20 SRD for more info.
 * 
 * @see Monster#energyresistance
 */
public class ResistEnergy extends Touch {
	int resistance;

	/** Constructor. */
	public ResistEnergy() {
		super("Resist energy", 2, ChallengeRatingCalculator.ratespelllikeability(2, 7),
				Realm.GOOD);
		resistance = 20 / 5;
		casterlevel = 7;
		castinbattle = true;
		castonallies = true;
		castoutofbattle = true;
		ispotion = true;
	}

	@Override
	public String castpeacefully(Combatant caster, Combatant target) {
		target.addcondition(new Resistant(target, resistance, casterlevel));
		return target + " is looking reflective!";
	}

	@Override
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		return castpeacefully(caster, target);
	}
}