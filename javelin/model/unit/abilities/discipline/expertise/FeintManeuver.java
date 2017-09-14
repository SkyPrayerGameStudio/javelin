package javelin.model.unit.abilities.discipline.expertise;

import javelin.controller.action.Action;
import javelin.controller.action.maneuver.Feint;

public class FeintManeuver extends CombatExpertiseManeuver {
	public FeintManeuver() {
		super("Feint", 2);
	}

	@Override
	public Action getaction() {
		return Feint.INSTANCE;
	}
}