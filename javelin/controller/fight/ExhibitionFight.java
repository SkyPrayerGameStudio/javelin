package javelin.controller.fight;

import java.util.ArrayList;

import javelin.controller.fight.tournament.Exhibition;
import javelin.controller.map.Arena;
import javelin.model.unit.Combatant;

/**
 * Tournament event.
 * 
 * @see Exhibition
 * 
 * @author alex
 */
public class ExhibitionFight extends Fight {
	/** Constructor. */
	public ExhibitionFight() {
		map = new Arena();
		meld = true;
		friendly = true;
		hide = false;
		bribe = false;
		canflee = false;
	}

	@Override
	public ArrayList<Combatant> getmonsters(int teamel) {
		return null;
	}

	@Override
	public int getel(int teamel) {
		return teamel;
	}
}