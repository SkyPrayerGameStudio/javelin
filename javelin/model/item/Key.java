package javelin.model.item;

import java.util.ArrayList;

import javelin.controller.challenge.RewardCalculator;
import javelin.controller.old.Game;
import javelin.model.Realm;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.Chest;
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
		super(color.getname() + " key", determineprice(color), null);
		this.color = color;
		usedinbattle = false;
		waste = false;
	}

	private static int determineprice(Realm r) {
		for (Actor a : World.getactors()) {
			if (a instanceof Temple) {
				Temple temple = (Temple) a;
				if (temple.realm.equals(r)) {
					return 4 * RewardCalculator.getgold(temple.getlevel());
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
	 * @return A key to an unlocked {@link Temple}, that the player doesn't
	 *         currently possess. If none fits the description, a random key,
	 *         instead.
	 */
	public static Key generate() {
		ArrayList<Realm> realms = new ArrayList<Realm>();
		for (Realm r : Realm.values()) {
			realms.add(r);
		}
		for (Actor a : World.getactors()) {
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
