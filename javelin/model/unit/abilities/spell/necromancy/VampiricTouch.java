package javelin.model.unit.abilities.spell.necromancy;

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.model.Realm;
import javelin.model.state.BattleState;
import javelin.model.unit.abilities.spell.Touch;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.Condition;

/**
 * See the d20 SRD for more info.
 */
public class VampiricTouch extends Touch {
	public class Vampiric extends Condition {
		int steal;

		public Vampiric(float expireat, Combatant caster, int steal,
				Integer casterlevelp) {
			super(expireat, caster, Effect.POSITIVE, "vampiric", casterlevelp,
					1);
			this.steal = steal;
		}

		@Override
		public void start(Combatant c) {
			// see VampiricTouch

		}

		@Override
		public void end(Combatant c) {
			c.damage(steal);
		}

		@Override
		public void finish(BattleState s) {
			// Game.message(caster + " loses temporary hit points, is now "
			// + caster.getStatus() + ".", null, Delay.BLOCK);
		}

		@Override
		public void merge(Combatant c, Condition condition) {
			steal += ((Vampiric) condition).steal;
		}
	}

	/** Constructor. */
	public VampiricTouch() {
		super("Vampiric touch", 3,
				ChallengeRatingCalculator.ratespelllikeability(3), Realm.EVIL);
		castinbattle = true;
		provokeaoo = false;
	}

	@Override
	public String cast(final Combatant caster, final Combatant target,
			final BattleState s, final boolean saved) {
		int steal = 21;
		final int max = target.hp + 10;
		if (steal > max) {
			steal = max;
		}
		target.damage(steal, s, target.source.energyresistance);
		final int originalhp = caster.hp;
		caster.heal(steal, true);
		caster.addcondition(new Vampiric(Float.MAX_VALUE, caster,
				caster.hp - originalhp, casterlevel));
		return describe(target) + "\n" + describe(caster);
	}

	public String describe(final Combatant c) {
		return c + " is " + c.getstatus();
	}

}