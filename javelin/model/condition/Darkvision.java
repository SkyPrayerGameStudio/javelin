package javelin.model.condition;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * @see javelin.model.spell.transmutation.Darkvision
 * @author alex
 */
public class Darkvision extends Condition {
	int original;

	/**
	 * Constructor.
	 * 
	 * @param casterlevelp
	 */
	public Darkvision(Combatant c, Integer casterlevelp) {
		super(Float.MAX_VALUE, c, Effect.NEUTRAL, "darkvision", casterlevelp,
				3);
	}

	@Override
	public void start(Combatant c) {
		this.original = c.source.vision;
		c.source.vision = Monster.VISION_DARK;
	}

	@Override
	public void end(Combatant c) {
		c.source.vision = Math.min(original, c.source.vision);
	}
}
