package javelin.model.item;

import java.util.ArrayList;

import javelin.controller.challenge.RewardCalculator;
import javelin.controller.old.Game;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.world.WorldActor;
import javelin.model.world.location.dungeon.Chest;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.temple.Temple;
import javelin.view.screen.InfoScreen;
import tyrant.mikera.engine.RPG;

/**
 * Keys are found in {@link Dungeon} {@link Chest}s. They unlock {@link Temple}
 * s.
 * 
 * @see Chest#key
 * @see Temple#open
 * @author alex
 */
public class Key extends Item {
	/** Color/realm of this key. */
	public Realm color;

	/**
	 * @deprecated
	 * @see #generate()
	 */
	@Deprecated
	public Key(Realm color) {
		super(color.toString() + " key", determineprice(color), null);
		this.color = color;
		usedinbattle = false;
	}

	private static int determineprice(Realm r) {
		for (WorldActor a : WorldActor.getall()) {
			if (a instanceof Temple) {
				Temple temple = (Temple) a;
				if (temple.realm.equals(r)) {
					return 4 * RewardCalculator.getgold(temple.level);
				}
			}
		}
		return 0;
	}

	@Override
	public boolean use(Combatant user) {
		return false;
	}

	@Override
	public boolean usepeacefully(Combatant m) {
		new InfoScreen("").print("\"I wonder what this unlocks?\"");
		Game.getInput();
		return true;
	}

	/**
	 * @param realm
	 *            if <code>null</code> generates a key of random color.
	 */
	public static Key generate() {
		ArrayList<Realm> realms = new ArrayList<Realm>();
		for (Realm r : Realm.values()) {
			realms.add(r);
		}
		for (WorldActor a : WorldActor.getall()) {
			Temple temple = a instanceof Temple ? (Temple) a : null;
			if (temple != null && temple.open) {
				realms.remove(temple.realm);
				continue;
			}
		}
		for (Item i : Item.getplayeritems()) {
			Key key = i instanceof Key ? (Key) i : null;
			if (key != null && realms.contains(key.color)) {
				realms.remove(key.color);
			}
		}
		return new Key(realms.isEmpty() ? Realm.random() : RPG.pick(realms));
	}

	@Override
	public void expend() {
		// isn't used in the traditional manner
	}

	/**
	 * @see #expend()
	 */
	public void unlock() {
		super.expend();
	}
}
