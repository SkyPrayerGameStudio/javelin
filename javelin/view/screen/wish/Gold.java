package javelin.view.screen.wish;

import javelin.controller.challenge.RewardCalculator;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;

public class Gold extends Wish {

	public Gold(WishScreen screen) {
		super("gold ($" + RewardCalculator.getgold(screen.rubies + 1) + ")",
				'g', screen.rubies, false, screen);
	}

	@Override
	protected boolean wish(Combatant target) {
		Squad.active.gold += RewardCalculator.getgold(screen.rubies + 1);
		return true;
	}
}
