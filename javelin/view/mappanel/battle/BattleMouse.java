package javelin.view.mappanel.battle;

import java.awt.event.MouseEvent;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.action.Fire;
import javelin.controller.action.ai.MeleeAttack;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.model.state.BattleState;
import javelin.model.state.BattleState.Vision;
import javelin.model.unit.Attack;
import javelin.model.unit.Combatant;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Mouse;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.battle.overlay.MoveOverlay;
import javelin.view.mappanel.battle.overlay.Mover;
import javelin.view.mappanel.battle.overlay.Mover.Step;
import javelin.view.mappanel.battle.overlay.TargetOverlay;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.StatisticsScreen;

public class BattleMouse extends Mouse {

	enum Action {
		MELEE, RANGED, MOVE
	}

	/**
	 * TODO there is an edge case here for the future: if you're not engaged
	 * with an opponent but could either attack with a ranged weapon or a reach
	 * weapon
	 */
	static Action getaction(final Combatant current, final Combatant target,
			final BattleState s) {
		if (target == null) {
			return Action.MOVE;
		}
		if (target.isAlly(current, s)) {
			return null;
		}
		if (current.isadjacent(target)) {
			return current.source.melee.isEmpty() ? null : Action.MELEE;
		}
		return current.source.ranged.isEmpty() || s.isengaged(current)
				|| s.hasLineOfSight(current, target) == Vision.BLOCKED ? null
						: Action.RANGED;
	}

	public BattleMouse(MapPanel panel) {
		super(panel);
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
		if (overrideinput()) {
			return;
		}
		if (!Game.getUserinterface().waiting) {
			return;
		}
		final Tile t = (Tile) e.getSource();
		final BattleState s = BattlePanel.state;
		final Combatant target = s.getCombatant(t.x, t.y);
		final int button = e.getButton();
		if (button == MouseEvent.BUTTON3 && target != null) {
			BattleScreen.perform(new Runnable() {
				@Override
				public void run() {
					new StatisticsScreen(target);
				}
			});
			return;
		}
		if (button == MouseEvent.BUTTON1) {
			final Combatant current = Game.hero().combatant;
			final Action a = getaction(current, target, s);
			if (a == Action.MOVE) {
				if (MapPanel.overlay instanceof MoveOverlay) {
					final MoveOverlay walk = (MoveOverlay) MapPanel.overlay;
					if (!walk.path.steps.isEmpty()) {
						walk.clear();
						BattleScreen.perform(new Runnable() {
							@Override
							public void run() {
								final Step to = walk.path.steps
										.get(walk.path.steps.size() - 1);
								BattleState move =
										BattleScreen.active.map.getState();
								Combatant c = move.clone(current);
								c.location[0] = to.x;
								c.location[1] = to.y;
								c.ap += to.apcost;
								BattleScreen.active.map.setState(move);
							}
						});
					}
				}
				return;
			} else if (a == Action.MELEE) {
				BattleScreen.perform(new Runnable() {
					@Override
					public void run() {
						current.meleeAttacks(target, s);
					}
				});
			} else if (a == Action.RANGED) {
				BattleScreen.perform(new Runnable() {
					@Override
					public void run() {
						current.rangedattacks(target, s);
					}
				});
			}
			if (MapPanel.overlay != null) {
				MapPanel.overlay.clear();
			}
			return;
		}
		super.mouseClicked(e);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		MoveOverlay.cancel();
		if (BattleScreen.lastlooked != null) {
			BattleScreen.lastlooked = null;
			BattleScreen.active.statuspanel.repaint();
		}
		if (!Game.getUserinterface().waiting) {
			return;
		}
		if (MapPanel.overlay != null) {
			MapPanel.overlay.clear();
		}
		BattleScreen.active.messagepanel.clear();
		try {
			final Combatant current = Game.hero().combatant;
			final Tile t = (Tile) e.getSource();
			final BattleState s = BattlePanel.state;
			final Combatant target = s.getCombatant(t.x, t.y);
			final Action a = getaction(current, target, s);
			if (a == Action.MOVE) {
				MoveOverlay
						.schedule(new MoveOverlay(new Mover(
								new Point(current.location[0],
										current.location[1]),
								new Point(t.x, t.y), current, s)));
				return;
			} else if (a == Action.MELEE) {
				final List<Attack> attack = current.currentmelee.next == null
						|| current.currentmelee.next.isEmpty()
								? current.source.melee.get(0)
								: current.currentmelee.next;
				final String chance = MeleeAttack.SINGLETON.getchance(current,
						target, attack.get(0), s);
				status(target + " (" + target.getStatus() + ", " + chance
						+ " to hit)", target);
			} else if (a == Action.RANGED) {
				status(Fire.SINGLETON.describehitchance(current, target, s),
						target);
			} else if (target != null) {
				BattleScreen.lastlooked = target;
				BattleScreen.active.statuspanel.repaint();
			} else {
				return;
			}
		} finally {
			Game.messagepanel.getPanel().repaint();
		}
	}

	void status(String s, Combatant target) {
		MapPanel.overlay =
				new TargetOverlay(target.location[0], target.location[1]);
		Game.message(s, null, Delay.NONE);
		BattleScreen.active.updateMessages();
	}
}