package javelin.controller.terrain.hazard;

import javelin.Javelin;
import javelin.controller.Weather;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.Season;
import tyrant.mikera.engine.RPG;

/**
 * Can happen during any storm or in spring time if it's raining.
 * 
 * TODO would be cool to have the party divide in 2 when a storm hits
 * 
 * @author alex
 */
public class Flood extends Hazard {
	static final int DC = (15 + 20) / 2;

	@Override
	public void hazard(int hoursellapsed) {
		for (Combatant c : Squad.active.members) {
			Monster m = c.source;
			if (RPG.r(1, 20) + Monster.getbonus(m.dexterity) < DC
					&& Javelin.roll(m.skills.survive(m)) < DC) {
				GettingLost.getlost("Squad is taken by a flash flood!", 0);
				return;
			}
		}
	}

	@Override
	public boolean validate() {
		if (Squad.active.fly()) {
			return false;
		}
		return Weather.current == Weather.STORM
				|| (Weather.current == Weather.RAIN
						&& Season.current == Season.SPRING);
	}
}
