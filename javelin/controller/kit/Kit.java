package javelin.controller.kit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javelin.Javelin;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.ability.RaiseAbility;
import javelin.controller.upgrade.classes.ClassLevelUpgrade;
import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.town.labor.military.Academy;
import javelin.model.world.location.unique.AdventurersGuild;

/**
 * Kits represent sets of {@link Upgrade}s that constitute a role a character
 * may have a play in. As much inspired on AD&D kits as actual character
 * classes, these are used on the {@link AdventurersGuild} and {@link Academy}
 * types as means of upgrading {@link Combatant}s.
 *
 * Kits are usually created by piecing together 3 to 7 lowest-level upgrades.
 *
 * @author alex
 */
public abstract class Kit implements Serializable {
	static {
		UpgradeHandler.singleton.gather();
	}

	public static final List<Kit> KITS = Arrays
			.asList(new Kit[] { Assassin.INSTANCE, Barbarian.INSTANCE,
					Bard.INSTANCE, Cleric.INSTANCE, Druid.INSTANCE,
					Fighter.INSTANCE, Monk.INSTANCE, Paladin.INSTANCE,
					Ranger.INSTANCE, Rogue.INSTANCE, Wizard.INSTANCE, });

	public String name;
	public HashSet<Upgrade> basic = new HashSet<Upgrade>();
	public HashSet<Upgrade> extension = new HashSet<Upgrade>();
	public ClassLevelUpgrade classlevel;

	public Kit(String name, ClassLevelUpgrade classadvancement,
			RaiseAbility raiseability) {
		this.name = name;
		classlevel = classadvancement;
		basic.add(classadvancement);
		basic.add(raiseability);
		define();
		extend(UpgradeHandler.singleton);
		int nupgrades = basic.size();
		if (!(3 <= nupgrades && nupgrades <= 7) && Javelin.DEBUG) {
			throw new RuntimeException(
					"Kit " + name + " has " + nupgrades + " upgrades");
		}
	}

	protected abstract void extend(UpgradeHandler h);

	abstract protected void define();

	public boolean ispreffered(int i) {
		return false;
	}

	public int getpreferredability(Monster source) {
		int preferred = Integer.MIN_VALUE;
		for (Upgrade u : basic) {
			if (u instanceof RaiseAbility) {
				int ability = ((RaiseAbility) u).getattribute(source);
				if (ability > preferred) {
					preferred = ability;
				}
			}
		}
		if (preferred == Integer.MIN_VALUE) {
			throw new RuntimeException("Attribute not found for kit " + name);
		}
		return preferred;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * @return <code>true</code> if this is a good choice for the given
	 *         {@link Monster}. The default implementation just compares the two
	 *         given ability scores to this class
	 *         {@link #getpreferredability(Monster)}.
	 */
	public boolean allow(int bestability, int secondbest, Monster m) {
		int score = getpreferredability(m);
		return score == bestability || score == secondbest;
	}

	public static List<Kit> gerpreferred(Monster m) {
		ArrayList<Integer> attributes = new ArrayList<Integer>(6);
		attributes.add(m.strength);
		attributes.add(m.dexterity);
		attributes.add(m.constitution);
		attributes.add(m.intelligence);
		attributes.add(m.wisdom);
		attributes.add(m.charisma);
		attributes.sort(null);
		int[] best = new int[] { attributes.get(4), attributes.get(5) };
		ArrayList<Kit> kits = new ArrayList<Kit>(1);
		for (Kit k : KITS) {
			if (k.allow(best[0], best[1], m)) {
				kits.add(k);
			}
		}
		return kits;
	}

	public HashSet<Upgrade> getupgrades() {
		HashSet<Upgrade> upgrades = new HashSet<Upgrade>(basic);
		upgrades.addAll(extension);
		return upgrades;
	}
}
