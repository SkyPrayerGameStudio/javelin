package javelin.controller.action.world;

import java.awt.event.KeyEvent;
import java.util.HashMap;

import javelin.Javelin;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.fight.RandomEncounter;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.basic.Lodge;
import javelin.view.screen.WorldScreen;

/**
 * Rest in the {@link WorldScreen}. High chance of finding a monster instead.
 * 
 * @author alex
 */
public class Camp extends WorldAction {
	static final boolean DEBUG = Javelin.DEBUG && true;

	static final String PROMPT = "Are you sure you want to try to set up camp in this wild area?\n"
			+ "Monsters may interrupt you.\n\n"
			+ "Press c to set camp, w to camp for a week or any other key to cancel...";
	static final String PROMPTDEBUG = "DEBUG CAMP\n"
			+ "(c)amp (d)ay (w)eek (m)onth (s)eason (y)ear?";
	static final String INSIDETOWN = "Cannot camp inside a town's district!\n"
			+ "Try moving further into the wilderness.\n";

	static final HashMap<Character, int[]> PERIODS = new HashMap<Character, int[]>();

	static {
		final int day = 24;
		PERIODS.put('c', new int[] { 8, 2 });
		PERIODS.put('w', new int[] { 7 * day, 12 });
		if (DEBUG) {
			PERIODS.put('d', new int[] { 1 * day, 12 });
			PERIODS.put('m', new int[] { 30 * day, 12 });
			PERIODS.put('s', new int[] { 100 * day, 12 });
			PERIODS.put('y', new int[] { 400 * day, 12 });
		}
	}

	/** Constructor. */
	public Camp() {
		super("Camp", new int[] { KeyEvent.VK_C }, new String[] { "c" });
	}

	@Override
	public void perform(WorldScreen screen) {
		if (quickheal() || Dungeon.active != null) {
			throw new RepeatTurn();
		}
		Town t = (Town) Squad.active.findnearest(Town.class);
		if (t != null && t.getdistrict().getarea()
				.contains(Squad.active.getlocation())) {
			Javelin.message(INSIDETOWN, false);
			return;
		}
		int[] period = PERIODS
				.get(Javelin.prompt(DEBUG ? PROMPTDEBUG : PROMPT));
		if (period == null) {
			return;
		}
		final int hours = period[0];
		final int rest = period[1];
		for (int i = 0; i < hours; i++) {
			Squad.active.hourselapsed += 1;
			RandomEncounter.encounter(1 / WorldScreen.HOURSPERENCOUNTER);
			if (i > 0 && (i + 1) % rest == 0) {
				Lodge.rest(1, rest, false, Lodge.LODGE);
			}
		}
	}

	boolean quickheal() {
		if (!Squad.active.canheal()) {
			return false;
		}
		if (Javelin.prompt("Do you want to cast spells to heal your party?\n"
				+ "Press c to cast or any other key to continue...") != 'c') {
			return false;
		}
		Squad.active.quickheal();
		return true;
	}
}
