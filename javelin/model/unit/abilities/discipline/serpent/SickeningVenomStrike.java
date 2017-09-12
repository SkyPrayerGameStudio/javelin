package javelin.model.unit.abilities.discipline.serpent;

import javelin.controller.action.ai.attack.DamageChance;
import javelin.model.state.BattleState;
import javelin.model.unit.abilities.discipline.Strike;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.condition.Sickened;
import javelin.model.unit.condition.abilitydamage.ConstitutionDamage;
import tyrant.mikera.engine.RPG;

public class SickeningVenomStrike extends Strike {
	static final int SICKENDURATION = RPG.average(1, 4);

	public SickeningVenomStrike() {
		super("Sickening Venom Strike", 3);
	}

	@Override
	public void preattacks(Combatant current, Combatant target, Attack a,
			BattleState s) {
		// nothing
	}

	@Override
	public void postattacks(Combatant current, Combatant target, Attack a,
			BattleState s) {
		// nothing
	}

	@Override
	public void prehit(Combatant current, Combatant target, Attack a,
			DamageChance dc, BattleState s) {
		target.addcondition(new ConstitutionDamage(2, current));
		if (!save(target.source.fortitude(), 13, current)) {
			final float expireat = current.ap + SICKENDURATION;
			target.addcondition(new Sickened(expireat, target));
		}
	}

	@Override
	public void posthit(Combatant current, Combatant target, Attack a,
			DamageChance dc, BattleState s) {
		// nothing
	}
}
