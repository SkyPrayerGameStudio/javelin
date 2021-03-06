package javelin.controller.upgrade.ability;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * @see RaiseAbility
 */
public class RaiseDexterity extends RaiseAbility {
	public RaiseDexterity() {
		super("dexterity");
	}

	@Override
	int getabilityvalue(Monster m) {
		return m.dexterity;
	}

	@Override
	boolean setattribute(Combatant m, int l) {
		m.source.raisedexterity(+1);
		return true;
	}

}
