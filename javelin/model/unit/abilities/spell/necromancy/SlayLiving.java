package javelin.model.unit.abilities.spell.necromancy;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Touch;

/**
 * See the d20 SRD for more info.
 */
public class SlayLiving extends Touch {
	public SlayLiving() {
		super("Slay living", 5, ChallengeCalculator.ratespelllikeability(5),
				Realm.EVIL);
		castinbattle = true;
		provokeaoo = false;
	}

	@Override
	public String cast(final Combatant caster, final Combatant target,
			final boolean saved, final BattleState s, ChanceNode cn) {
		if (saved) {
			target.damage(Math.round(3 * 3.5f + 9), s,
					target.source.energyresistance);
			return target + " resists, is now " + target.getstatus() + ".";
		}
		target.damage(target.hp + 10, s, 0);
		return target + " is killed!";
	}

	@Override
	public int save(final Combatant caster, final Combatant target) {
		return calculatesavedc(target.source.fortitude(), caster);
	}

}
