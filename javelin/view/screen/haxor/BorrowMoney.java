package javelin.view.screen.haxor;

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.model.unit.Squad;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.fortification.Fortification;
import javelin.model.world.location.unique.Haxor;
import javelin.view.screen.InfoScreen;

/**
 * Will lend money to a {@link Squad} once but will get it back on a future
 * visit.
 * 
 * @see Haxor#borrowed
 * @author alex
 */
public class BorrowMoney extends Hax {
	/**
	 * See {@link Hax#Hax(String, double, boolean)}.
	 * 
	 * @param keyp
	 */
	public BorrowMoney(String name, Character keyp, double price,
			boolean requirestargetp) {
		super(name, keyp, price, requirestargetp);
	}

	@Override
	protected boolean hack(Combatant target, HaxorScreen s) {
		Haxor.singleton.borrowed = Fortification.getspoils(
				ChallengeRatingCalculator.calculateel(Squad.active.members));
		Squad.active.gold += Haxor.singleton.borrowed;
		s.print(charge());
		InfoScreen.feedback();
		return true;
	}

	/**
	 * @return Textual description of owned money.
	 */
	public static String charge() {
		return "Don't come back until you can pay the $"
				+ Haxor.singleton.borrowed + " you own me!";
	}
}
