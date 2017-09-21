package javelin.model.world;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.CrCalculator;
import javelin.controller.comparator.CombatantByCrComparator;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.Fight;
import javelin.controller.fight.IncursionFight;
import javelin.controller.fight.RandomEncounter;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.unit.Squad;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.Location;
import javelin.model.world.location.Portal;
import javelin.model.world.location.town.Town;
import javelin.view.Images;
import javelin.view.screen.WorldScreen;
import tyrant.mikera.engine.RPG;

/**
 * An attacking {@link Squad}, trying to destroy a {@link Town} or other
 * {@link Actor}. Each one that appears grows stronger, which should eventually
 * end the game.
 *
 * An {@link Incursion} is similar to an invading {@link Squad} while an
 * Invasion refers to the ongoing process of trying to destroy the player's
 * region.
 *
 * @author alex
 */
public class Incursion extends Actor {
	static final int PREFERREDVICTORYCHANCE = 5 + 2;
	/** Only taken into account if running {@link Javelin#DEBUG}. */
	static final boolean SPAWN = true;
	/** Move even if {@link Javelin#DEBUGDISABLECOMBAT} is enabled. */
	static final boolean FORCEMOVEMENT = false;
	static final VictoryChance VICTORYCHANCES = new VictoryChance();

	static class VictoryChance {
		HashMap<Integer, Integer> chances = new HashMap<Integer, Integer>();

		VictoryChance() {
			chances.put(-4, 9);
			chances.put(-3, 8);
			chances.put(-2, 7);
			chances.put(-1, 6);
			chances.put(0, 5);
			chances.put(1, 4);
			chances.put(2, 3);
			chances.put(3, 2);
			chances.put(4, 1);
		}

		/**
		 * @param defender
		 *            Encounter level.
		 * @param attacker
		 *            Encounter level.
		 * @return A chance from 1 to 9 that the attacker will win (meant to be
		 *         used with a d10 roll).
		 */
		public Integer get(int attackerel, int defenderel) {
			int gap = defenderel - attackerel;
			if (gap < -4) {
				gap = -4;
			} else if (gap > 4) {
				gap = 4;
			}
			return chances.get(gap);
		}
	}

	/** Should probably move this to {@link Portal}? */
	public static Integer currentel = 1;

	/** @see #getel() */
	public List<Combatant> squad = new ArrayList<Combatant>();

	Actor target = null;

	/**
	 * @param x
	 *            {@link World} coordinate.
	 * @param y
	 *            {@link World} coordinate.
	 * @param squadp
	 *            See {@link #currentel}.
	 * @param r
	 *            See {@link Actor#realm}.
	 */
	public Incursion(final int x, final int y, List<Combatant> squadp,
			Realm r) {
		this.x = x;
		this.y = y;
		if (squadp == null) {
			ArrayList<Terrain> terrains = new ArrayList<Terrain>(1);
			terrains.add(Terrain.get(x, y));
			squad.addAll(Fight.generate(Incursion.currentel, terrains));
			currentel += 1;
		} else {
			squad = squadp;
		}
		realm = r;
	}

	/**
	 * Incursions only move once a day but this is balanced by several implicit
	 * benefits like canonical {@link #squad} HP not decreasing after a battle
	 * that has been won, jumping over certain {@link Location}s, not having to
	 * deal with {@link RandomEncounter}...
	 *
	 * @param s
	 *            TODO use {@link WorldScreen#current}
	 */
	public void move() {
		choosetarget();
		if (target == null) {
			displace();
			return;
		}
		final int targetx = target.x;
		final int targety = target.y;
		int newx = x + determinemove(x, targetx);
		int newy = y + determinemove(y, targety);
		if (Terrain.get(newx, newy).equals(Terrain.WATER)) {
			displace();
			return;
		}
		Actor arrived = World.get(newx, newy, World.getactors());
		x = newx;
		y = newy;
		place();
		if (arrived != null) {
			attack(arrived);
		}
	}

