package javelin.controller.action.maneuver;

import javelin.controller.ai.ChanceNode;
import javelin.controller.old.Game.Delay;
import javelin.model.condition.DefensiveStance;
import javelin.model.feat.CombatExpertise;
import javelin.model.state.BattleState;
import javelin.model.unit.Attack;
import javelin.model.unit.Combatant;

public class DefensiveAttack extends Maneuver {

	public DefensiveAttack() {
		super("Defensive attack", "D", 'D', CombatExpertise.singleton, 0);
	}

	@Override
	boolean validatetarget(Combatant target) {
		return true;
	}

	@Override
	int getsavebonus(Combatant targetCombatant) {
		return 0;
	}

	@Override
	int getattackerbonus(Combatant combatant) {
		return 0;
	}

	@Override
	public float calculatesavechance(int dc, int bonus) {
		return 0;
	}

	@Override
	float calculatemisschance(Combatant current, Combatant targetCombatant,
			BattleState battleState, int touchattackbonus) {
		final float misschance =
				super.calculatemisschance(current, targetCombatant, battleState,
						touchattackbonus) - amount(current, 4) / 20f;
		return misschance >= .05f ? misschance : .05f;
	}

	@Override
	ChanceNode miss(Combatant combatant, Combatant target,
			BattleState battleState, float chance) {
		battleState = battleState.clone();
		combatant = battleState.clone(combatant);
		combatant.addcondition(new DefensiveStance(combatant.ap + .1f,
				combatant, amount(combatant, 2)));
		return new ChanceNode(battleState, chance, "Defensive attack misses...",
				Delay.WAIT);
	}

	@Override
	ChanceNode hit(Combatant combatant, Combatant target,
			BattleState battleState, float chance) {
		battleState = battleState.clone();
		combatant = battleState.clone(combatant);
		target = battleState.clone(target);
		combatant.addcondition(new DefensiveStance(combatant.ap + .1f,
				combatant, amount(combatant, 2)));
		Attack attack = combatant.source.melee.get(0).get(0);
		target.damage(attack.getaveragedamage(), battleState, attack.energy
				? target.source.energyresistance : target.source.dr);
		return new ChanceNode(battleState, chance, "Defensive attack hits!\n"
				+ target + " is " + target.getStatus() + ".", Delay.WAIT);
	}

	static public int amount(final Combatant combatant, int fallback) {
		final int acbonus = Math.min(combatant.source.getbaseattackbonus(), 5);
		return acbonus == 0 ? fallback : acbonus;
	}
}
