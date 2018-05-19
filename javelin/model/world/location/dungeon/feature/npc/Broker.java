package javelin.model.world.location.dungeon.feature.npc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.Difficulty;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.Fight;
import javelin.model.item.key.door.Key;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.unique.MercenariesGuild;
import javelin.view.screen.Option;
import javelin.view.screen.town.SelectScreen;
import tyrant.mikera.engine.RPG;

public class Broker extends Inhabitant {
	class InhabitantFight extends Fight {
		List<Combatant> enemy;

		public InhabitantFight(List<Combatant> enemy) {
			this.enemy = enemy;
			rewardgold = false;
			bribe = false;
			hide = false;
		}

		@Override
		public ArrayList<Combatant> getmonsters(Integer teamel) {
			return new ArrayList<Combatant>(enemy);
		}

		@Override
		public String reward() {
			String rewards = super.reward();
			Squad.active.gold += gold;
			rewards += " Party receives $" + Javelin.format(gold) + "!\n";
			return rewards;
		}

		@Override
		public boolean onend() {
			boolean end = super.onend();
			for (Key k : keys) {
				k.grab();
			}
			return end;
		}
	}

	class SellKey extends Option {
		Key item;

		SellKey(Key k) {
			super("Buy " + k.name.toLowerCase(), k.price);
			item = k;
		}
	}

	class InhabitantScreen extends SelectScreen {
		List<Combatant> enemy = Arrays.asList(new Combatant[] { inhabitant });
		Option extractinformation = new Option("Extract information", 0);
		Option sellinformation = new Option("Buy information",
				hints * RewardCalculator.getgold(Dungeon.active.level) / 10);
		Option attack = new Option(
				"Attack (" + Difficulty.describe(enemy) + ")", 0);
		Option hire = new Option("Hire", MercenariesGuild.getfee(inhabitant));

		public InhabitantScreen() {
			super("You encounter a friendly " + inhabitant + "!", null);
		}

		@Override
		public String getCurrency() {
			return "$";
		}

		@Override
		public String printpriceinfo(Option o) {
			return o.price == 0 ? "" : super.printpriceinfo(o);
		}

		@Override
		public String printinfo() {
			return "You have $" + Javelin.format(Squad.active.gold) + ".";
		}

		@Override
		public List<Option> getoptions() {
			ArrayList<Option> options = new ArrayList<Option>();
			int diplomacy = Squad.active.getbest(Skill.DIPLOMACY)
					.taketen(Skill.DIPLOMACY);
			if (diplomacy >= diplomacydc + 5) {
				options.add(hire);
			}
			if (diplomacy >= diplomacydc) {
				for (Key k : keys) {
					options.add(new SellKey(k));
				}
			}
			if (hints > 0) {
				if (diplomacy >= diplomacydc) {
					options.add(extractinformation);
				} else if (diplomacy >= diplomacydc - 5) {
					options.add(sellinformation);
				}
			}
			options.add(attack);
			while (options.contains(null)) {
				options.remove(null);
			}
			return options;
		}

		@Override
		public boolean select(Option o) {
			if (o.price > Squad.active.gold) {
				print(text + "\nNot enough gold...");
				return false;
			}
			Squad.active.gold -= o.price;
			gold += o.price;
			if (o instanceof SellKey) {
				return sellkey(((SellKey) o).item);
			}
			if (o == extractinformation || o == sellinformation) {
				return getinformation();
			}
			if (o == hire) {
				return hire();
			}
			if (o == attack) {
				return attack();
			}
			throw new RuntimeException("#unknowninhabitantoption");
		}

		boolean attack() {
			Dungeon.active.features.remove(Broker.this);
			throw new StartBattle(new InhabitantFight(enemy));
		}

		boolean hire() {
			inhabitant.setmercenary(true);
			Squad.active.members.add(inhabitant);
			Dungeon.active.features.remove(Broker.this);
			hire = null;
			attack = null;
			return true;
		}

		boolean getinformation() {
			for (int i = 0; i < hints; i++) {
				Feature f = Dungeon.active.getundiscoveredfeature();
				if (f != null) {
					Dungeon.active.discover(f);
				}
			}
			hints = 0;
			return true;
		}

		boolean sellkey(Key k) {
			k.grab();
			keys.remove(k);
			return true;
		}
	}

	ArrayList<Key> keys = new ArrayList<Key>();
	protected int hints = RPG.r(1, 10);

	public Broker(int xp, int yp) {
		super(xp, yp, Dungeon.active.level + Difficulty.DIFFICULT,
				Dungeon.active.level + Difficulty.DEADLY);
		int nkeys = RPG.r(1, 4) - 1;
		for (int i = 0; i < nkeys; i++) {
			keys.add(Key.generate());
		}
	}

	@Override
	public boolean activate() {
		new InhabitantScreen().show();
		return false;
	}
}