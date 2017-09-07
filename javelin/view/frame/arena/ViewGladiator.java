package javelin.view.frame.arena;

import java.awt.Container;
import java.awt.Font;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;

import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.unique.minigame.Arena;
import javelin.view.frame.Frame;
import javelin.view.screen.StatisticsScreen;

/**
 * Shows one of the {@link Arena#gladiators} and allows access to other
 * functions such as renaming and upgrading.
 * 
 * @author alex
 */
public class ViewGladiator extends Frame {
	private ActionListener doupgrade = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			UpgradeGladiatorWindow.boost(c, 1);
			new UpgradeGladiatorWindow(c).show(ViewGladiator.this);
		}
	};
	private ActionListener dorename = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			String name =
					JOptionPane.showInputDialog("Rename " + c + " to what?");
			if (name != null && !name.isEmpty()) {
				c.source.customName = name;
				frame.setTitle(name);
			}
		}
	};
	private ActionListener doreturn = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			frame.dispose();
		}
	};

	Combatant c;

	/** Constructor. */
	public ViewGladiator(Combatant c, Arena arena) {
		super(c.toString());
		this.c = c;
	}

	@Override
	protected Container generate() {
		TextArea statistics = new TextArea(StatisticsScreen.gettext(c, false));
		statistics.setEditable(false);
		statistics.setFont(Font.decode(Font.MONOSPACED));
		statistics.setPreferredSize(Frame.getdialogsize());

		Panel actions = new Panel();
		ArenaWindow.newbutton("Upgrade (1 coin)", actions, doupgrade)
				.setEnabled(ArenaWindow.arena.coins >= 1);
		ArenaWindow.newbutton("Rename", actions, dorename);
		ArenaWindow.newbutton("Return", actions, doreturn);

		Panel panel = new Panel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(statistics);
		panel.add(actions);
		return panel;
	}

}