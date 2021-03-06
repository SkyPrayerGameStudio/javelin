package javelin.controller.ai;

import java.util.List;

import javelin.controller.ai.valueselector.ValueSelector;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/**
 * Javelin's implementation of {@link AlphaBetaSearch}.
 * 
 * @author alex
 */
public class BattleAi extends AlphaBetaSearch {
	/**
	 * Using {@link Integer#MAX_VALUE} (over 2 billion) could have been making
	 * the AI think taking extremely unlikely actions would be good to win the
	 * game.
	 * 
	 * Ideally should use something that will never be reached by the
	 * {@link #ratechallenge(List)} but not any higher.
	 */
	private static final float LIMIT = 1000;

	public BattleAi(final int aiDepth) {
		super(aiDepth);
	}

	@Override
	protected Node catchMemoryIssue(final Error e) {
		throw e;
	}

	@Override
	public float utility(final Node node) {
		final BattleState state = (BattleState) node;
		final float redTeam = BattleAi.ratechallenge(state.getRedTeam());
		if (redTeam == 0f) {
			return -LIMIT;
		}
		final float blueTeam = BattleAi.ratechallenge(state.getBlueTeam());
		if (blueTeam == 0f) {
			return LIMIT;
		}
		return (redTeam - measuredistances(state.redTeam, state.blueTeam))
				- (blueTeam - measuredistances(state.blueTeam, state.redTeam));
	}

	static private float ratechallenge(final List<Combatant> team) {
		float challenge = 0f;
		for (final Combatant c : team) {
			challenge +=
					c.source.challengeRating * (1 + c.hp / (float) c.maxhp);
		}
		return challenge;
	}

	/** TODO round to float, really? */
	static private float measuredistances(List<Combatant> us,
			List<Combatant> them) {
		Combatant active = null;
		for (Combatant mate : us) {
			if (active == null || mate.ap < active.ap) {
				active = mate;
			}
		}
		int minimum = Integer.MAX_VALUE;
		for (Combatant foe : them) {
			minimum = Math.min(minimum,
					Math.max(Math.abs(active.location[0] - foe.location[0]),
							Math.abs(active.location[1] - foe.location[1])));
		}
		return minimum / 100f;
	}

	@Override
	public boolean terminalTest(final Node node) {
		final BattleState state = (BattleState) node;
		return state.getRedTeam().isEmpty() || state.getBlueTeam().isEmpty();
	}

	@Override
	public ValueSelector getplayer(Node node) {
		BattleState s = (BattleState) node;
		return s.blueTeam.contains(s.next) ? minValueSelector
				: maxValueSelector;
	}
}