package javelin.model.unit.feat.skill;

import javelin.model.unit.Skills;
import javelin.model.unit.feat.Feat;

/**
 * +2 on perception and sense motive rolls by Pathfinder rules.
 * 
 * @see Skills
 * @author alex
 */
public class Alertness extends Feat {
	/** Single instance. */
	public static final Feat SINGLETON = new Alertness();
	/** +1 since we don't support Sense Motive in the game. */
	public static final int BONUS = 3;

	/** Constructor. */
	private Alertness() {
		super("alertness");
	}
}
