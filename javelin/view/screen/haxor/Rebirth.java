package javelin.view.screen.haxor;

import java.math.BigDecimal;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * Return an unit to it's original {@link Monster} form. Keep all the XP though
 * so it can be spent again.
 * 
 * @author alex
 */
public class Rebirth extends Hax {
	public Rebirth(String name, Character keyp, double price,
			boolean requirestargetp) {
		super(name, keyp, price, requirestargetp);
	}

	@Override
	protected boolean hack(Combatant target, HaxorScreen s) {
		ChallengeRatingCalculator.calculateCr(target.source);
		Float originalcr = target.source.challengeRating;
		target.spells.clear();
		String customname = target.source.customName;
		target.source = Javelin.getmonster(target.source.name);
		target.source.customName = customname;
		ChallengeRatingCalculator.calculateCr(target.source);
		target.xp = target.xp.add(
				new BigDecimal(originalcr - target.source.challengeRating));
		if (target.xp.intValue() < 0) {
			target.xp = new BigDecimal(0);
		}
		return true;
	}
}