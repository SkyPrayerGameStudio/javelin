package javelin.model.state;

import java.awt.Image;

import javelin.controller.fight.Fight;
import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;
import javelin.view.Images;

/**
 * A crystal that is evolved after a {@link Combatant} leaves the battle-field
 * and can be used as power-up later on.
 * 
 * @author alex
 */
public class Meld {
	/**
	 * If <code>true</code> will always generate meld.
	 * 
	 * @see Fight#meld
	 */
	public static final boolean DEBUG = false;
	/** Meld location. */
	public final int x;
	/** Meld location. */
	public final int y;
	/**
	 * Will turn from melding crystal to formed (active) crystal when this
	 * action point (turn) has been reached.
	 */
	public float meldsat;
	/**
	 * Challenge rating for the creature who died, generating this meld.
	 * 
	 * @see Monster#challengerating
	 */
	public final float cr;

	/** Constructor. */
	public Meld(final int x, final int y, final float meldsat, Combatant dead) {
		this.x = x;
		this.y = y;
		this.meldsat = meldsat;
		cr = dead == null ? 0 : dead.source.challengerating;
	}

	/**
	 * @param state
	 *            Current state.
	 * @return <code>true</code> if this meld has solidified.
	 * @see #meldsat
	 */
	public boolean crystalize(BattleState state) {
		return state.next.ap >= meldsat;
	}

	/**
	 * @param state
	 *            Current state.
	 * @return Image representing this meld's state.
	 * @see #crystalize(BattleState)
	 */
	public Image getimage(BattleState state) {
		return state.next.ap >= meldsat ? Images.MELD : Images.DEAD;
	}
}
