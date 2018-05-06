package javelin.view.screen.wish;

import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.conjuration.teleportation.GreaterTeleport;

/**
 * Teleports player to any town and shows their .
 * 
 * @author alex
 */
public class Teleport extends Wish {
	/**
	 * See {@link Wish#Hax(String, double, boolean)}.
	 * 
	 * @param haxorScreen
	 */
	public Teleport(String name, Character keyp, int price,
			boolean requirestargetp, WishScreen haxorScreen) {
		super(name, keyp, price, requirestargetp, haxorScreen);
	}

	@Override
	protected boolean wish(Combatant target) {
		GreaterTeleport spell = new GreaterTeleport();
		spell.showterrain = true;
		spell.castpeacefully(null);
		return true;
	}
}
