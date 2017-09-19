package javelin.controller.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javelin.Javelin;
import javelin.controller.action.Action;
import javelin.controller.action.ActionMapping;
import javelin.controller.action.ai.AiAction;
import javelin.controller.action.ai.Flee;
import javelin.controller.exception.StopThinking;
import javelin.model.state.BattleState;
import javelin.model.unit.attack.Combatant;

/**
 * An {@link Iterator} that uses {@link Action}s from
 * {@link ActionMapping#ACTIONS} and returns it's successors as
 * {@link ChanceNode}.
 * 
 * @see AiAction#getoutcomes(Combatant, BattleState)
 * 
 * @author alex
 */
public final class ActionProvider
		implements Iterable<List<ChanceNode>>, Iterator<List<ChanceNode>> {
	static final ArrayList<Action> AIACTIONS = new ArrayList<Action>();

	static {
		for (Action a : ActionMapping.ACTIONS) {
			if (a instanceof AiAction) {
				ActionProvider.AIACTIONS.add(a);
			}
		}
	}

	static public void validate(final List<ChanceNode> newsucessors) {
		float sum = 0;
		for (final ChanceNode cn : newsucessors) {
			sum += cn.chance;
		}
		if (.95 >= sum || sum >= 1.05f) {
			throw new RuntimeException("Invalid chances");
		}
	}

	final BattleState battleState;
	final Stack<Action> actions = new Stack<Action>();

	public ActionProvider(BattleState s) {
		this.battleState = s;
		s.next();
		if (s.next.burrowed) {
			for (Action a : AIACTIONS) {
				if (a.allowburrowed) {
					actions.add(a);
				}
			}
			return;
		}
		if (Flee.flee(s.next, s)) {
			actions.add(Flee.SINGLETON);
		} else {
			actions.addAll(ActionProvider.AIACTIONS);
		}
	}

	@Override
	public Iterator<List<ChanceNode>> iterator() {
		return this;
	}

	final Stack<List<ChanceNode>> queue = new Stack<List<ChanceNode>>();

	@Override
	public boolean hasNext() {
		return !actions.isEmpty() || !queue.isEmpty();
	}

	@Override
	public List<ChanceNode> next() {
		if (Thread.interrupted()) {
			throw new StopThinking();
		}
		if (!queue.isEmpty()) {
			List<ChanceNode> n = queue.pop();
			return n;
		}
		if (actions.isEmpty()) {
			return Collections.emptyList();
		}
		final BattleState stateclone = battleState.clonedeeply();
		final List<List<ChanceNode>> outcomes = ((AiAction) actions.pop())
				.getoutcomes(stateclone.next, stateclone);
		for (final List<ChanceNode> sucessors : outcomes) {
			if (!sucessors.isEmpty()) {
				if (Javelin.DEBUG) {
					ActionProvider.validate(sucessors);
				}
				queue.add(sucessors);
			}
		}
		return next();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}