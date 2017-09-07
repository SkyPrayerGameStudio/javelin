package javelin.model.unit.feat.attack;

import javelin.model.unit.feat.Feat;

/**
 * See the d20 SRD for more info.
 */
public class Multiattack extends Feat {
	/** Unique instance of this {@link Feat}. */
	public static final Feat SINGLETON = new Multiattack();

	/** Constructor. */
	private Multiattack() {
		super("multiattack");
	}
}
