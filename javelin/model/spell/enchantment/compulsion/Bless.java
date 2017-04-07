package javelin.model.spell.enchantment.compulsion;

import java.util.List;

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.upgrade.Spell;
import javelin.model.Realm;
import javelin.model.condition.Condition;
import javelin.model.condition.Heroic;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * @author alex
 */
public class Bless extends Spell {
	public class Blessed extends Condition {
		int bonus = +1;

		public Blessed(Combatant c) {
			super(Float.MAX_VALUE, c, Effect.POSITIVE, "blssed", 1);
		}

		@Override
		public void start(Combatant c) {
			c.source = c.source.clone();
			Heroic.raiseboth(c.source, bonus);
		}

		@Override
		public void end(Combatant c) {
			c.source = c.source.clone();
			Heroic.raiseboth(c.source, -bonus);
		}
	}

	public Bless() {
		super("Bless", 1, ChallengeRatingCalculator.ratespelllikeability(1),
				Realm.GOOD);
		castonallies = true;
		castinbattle = true;
		isscroll = true;
	}

	@Override
	public String cast(Combatant caster, Combatant target, BattleState s,
			boolean saved) {
		for (Combatant c : s.getteam(caster)) {
			c.addcondition(new Blessed(c));
		}
		return "All allies are blessed!";
	}

	@Override
	public void filtertargets(Combatant combatant, List<Combatant> targets,
			BattleState s) {
		targetself(combatant, targets);
	}
}
