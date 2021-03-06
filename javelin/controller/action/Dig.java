package javelin.controller.action;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.action.ai.AiAction;
import javelin.controller.ai.ChanceNode;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.model.BattleMap;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import tyrant.mikera.engine.Thing;

/**
 * @see Monster#burrow
 * @author alex
 */
public class Dig extends Action implements AiAction {
	/** Unique Dig instance to be created. */
	public static final Action SINGLETON = new Dig();

	private Dig() {
		super("Dig (requires burrow speed)", "d");
		allowwhileburrowed = true;
	}

	@Override
	public boolean perform(Combatant hero, BattleMap m, Thing thing) {
		if (hero.source.burrow == 0) {
			Game.message("Cannot burrow!", null, Delay.WAIT);
			return false;
		}
		BattleState s = m.getState();
		if (flooded(hero, s)) {
			return false;
		}
		Game.message(dig(hero, s), null, Delay.WAIT);
		return true;
	}

	static boolean flooded(Combatant hero, BattleState s) {
		return s.map[hero.location[0]][hero.location[1]].flooded;
	}

	String dig(Combatant hero, BattleState s) {
		if (hero.burrowed) { // resurface
			hero.ap += Movement.converttoap(hero.source.burrow);
			hero.burrowed = false;
			hero.acmodifier -= 4;
			return hero + " unburrows...";
		} else if (s.isengaged(hero)) {// disengage + burrow
			hero.ap += Movement.disengage(hero);
			hero.burrowed = true;
			hero.acmodifier -= 4;
			return hero + " disengages...";
		} else { // burrow
			hero.ap += Movement.converttoap(hero.source.burrow);
			hero.burrowed = true;
			hero.acmodifier += 4;
			return hero + " burrows...";
		}
	}

	@Override
	public List<List<ChanceNode>> getoutcomes(BattleState s, Combatant active) {
		ArrayList<List<ChanceNode>> outcomes =
				new ArrayList<List<ChanceNode>>(1);
		if (active.source.burrow == 0 || flooded(active, s)) {
			return outcomes;
		}
		ArrayList<ChanceNode> outcome = new ArrayList<ChanceNode>(1);
		s = s.clone();
		active = s.clone(active);
		outcome.add(new ChanceNode(s, 1, dig(active, s), Delay.BLOCK));
		return outcomes;
	}

	/**
	 * Show an error and refuse to do this while burrowed.
	 * 
	 * @throws RepeatTurn
	 */
	public static void refuse() {
		Game.message("Cannot do this while burrowed...", null, Delay.WAIT);
		throw new RepeatTurn();
	}
}