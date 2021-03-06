package javelin.controller.quality.resistance;

import javelin.controller.quality.Quality;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * Tries to treat all energies as a single resistance.
 * 
 * They should be .02CR but currently the game assume 1 point of energy
 * resistance is actually 5 (one for each type of energy resistance).
 * 
 * @author alex
 * @see javelin.controller.quality.resistance.EnergyResistance#RESISTANCETYPES
 * @see Monster#energyresistance
 */
public class EnergyResistance extends Quality {
	/**
	 * See the d20 SRD for more info.
	 */
	static public class EnergyResistanceUpgrade extends Upgrade {

		public EnergyResistanceUpgrade() {
			super("Energy resistance");
		}

		@Override
		public String info(Combatant m) {
			return "Currently resists " + m.source.energyresistance
					+ " points of energy damage";
		}

		@Override
		public boolean apply(Combatant m) {
			if (m.source.energyresistance == Integer.MAX_VALUE) {
				return false;
			}
			m.source.energyresistance += 1;
			return true;
		}

	}

	static final String[] RESISTANCETYPES =
			new String[] { "cold", "fire", "acid", "electricity", "sonic" };
	private static final String[] BLACKLIST = new String[] { "turn resistance",
			"resistance to ranged attacks", "charm resistance" };

	public EnergyResistance() {
		super("resistance");
	}

	@Override
	public void add(String declaration, Monster m) {
		if (m.energyresistance == Integer.MAX_VALUE) {
			return;
		}
		declaration = declaration.toLowerCase();
		for (String ignore : BLACKLIST) {
			if (declaration.contains(ignore)) {
				return;
			}
		}
		float amount = Integer.parseInt(
				declaration.substring(declaration.lastIndexOf(' ') + 1));
		float types = 0;
		for (String type : RESISTANCETYPES) {
			if (declaration.contains(type)) {
				types += 1f;
			}
		}
		m.energyresistance = Math.round(amount * types / 5f);
	}

	@Override
	public boolean has(Monster monster) {
		return 0 < monster.energyresistance && monster.energyresistance < Integer.MAX_VALUE;
	}

	@Override
	public float rate(Monster monster) {
		return monster.energyresistance * .02f * 5;
	}

	@Override
	public void listupgrades(UpgradeHandler handler) {
		handler.magic.add(new EnergyResistanceUpgrade());
	}

	@Override
	public String describe(Monster m) {
		return "energy resistance " + m.energyresistance;
	}
}