	void attack(Actor target) {
		Boolean status = target.destroy(this);
		if (status == null) {
			return;
		}
		if (status == true) {
			target.remove();
		} else if (status == false) {
			remove();
		}
	}

	/**
	 * Updates {@link #target} when first called or if it's been destroyed. Will
	 * not consider {@link Actor#impermeable} targets, those from teh same
	 * {@link Actor#realm} or targets that would require the Incursion to cross
	 * water to get there. If this results in no potential target, will assgign
	 * <code>null</code> to {@link #target}.
	 * 
	 * Once tha filtering is done, will select the closest target that allows
	 * for the {@link #PREFERREDVICTORYCHANCE} of winning. If none is available,
	 * will use the nearest valid target.
	 */
	void choosetarget() {
		final ArrayList<Actor> actors = World.getactors();
		if (target != null && target == World.get(target.x, target.y, actors)) {
			return;
		}
		List<Actor> targets = new ArrayList<Actor>();
		for (final Actor a : actors) {
			if (!a.impermeable && a.realm != realm
					&& !crosseswater(this, a.x, a.y)) {
				targets.add(a);
			}
		}
		if (targets.isEmpty()) {
			target = null;
			return;
		}
		sortbydistance(targets);
		for (Actor target : targets) {
			int incursionel = getel();
			if (VICTORYCHANCES.get(incursionel,
					target.getel(incursionel)) >= PREFERREDVICTORYCHANCE) {
				this.target = target;
				return;
			}
		}
		target = targets.get(0);
	}

	/**
	 * @param tox
	 * @param toy
	 * @return Checks if there is any body of water between these two actors.
	 */
	public static boolean crosseswater(Actor from, int tox, int toy) {
		if (Terrain.get(tox, toy).equals(Terrain.WATER)) {
			return true;
		}
		int x = from.x;
		int y = from.y;
		while (x != tox || y != toy) {
			x += determinemove(x, tox);
			y += determinemove(y, toy);
			if (Terrain.get(x, y).equals(Terrain.WATER)) {
				return true;
			}
		}
		return false;
	}

	static int determinemove(final int me, final int target) {
		if (target == me) {
			return 0;
		}
		return target > me ? +1 : -1;
	}

	@Override
	public void turn(long time, WorldScreen world) {
		move();
	}

	/**
	 * A rate of 1 incursion every 18 days means that it will take a year for a
	 * level 20 incursion to appear.
	 */
	public static boolean spawn() {
		if (Javelin.DEBUG && !SPAWN) {
			return false;
		}
		if (!RPG.chancein(18)) {
			return false;
		}
		ArrayList<Actor> portals = World.getall(Portal.class);
		Collections.shuffle(portals);
		for (Actor p : portals) {
			if (((Portal) p).invasion) {
				place(p.realm, p.x, p.y, null);
				return true;
			}
		}
		return false;
	}

	// static int spawned = 0;

	/**
	 * Creates and places a new incursion. Finds an empty spot close to the
	 * given coordinates.
	 *
	 * @param r
	 *            See {@link Actor#realm}.
	 * @param x
	 *            Starting {@link World} coordinate.
	 * @param y
	 *            Starting {@link World} coordinate.
	 * @param squadp
	 *            See {@link Incursion#squad}.
	 * @see Actor#place()
	 */
	public static void place(Realm r, int x, int y, List<Combatant> squadp) {
		if (Javelin.DEBUG && !SPAWN) {
			return;
		}
		int size = World.scenario.size;
		ArrayList<Actor> actors = World.getactors();
		while (World.get(x, y, actors) != null) {
			int delta = RPG.pick(new int[] { -1, 0, +1 });
			if (RPG.r(1, 2) == 1) {
				x += delta;
			} else {
				y += delta;
			}
			if (x < 0) {
				x = 0;
			} else if (y < 0) {
				y = 0;
			} else if (x >= size) {
				x = size - 1;
			} else if (y >= size) {
				y = size - 1;
			}
		}
		new Incursion(x, y, squadp, r).place();
	}

