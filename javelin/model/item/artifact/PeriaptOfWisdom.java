package javelin.model.item.artifact;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * Raises {@link Monster#wisdom}.
 * 
 * @author alex
 */
public class PeriaptOfWisdom extends Artifact {
	private int bonus;

	/** Constructor. */
	public PeriaptOfWisdom(int bonus, int price) {
		super("Periapt of wisdom +" + bonus, price, Slot.COLLAR);
		this.bonus = bonus;
	}

	@Override
	protected void apply(Combatant c) {
		c.source.raisewisdom(bonus);
	}

	@Override
	protected void negate(Combatant c) {
		c.source.raisewisdom(-bonus);
	}

}
