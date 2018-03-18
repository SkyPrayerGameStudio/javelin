package javelin.model.world.location.haunt;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.CrCalculator;
import javelin.controller.comparator.MonstersByName;
import javelin.controller.fight.LocationFight;
import javelin.controller.map.location.LocationMap;
import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.fortification.Fortification;
import javelin.model.world.location.town.labor.basic.Dwelling;
import javelin.view.screen.HauntScreen;
import javelin.view.screen.WorldScreen;
import tyrant.mikera.engine.RPG;

public abstract class Haunt extends Fortification {
	private static final int MAXIMUMAVAILABLE = 5;
	ArrayList<Monster> dwellers = new ArrayList<Monster>();
	public ArrayList<Monster> available = new ArrayList<Monster>();
	int delay = -1;
	/**
	 * Will be added in order to artifically modify the target encounter level.
	 */
	int elmodifier = 0;

	/**
	 * @param description
	 *            Location name.
	 * @param tier
	 *            A number from 1 to 4 (inclusive), determining the level range
	 *            for this haunt (1-5, 6-10, 11-15, 16-20).
	 */
	public Haunt(String description, String[] monsters) {
		super(description, description, Integer.MAX_VALUE, Integer.MIN_VALUE);
		realm = null;
		sacrificeable = true;
		for (String name : monsters) {
			Monster m = Javelin.getmonster(name);
			dwellers.add(m);
			int cr = Math.max(1, Math.round(m.challengerating));
			if (cr < minlevel) {
				minlevel = cr;
			}
			if (cr > maxlevel) {
				maxlevel = cr;
			}
		}
	}

	@Override
	public List<Combatant> getcombatants() {
		return garrison;
	}

	@Override
	protected LocationFight fight() {
		return new LocationFight(this, getmap());
	}

	abstract LocationMap getmap();

	@Override
	protected void generategarrison(int minlevel, int maxlevel) {
		int minel = CrCalculator.leveltoel(minlevel) + elmodifier;
		int maxel = CrCalculator.leveltoel(maxlevel) + elmodifier;
		int el = Integer.MIN_VALUE;
		List<List<Combatant>> possibilities = new ArrayList<List<Combatant>>();
		while (el < maxel) {
			garrison.add(new Combatant(RPG.pick(dwellers).clone(), true));
			el = CrCalculator.calculateel(garrison);
			if (minel <= el && el <= maxel) {
				possibilities.add(new ArrayList<Combatant>(garrison));
			}
		}
		if (possibilities.isEmpty()) {
			generategarrison(minlevel, maxlevel);
		} else {
			garrison = RPG.pick(possibilities);
		}
	}

	@Override
	public void turn(long time, WorldScreen world) {
		super.turn(time, world);
		if (ishostile()) {
			return;
		}
		delay -= 1;
		if (delay > 0) {
			return;
		}
		if (Javelin.DEBUG) {
			System.out.println("Generate @" + descriptionknown);
		}
		if (available.size() + 1 > MAXIMUMAVAILABLE) {
			available.remove(RPG.pick(available));
		}
		Monster m = RPG.pick(dwellers);
		available.add(m);
		available.sort(MonstersByName.INSTANCE);
		delay = Dwelling.getspawnrate(m.challengerating);
	}

	@Override
	public boolean interact() {
		if (!super.interact()) {
			return false;
		}
		new HauntScreen(this).show();
		return true;
	}

	@Override
	public void capture() {
		super.capture();
		available.clear();
		delay = RPG.r(1, 6) + RPG.r(1, 6);
	}

	@Override
	public boolean isworking() {
		return !ishostile() && available.isEmpty();
	}
}