	/**
	 * @param from
	 *            Clones the {@link Combatant}s here into...
	 * @return a new list.
	 * @see Combatant#clone()
	 * @see Combatant#clonesource()
	 */
	static public ArrayList<Combatant> getsafeincursion(List<Combatant> from) {
		int size = from.size();
		ArrayList<Combatant> to = new ArrayList<Combatant>(size);
		for (int i = 0; i < size; i++) {
			to.add(from.get(i).clone().clonesource());
		}
		return to;
	}

	@Override
	public Boolean destroy(Incursion attacker) {
		/* don't inline the return statement, null bug */
		if (attacker.realm == realm) {
			return ignoreincursion(attacker);
		} else {
			return fight(attacker.getel(), getel());
		}
	}

	/**
	 * Helper method for {@link #destroy(Incursion)}
	 *
	 * @return <code>null</code>.
	 */
	static public Boolean ignoreincursion(Actor attacker) {
		attacker.displace();
		return null;
	}

	@Override
	public Integer getel(int attackerel) {
		return getel();
	}

	/**
	 * @return Encounter level for {@link #squad}.
	 * @see CrCalculator#calculateel(List)
	 */
	public int getel() {
		return CrCalculator.calculateel(squad);
	}

	/**
	 * Helper method for {@link #destroy(Incursion)}. Uses a percentage to
	 * decide which combatant wins (adapted from CCR).
	 *
	 * @param attacker
	 *            Encounter level.
	 * @param defender
	 *            Encounter level. {@link Integer#MIN_VALUE} means an automatic
	 *            victory for the attacker.
	 * @return <code>true</code> if attacker wins.
	 */
	public static boolean fight(int attacker, int defender) {
		return defender == Integer.MIN_VALUE
				|| RPG.r(1, 10) <= VICTORYCHANCES.get(attacker, defender);
	}

	@Override
	public String toString() {
		return "An incursion";
	}

	@Override
	public boolean interact() {
		if (!Location.headsup(squad, toString(), true)) {
			return false;
		}
		throw new StartBattle(new IncursionFight(this));
	}

	@Override
	public List<Combatant> getcombatants() {
		return squad;
	}

	@Override
	public Image getimage() {
		if (squad == null) {
			return null;
		}
		Combatant leader = null;
		for (Combatant c : squad) {
			if (leader == null
					|| c.source.challengerating > leader.source.challengerating) {
				leader = c;
			}
		}
		return Images.getImage(leader.source.avatarfile);
	}

	@Override
	public String describe() {
		return "Enemy incursion (" + CrCalculator.describedifficulty(squad)
				+ " fight):\n\n" + Squad.active.spot(squad);
	}

	/**
	 * @return Like {@link #fight(int, int)} but also damage the survivor,
	 *         killing off creatures according to the gravity of battle.
	 */
	public boolean fight(List<Combatant> defenders) {
		int me = getel();
		int them = CrCalculator.calculateel(defenders);
		boolean win = fight(me, them);
		if (win) {
			damage(squad, VICTORYCHANCES.get(me, them));
		} else {
			damage(defenders, VICTORYCHANCES.get(them, me));
		}
		return win;
	}

	/** TODO this hasn't been properly tested yet */
	void damage(List<Combatant> survivors, int chance) {
		int totalcr = 0;
		for (Combatant c : survivors) {
			totalcr += c.source.challengerating;
		}
		float damage = totalcr * (1 - chance / 10f);
		LinkedList<Combatant> wounded = new LinkedList<Combatant>(survivors);
		Collections.sort(wounded, CombatantByCrComparator.SINGLETON);
		while (damage > 0 && survivors.size() > 1) {
			Combatant dead = wounded.pop();
			survivors.remove(dead);
			damage -= dead.source.challengerating;
		}
	}

	public static ArrayList<Incursion> getincursions() {
		ArrayList<Incursion> all = new ArrayList<Incursion>();
		for (Actor a : World.getall(Incursion.class)) {
			if (a instanceof Incursion) {
				all.add((Incursion) a);
			}
		}
		return all;
	}
}
