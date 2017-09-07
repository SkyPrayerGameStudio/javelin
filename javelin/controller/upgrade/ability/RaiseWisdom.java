package javelin.controller.upgrade.ability;

import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;

/**
 * @see RaiseAbility
 */
public class RaiseWisdom extends RaiseAbility {
	public static final RaiseAbility SINGLETON = new RaiseWisdom();

	private RaiseWisdom() {
		super("wisdom");
	}

	@Override
	int getabilityvalue(Monster m) {
		return m.wisdom;
	}

	@Override
	boolean setattribute(Combatant m, int l) {
		m.source.raisewisdom(+2);
		return true;
	}

	@Override
	public int getattribute(Monster source) {
		return source.wisdom;
	}
}
